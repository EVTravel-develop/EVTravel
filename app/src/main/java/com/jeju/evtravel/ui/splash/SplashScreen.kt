// com/jeju/evtravel/ui/splash/SplashScreen.kt
package com.jeju.evtravel.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jeju.evtravel.R
import com.jeju.evtravel.service.auth.FirebaseAuthService
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(1500) // 살짝 대기(로고 표시 용)
        if (FirebaseAuthService.isLoggedIn()) {
            // 로그인 O → 지도 화면
            navController.navigate("map") {
                popUpTo("SplashScreen") { inclusive = true }
            }
        } else {
            // 로그인 X → 온보딩
            navController.navigate("onboarding") {
                popUpTo("SplashScreen") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = "Splash Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(120.dp)
        )
    }
}
