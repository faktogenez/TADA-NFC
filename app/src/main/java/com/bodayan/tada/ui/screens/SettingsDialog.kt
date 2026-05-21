package com.bodayan.tada.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bodayan.tada.config.CardConfig
import com.bodayan.tada.ui.components.CoupangAdView

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
                .fillMaxWidth(CardConfig.cardWidthFraction)
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
                    Text(
                        text = CardConfig.translate("settings"), 
                        fontSize = 28.sp, 
                        fontWeight = FontWeight.Black, 
                        color = CardConfig.activeAccent
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close, 
                            contentDescription = null, 
                            tint = CardConfig.activeText.copy(alpha = 0.5f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                SectionLabel(CardConfig.translate("language"))
                
                CardConfig.Language.entries.forEach { lang -> 
                    BigControlTile(lang.label, CardConfig.currentLanguage == lang) { 
                        CardConfig.currentLanguage = lang 
                    }
                    Spacer(modifier = Modifier.height(8.dp)) 
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Surface(
                    color = CardConfig.activeAccent.copy(alpha = 0.05f), 
                    shape = RoundedCornerShape(20.dp), 
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp), 
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { 
                                val intent = Intent(Intent.ACTION_SEND).apply { 
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${CardConfig.shareAppMessage}\n${CardConfig.getAppLink(context.packageName)}") 
                                }
                                context.startActivity(Intent.createChooser(intent, "Share")) 
                            }, 
                            modifier = Modifier.fillMaxWidth().height(56.dp), 
                            colors = ButtonDefaults.buttonColors(containerColor = CardConfig.activeAccent), 
                            shape = RoundedCornerShape(12.dp)
                        ) { 
                            Icon(Icons.Default.Share, null)
                            Spacer(Modifier.width(12.dp))
                            Text(CardConfig.shareAppLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp) 
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val url = "https://wa.me/${CardConfig.SUPPORT_WHATSAPP}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.Forum, null, tint = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = CardConfig.translate("contact_dev"), 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 16.sp, 
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        CoupangAdView()

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${CardConfig.translate("version")} 1.0.3", 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Medium, 
                            color = CardConfig.activeText.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) { 
    Text(
        text = text.uppercase(), 
        fontSize = 12.sp, 
        fontWeight = FontWeight.Bold, 
        color = CardConfig.activeAccent, 
        letterSpacing = 1.sp, 
        modifier = Modifier.padding(bottom = 8.dp)
    ) 
}

@Composable
fun BigControlTile(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick, 
        shape = RoundedCornerShape(16.dp), 
        color = if (isSelected) CardConfig.activeAccent else CardConfig.activeAccent.copy(0.05f), 
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold, 
                color = if (isSelected) CardConfig.activeBg else CardConfig.activeText
            )
            if (isSelected) Icon(Icons.Default.Check, null, tint = CardConfig.activeBg)
        }
    }
}
