package com.jeju.evtravel.ui.map

import android.Manifest
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberLocationPermissionState(): MultiplePermissionsState {
    return rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandlePermissionRequest(
    permissionState: MultiplePermissionsState,
    onPermissionDenied: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted && !permissionState.shouldShowRationale) {
            Log.d("PermissionDebug", "권한 요청 실행")
            permissionState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(permissionState.allPermissionsGranted, permissionState.shouldShowRationale) {
        if (!permissionState.allPermissionsGranted && permissionState.shouldShowRationale) {
            Log.d("PermissionDebug", "권한 거부됨 감지 → 콜백 호출")
            onPermissionDenied()
        }
    }
}