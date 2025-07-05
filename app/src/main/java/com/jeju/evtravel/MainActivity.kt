package com.jeju.evtravel

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jeju.evtravel.ui.KakaoMapScreen
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.utils.MapUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("KeyHash", MapUtils.getHashKey(this))
        // Kakao SDK 초기화
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KakaoMapScreen(
                        modifier = Modifier.fillMaxSize(),
                        locationX = 126.570667, // 예시 경도
                        locationY = 33.450701  // 예시 위도
                    )
                }
            }
        }
    }
}

