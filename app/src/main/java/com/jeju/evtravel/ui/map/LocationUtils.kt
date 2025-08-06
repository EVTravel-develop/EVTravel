package com.jeju.evtravel.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.kakao.vectormap.LatLng

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