package com.bodayan.tada.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.bodayan.tada.config.CardConfig
import com.bodayan.tada.ui.components.AdItem
import com.bodayan.tada.ui.components.CoupangNativeAd
import android.content.Intent
import android.net.Uri
import android.util.Log

@Composable
fun TadaLogo(
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(CardConfig.logoSize())
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "T",
            color = Color.White,
            fontSize = CardConfig.logoIconLetterSize(),
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TadaCard(
    balance: String,
    cardNumber: String,
    userType: String = "",
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CardConfig.headerHeight())
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                currentHeaderColor.copy(alpha = 0.9f),
                                currentHeaderColor
                            )
                        )
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TadaLogo(backgroundColor = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "TADA",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = CardConfig.logoTextSize(),
                        letterSpacing = 2.sp
                    )
                }

                IconButton(
                    onClick = onCloseClick, 
                    modifier = Modifier.size(CardConfig.closeIconSize())
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize(0.6f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardConfig.bodyBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(CardConfig.spacingBodyPadding()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val isUnknown = userType.uppercase() == "UNKNOWN"
                    val isRetry = userType.uppercase() == "RETRY"
                    val categoryIcon = if (isUnknown || isRetry) null else getCategoryIcon(userType.uppercase())
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isUnknown || isRetry) {
                            Icon(
                                imageVector = if (isRetry) Icons.Outlined.CreditCard else Icons.Outlined.CreditCardOff,
                                contentDescription = null,
                                tint = currentHeaderColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
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
                                text = if (isUnknown) CardConfig.translate("card_not_supported").uppercase() 
                                       else if (isRetry) CardConfig.translate("tap_again").uppercase()
                                       else CardConfig.translate(userType.uppercase()),
                                color = currentHeaderColor.copy(alpha = 0.8f),
                                fontSize = CardConfig.userTypeSize(),
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            if (!isUnknown && !isRetry && (userType.uppercase() == "CHILD" || userType.uppercase() == "YOUTH")) {
                                val ageKey = if (userType.uppercase() == "CHILD") "age_child" else "age_youth"
                                Text(
                                    text = " (${CardConfig.translate(ageKey)})",
                                    color = CardConfig.secondaryTextColor,
                                    fontSize = CardConfig.ageInfoSize(),
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (userType.uppercase() == "HIPASS") {
                                Text(
                                    text = "+",
                                    color = currentHeaderColor.copy(alpha = 0.8f),
                                    fontSize = CardConfig.userTypeSize(),
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        if (isUnknown || isRetry) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isRetry) 
                                    CardConfig.translate("instruction") 
                                else 
                                    CardConfig.translate("card_not_supported_desc"),
                                color = CardConfig.secondaryTextColor,
                                fontSize = 13.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }

                    if (!isUnknown && !isRetry) {
                        Spacer(modifier = Modifier.height(CardConfig.spacingAgeToBalance()))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "₩",
                                color = CardConfig.balanceSymbolColor,
                                fontSize = CardConfig.balanceSymbolSize(),
                                fontWeight = FontWeight.Light,
                                modifier = Modifier.padding(top = 10.dp, end = 4.dp)
                            )
                            Text(
                                text = balance,
                                color = CardConfig.bodyTextColor,
                                fontSize = CardConfig.balanceTextSize(),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        }

                        Spacer(modifier = Modifier.height(CardConfig.spacingBalanceToNumber()))

                        Text(
                            text = cardNumber,
                            color = CardConfig.secondaryTextColor,
                            fontSize = CardConfig.cardNumberSize(),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )

                        if (userType.uppercase() != "UNKNOWN" && userType.uppercase() != "RETRY") {
                            if (userType.uppercase() != "HIPASS") {
                                Spacer(modifier = Modifier.height(CardConfig.historyBtnTopSpacing))
                                Button(
                                    onClick = onHistoryClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = currentHeaderColor.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(CardConfig.historyBtnCornerRadius),
                                    contentPadding = PaddingValues(horizontal = CardConfig.historyBtnPadding),
                                    modifier = Modifier
                                        .height(CardConfig.historyBtnHeight)
                                        .width(CardConfig.historyBtnWidth)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.History,
                                        contentDescription = null,
                                        tint = currentHeaderColor,
                                        modifier = Modifier.size(CardConfig.historyBtnIconSize)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = CardConfig.translate("history").uppercase(),
                                        color = currentHeaderColor,
                                        fontSize = CardConfig.historyBtnFontSize,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            val context = LocalContext.current
                            CoupangNativeAd(
                                ads = listOf(
                                    AdItem(
                                        title = "쎈탁с 닥터피플 멀티비타민 올인원, 1개, 36정",
                                        discount = "25%",
                                        price = "14,200원"
                                    ),
                                    AdItem(
                                        title = "Premium Multi-Vitamin Daily Care Gold, 60 Tabs",
                                        discount = "30%",
                                        price = "28,500원"
                                    ),
                                    AdItem(
                                        title = "Natural Energy Booster Organic Extract, 500ml",
                                        discount = "15%",
                                        price = "9,900원"
                                    )
                                ),
                                onClick = { ad ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://partners.coupang.com/"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Log.e("TadaCard", "Error opening Coupang link", e)
                                    }
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .padding(
                            end = CardConfig.settingsIconPaddingEnd,
                            bottom = CardConfig.settingsIconPaddingBottom
                        )
                        .size(CardConfig.settingsIconSize())
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
        "HIPASS" -> Icons.Outlined.DirectionsCar
        "ADULT" -> Icons.Outlined.Person
        "CHILD" -> Icons.Outlined.ChildCare
        "YOUTH" -> Icons.Outlined.School
        "UNKNOWN" -> Icons.Outlined.HelpOutline
        else -> Icons.Outlined.ErrorOutline
    }
}
