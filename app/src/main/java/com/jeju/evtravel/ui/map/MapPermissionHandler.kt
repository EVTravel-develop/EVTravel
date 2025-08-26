package com.jeju.evtravel.ui.map

import android.Manifest
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var hasRequested by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted && !permissionState.shouldShowRationale) {
            Log.d("PermissionDebug", "권한 요청 실행")
            permissionState.launchMultiplePermissionRequest()
            hasRequested = true
        }
    }

    LaunchedEffect(permissionState.allPermissionsGranted, permissionState.shouldShowRationale) {
        if (!permissionState.allPermissionsGranted && permissionState.shouldShowRationale) {
            Log.d("PermissionDebug", "권한 거부됨 감지 → 콜백 호출")
            onPermissionDenied()
        }
    }

    // 영구 거부(다시 묻지 않음) 감지 후 콜백
    LaunchedEffect(permissionState.allPermissionsGranted, hasRequested) {
        if (hasRequested
            && !permissionState.allPermissionsGranted
            && !permissionState.shouldShowRationale
        ) {
            Log.d("PermissionDebug", "영구 거부 감지 → 콜백 호출")
            onPermissionDenied()
        }
    }
}