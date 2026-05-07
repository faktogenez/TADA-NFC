package com.example.tada_nfc.ui.components

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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.tada_nfc.config.CardConfig
import kotlinx.coroutines.delay

data class AdItem(
    val title: String,
    val discount: String,
    val price: String,
    val imageUrl: String = ""
)

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
                            text = ad.title,
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
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
