package com.bodayan.tada.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.exoplayer.ExoPlayer
import com.bodayan.tada.ui.components.CachedVideoPlayer

@Composable
fun SplashScreen(player: ExoPlayer?) {
    Box(modifier = Modifier.fillMaxSize()) {
        player?.let { CachedVideoPlayer(it) }
    }
}
