package com.bodayan.tada

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bodayan.tada.config.CardConfig
import com.bodayan.tada.firebase.FirebaseManager
import com.bodayan.tada.models.AppState
import com.bodayan.tada.nfc.TmoneyReader
import com.bodayan.tada.providers.TadaWidgetProvider
import com.bodayan.tada.services.BalanceNotificationService
import com.bodayan.tada.ui.screens.*
import com.bodayan.tada.ui.theme.TADA_NFCTheme
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val nfcAdapter by lazy { NfcAdapter.getDefaultAdapter(this) }
    private val vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private var appState by mutableStateOf<AppState>(
        if (intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) AppState.Processing
        else AppState.Splash
    )
    private var cardRotation by mutableStateOf(0f)
    private var showSettings by mutableStateOf(false)

    private var splashPlayer: ExoPlayer? = null
    private var waitingPlayer: ExoPlayer? = null
    private val playerCache = mutableMapOf<Int, ExoPlayer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
            FirebaseManager.init()
            FirebaseManager.fetchAds(this) { 
                // Ads are updated in manager's cache
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Firebase init error: ${e.message}")
        }

        requestNotificationPermission()
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val nfc = NfcAdapter.getDefaultAdapter(this)
        val isNfcEnabled = nfc?.isEnabled == true

        if (!isNfcEnabled) {
            appState = AppState.NfcDisabled
        } else {
            if (intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { processTmoneyTag(it) }
            } else {
                splashPlayer = createPlayer(R.raw.splash, false) {
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
                BackHandler(enabled = appState is AppState.Processing || appState is AppState.NfcDisabled) {
                    if (appState is AppState.NfcDisabled) {
                        finishAffinity()
                    }
                }

                val bgAlpha by animateFloatAsState(
                    targetValue = if (appState is AppState.CardResult || appState is AppState.Error || appState is AppState.Processing || appState is AppState.History || appState is AppState.NfcDisabled) 0.6f else 1.0f,
                    animationSpec = tween(600),
                    label = "bgAlpha"
                )
                val bgColor = if (appState is AppState.CardResult || appState is AppState.Error || appState is AppState.Processing || appState is AppState.History || appState is AppState.NfcDisabled) Color.Black else Color(0xFFFAF9F6) // Premium Off-White
                
                Box(modifier = Modifier.fillMaxSize().background(bgColor.copy(alpha = bgAlpha))) {
                    AnimatedContent(targetState = appState, label = "MainFlow") { state ->
                        when (state) {
                            is AppState.Splash -> {
                                SplashScreen(splashPlayer)
                            }
                            is AppState.WaitingForCard -> {
                                WaitingScreen(waitingPlayer)
                            }
                            is AppState.Processing -> {
                                ProcessingScreen()
                            }
                            is AppState.NfcDisabled -> NfcDisabledDialog(
                                onEnableClick = {
                                    try {
                                        startActivity(Intent(android.provider.Settings.ACTION_NFC_SETTINGS))
                                    } catch (e: Exception) {
                                        startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                                    }
                                },
                                onCloseClick = { finishAffinity() }
                            )
                            is AppState.CardResult -> CardResultOverlay(
                                targetBalance = state.balance,
                                targetCardNumber = state.cardNumber,
                                targetUserType = state.userType,
                                targetRotation = cardRotation,
                                onDismiss = { finishAffinity() },
                                onSettingsClick = { showSettings = true },
                                onHistoryClick = { appState = AppState.History(result = state) }
                            )
                            is AppState.History -> HistoryScene(
                                transactions = state.result.transactions,
                                onBackClick = { appState = state.result }
                            )
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
                            else -> {}
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { processTmoneyTag(it) }
        }
    }

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

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    private fun processTmoneyTag(tag: Tag) {
        if (appState !is AppState.CardResult) appState = AppState.Processing
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Small delay to prevent race condition with foreground dispatch
                kotlinx.coroutines.delay(150)
                val result = TmoneyReader.read(tag)
                
                withContext(Dispatchers.Main) {
                    if (result != null) {
                        try {
                            com.google.firebase.Firebase.analytics.logEvent("card_read") {
                                param("user_type", result.userType)
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Analytics error: ${e.message}")
                        }

                        vibrateConfirmation()
                        cardRotation += 180f
                        appState = AppState.CardResult(result.balance, result.number, result.userType, result.transactions)
                        
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
                Log.e("MainActivity", "Unexpected error during NFC processing", e)
                withContext(Dispatchers.Main) {
                    if (appState is AppState.Processing) appState = AppState.WaitingForCard
                }
            }
        }
    }

    private fun vibrateConfirmation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(100)
        }
    }

    private fun vibrateError() = vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1))

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}
