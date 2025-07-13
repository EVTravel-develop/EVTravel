package com.jeju.evtravel.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.jeju.evtravel.BuildConfig
import com.jeju.evtravel.auth.AuthViewModel
import com.jeju.evtravel.ui.SplashScreen
import androidx.compose.runtime.collectAsState
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.utils.MapUtils
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    // AuthViewModel 인스턴스 초기화
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kakao Map SDK 초기화
        Log.d("KeyHash", MapUtils.getHashKey(this))
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        // 스플래시 화면 표시
        setContent {
            // 로그인 상태를 확인하여 로그인/비로그인 상태에 따라 다른 화면 표시
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = null)

            LaunchedEffect(isLoggedIn) {
                // 로그인 상태일 때 MapScreen으로 이동
                if (isLoggedIn == true) {
                    startMapScreen()
                }
                // 비로그인 상태일 때 LoginScreen으로 이동
                else {
                    startLoginScreen()
                }
                // 액티비티 종료
                finish()
            }

            // 스플래시 화면 표시
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    SplashScreen()
                }
            }
        }

        // 스플래시 화면에서 로그인 상태 확인 시작
        authViewModel.checkLoginStatus(this@MainActivity)
    }

    // MapScreen으로 이동하는 함수
    private fun startMapScreen() {
        startActivity(Intent(this, MapActivity::class.java).apply {
            // 예시로 hard-coded한 좌표와 줌 레벨 값
            putExtra("locationX", 126.570667)
            putExtra("locationY", 33.450701)
            putExtra("zoomLevel", 15)
        })
    }

    // LoginScreen으로 이동하는 함수
    private fun startLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}

