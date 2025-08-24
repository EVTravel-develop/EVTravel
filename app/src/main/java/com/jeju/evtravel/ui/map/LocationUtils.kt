package com.jeju.evtravel.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.kakao.vectormap.LatLng

/**
 * 현재 위치를 가져옵니다.
 *
 * @param context 앱 컨텍스트
 * @param fusedLocationClient 위치 클라이언트
 * @param onLocationResult 위치 결과를 받는 콜백
 *
 * @see FusedLocationProviderClient
 */
fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationResult: (LatLng?) -> Unit
) {
    if (
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {
        onLocationResult(null)
        return
    }

    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationResult(LatLng.from(location.latitude, location.longitude))
            } else {
                onLocationResult(null)
            }
        }
        .addOnFailureListener {
            it.printStackTrace()
            onLocationResult(null)
        }
}