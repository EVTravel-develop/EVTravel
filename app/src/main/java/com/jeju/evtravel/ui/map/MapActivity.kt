package com.jeju.evtravel.ui.map
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.jeju.evtravel.BuildConfig
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.utils.MapUtils

class MapActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        // 카카오 키 해시 출력
        Log.d("KeyHash", MapUtils.getHashKey(this))
        // Kakao SDK 초기화
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        super.onCreate(savedInstanceState)

        // 현재 위치 정보를 가져 오기 위한 fusedLocationClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KakaoMapScreen(fusedLocationClient)
                }
            }
        }
    }
}
