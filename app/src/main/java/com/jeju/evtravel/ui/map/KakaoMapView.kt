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

    val mapViewContainer = remember {
        FrameLayout(context).apply {
            id = ViewGroup.generateViewId()
        }
    }
    val mapView = remember {
        MapView(context)
    }

    DisposableEffect(lifecycleOwner) {
        val mapLifeCycleCallback = object : MapLifeCycleCallback() {
            override fun onMapDestroy() {}
            override fun onMapError(error: Exception?) {
                error?.printStackTrace()
            }
        }
        val mapReadyCallback = object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                onMapReady(kakaoMap)
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    (mapView.parent as? ViewGroup)?.removeView(mapView)
                    mapViewContainer.addView(mapView)
                    mapView.start(mapLifeCycleCallback, mapReadyCallback)
                }
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

    AndroidView(
        factory = {
            mapViewContainer
        },
        modifier = modifier.fillMaxSize()
    )
}