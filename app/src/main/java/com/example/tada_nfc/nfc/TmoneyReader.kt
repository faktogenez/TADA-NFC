package com.example.tada_nfc.nfc

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCard
import androidx.compose.material.icons.outlined.DirectionsBus
import com.example.tada_nfc.config.CardConfig
import com.example.tada_nfc.models.CardDataInternal
import com.example.tada_nfc.models.TransactionPlaceholder
import java.io.IOException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object TmoneyReader {
    private const val TAG = "TmoneyReader"
    private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' })

    // APDU Constants
    private val CMD_SELECT_SECONDARY_AID = byteArrayOf(0, 0xA4.toByte(), 4, 0, 7, 0xA0.toByte(), 0, 0, 2, 0x45.toByte(), 0, 1, 0)
    private val CMD_CARDINFO_3C = byteArrayOf(0, 0xB0.toByte(), 0x88.toByte(), 0x00, 0x3C.toByte())
    private val CMD_BALANCE_HIPASS = byteArrayOf(0x80.toByte(), 0x5C.toByte(), 0, 0, 4)

    fun read(tag: Tag): CardDataInternal? {
        Log.i(TAG, ">>> SCAN STARTED <<<")
        val techList = tag.techList.toList()
        if (techList.contains("android.nfc.tech.IsoDep")) {
            return readIsoDep(IsoDep.get(tag))
        }
        return null
    }

    private fun readIsoDep(iso: IsoDep?): CardDataInternal? {
        if (iso == null) return null
        try {
            iso.connect()
            iso.timeout = 2000

            val aids = listOf(
                Pair(hexToBytes("D4100000030001"), "TMONEY"),
                Pair(hexToBytes("D4100000140001"), "CASHBEE"),
                Pair(hexToBytes("A0000002450001"), "HIPASS"),
                Pair(hexToBytes("D410000029000001"), "RAILPLUS")
            )

            var selectRes: ByteArray? = null
            var cardBrand = ""

            for (pair in aids) {
                val aid = pair.first
                val selectCmd = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, aid.size.toByte()) + aid + byteArrayOf(0x00)
                val res = iso.transceive(selectCmd)
                if (isSuccessStatus(res)) {
                    selectRes = res
                    cardBrand = pair.second
                    Log.d(TAG, "Raw Select Response: ${toHex(res)}")
                    break
                }
            }

            if (selectRes == null) return null

            // 1. Balance Calculation
            val balanceCmd = if (cardBrand == "HIPASS") CMD_BALANCE_HIPASS else hexToBytes("904C000004")
            val bRes = iso.transceive(balanceCmd)
            val balanceInt = if (isSuccessStatus(bRes) && bRes.size >= 4) {
                (bRes[3].toInt() and 0xFF) or
                ((bRes[0].toInt() and 0xFF) shl 24) or
                ((bRes[1].toInt() and 0xFF) shl 16) or
                ((bRes[2].toInt() and 0xFF) shl 8)
            } else 0
            val balanceStr = decimalFormat.format(balanceInt)

            // 2. User Type Identification
            val userType = if (cardBrand == "HIPASS") "HIPASS" else {
                if (selectRes!!.size > 29) {
                    when (selectRes[29].toInt() and 0xFF) {
                        0x01 -> "ADULT"
                        0x02 -> "CHILD"
                        0x04 -> "YOUTH"
                        else -> "UNKNOWN"
                    }
                } else "UNKNOWN"
            }

            // 3. Card Number Search
            var fullNumber: String? = null
            if (cardBrand == "HIPASS") {
                fullNumber = extractCardNumberFromFCI(selectRes)
                val secondaryRes = iso.transceive(CMD_SELECT_SECONDARY_AID)
                if (fullNumber == null) fullNumber = extractCardNumberFromFCI(secondaryRes)
                if (fullNumber == null) {
                    val infoRes = iso.transceive(CMD_CARDINFO_3C)
                    if (isSuccessStatus(infoRes) && infoRes.size >= 20) {
                        fullNumber = formatBcdCardNumber(infoRes, 12, 8)
                    }
                }
            } else {
                if (selectRes!!.size >= 16) {
                    fullNumber = toHex(selectRes!!.sliceArray(8 until 16))
                }
            }

            val finalCardNumber = if (fullNumber != null && fullNumber.length >= 16) {
                "${fullNumber.substring(0, 4)} **** **** ${fullNumber.substring(12, 16)}"
            } else if (fullNumber != null) {
                fullNumber
            } else {
                "**** **** **** ****"
            }

            // 4. Transaction History
            val history = if (cardBrand != "HIPASS") readTransactionHistory(iso, cardBrand) else emptyList()

            return CardDataInternal(balanceStr, finalCardNumber, userType, history)
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Read Error", e)
            return null
        } finally {
            runCatching { iso.close() }
        }
    }

    private fun readTransactionHistory(iso: IsoDep, brand: String): List<TransactionPlaceholder> {
        val list = mutableListOf<TransactionPlaceholder>()
        val sfi: Byte = if (brand == "RAILPLUS") 116 else 36
        val le = if (brand == "TMONEY") 46 else 26
        
        for (i in 1..20) {
            try {
                val res = iso.transceive(byteArrayOf(0x00, 0xB2.toByte(), i.toByte(), sfi, le.toByte()))
                if (isSuccessStatus(res) && res.size >= 20) {
                    val tx = parseRecord(res, brand)
                    if (tx != null) list.add(tx)
                    else if ((res[res.size - 2].toInt() and 0xFF) == 0x6A) break
                } else break
            } catch (e: Exception) { break }
        }
        return list
    }

    private fun parseRecord(data: ByteArray, brand: String): TransactionPlaceholder? {
        if (data.size < 20) return null
        
        if (data.take(16).all { it == 0.toByte() || it == 0xFF.toByte() }) return null

        return try {
            val typeByte = data[0].toInt() and 0xFF
            val isCharge = typeByte == 2
            
            val amount = if (brand == "RAILPLUS") {
                ((data[2].toInt() and 0xFF) shl 24) or ((data[3].toInt() and 0xFF) shl 16) or ((data[4].toInt() and 0xFF) shl 8) or (data[5].toInt() and 0xFF)
            } else {
                ((data[12].toInt() and 0xFF) shl 8) or (data[13].toInt() and 0xFF)
            }

            val balanceAfter = if (brand == "RAILPLUS") {
                ((data[10].toInt() and 0xFF) shl 24) or ((data[11].toInt() and 0xFF) shl 16) or ((data[12].toInt() and 0xFF) shl 8) or (data[13].toInt() and 0xFF)
            } else {
                ((data[4].toInt() and 0xFF) shl 8) or (data[5].toInt() and 0xFF)
            }

            if (amount == 0) return null

            TransactionPlaceholder(
                type = CardConfig.translate(if (isCharge) "top_up_tx" else "transit_tx"),
                amount = (if (isCharge) "+" else "-") + "₩" + decimalFormat.format(amount),
                date = parseDate(data, brand),
                balanceAfter = "₩" + decimalFormat.format(balanceAfter),
                icon = if (isCharge) Icons.Outlined.AddCard else Icons.Outlined.DirectionsBus
            )
        } catch (e: Exception) { null }
    }

    private fun parseDate(data: ByteArray, brand: String): String {
        return try {
            if (brand == "RAILPLUS") {
                if (data.size >= 19) {
                    val y = formatBcd(data[14])
                    val m = formatBcd(data[15])
                    val d = formatBcd(data[16])
                    val hh = formatBcd(data[17])
                    val mm = formatBcd(data[18])
                    val mInt = m.toIntOrNull() ?: 0
                    if (mInt in 1..12) "20$y.$m.$d\n$hh:$mm" else ""
                } else ""
            } else {
                if (data.size >= 25) {
                    val y = formatBcd(data[20])
                    val m = formatBcd(data[21])
                    val d = formatBcd(data[22])
                    val hh = formatBcd(data[23])
                    val mm = formatBcd(data[24])
                    val mInt = m.toIntOrNull() ?: 0
                    if (mInt in 1..12) return "20$y.$m.$d\n$hh:$mm"
                }
                
                if (data.size >= 10) {
                    val m = formatBcd(data[6])
                    val d = formatBcd(data[7])
                    val hh = formatBcd(data[8])
                    val mm = formatBcd(data[9])
                    val mInt = m.toIntOrNull() ?: 0
                    if (mInt in 1..12) return "$m.$d\n$hh:$mm"
                }
                ""
            }
        } catch (e: Exception) { "" }
    }

    private fun formatBcd(b: Byte): String {
        val i = b.toInt() and 0xFF
        val high = (i shr 4) and 0x0F
        val low = i and 0x0F
        if (high > 9 || low > 9) return "00"
        return "%02x".format(i)
    }

    private fun extractCardNumberFromFCI(data: ByteArray?): String? {
        if (data == null || data.size < 2) return null
        if (!isSuccessStatus(data)) return null
        for (i in 0 until data.size - 9) {
            val tag = data[i].toInt() and 0xFF
            if (tag == 0x13 && i + 9 < data.size && (data[i + 1].toInt() and 0xFF == 0x08)) {
                return formatBcdCardNumber(data, i + 2, 8)
            }
        }
        return null
    }

    private fun formatBcdCardNumber(data: ByteArray, offset: Int, len: Int): String? {
        if (offset + len > data.size) return null
        val sb = StringBuilder()
        for (i in 0 until len) {
            val b = data[offset + i].toInt() and 0xFF
            val d1 = (b shr 4) and 0x0F
            val d2 = b and 0x0F
            if (d1 <= 9) sb.append(d1)
            if (d2 <= 9) sb.append(d2)
        }
        return sb.toString()
    }

    private fun isSuccessStatus(res: ByteArray?): Boolean {
        if (res == null || res.size < 2) return false
        val sw1 = res[res.size - 2].toInt() and 0xFF
        val sw2 = res[res.size - 1].toInt() and 0xFF
        return (sw1 == 0x90 && sw2 == 0x00) || sw1 == 0x62
    }

    private fun hexToBytes(s: String): ByteArray = s.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    private fun toHex(bytes: ByteArray): String = bytes.joinToString("") { "%02X".format(it) }
}
