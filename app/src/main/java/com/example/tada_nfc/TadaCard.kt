package com.example.tada_nfc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tada_nfc.ui.theme.TADA_NFCTheme

/**
 * Основной компонент карточки баланса.
 * Дизайн адаптирован под скриншот: синяя шапка и светлое тело.
 * Все размеры масштабируются для корректного отображения всех элементов.
 */
@Composable
fun TadaCard(
    balance: String,
    cardNumber: String,
    userType: String = "",
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(CardConfig.cardCornerRadius),
        modifier = modifier
            .fillMaxWidth(CardConfig.cardWidthFraction)
            .aspectRatio(CardConfig.cardAspectRatio),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- Шапка карточки (Header) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.22f) // Шапка занимает 22% высоты
                    .background(CardConfig.headerColor)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(CardConfig.logoSize),
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "T",
                                color = CardConfig.headerColor,
                                fontWeight = FontWeight.Black,
                                fontSize = (CardConfig.logoSize.value * 0.6).sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "TADA",
                        color = CardConfig.headerTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = CardConfig.logoTextSize
                    )
                }

                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier.size(CardConfig.closeIconSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = CardConfig.headerTextColor,
                        modifier = Modifier
                            .fillMaxSize(0.8f)
                            .border(1.5.dp, Color.White, CircleShape)
                            .padding(2.dp)
                    )
                }
            }

            // --- Тело карточки (Body) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CardConfig.bodyBackgroundColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Категория (приглушена)
                    val displayType = CardConfig.translate(userType.uppercase())
                    val finalLabel = if (userType.uppercase() == "HIPASS") "$displayType+" else displayType
                    
                    Text(
                        text = finalLabel,
                        color = CardConfig.userTypeColor,
                        fontSize = CardConfig.userTypeSize,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // 2. БАЛАНС (Центральный и самый крупный элемент)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "₩",
                            color = CardConfig.balanceSymbolColor,
                            fontSize = CardConfig.balanceSymbolSize,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(top = 12.dp, end = 4.dp)
                        )
                        Text(
                            text = balance,
                            color = CardConfig.bodyTextColor,
                            fontSize = CardConfig.balanceTextSize,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 3. Номер карты (внизу, приглушен)
                    Text(
                        text = cardNumber,
                        color = CardConfig.secondaryTextColor,
                        fontSize = CardConfig.cardNumberSize,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp),
                        maxLines = 1
                    )
                }

                // Кнопка инфо (i в круге)
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(CardConfig.settingsIconSize)
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Settings",
                        tint = CardConfig.headerColor,
                        modifier = Modifier
                            .fillMaxSize(0.8f)
                            .border(1.5.dp, CardConfig.headerColor, CircleShape)
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TadaCardPreview() {
    TADA_NFCTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            TadaCard(
                balance = "182,500",
                cardNumber = "3212 **** **** 6788",
                userType = "HIPASS"
            )
        }
    }
}
