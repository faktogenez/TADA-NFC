package com.example.tada_nfc.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tada_nfc.ui.components.CachedVideoPlayer
import com.example.tada_nfc.config.CardConfig
import androidx.media3.exoplayer.ExoPlayer

@Composable
fun WaitingScreen(player: ExoPlayer?) {
    Box(modifier = Modifier.fillMaxSize()) {
        player?.let { CachedVideoPlayer(it) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
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
