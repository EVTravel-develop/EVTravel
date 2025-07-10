package com.jeju.evtravel.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.kakao.vectormap.MapView
import androidx.compose.ui.Modifier
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
//import com.kakao.vectormap.label.LabelLayer

@Composable
fun KakaoMapScreen(
    modifier: Modifier = Modifier,
    locationX: Double, // 서버에서 제공하는 X 값 (경도)
    locationY: Double, // 서버에서 제공하는 Y 값 (위도)
    zoomLevel: Int
) {
    val context = LocalContext.current
    // Compose의 생명 주기를 가지는 LifecycleOwner
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {    // KakaoMapView를 기억하여 재사용할 수 있도록 설정
        MapView(context).apply {
            // MapView 초기화 설정이 필요하다면 여기에 추가
        }
    }

    // 라이프사이클 관리
    DisposableEffect(Unit) {
        onDispose {
//            mapView.onPause()
//            mapView.onDestroy()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(), // AndroidView의 높이 임의 설정
        factory = { context ->
            mapView.apply {
                mapView.start(
                    object : MapLifeCycleCallback() {
                        // 지도 생명 주기 콜백: 지도가 파괴될 때 호출
                        override fun onMapDestroy() {
                            Log.d("KAKAO_MAP", "Map destroyed")
                        }

                        // 지도 생명 주기 콜백: 지도 로딩 중 에러가 발생했을 때 호출
                        override fun onMapError(exception: Exception?) {
                            Log.e("KAKAO_MAP", "지도 로딩 오류 발생: ${exception?.message}", exception)
                        }
                    },
                    object : KakaoMapReadyCallback() {
                        // KakaoMap이 준비되었을 때 호출
                        override fun onMapReady(kakaoMap: KakaoMap) {
                            try {
                                val position = LatLng.from(locationY, locationX)
                                val cameraUpdate = CameraUpdateFactory.newCenterPosition(position, zoomLevel)

                                // 라벨 추가
                                val style = kakaoMap.labelManager?.addLabelStyles(
                                    LabelStyles.from(LabelStyle.from())
                                )
                                val options = LabelOptions.from(position).setStyles(style)
                                val layer = kakaoMap.labelManager?.layer
                                layer?.addLabel(options)

                                // 카메라 이동 (한 번만 호출)
                                kakaoMap.moveCamera(cameraUpdate)

                                // 위치 기반 서비스 활성화
//                                kakaoMap.locationTrackingMode = LocationTrackingMode.Face
//                                kakaoMap.isCurrentLocationEnabled = true

                            } catch (e: Exception) {
                                Log.e("KAKAO_MAP", "지도 설정 오류: ${e.message}", e)
                            }
                        }

                        override fun getPosition(): LatLng {
                            // 현재 위치를 반환
                            return LatLng.from(locationY, locationX)
                        }
                    },
                )
            }
        },
    )
}