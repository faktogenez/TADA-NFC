package com.example.tada_nfc.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tada_nfc.config.CardConfig
import com.example.tada_nfc.models.TransactionPlaceholder
import com.example.tada_nfc.ui.components.CoupangAdView

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
                .fillMaxWidth(CardConfig.cardWidthFraction)
                .fillMaxHeight(0.9f)
                .border(2.dp, CardConfig.activeAccent.copy(0.3f), RoundedCornerShape(28.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
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
                
                CoupangAdView()

                Spacer(modifier = Modifier.height(16.dp))

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
        color = Color(0xFFFFFFFF),
        shape = RoundedCornerShape(CardConfig.historyItemCornerRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFECEFF1)),
        modifier = Modifier.fillMaxWidth().height(CardConfig.historyItemHeight)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CardConfig.historyItemPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tx.icon,
                contentDescription = null,
                tint = CardConfig.activeAccent, // Slate style
                modifier = Modifier.size(CardConfig.historyIconSize)
            )
            
            Spacer(Modifier.width(16.dp))
            
            Text(
                text = tx.amount,
                fontWeight = FontWeight.Black,
                fontSize = CardConfig.historyAmountTextSize,
                color = if (tx.amount.startsWith("+")) Color(0xFF2E7D32) else Color(0xFFC62828),
                modifier = Modifier.weight(1f)
            )
            
            Box(modifier = Modifier.fillMaxHeight().padding(bottom = 8.dp), contentAlignment = Alignment.BottomEnd) {
                Text(
                    text = tx.balanceAfter,
                    fontSize = CardConfig.historyBalanceTextSize,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF546E7A) // Muted Slate
                )
            }
        }
    }
}
