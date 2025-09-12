package com.jeju.evtravel.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.jeju.evtravel.BuildConfig
import com.kakao.vectormap.LatLng
import com.jeju.evtravel.domain.model.Place

data class NavigationApp(
    val name: String,
    val packageName: String,
    val scheme: (start: LatLng?, end: LatLng, dname: String) -> String
)

val applicationId = BuildConfig.APPLICATION_ID

private val navigationApps = listOf(
    // 카카오 맵
    NavigationApp(
        name = "카카오 맵",
        packageName = "net.daum.android.map",
        scheme = { start, end, dname ->
            val startParam = start?.let { "sp=${it.latitude},${it.longitude}&" } ?: ""
            // dname이 아닌 epn을 사용합니다.
            "kakaomap://route?${startParam}ep=${end.latitude},${end.longitude}&by=car"
        }
    ),
    // 네이버 지도
    NavigationApp(
        name = "네이버 지도",
        packageName = "com.nhn.android.nmap",
        scheme = { start, end, dname ->
            val startParam = start?.let { "slat=${it.latitude}&slng=${it.longitude}&" } ?: ""
            // dname 파라미터 추가
            "nmap://route?${startParam}dlat=${end.latitude}&dlng=${end.longitude}&dname=${dname}&appname=${applicationId}"
        }
    ),
    // 티맵 (T-Map)
    NavigationApp(
        name = "T맵",
        packageName = "com.skt.tmap.ku",
        scheme = { start, end, dname ->
            val startParam = start?.let { "startx=${it.longitude}&starty=${it.latitude}&" } ?: ""
            // T맵은 goalname을 사용합니다.
            "tmap://route?${startParam}goalx=${end.longitude}&goaly=${end.latitude}&reqCoordType=WGS84"
        }
    )
)

fun getAvailableNavigationApps(context: Context): List<NavigationApp> {
    val pm = context.packageManager
    return navigationApps.filter { app ->
        try {
            pm.getPackageInfo(app.packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}

// 수정된 launchNavigationApp 함수
fun launchNavigationApp(context: Context, app: NavigationApp, start: LatLng?, end: LatLng, dname: String) {
    try {
        val schemeUri = Uri.parse(app.scheme(start, end, dname))
        Log.d("NaviLog", "생성된 URL: $schemeUri")

        val intent = Intent(Intent.ACTION_VIEW, schemeUri)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)

        val packageManager = context.packageManager
        val installed = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 이상
                packageManager.getPackageInfo(app.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                // 이전 버전
                @Suppress("Deprecation")
                packageManager.getPackageInfo(app.packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

        if (installed) {
            context.startActivity(intent)
        } else {
            // 설치되어 있지 않으면 플레이 스토어로 이동
            val marketUri = Uri.parse("market://details?id=${app.packageName}")
            val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
            context.startActivity(marketIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "${app.name}을(를) 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
    }
}