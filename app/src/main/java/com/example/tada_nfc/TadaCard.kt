package com.example.tada_nfc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
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
 * Адаптивный компонент карточки баланса.
 * Использует параметры из CardConfig для управления отступами и размерами.
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
    val currentHeaderColor = CardConfig.getHeaderColor(userType)

    Card(
        shape = RoundedCornerShape(CardConfig.cardCornerRadius),
        modifier = modifier
            .fillMaxWidth(CardConfig.cardWidthFraction)
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // --- Шапка карточки ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CardConfig.headerHeight)
                    .background(currentHeaderColor)
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
                                color = currentHeaderColor,
                                fontWeight = FontWeight.Black,
                                fontSize = (CardConfig.logoSize.value * 0.6).sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "TADA",
                        color = Color.White,
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
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize(0.6f)
                    )
                }
            }

            // --- Тело карточки ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardConfig.bodyBackgroundColor)
                    .padding(CardConfig.spacingBodyPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Блок категории
                    val displayType = if (userType.uppercase() == "UNKNOWN") 
                        CardConfig.translate("unsupported_card") 
                    else 
                        CardConfig.translate(userType.uppercase())
                    
                    val finalLabel = if (userType.uppercase() == "HIPASS") "$displayType+" else displayType
                    val categoryIcon = getCategoryIcon(userType.uppercase())
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (categoryIcon != null) {
                                Icon(
                                    imageVector = categoryIcon,
                                    contentDescription = null,
                                    tint = currentHeaderColor.copy(alpha = 0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = finalLabel,
                                color = currentHeaderColor.copy(alpha = 0.8f),
                                fontSize = CardConfig.userTypeSize,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        if (userType.uppercase() == "CHILD" || userType.uppercase() == "YOUTH") {
                            Spacer(modifier = Modifier.height(CardConfig.spacingCategoryToAge))
                            val ageKey = if (userType.uppercase() == "CHILD") "age_child" else "age_youth"
                            Text(
                                text = CardConfig.translate(ageKey),
                                color = CardConfig.secondaryTextColor,
                                fontSize = CardConfig.ageInfoSize,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(CardConfig.spacingAgeToBalance))

                    // 2. Блок Баланса
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "₩",
                            color = CardConfig.balanceSymbolColor,
                            fontSize = CardConfig.balanceSymbolSize,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(top = 10.dp, end = 4.dp)
                        )
                        Text(
                            text = balance,
                            color = CardConfig.bodyTextColor,
                            fontSize = CardConfig.balanceTextSize,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }

                    Spacer(modifier = Modifier.height(CardConfig.spacingBalanceToNumber))

                    // 3. Блок номера карты
                    Text(
                        text = cardNumber,
                        color = CardConfig.secondaryTextColor,
                        fontSize = CardConfig.cardNumberSize,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }

                // Кнопка настроек
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(CardConfig.settingsIconSize)
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = currentHeaderColor.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxSize(0.7f)
                    )
                }
            }
        }
    }
}


private fun getCategoryIcon(userType: String): ImageVector? {
    return when (userType.uppercase()) {
        "HIPASS" -> Icons.Outlined.DirectionsCar // Машина (дорожные сборы)
        "ADULT" -> Icons.Outlined.Person         // Стандартный пользователь
        "CHILD" -> Icons.Outlined.ChildCare      // Ребенок
        "YOUTH" -> Icons.Outlined.School         // Школьник/Студент
        "UNKNOWN" -> Icons.Outlined.HelpOutline  // Вопросительный знак (не распознано)
        else -> Icons.Outlined.ErrorOutline      // Ошибка по умолчанию
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
                userType = "YOUTH"
            )
        }
    }
}
