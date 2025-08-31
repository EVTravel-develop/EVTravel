package com.jeju.evtravel


import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EvTravelApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val cr = FirebaseCrashlytics.getInstance()

        val isProd = BuildConfig.FLAVOR == "prod"
        val kakaoNative = BuildConfig.KAKAO_NATIVE_APP_KEY
        val kakaoRest   = BuildConfig.KAKAO_REST_API_KEY

// 비밀번호/키 전체는 절대 로그 금지 → 마지막 4자리만
        cr.setCustomKey("flavor", BuildConfig.FLAVOR)
        cr.setCustomKey("buildType", BuildConfig.BUILD_TYPE)
        cr.setCustomKey("kakao_native_last4", kakaoNative.takeLast(4))
        cr.setCustomKey("kakao_rest_last4", kakaoRest.takeLast(4))
        cr.setCustomKey("kakao_native_blank", kakaoNative.isBlank())
        cr.setCustomKey("kakao_rest_blank", kakaoRest.isBlank())

        if (isProd && (kakaoNative.isBlank() || kakaoRest.isBlank())) {
            cr.recordException(
                IllegalStateException("config_missing: nativeBlank=${kakaoNative.isBlank()} restBlank=${kakaoRest.isBlank()}")
            )
        }

        // ✅ 카카오 로그인 SDK 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        // ✅ 카카오 맵 SDK 초기화
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}