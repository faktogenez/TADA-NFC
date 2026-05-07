package com.example.tada_nfc.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PortableWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tada_nfc.config.CardConfig
import com.example.tada_nfc.ui.components.TadaLogo

@Composable
fun NfcDisabledDialog(onEnableClick: () -> Unit, onCloseClick: () -> Unit) {
    val vibrator = (LocalContext.current.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
    val errorColor = CardConfig.colorError
    val accentColor = CardConfig.dialogAccent
    
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
                    modifier = Modifier.padding(
                        horizontal = CardConfig.nfcOffContentPadding(), 
                        vertical = CardConfig.nfcOffContentPadding() * 1.3f
                    ),
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
                    
                    // 3. Title
                    Text(
                        text = CardConfig.translate("nfc_off_title").uppercase(),
                        fontSize = CardConfig.nfcOffTitleSize(),
                        fontWeight = FontWeight.Black,
                        color = errorColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        maxLines = 1
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
