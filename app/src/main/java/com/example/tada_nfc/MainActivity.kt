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
    private const val TAG = "TmoneyReader"
    private val decimalFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' })

    fun read(tag: Tag): CardData? {
        val techList = tag.techList.toList()
        if (techList.contains("android.nfc.tech.IsoDep")) {
            return readIsoDep(IsoDep.get(tag))
        }
        return null
    }

    private fun readIsoDep(iso: IsoDep?): CardData? {
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
                val cmd = byteArrayOf(0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), aid.size.toByte()) + aid + byteArrayOf(0x00.toByte())
                val res = iso.transceive(cmd)
                if (isSuccess(res)) {
                    selectRes = res
                    cardBrand = pair.second
                    break
                }
            }

            if (selectRes == null) return null

            // Balance
            val balanceCmd = if (cardBrand == "HIPASS") hexToBytes("905C000004") else hexToBytes("904C000004")
            val bRes = iso.transceive(balanceCmd)
            if (!isSuccess(bRes) || bRes.size < 4) return null
            val balanceInt = (bRes[0].toInt() and 0xFF shl 24) or (bRes[1].toInt() and 0xFF shl 16) or (bRes[2].toInt() and 0xFF shl 8) or (bRes[3].toInt() and 0xFF)
            val balanceStr = decimalFormat.format(balanceInt)

            var userType = ""
            var cardNumber = ""

            if (cardBrand == "HIPASS") {
                userType = "HIPASS"
                var rawNum: ByteArray? = null
                
                // Try from select response
                val idx = findPattern(selectRes, byteArrayOf(0x00.toByte(), 0x20.toByte()))
                if (idx != -1 && selectRes.size >= idx + 8) {
                    rawNum = selectRes.sliceArray(idx until idx + 8)
                } else {
                    // Try SFI 0x88
                    val sfiRes = iso.transceive(hexToBytes("00B088000C"))
                    if (isSuccess(sfiRes)) {
                        val idxSfi = findPattern(sfiRes, byteArrayOf(0x00.toByte(), 0x20.toByte()))
                        if (idxSfi != -1 && sfiRes.size >= idxSfi + 8) {
                            rawNum = sfiRes.sliceArray(idxSfi until idxSfi + 8)
                        }
                    }
                }
                
                if (rawNum != null) {
                    val h = toHex(rawNum) // Already 16 digits
                    cardNumber = "${h.substring(0, 4)} ${h.substring(4, 8)} ${h.substring(8, 12)} ${h.substring(12, 16)}"
                } else {
                    cardNumber = "0020 **** **** ****"
                }
            } else {
                // User Type
                userType = if (selectRes.size > 29) {
                    when (selectRes[29].toInt() and 0xFF) {
                        0x01 -> "ADULT"
                        0x02 -> "CHILD"
                        0x04 -> "YOUTH"
                        else -> "UNKNOWN"
                    }
                } else "UNKNOWN"

                // Card Number
                var rawBcd: ByteArray? = null
                val tagIdx = findPattern(selectRes, byteArrayOf(0x12.toByte(), 0x08.toByte()))
                if (tagIdx != -1 && selectRes.size >= tagIdx + 10) {
                    rawBcd = selectRes.sliceArray(tagIdx + 2 until tagIdx + 10)
                } else if (selectRes.size >= 16) {
                    rawBcd = selectRes.sliceArray(8 until 16)
                }

                // T-money fallback
                if (cardBrand == "TMONEY" && (rawBcd == null || rawBcd.all { it == 0.toByte() })) {
                    val rRes = iso.transceive(hexToBytes("00B2011400"))
                    if (isSuccess(rRes) && rRes.size >= 0x2E + 8) {
                        rawBcd = rRes.sliceArray(0x2E until 0x2E + 8)
                    }
                }

                if (rawBcd != null) {
                    val b = toHex(rawBcd)
                    cardNumber = "${b.substring(0, 4)} **** **** ${b.substring(12, 16)}"
                } else {
                    cardNumber = "**** **** **** ****"
                }
            }

            return CardData(balanceStr, cardNumber, userType)
        } catch (e: Exception) {
            throw e
        } finally {
            runCatching { iso.close() }
        }
    }

    private fun hexToBytes(s: String): ByteArray = s.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    private fun toHex(bytes: ByteArray): String = bytes.joinToString("") { "%02X".format(it) }
    private fun isSuccess(res: ByteArray): Boolean = res.size >= 2 && res[res.size - 2] == 0x90.toByte()
    private fun findPattern(data: ByteArray, pattern: ByteArray): Int {
        if (data.size < pattern.size) return -1
        for (i in 0..data.size - pattern.size) {
            var match = true
            for (j in pattern.indices) {
                if (data[i + j] != pattern[j]) {
                    match = false
                    break
                }
            }
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
                    AnimatedContent(
                        targetState = appState,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(150)) },
                        label = "MainFlow"
                    ) { state ->
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
                                        Text(
                                            text = CardConfig.videoInstruction.uppercase(),
                                            fontSize = CardConfig.instructionFontSize,
                                            fontWeight = CardConfig.instructionFontWeight,
                                            color = CardConfig.instructionColor,
                                            textAlign = TextAlign.Center,
                                            style = TextStyle(shadow = Shadow(color = CardConfig.instructionShadowColor, blurRadius = CardConfig.instructionShadowBlur))
                                        )
                                    }
                                }
                            }
                            is AppState.Processing -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)
                                }
                            }
                            is AppState.CardResult -> CardResultOverlay(
                                targetBalance = state.balance,
                                targetCardNumber = state.cardNumber,
                                targetUserType = state.userType,
                                targetRotation = cardRotation,
                                onDismiss = { 
                                    appState = AppState.WaitingForCard
                                    moveTaskToBack(true) 
                                },
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
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) { if (state == Player.STATE_ENDED) onEnd() }
            })
            playWhenReady = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        splashPlayer?.release()
        waitingPlayer?.release()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { processTmoneyTag(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableReaderMode(this, ::processTmoneyTag, 0x0F, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    private fun processTmoneyTag(tag: Tag) {
        if (appState !is AppState.CardResult) appState = AppState.Processing
        
        Thread {
            try {
                val result = TmoneyReader.read(tag)
                
                runOnUiThread {
                    if (result != null) {
                        vibrateConfirmation()
                        cardRotation += 180f
                        appState = AppState.CardResult(result.balance, result.number, result.userType)
                    } else {
                        vibrateError()
                        Toast.makeText(this, "Приложите карту еще раз", Toast.LENGTH_SHORT).show()
                        if (appState is AppState.Processing) {
                            appState = AppState.WaitingForCard
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    vibrateError()
                    Toast.makeText(this, "Приложите карту еще раз", Toast.LENGTH_SHORT).show()
                    if (appState is AppState.Processing) {
                        appState = AppState.WaitingForCard
                    }
                }
            }
        }.start()
    }

    private fun vibrateConfirmation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(100)
        }
    }

    private fun vibrateError() = vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1))
}

