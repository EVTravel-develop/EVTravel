package com.jeju.evtravel.ui.map

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ✅ 1. MapView와 이를 담을 FrameLayout 컨테이너를 한 번만 생성합니다.
    val mapViewContainer = remember {
        FrameLayout(context).apply {
            // FrameLayout의 ID를 설정해야 MapView가 정상적으로 동작할 수 있습니다.
            id = ViewGroup.generateViewId()
        }
    }
    val mapView = remember {
        MapView(context)
    }

    // ✅ 2. 생명주기 이벤트를 관찰하여 MapView를 컨테이너에 추가/제거합니다.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                // 앱이 화면에 보일 때 (RESUMED) MapView를 컨테이너에 추가합니다.
                Lifecycle.Event.ON_RESUME -> {
                    // mapView가 이미 다른 부모에 속해있을 수 있으므로 먼저 제거합니다.
                    (mapView.parent as? ViewGroup)?.removeView(mapView)
                    mapViewContainer.addView(mapView)
                    mapView.start(
                        object : MapLifeCycleCallback() {
                            override fun onMapDestroy() {}
                            override fun onMapError(error: Exception?) {
                                error?.printStackTrace()
                            }
                        },
                        object : KakaoMapReadyCallback() {
                            override fun onMapReady(kakaoMap: KakaoMap) {
                                onMapReady(kakaoMap)
                            }
                        }
                    )
                }
                // 앱이 백그라운드로 갈 때 (PAUSED) MapView를 컨테이너에서 제거합니다.
                // 이렇게 하면 그래픽 리소스가 정리되고, 돌아왔을 때 새로 생성됩니다.
                Lifecycle.Event.ON_PAUSE -> {
                    mapViewContainer.removeView(mapView)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ✅ 3. AndroidView는 이제 MapView가 아닌, 그것을 담는 컨테이너(FrameLayout)를 보여줍니다.
    AndroidView(
        factory = { mapViewContainer },
        modifier = modifier.fillMaxSize()
    )
}