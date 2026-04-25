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
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.tada_nfc.ui.theme.TADA_NFCTheme
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

sealed class AppState {
    object Splash : AppState()
    object WaitingForCard : AppState()
    object Processing : AppState()
    data class CardResult(val balance: String, val cardNumber: String, val userType: String) : AppState()
}

data class CardData(val balance: String, val number: String, val userType: String)

object TmoneyReader {
    private const val TAG = "DEBUG"
    private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' })

    // APDU Constants from Reference
    private val CMD_SELECT_SECONDARY_AID = byteArrayOf(0, 0xA4.toByte(), 4, 0, 7, 0xA0.toByte(), 0, 0, 2, 0x45.toByte(), 0, 1, 0)
    private val CMD_CARDINFO_3C = byteArrayOf(0, 0xB0.toByte(), 0x88.toByte(), 0x00, 0x3C.toByte())
    private val CMD_BALANCE_HIPASS = byteArrayOf(0x80.toByte(), 0x5C.toByte(), 0, 0, 4)

    fun read(tag: Tag): CardData? {
        Log.i(TAG, ">>> SCAN STARTED <<<")
        val techList = tag.techList.toList()
        if (techList.contains("android.nfc.tech.IsoDep")) return readIsoDep(IsoDep.get(tag))
        return null
    }

    private fun readIsoDep(iso: IsoDep?): CardData? {
        if (iso == null) return null
        try {
            iso.connect()
            iso.timeout = 5000

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
            val balanceStr = if (isSuccessStatus(bRes) && bRes.size >= 4) {
                val b = bRes
                val balanceInt = (b[3].toInt() and 0xFF) or
                        ((b[0].toInt() and 0xFF) shl 24) or
                        ((b[1].toInt() and 0xFF) shl 16) or
                        ((b[2].toInt() and 0xFF) shl 8)
                decimalFormat.format(balanceInt)
            } else "0"

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

            // 3. Card Number Search (STRICT REFERENCE LOGIC)
            var fullNumber: String? = null

            if (cardBrand == "HIPASS") {
                fullNumber = extractCardNumberFromFCI(selectRes)

                val secondaryRes = iso.transceive(CMD_SELECT_SECONDARY_AID)
                Log.d(TAG, "Secondary AID Response: ${toHex(secondaryRes)}")
                if (fullNumber == null) {
                    fullNumber = extractCardNumberFromFCI(secondaryRes)
                }

                if (fullNumber == null) {
                    val infoRes = iso.transceive(CMD_CARDINFO_3C)
                    Log.d(TAG, "New CardInfo (3C) Response: ${toHex(infoRes)}")
                    if (isSuccessStatus(infoRes) && infoRes.size >= 8) {
                        val cand1 = formatBcdCardNumber(infoRes, 0, 8)
                        val cand2 = formatBcdCardNumber(infoRes, 2, 8)
                        fullNumber = if (cand2 != null && cand2.startsWith("0020")) cand2 else cand1
                    }
                }
            } else {
                val rawStandard = findTagData(selectRes!!, 0x12.toByte()) ?:
                if (selectRes!!.size >= 16) selectRes!!.sliceArray(8 until 16) else null

                if (rawStandard != null && !isAllZeros(rawStandard)) {
                    fullNumber = toHex(rawStandard)
                }
            }

            // 4. MASKING LOGIC (STRICT: XXXX **** **** XXXX)
            val finalCardNumber = if (fullNumber != null && fullNumber.length >= 16) {
                "${fullNumber.substring(0, 4)} **** **** ${fullNumber.substring(12, 16)}"
            } else if (fullNumber != null) {
                fullNumber
            } else {
                "**** **** **** ****"
            }

            return CardData(balanceStr, finalCardNumber, userType)
        } catch (e: Exception) { Log.e(TAG, "Read Error", e); return null }
        finally { runCatching { iso.close() } }
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

    private var appState by mutableStateOf<AppState>(AppState.Splash)
    private var cardRotation by mutableStateOf(0f)
    private var showSettings by mutableStateOf(false)

    private var splashPlayer: ExoPlayer? = null
    private var waitingPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            appState = AppState.Processing
            intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { processTmoneyTag(it) }
        } else {
            appState = AppState.Splash
            splashPlayer = createPlayer(R.raw.splash, false) { appState = AppState.WaitingForCard }
            waitingPlayer = createPlayer(R.raw.waiting, true)
        }
        setContent {
            TADA_NFCTheme {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f))) {
                    AnimatedContent(targetState = appState, label = "MainFlow") { state ->
                        when (state) {
                            is AppState.Splash -> splashPlayer?.let { CachedVideoPlayer(it) }
                            is AppState.WaitingForCard -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    waitingPlayer?.let { CachedVideoPlayer(it) }
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Spacer(modifier = Modifier.fillMaxHeight(CardConfig.instructionYOffset))
                                        Text(text = CardConfig.videoInstruction.uppercase(), fontSize = CardConfig.instructionFontSize, fontWeight = CardConfig.instructionFontWeight, color = CardConfig.instructionColor, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                            is AppState.Processing -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) }
                            is AppState.CardResult -> CardResultOverlay(
                                targetBalance = state.balance, targetCardNumber = state.cardNumber, targetUserType = state.userType, targetRotation = cardRotation,
                                onDismiss = { appState = AppState.WaitingForCard; moveTaskToBack(true) },
                                onSettingsClick = { showSettings = true }
                            )
                        }
                    }
                    if (showSettings) SettingsDialog { showSettings = false }
                }
            }
        }
    }

    private fun createPlayer(resId: Int, isLooping: Boolean, onEnd: () -> Unit = {}): ExoPlayer {
        return ExoPlayer.Builder(this).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse("android.resource://$packageName/$resId")))
            repeatMode = if (isLooping) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
            prepare()
            addListener(object : Player.Listener { override fun onPlaybackStateChanged(state: Int) { if (state == Player.STATE_ENDED) onEnd() } })
            playWhenReady = true
        }
    }

    override fun onDestroy() { super.onDestroy(); splashPlayer?.release(); waitingPlayer?.release() }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); if (intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) { intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { processTmoneyTag(it) } } }
    override fun onResume() { super.onResume(); nfcAdapter?.enableReaderMode(this, ::processTmoneyTag, 0x0F, null) }
    override fun onPause() { super.onPause(); nfcAdapter?.disableReaderMode(this) }

    private fun processTmoneyTag(tag: Tag) {
        if (appState !is AppState.CardResult) appState = AppState.Processing
        Thread {
            try {
                val result = TmoneyReader.read(tag)
                runOnUiThread {
                    if (result != null) {
                        vibrateConfirmation(); cardRotation += 180f
                        appState = AppState.CardResult(result.balance, result.number, result.userType)
                    } else {
                        vibrateError(); Toast.makeText(this, CardConfig.translate("tap_again"), Toast.LENGTH_SHORT).show()
                        if (appState is AppState.Processing) appState = AppState.WaitingForCard
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { vibrateError(); Toast.makeText(this, CardConfig.translate("tap_again"), Toast.LENGTH_SHORT).show(); if (appState is AppState.Processing) appState = AppState.WaitingForCard }
            }
        }.start()
    }

    private fun vibrateConfirmation() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)) else @Suppress("DEPRECATION") vibrator.vibrate(100) }
    private fun vibrateError() = vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1))
}

