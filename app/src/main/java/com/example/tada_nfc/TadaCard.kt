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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
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
                            
                            // Нативная реклама (заглушка по дизайну с каруселью)
                            CoupangNativeAd(
                                ads = listOf(
                                    AdItem(
                                        title = "쎈탁스 닥터피플 멀티비타민 올인원, 1개, 36정",
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
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://partners.coupang.com/"))
                                    // context.startActivity(intent)
                                }
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
    val actualPageCount = ads.size
    val infinitePageCount = Int.MAX_VALUE
    val initialPage = infinitePageCount / 2
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { infinitePageCount }
    )
    
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(isDragged) {
        if (!isDragged) {
            while (true) {
                delay(CardConfig.adAutoScrollDelay)
                pagerState.animateScrollToPage(
                    page = pagerState.currentPage + 1,
                    animationSpec = tween(
                        durationMillis = CardConfig.adScrollDuration,
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
            contentPadding = PaddingValues(horizontal = CardConfig.adCarouselPadding),
            pageSpacing = CardConfig.adCarouselSpacing
        ) { index ->
            val page = index % actualPageCount
            val ad = ads[page]
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CardConfig.adImageSize + 25.dp) // Динамическая высота контейнера
                    .clickable { onClick(ad) },
                contentAlignment = Alignment.Center
            ) {
                // 1. Информационная плашка (Нижний слой)
                Surface(
                    color = CardConfig.adInfoBg,
                    shape = RoundedCornerShape(
                        topStart = CardConfig.adInfoCornerSmall, 
                        bottomStart = CardConfig.adInfoCornerSmall, 
                        topEnd = CardConfig.adInfoCornerLarge, 
                        bottomEnd = CardConfig.adInfoCornerLarge
                    ),
                    border = BorderStroke(1.dp, CardConfig.adBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CardConfig.adInfoHeight)
                        .offset(x = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = CardConfig.adTextPaddingStart, 
                                end = CardConfig.adTextPaddingEnd
                            ),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = CardConfig.adTextAlignment
                    ) {
                        Text(
                            text = ad.title,
                            fontSize = CardConfig.adTitleSize(),
                            lineHeight = 13.sp, // Немного увеличили высоту строки для читаемости двух строк
                            color = CardConfig.adTitleColor,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2, // Теперь разрешено 2 строки
                            overflow = TextOverflow.Ellipsis,
                            textAlign = if (CardConfig.adTextAlignment == Alignment.End) TextAlign.End else TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = CardConfig.adPriceArrangement
                        ) {
                            Surface(
                                color = CardConfig.adDiscountBg,
                                shape = RoundedCornerShape(CardConfig.adDiscountCorner)
                            ) {
                                Text(
                                    text = ad.discount,
                                    fontSize = CardConfig.adDiscountSize(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = ad.price,
                                fontSize = CardConfig.adPriceSize(),
                                color = CardConfig.adPriceColor,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // 2. Картинка товара (Верхний слой с поворотом)
                Surface(
                    color = CardConfig.adProductCardBg,
                    shape = RoundedCornerShape(CardConfig.adImageCorner),
                    shadowElevation = CardConfig.adImageShadow,
                    modifier = Modifier
                        .size(CardConfig.adImageSize)
                        .align(Alignment.CenterStart)
                        .graphicsLayer { rotationZ = CardConfig.adImageRotation }
                        .zIndex(1f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                        Icon(
                            imageVector = when(page % 3) {
                                0 -> Icons.Outlined.ShoppingBag
                                1 -> Icons.Outlined.AutoAwesome
                                else -> Icons.Outlined.LocalMall
                            },
                            contentDescription = null,
                            tint = Color.LightGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        // Здесь будет Coil или Glide для загрузки реального изображения
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Индикаторы
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(actualPageCount) { index ->
                val isSelected = (pagerState.currentPage % actualPageCount) == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 6.dp else 4.dp)
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
