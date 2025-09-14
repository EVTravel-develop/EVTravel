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

val navigationApps = listOf(
    // 카카오 맵
    NavigationApp(
        name = "카카오 맵",
        packageName = "net.daum.android.map",
        scheme = { start, end, dname ->
            val startParam = start?.let { "sp=${it.latitude},${it.longitude}&" } ?: ""
            // dname이 아닌 epn을 사용합니다.
            "kakaomap://route?${startParam}ep=${end.latitude},${end.longitude}&by=foot"
        }
    ),
    // 네이버 지도
    NavigationApp(
        name = "네이버 지도",
        packageName = "com.nhn.android.nmap",
        scheme = { start, end, dname ->
            val startParam = start?.let { "slat=${it.latitude}&slng=${it.longitude}&" } ?: ""
            // dname 파라미터 추가
            "nmap://route/walk?${startParam}dlat=${end.latitude}&dlng=${end.longitude}&dname=${dname}&appname=${applicationId}"
        }
    ),
    // 티맵 (T-Map)
    NavigationApp(
        name = "T맵",
        packageName = "com.skt.tmap.ku",
        scheme = { start, end, dname ->
            val startParam = start?.let { "startx=${it.longitude}&starty=${it.latitude}&" } ?: ""
            // T맵은 goalname을 사용합니다.
            "tmap://route?${startParam}goalx=${end.longitude}&goaly=${end.latitude}&reqCoordType=WGS84&resCoordType=WGS84"
        }
    )
)

// launchNavigationApp 함수
fun launchNavigationApp(context: Context, app: NavigationApp, start: LatLng?, end: LatLng, dname: String) {
    try {
        val schemeUri = Uri.parse(app.scheme(start, end, dname))
        Log.d("NaviLog", "생성된 URL: $schemeUri")

        val intent = Intent(Intent.ACTION_VIEW, schemeUri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("NaviLog", "앱 실행 실패: ${app.name}", e)
        Toast.makeText(context, "${app.name}을(를) 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
    }
}