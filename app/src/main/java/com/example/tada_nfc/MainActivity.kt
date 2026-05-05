package com.example.tada_nfc

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PortableWifiOff
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.tada_nfc.ui.theme.TADA_NFCTheme
import java.io.IOException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed class AppState {
    @Immutable object Splash : AppState()
    @Immutable object WaitingForCard : AppState()
    @Immutable object Processing : AppState()
    @Immutable object NfcDisabled : AppState()
    @Immutable data class CardResult(val balance: String, val cardNumber: String, val userType: String, val transactions: List<TransactionPlaceholder> = emptyList()) : AppState()
    @Immutable data class History(val result: CardResult) : AppState()
    @Immutable data class TopUp(val cardData: CardData, val amount: Int = 0) : AppState()
    @Stable sealed class Error(val message: String, val isRetry: Boolean = false) : AppState() {
        @Immutable data class CardNotSupported(val msg: String) : Error(msg, false)
        @Immutable data class Retry(val msg: String) : Error(msg, true)
    }
}

@Immutable
data class TransactionPlaceholder(
    val type: String,
    val amount: String,
    val date: String,
    val balanceAfter: String,
    val icon: ImageVector
)

data class CardDataInternal(val balance: String, val number: String, val userType: String, val transactions: List<TransactionPlaceholder>)
data class CardData(val balance: String, val number: String, val userType: String)

object TmoneyReader {
    private const val TAG = "DEBUG"
    private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' })

    // APDU Constants from Reference
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
        
        // Simple check for empty record
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
            // Debug: Log raw hex to identify date positions if needed
            // Log.d("DEBUG", "Raw record (${brand}): ${toHex(data)}")
            
            if (brand == "RAILPLUS") {
                // RailPlus: YY MM DD HH mm at offset 14..18
                if (data.size >= 19) {
                    val y = formatBcd(data[14])
                    val m = formatBcd(data[15])
                    val d = formatBcd(data[16])
                    val hh = formatBcd(data[17])
                    val mm = formatBcd(data[18])
                    // Validate month
                    val mInt = m.toIntOrNull() ?: 0
                    if (mInt in 1..12) "20$y.$m.$d\n$hh:$mm" else ""
                } else ""
            } else {
                // T-Money/Cashbee: 
                // In many records, timestamp (YY MM DD HH mm) is at offsets 20..24
                if (data.size >= 25) {
                    val y = formatBcd(data[20])
                    val m = formatBcd(data[21])
                    val d = formatBcd(data[22])
                    val hh = formatBcd(data[23])
                    val mm = formatBcd(data[24])
                    val mInt = m.toIntOrNull() ?: 0
                    if (mInt in 1..12) return "20$y.$m.$d\n$hh:$mm"
                }
                
                // Fallback for some T-Money versions: MM DD HH mm at offsets 6..9
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
        if (high > 9 || low > 9) return "00" // Not valid BCD
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
            if (tag == 0x86) Log.d(TAG, "Tag 0x86 found: Card is ready for data reading")
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

    private fun findTagData(data: ByteArray, tag: Byte): ByteArray? {
        val idx = findPattern(data, byteArrayOf(tag, 0x08.toByte()))
        return if (idx != -1 && data.size >= idx + 10) data.sliceArray(idx + 2 until idx + 10) else null
    }

    private fun isAllZeros(bytes: ByteArray): Boolean = bytes.all { it == 0.toByte() }
    private fun hexToBytes(s: String): ByteArray = s.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    private fun toHex(bytes: ByteArray): String = bytes.joinToString("") { "%02X".format(it) }

    private fun findPattern(data: ByteArray, pattern: ByteArray): Int {
        if (data.size < pattern.size) return -1
        for (i in 0..data.size - pattern.size) {
            var match = true
            for (j in pattern.indices) { if (data[i + j] != pattern[j]) { match = false; break } }
            if (match) return i
        }
        return -1
    }
}
class MainActivity : ComponentActivity() {
    private val nfcAdapter by lazy { NfcAdapter.getDefaultAdapter(this) }
    private val vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else { @Suppress("DEPRECATION") getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    }

    private var appState by mutableStateOf<AppState>(
        if (intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) AppState.Processing 
        else AppState.Splash
    )
    private var cardRotation by mutableStateOf(0f)
    private var showSettings by mutableStateOf(false)

    private var splashPlayer: ExoPlayer? = null
    private var waitingPlayer: ExoPlayer? = null

    // Player cache to avoid recreating
    private val playerCache = mutableMapOf<Int, ExoPlayer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Strict NFC check before anything else
        val nfc = NfcAdapter.getDefaultAdapter(this)
        val isNfcEnabled = nfc?.isEnabled == true
        
