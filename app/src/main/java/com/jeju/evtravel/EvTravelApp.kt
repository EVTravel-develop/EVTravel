package com.jeju.evtravel


import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class EvTravelApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.tag("KAKAO_LOCAL").i(
            "REST key len=${BuildConfig.KAKAO_REST_API_KEY.length}, " +
                    "suffix=${BuildConfig.KAKAO_REST_API_KEY.takeLast(6)}"
        )

        // ✅ 카카오 로그인 SDK 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        // ✅ 카카오 맵 SDK 초기화
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}