@OptIn(UnstableApi::class)
@Composable
fun CachedVideoPlayer(player: ExoPlayer) {
    AndroidView(factory = { context -> PlayerView(context).apply { this.player = player; useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM; setBackgroundColor(android.graphics.Color.TRANSPARENT) } }, modifier = Modifier.fillMaxSize())
}

@Composable
fun CardResultOverlay(targetBalance: String, targetCardNumber: String, targetUserType: String, targetRotation: Float, onDismiss: () -> Unit, onSettingsClick: () -> Unit) {
    var displayedBalance by remember { mutableStateOf(targetBalance) }
    var displayedCardNumber by remember { mutableStateOf(targetCardNumber) }
    var displayedUserType by remember { mutableStateOf(targetUserType) }
    val rotation = remember { Animatable(targetRotation - 180f) }
    LaunchedEffect(targetRotation) { rotation.animateTo(targetRotation, tween(CardConfig.flipAnimationDuration)) { if (this.value >= targetRotation - 90f) { displayedBalance = targetBalance; displayedCardNumber = targetCardNumber; displayedUserType = targetUserType } } }
    Box(modifier = Modifier.fillMaxSize().clickable(onClick = onDismiss), contentAlignment = CardConfig.cardScreenAlignment) {
        TadaCard(balance = displayedBalance, cardNumber = displayedCardNumber, userType = displayedUserType, modifier = Modifier.graphicsLayer { rotationY = rotation.value; cameraDistance = CardConfig.cameraDistance * density }.graphicsLayer { val norm = (rotation.value % 360 + 360) % 360; if (norm > 90 && norm < 270) rotationY = 180f }, onCloseClick = onDismiss, onSettingsClick = onSettingsClick)
    }
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = CardConfig.activeBg, modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f).border(2.dp, CardConfig.activeAccent.copy(0.3f), RoundedCornerShape(28.dp))) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                val context = LocalContext.current
                Text(text = CardConfig.translate("settings"), fontSize = 28.sp, fontWeight = FontWeight.Black, color = CardConfig.activeAccent)
                Spacer(modifier = Modifier.height(24.dp)); SectionLabel(CardConfig.translate("language"))
                CardConfig.Language.entries.forEach { lang -> BigControlTile(lang.label, CardConfig.currentLanguage == lang) { CardConfig.currentLanguage = lang }; Spacer(modifier = Modifier.height(8.dp)) }
                Spacer(modifier = Modifier.height(32.dp))
                Surface(color = CardConfig.activeAccent.copy(alpha = 0.05f), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = { val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "${CardConfig.shareAppMessage}\n${CardConfig.getAppLink(context.packageName)}") }; context.startActivity(Intent.createChooser(intent, "Share")) }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = CardConfig.activeAccent), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Share, null); Spacer(Modifier.width(12.dp)); Text(CardConfig.shareAppLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "${CardConfig.translate("version")} 1.0.3", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = CardConfig.activeText.copy(alpha = 0.4f))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = CardConfig.activeBg, contentColor = CardConfig.activeText.copy(alpha = 0.8f)), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, CardConfig.activeText.copy(alpha = 0.2f))) { Text(text = CardConfig.translate("close"), fontWeight = FontWeight.Bold, fontSize = 16.sp) }
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