        requestNotificationPermission()
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!isNfcEnabled) {
            appState = AppState.NfcDisabled
        } else {
            if (intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
                intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { processTmoneyTag(it) }
            } else {
                splashPlayer = createPlayer(R.raw.splash, false) {
                    // One more check before moving from splash to waiting
                    if (nfcAdapter?.isEnabled == true) {
                        appState = AppState.WaitingForCard
                        waitingPlayer = createPlayer(R.raw.waiting, true)
                    } else {
                        appState = AppState.NfcDisabled
                    }
                }
            }
        }
        setContent {
            TADA_NFCTheme {
                // Foolproof: Prevent accidental back press during scanning or when NFC is off
                BackHandler(enabled = appState is AppState.Processing || appState is AppState.NfcDisabled) {
                    if (appState is AppState.NfcDisabled) {
                        finishAffinity() // If NFC is off and they try to go back, just close the app properly
                    }
                }

                val bgAlpha by animateFloatAsState(
                    targetValue = if (appState is AppState.CardResult || appState is AppState.Error || appState is AppState.Processing || appState is AppState.History || appState is AppState.NfcDisabled) 0.6f else 1.0f,
                    animationSpec = tween(600),
                    label = "bgAlpha"
                )
                val bgColor = if (appState is AppState.CardResult || appState is AppState.Error || appState is AppState.Processing || appState is AppState.History || appState is AppState.NfcDisabled) Color.Black else Color.White
                Box(modifier = Modifier.fillMaxSize().background(bgColor.copy(alpha = bgAlpha))) {
                    AnimatedContent(targetState = appState, label = "MainFlow") { state ->
                        when (state) {
                            is AppState.Splash -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    splashPlayer?.let { CachedVideoPlayer(it) }
                                }
                            }
                            is AppState.WaitingForCard -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    waitingPlayer?.let { CachedVideoPlayer(it) }
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Spacer(modifier = Modifier.fillMaxHeight(CardConfig.instructionYOffset))
                                        Text(
                                            text = CardConfig.videoInstruction.uppercase(),
                                            fontSize = CardConfig.instructionFontSize(),
                                            fontWeight = CardConfig.instructionFontWeight,
                                            color = CardConfig.instructionColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            is AppState.Processing -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = CardConfig.translate("hold_card_writing").uppercase(),
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                            is AppState.NfcDisabled -> NfcDisabledDialog(
                                onEnableClick = {
                                    startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                                },
                                onCloseClick = {
                                    finishAffinity() // Закрывает все активити и завершает работу приложения
                                }
                            )
                            is AppState.CardResult -> CardResultOverlay(
                                targetBalance = state.balance, targetCardNumber = state.cardNumber, targetUserType = state.userType, targetRotation = cardRotation,
                                onDismiss = { finishAffinity() },
                                onSettingsClick = { showSettings = true },
                                onHistoryClick = {
                                    appState = AppState.History(result = state)
                                }
                            )
                            is AppState.History -> HistoryScene(
                                transactions = state.result.transactions,
                                onBackClick = { appState = state.result }
                            )
                            is AppState.TopUp -> { /* Placeholder for future top-up UI */ }
                            is AppState.Error -> {
                                CardResultOverlay(
                                    targetBalance = "0",
                                    targetCardNumber = "**** **** **** ****",
                                    targetUserType = if (state.isRetry) "RETRY" else "UNKNOWN",
                                    targetRotation = cardRotation,
                                    onDismiss = { finishAffinity() },
                                    onSettingsClick = { showSettings = true },
                                    onHistoryClick = {}
                                )
                            }
                        }
                    }
                    if (showSettings) SettingsDialog { showSettings = false }
                }
            }
        }
    }

    private fun createPlayer(resId: Int, isLooping: Boolean, onEnd: () -> Unit = {}): ExoPlayer {
        return playerCache.getOrPut(resId) {
            ExoPlayer.Builder(this).build().apply {
                setMediaItem(MediaItem.fromUri(Uri.parse("android.resource://$packageName/$resId")))
                repeatMode = if (isLooping) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
                prepare()
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) onEnd()
                    }
                })
                playWhenReady = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerCache.values.forEach { it.release() }
        playerCache.clear()
        splashPlayer = null
        waitingPlayer = null
    }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); if (intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) { intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { processTmoneyTag(it) } } }
    override fun onResume() {
        super.onResume()
        checkNfcStatus()
        nfcAdapter?.enableReaderMode(this, ::processTmoneyTag, 0x0F, null)
    }

    private fun checkNfcStatus() {
        if (nfcAdapter == null || !nfcAdapter!!.isEnabled) {
            if (appState !is AppState.CardResult) {
                appState = AppState.NfcDisabled
            }
        } else if (appState == AppState.NfcDisabled) {
            if (waitingPlayer == null) {
                waitingPlayer = createPlayer(R.raw.waiting, true)
            }
            appState = AppState.WaitingForCard
        }
    }
    override fun onPause() { super.onPause(); nfcAdapter?.disableReaderMode(this) }

    private fun processTmoneyTag(tag: Tag) {
        if (appState !is AppState.CardResult) appState = AppState.Processing
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = TmoneyReader.read(tag)
                
                withContext(Dispatchers.Main) {
                    if (result != null) {
                        vibrateConfirmation()
                        cardRotation += 180f
                        appState = AppState.CardResult(result.balance, result.number, result.userType, result.transactions)
                        
                        // Async saving to preferences
                        val prefs = getSharedPreferences("tada_prefs", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("last_balance", "₩ ${result.balance}")
                            putString("last_card_number", result.number)
                            apply()
                        }
                        TadaWidgetProvider.updateAllWidgets(this@MainActivity)
                        BalanceNotificationService.updateNotification(this@MainActivity, "₩ ${result.balance}", result.userType, result.number)
                    } else {
                        vibrateError()
                        appState = AppState.Error.CardNotSupported(CardConfig.translate("unsupported_card"))
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    vibrateError()
                    appState = AppState.Error.Retry(CardConfig.translate("tap_again"))
                }
            } catch (e: Exception) {
                Log.e("DEBUG", "Unexpected error during NFC processing", e)
                withContext(Dispatchers.Main) {
                    if (appState is AppState.Processing) appState = AppState.WaitingForCard
                }
            }
        }
    }

    private fun vibrateConfirmation() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)) else @Suppress("DEPRECATION") vibrator.vibrate(100) }
    private fun vibrateError() = vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1))

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun CachedVideoPlayer(player: ExoPlayer) {
    AndroidView(factory = { context -> PlayerView(context).apply { this.player = player; useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM; setBackgroundColor(android.graphics.Color.TRANSPARENT) } }, modifier = Modifier.fillMaxSize())
}

