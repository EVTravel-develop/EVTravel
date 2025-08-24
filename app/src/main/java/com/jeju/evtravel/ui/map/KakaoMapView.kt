package com.jeju.evtravel.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView

/**
 * KakaoMap을 렌더링하는 컴포지션 함수입니다.
 *
 * @param modifier 컴포지션 함수에 적용할 모디파이어입니다.
 * @param onMapReady KakaoMap이 준비되면 호출되는 콜백 함수입니다.
 */
@Composable
fun KakaoMapView(
    modifier: Modifier = Modifier,
    onMapReady: (KakaoMap) -> Unit
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {}
                        override fun onMapError(exception: Exception?) {
                            exception?.printStackTrace()
                        }
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(map: KakaoMap) {
                            onMapReady(map)
                        }
                    }
                )
            }
        }
    )
}