package com.jeju.evtravel.ui.detail

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun PlaceDetailScreen(placeId: String) {
    val placeUrl = "https://place.map.kakao.com/$placeId"

    // 공통 비율
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val minHeight = (screenHeight * 0.3f).dp  // 최소 25%
    val maxHeight = (screenHeight * 0.9f).dp // 최대 90%

//    Box(
//        modifier = Modifier
//            .fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        AndroidView(
//            factory = { ctx ->
//                WebView(ctx).apply {
//                    settings.javaScriptEnabled = true
//                    webViewClient = WebViewClient()
//                    loadUrl(placeUrl)
//                }
//            }
//        )
//    }
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight, max = maxHeight)
                .background(Color.White),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = WebViewClient()
                        loadUrl(placeUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}