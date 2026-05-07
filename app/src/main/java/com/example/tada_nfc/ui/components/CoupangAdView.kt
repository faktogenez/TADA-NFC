package com.example.tada_nfc.ui.components

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tada_nfc.config.CardConfig

@Composable
fun CoupangAdView() {
    val context = LocalContext.current
    val partnersId = CardConfig.COUPANG_PARTNERS_ID
    
    if (partnersId == 0) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            if (url != null && (url.startsWith("coupang://") || url.startsWith("market://") || url.contains("play.google.com"))) {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    return true
                                } catch (e: Exception) {
                                    return false
                                }
                            }
                            return false
                        }
                    }
                    
                    val bannerHtml = """
                        <div style="width:100%; display: flex; justify-content: center;">
                            <script src="https://ads-partners.coupang.com/g.js"></script>
                            <script>
                                new PartnersCoupang.G({
                                    "id": $partnersId,
                                    "width": "100%",
                                    "height": "100"
                                });
                            </script>
                        </div>
                    """.trimIndent()
                    
                    loadDataWithBaseURL("https://ads-partners.coupang.com", bannerHtml, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = CardConfig.translate("coupang_disclosure"),
            fontSize = 10.sp,
            lineHeight = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
