package com.jeju.evtravel.activity


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jeju.evtravel.ui.LoginScreen

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onKakaoLogin = { /* TODO: Kakao 로그인 처리 */ },
                        onGuestLogin = { /* TODO: 게스트 로그인 처리 */ }
                    )
                }
            }
        }
    }
}