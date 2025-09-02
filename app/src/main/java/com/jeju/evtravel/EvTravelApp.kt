package com.jeju.evtravel


import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class EvTravelApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }

        Timber.tag("KAKAO_LOCAL").w(
            "REST key len=${BuildConfig.KAKAO_REST_API_KEY.length}, " +
                    "suffix=${BuildConfig.KAKAO_REST_API_KEY.takeLast(6)}"
        )

        // ✅ 카카오 로그인 SDK 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        // ✅ 카카오 맵 SDK 초기화
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}

class ReleaseTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        // 기본은 W/E만 허용
        if (priority >= Log.WARN) return true
        // KAKAO_LOCAL 태그는 INFO도 일시 허용(원하면 지워도 됨)
        if (tag == "KAKAO_LOCAL" && priority >= Log.INFO) return true
        return false
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        Log.println(priority, tag ?: "APP", message)
        t?.let { Log.println(priority, tag ?: "APP", Log.getStackTraceString(it)) }
    }
}