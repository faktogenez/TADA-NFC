package com.bodayan.tada.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.bodayan.tada.config.CardConfig
import com.bodayan.tada.models.AdItem
import kotlinx.coroutines.delay

@Composable
fun CoupangNativeAd(
    ads: List<AdItem>,
    onClick: (AdItem) -> Unit
) {
    val actualPageCount = ads.size
    if (actualPageCount == 0) return

    val infinitePageCount = Int.MAX_VALUE
    val initialPage = infinitePageCount / 2
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { infinitePageCount }
    )
    
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(isDragged, actualPageCount) {
        if (!isDragged && actualPageCount > 1) {
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
                    .height(CardConfig.adImageSize + 25.dp)
                    .clickable { onClick(ad) },
                contentAlignment = Alignment.Center
            ) {
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
                            text = ad.description,
                            fontSize = CardConfig.adTitleSize(),
                            lineHeight = 13.sp,
                            color = CardConfig.adTitleColor,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = if (CardConfig.adTextAlignment == Alignment.End) TextAlign.End else TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = CardConfig.adPriceArrangement
                        ) {
                            val isStore = ad.discount.contains("%") || ad.action.type == "app"
                            val themeColor = when {
                                isStore -> Color(0xFFC62828)      // Красный (Магазины/Скидки)
                                ad.action.type == "telegram" -> Color(0xFF0088CC) // Синий (Telegram)
                                else -> Color(0xFF2E7D32)         // Зеленый (Сайты)
                            }

                            if (ad.discount.isNotBlank()) {
                                Surface(
                                    color = themeColor,
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
                            }
                            Text(
                                text = ad.price,
                                fontSize = CardConfig.adPriceSize(),
                                color = themeColor,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Surface(
                    color = CardConfig.adProductCardBg,
                    shape = RoundedCornerShape(CardConfig.adImageCorner),
                    border = BorderStroke(2.dp, Color.White), // Белая обводка 2dp
                    shadowElevation = CardConfig.adImageShadow,
                    modifier = Modifier
                        .size(CardConfig.adImageSize)
                        .align(Alignment.CenterStart)
                        .graphicsLayer { rotationZ = CardConfig.adImageRotation }
                        .zIndex(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (ad.imageUrl.isNotBlank()) {
                            val finalUrl = remember(ad.imageUrl) {
                                if (ad.imageUrl.contains("drive.google.com")) {
                                    val id = ad.imageUrl.substringAfter("/d/").substringBefore("/")
                                    "https://lh3.googleusercontent.com/u/0/d/$id"
                                } else ad.imageUrl
                            }
                            AsyncImage(
                                model = finalUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = null,
                                tint = Color.LightGray.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "이 포스팅은 쿠팡 파트너스 활동의 일환으로, 이에 따른 일정액의 수수료를 제공받습니다.",
            fontSize = 9.sp,
            lineHeight = 10.sp,
            color = Color.Gray.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
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