@OptIn(UnstableApi::class)
@Composable
fun CachedVideoPlayer(player: ExoPlayer) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                this.player = player; useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun CardResultOverlay(targetBalance: String, targetCardNumber: String, targetUserType: String, targetRotation: Float, onDismiss: () -> Unit, onSettingsClick: () -> Unit) {
    var displayedBalance by remember { mutableStateOf(targetBalance) }
    var displayedCardNumber by remember { mutableStateOf(targetCardNumber) }
    var displayedUserType by remember { mutableStateOf(targetUserType) }
    val rotation = remember { Animatable(targetRotation - 180f) }

    LaunchedEffect(targetRotation) {
        rotation.animateTo(targetRotation, tween(CardConfig.flipAnimationDuration)) {
            if (this.value >= targetRotation - 90f) {
                displayedBalance = targetBalance
                displayedCardNumber = targetCardNumber
                displayedUserType = targetUserType
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().clickable(onClick = onDismiss), contentAlignment = CardConfig.cardScreenAlignment) {
        TadaCard(
            balance = displayedBalance,
            cardNumber = displayedCardNumber,
            userType = displayedUserType,
            modifier = Modifier.graphicsLayer {
                rotationY = rotation.value
                cameraDistance = CardConfig.cameraDistance * density
            }.graphicsLayer {
                val norm = (rotation.value % 360 + 360) % 360
                if (norm > 90 && norm < 270) rotationY = 180f
            },
            onCloseClick = onDismiss,
            onSettingsClick = onSettingsClick
        )
    }
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp), color = CardConfig.activeBg,
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f).border(2.dp, CardConfig.activeAccent.copy(0.3f), RoundedCornerShape(28.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                val context = LocalContext.current
                Text(text = CardConfig.translate("settings"), fontSize = 28.sp, fontWeight = FontWeight.Black, color = CardConfig.activeAccent)
                Spacer(modifier = Modifier.height(24.dp)); SectionLabel(CardConfig.translate("language"))
                CardConfig.Language.values().forEach { lang -> BigControlTile(lang.label, CardConfig.currentLanguage == lang) { CardConfig.currentLanguage = lang }; Spacer(modifier = Modifier.height(8.dp)) }
                Spacer(modifier = Modifier.height(24.dp)); SectionLabel(CardConfig.translate("theme"))
                CardConfig.ThemeType.values().forEach { theme -> BigThemeTile(theme, CardConfig.currentTheme == theme) { CardConfig.currentTheme = theme }; Spacer(modifier = Modifier.height(8.dp)) }
                Spacer(modifier = Modifier.height(32.dp))
                Surface(color = CardConfig.activeAccent.copy(alpha = 0.05f), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply { 
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${CardConfig.shareAppMessage}\n${CardConfig.getAppLink(context.packageName)}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share"))
                        }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = CardConfig.activeAccent), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Share, null); Spacer(Modifier.width(12.dp)); Text(CardConfig.shareAppLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "${CardConfig.translate("version")} 1.0.3", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = CardConfig.activeText.copy(alpha = 0.4f))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = CardConfig.activeBg, contentColor = CardConfig.activeText.copy(alpha = 0.8f)), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, CardConfig.activeText.copy(alpha = 0.2f))) {
                    Text(text = CardConfig.translate("close"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

@Composable
fun BigThemeTile(theme: CardConfig.ThemeType, isSelected: Boolean, onClick: () -> Unit) {
    val color = when(theme) {
        CardConfig.ThemeType.DARK -> Color(0xFFBD93F9)
        CardConfig.ThemeType.LIGHT -> Color(0xFF007AFF)
        CardConfig.ThemeType.NEON -> Color(0xFF00FFFF)
        CardConfig.ThemeType.SAKURA -> Color(0xFFFF69B4)
    }
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = if (isSelected) color.copy(0.1f) else Color.Transparent, border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, color) else null, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(24.dp).background(color, CircleShape)); Spacer(Modifier.width(16.dp)); Text(theme.displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CardConfig.activeText)
        }
    }
}
