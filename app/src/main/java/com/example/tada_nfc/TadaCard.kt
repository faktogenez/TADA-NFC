package com.example.tada_nfc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tada_nfc.ui.theme.TADA_NFCTheme

/**
 * Основной компонент карточки баланса.
 * Дизайн адаптирован под скриншот: синяя шапка и светлое тело.
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
                    .fillMaxHeight(0.22f)
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

                // Иконка закрытия (Просто 'X' без круга)
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier.size(CardConfig.closeIconSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = CardConfig.headerTextColor,
                        modifier = Modifier.size(32.dp)
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
                    // 1. Категория с иконкой
                    val displayType = CardConfig.translate(userType.uppercase())
                    val finalLabel = if (userType.uppercase() == "HIPASS") "$displayType+" else displayType
                    val categoryIcon = getCategoryIcon(userType.uppercase())
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        if (categoryIcon != null) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = CardConfig.userTypeColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = finalLabel,
                            color = CardConfig.userTypeColor,
                            fontSize = CardConfig.userTypeSize,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // 2. БАЛАНС
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

                    // 3. Номер карты
                    Text(
                        text = cardNumber,
                        color = CardConfig.secondaryTextColor,
                        fontSize = CardConfig.cardNumberSize,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp),
                        maxLines = 1
                    )
                }

                // Кнопка настроек (Шестеренка)
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(CardConfig.settingsIconSize)
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = CardConfig.headerColor,
                        modifier = Modifier.size(CardConfig.settingsIconSize)
                    )
                }
            }
        }
    }
}

private fun getCategoryIcon(userType: String): ImageVector? {
    return when (userType) {
        "HIPASS" -> Icons.Default.DirectionsCar
        "ADULT" -> Icons.Default.Person
        "CHILD" -> Icons.Default.Face
        "YOUTH" -> Icons.Default.School
        else -> null
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
