package com.bodayan.tada.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import com.bodayan.tada.config.CardConfig
import com.bodayan.tada.models.AdItem
import com.bodayan.tada.ui.components.TadaCard

@Composable
fun CardResultOverlay(
    targetBalance: String, 
    targetCardNumber: String, 
    targetUserType: String, 
    targetRotation: Float,
    ads: List<AdItem>,
    onDismiss: () -> Unit, 
    onSettingsClick: () -> Unit, 
    onHistoryClick: () -> Unit,
    onAdClick: (AdItem) -> Unit
) {
    var displayedBalance by remember { mutableStateOf(targetBalance) }
    var displayedCardNumber by remember { mutableStateOf(targetCardNumber) }
    var displayedUserType by remember { mutableStateOf(targetUserType) }
    
    val rotation = remember { Animatable(targetRotation - 180f) }
    val density = LocalDensity.current.density
    
    LaunchedEffect(targetRotation) { 
        rotation.animateTo(targetRotation, tween(CardConfig.flipAnimationDuration)) { 
            if (this.value >= targetRotation - 90f) { 
                displayedBalance = targetBalance
                displayedCardNumber = targetCardNumber
                displayedUserType = targetUserType 
            } 
        } 
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss),
        contentAlignment = CardConfig.cardScreenAlignment
    ) {
        TadaCard(
            balance = displayedBalance, 
            cardNumber = displayedCardNumber, 
            userType = displayedUserType, 
            modifier = Modifier
                .graphicsLayer { 
                    rotationY = rotation.value
                    cameraDistance = CardConfig.cameraDistance * density 
                }
                .graphicsLayer { 
                    val norm = (rotation.value % 360 + 360) % 360
                    if (norm > 90 && norm < 270) rotationY = 180f 
                }, 
            onCloseClick = onDismiss, 
            onSettingsClick = onSettingsClick, 
            onHistoryClick = onHistoryClick,
            ads = ads,
            onAdClick = onAdClick
        )
    }
}