@Composable
fun NfcDisabledDialog(onEnableClick: () -> Unit, onCloseClick: () -> Unit) {
    val vibrator = (LocalContext.current.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
    val errorColor = CardConfig.colorError
    val accentColor = CardConfig.dialogAccent
    val purpleBorder = CardConfig.colorPurpleBorder
    
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") vibrator?.vibrate(300)
        }
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(CardConfig.nfcOffCornerRadius),
            color = CardConfig.colorDialogBg,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(errorColor)
                        .padding(horizontal = 16.dp, vertical = CardConfig.nfcOffHeaderPadding()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TadaLogo(backgroundColor = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TADA",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = CardConfig.logoTextSize()
                        )
                    }
                    IconButton(onClick = onCloseClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = CardConfig.nfcOffContentPadding(), vertical = CardConfig.nfcOffContentPadding() * 1.3f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 2. Icon
                    Icon(
                        imageVector = Icons.Default.PortableWifiOff,
                        contentDescription = null,
                        tint = errorColor,
                        modifier = Modifier.size(CardConfig.nfcOffIconSize())
                    )
                    
                    Spacer(modifier = Modifier.height(CardConfig.nfcOffContentPadding()))
                    
                    // 3. Title (without border)
                    Text(
                        text = CardConfig.translate("nfc_off_title").uppercase(),
                        fontSize = CardConfig.nfcOffTitleSize(),
                        fontWeight = FontWeight.Black,
                        color = errorColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        maxLines = 1 // Гарантируем одну строку
                    )
                    
                    Spacer(modifier = Modifier.height(CardConfig.nfcOffContentPadding()))
                    
                    // 4. Description
                    Text(
                        text = CardConfig.translate("nfc_off_desc"),
                        fontSize = CardConfig.nfcOffDescSize(),
                        fontWeight = FontWeight.Normal,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = CardConfig.nfcOffDescSize() * 1.3f
                    )
                    
                    Spacer(modifier = Modifier.height(CardConfig.nfcOffContentPadding() * 1.5f))
                    
                    // 5. Button
                    Button(
                        onClick = onEnableClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = CardConfig.translate("enable_nfc").uppercase(),
                            fontSize = CardConfig.nfcOffButtonTextSize(),
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScene(transactions: List<TransactionPlaceholder>, onBackClick: () -> Unit) {
    Dialog(
        onDismissRequest = onBackClick,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = CardConfig.activeBg,
            modifier = Modifier
                .fillMaxWidth(CardConfig.cardWidthFraction) // Ширина строго как у карточки баланса
                .fillMaxHeight(0.9f)
                .border(2.dp, CardConfig.activeAccent.copy(0.3f), RoundedCornerShape(28.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header row matching Settings style
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = CardConfig.activeAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = CardConfig.translate("history"),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = CardConfig.activeAccent
                        )
                    }
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = CardConfig.activeText.copy(alpha = 0.5f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List or Empty State
                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                                contentDescription = null,
                                tint = CardConfig.activeAccent.copy(alpha = 0.15f),
                                modifier = Modifier.size(100.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            // Можно добавить маленькую иконку запрета поверх, если нужно еще больше акцента
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = CardConfig.activeAccent.copy(alpha = 0.1f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(CardConfig.historyListSpacing)
                    ) {
                        items(transactions) { tx ->
                            TransactionItem(tx)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(tx: TransactionPlaceholder) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(CardConfig.historyItemCornerRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().height(CardConfig.historyItemHeight)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CardConfig.historyItemPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Большая черная иконка (размер из конфига)
            Icon(
                imageVector = tx.icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(CardConfig.historyIconSize)
            )
            
            Spacer(Modifier.width(historyItemPaddingOffset()))
            
            // Сумма (размер из конфига)
            Text(
                text = tx.amount,
                fontWeight = FontWeight.Black,
                fontSize = CardConfig.historyAmountTextSize,
                color = if (tx.amount.startsWith("+")) Color(0xFF10B981) else Color(0xFFB91C1C),
                modifier = Modifier.weight(1f)
            )
            
            // Остаток (внизу справа, размер из конфига)
            Box(modifier = Modifier.fillMaxHeight().padding(bottom = 8.dp), contentAlignment = Alignment.BottomEnd) {
                Text(
                    text = tx.balanceAfter,
                    fontSize = CardConfig.historyBalanceTextSize,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun historyItemPaddingOffset() = 16.dp

@Composable
fun CardResultOverlay(targetBalance: String, targetCardNumber: String, targetUserType: String, targetRotation: Float, onDismiss: () -> Unit, onSettingsClick: () -> Unit, onHistoryClick: () -> Unit) {
    var displayedBalance by remember { mutableStateOf(targetBalance) }
    var displayedCardNumber by remember { mutableStateOf(targetCardNumber) }
    var displayedUserType by remember { mutableStateOf(targetUserType) }
    val rotation = remember { Animatable(targetRotation - 180f) }
    LaunchedEffect(targetRotation) { rotation.animateTo(targetRotation, tween(CardConfig.flipAnimationDuration)) { if (this.value >= targetRotation - 90f) { displayedBalance = targetBalance; displayedCardNumber = targetCardNumber; displayedUserType = targetUserType } } }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss),
        contentAlignment = CardConfig.cardScreenAlignment
    ) {
        TadaCard(balance = displayedBalance, cardNumber = displayedCardNumber, userType = displayedUserType, modifier = Modifier.graphicsLayer { rotationY = rotation.value; cameraDistance = CardConfig.cameraDistance * density }.graphicsLayer { val norm = (rotation.value % 360 + 360) % 360; if (norm > 90 && norm < 270) rotationY = 180f }, onCloseClick = onDismiss, onSettingsClick = onSettingsClick, onHistoryClick = onHistoryClick)
    }
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp), 
            color = CardConfig.activeBg, 
            modifier = Modifier
                .fillMaxWidth(CardConfig.cardWidthFraction) // Ширина строго как у карточки баланса
                .fillMaxHeight(0.9f)
                .border(2.dp, CardConfig.activeAccent.copy(0.3f), RoundedCornerShape(28.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                val context = LocalContext.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = CardConfig.translate("settings"), fontSize = 28.sp, fontWeight = FontWeight.Black, color = CardConfig.activeAccent)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = CardConfig.activeText.copy(alpha = 0.5f))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp)); SectionLabel(CardConfig.translate("language"))
                CardConfig.Language.entries.forEach { lang -> BigControlTile(lang.label, CardConfig.currentLanguage == lang) { CardConfig.currentLanguage = lang }; Spacer(modifier = Modifier.height(8.dp)) }
                Spacer(modifier = Modifier.height(32.dp))
                Surface(color = CardConfig.activeAccent.copy(alpha = 0.05f), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = { val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "${CardConfig.shareAppMessage}\n${CardConfig.getAppLink(context.packageName)}") }; context.startActivity(Intent.createChooser(intent, "Share")) }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = CardConfig.activeAccent), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Share, null); Spacer(Modifier.width(12.dp)); Text(CardConfig.shareAppLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Кнопка WhatsApp Поддержки
                        Button(
                            onClick = {
                                val url = "https://wa.me/${CardConfig.SUPPORT_WHATSAPP}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // Цвет WhatsApp
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.Forum, null, tint = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Text(CardConfig.translate("contact_dev"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "${CardConfig.translate("version")} 1.0.3", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = CardConfig.activeText.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) { Text(text.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CardConfig.activeAccent, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp)) }

@Composable
fun BigControlTile(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = if (isSelected) CardConfig.activeAccent else CardConfig.activeAccent.copy(0.05f), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isSelected) CardConfig.activeBg else CardConfig.activeText)
            if (isSelected) Icon(Icons.Default.Check, null, tint = CardConfig.activeBg)
        }
    }
}
