package com.example.tada_nfc

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tada_nfc.ui.theme.TADA_NFCTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Компактный логотип TADA с буквой T.
 */
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

/**
 * Адаптивный компонент карточки баланса.
 */
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
            // --- Шапка карточки ---
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

            // --- Тело карточки ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardConfig.bodyBackgroundColor)
                    .padding(CardConfig.spacingBodyPadding()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 1. Блок категории
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
                                fontSize = if (isUnknown || isRetry) CardConfig.userTypeSize() else CardConfig.userTypeSize(),
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

                        // 2. Блок Баланса
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

                        // 3. Блок номера карты
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
                            
                            // Нативная реклама (заглушка по дизайну)
                            CoupangNativeAd(
                                ads = listOf(
                                    AdItem(
                                        title = "판타스 닥터피플 캔ди비\n다면 뷰엔틴, 1개, 36정",
                                        discount = "25%",
                                        price = "14,200원"
                                    ),
                                    AdItem(
                                        title = "Premium Multi-Vitamin\nDaily Care Gold, 60 Tabs",
                                        discount = "30%",
                                        price = "28,500원"
                                    ),
                                    AdItem(
                                        title = "Natural Energy Booster\nOrganic Extract, 500ml",
                                        discount = "15%",
                                        price = "9,900원"
                                    )
                                ),
                                onClick = { /* Будет открывать ссылку */ }
                            )
                        }
                    }
                }

                // Кнопка настроек
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
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


/**
 * Данные для одного рекламного баннера
 */
data class AdItem(
    val title: String,
    val discount: String,
    val price: String,
    val imageUrl: String = ""
)

/**
 * Нативный рекламный блок Coupang с каруселью и автослайдом.
 */
@Composable
fun CoupangNativeAd(
    ads: List<AdItem>,
    onClick: (AdItem) -> Unit
) {
    // Увеличиваем количество страниц для создания эффекта бесконечной прокрутки
    val actualPageCount = ads.size
    val infinitePageCount = Int.MAX_VALUE
    val initialPage = infinitePageCount / 2
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { infinitePageCount }
    )
    
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    // Плавная автоматическая прокрутка
    LaunchedEffect(isDragged) {
        if (!isDragged) {
            while (true) {
                delay(3000) // Пауза перед началом движения
                pagerState.animateScrollToPage(
                    page = pagerState.currentPage + 1,
                    animationSpec = tween(
                        durationMillis = 2000, // Длительность самого перехода (медленно и плавно)
                        easing = LinearEasing
                    )
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp),
            pageSpacing = 0.dp
        ) { index ->
            val page = index % actualPageCount
            val ad = ads[page]
            Surface(
                color = CardConfig.adCardBg,
                shape = RoundedCornerShape(CardConfig.adCardCornerRadius),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .fillMaxWidth()
                    .clickable { onClick(ad) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Изображение товара
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when(page % 3) {
                                    0 -> Icons.Outlined.ShoppingBag
                                    1 -> Icons.Outlined.AutoAwesome
                                    else -> Icons.Outlined.LocalMall
                                },
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = ad.title,
                            fontSize = CardConfig.adTitleSize(),
                            lineHeight = 14.sp,
                            color = CardConfig.adTitleColor,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = CardConfig.adDiscountColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = ad.discount,
                                    fontSize = CardConfig.adDiscountSize(),
                                    color = CardConfig.adDiscountColor,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ad.price,
                                fontSize = CardConfig.adPriceSize(),
                                color = CardConfig.adPriceColor,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Индикаторы страниц (точки)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(actualPageCount) { index ->
                val isSelected = (pagerState.currentPage % actualPageCount) == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 7.dp else 5.dp)
                        .background(
                            color = if (isSelected) Color.Gray else Color.LightGray.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                )
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
