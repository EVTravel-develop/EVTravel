package com.jeju.evtravel.ui.map

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng

/**
 * 카카오 맵을 표시하는 컴포저블.
 *
 * 사용자에게 카카오 맵을 표시하고, 현재 위치를 표시하는 마커를 추가할 수 있습니다.
 * 카카오 맵을 표시하려면 [FusedLocationProviderClient]를 전달하여야 합니다.
 *
 * @param fusedLocationClient FusedLocationProviderClient instance
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun KakaoMapScreen(fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current
    val permissionState = rememberLocationPermissionState()
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }

    // 권한이 필요할 때 다이얼로그 표시
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // 권한 상태에 따라 다이얼로그 표시
    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted) {
            if (permissionState.shouldShowRationale) {
                showRationaleDialog = true
            } else {
                showSettingsDialog = true
            }
        }
    }

    // 위치 정보 가져오기
    LaunchedEffect(permissionState.allPermissionsGranted, kakaoMap) {
        if (permissionState.allPermissionsGranted && kakaoMap != null) {
            getCurrentLocation(context, fusedLocationClient) {
                currentLatLng = it
                kakaoMap?.moveToLocationWithMarker(it)
            }
        }
    }

    // 화면 구성
    Column(Modifier.fillMaxSize()) {
        Text("카카오 맵", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        KakaoMapView(onMapReady = { kakaoMap = it })
    }

    // 위치 권한 알림 다이얼로그
    if (showRationaleDialog) {
        PermissionRationaleDialog(
            onConfirm = {
                permissionState.launchMultiplePermissionRequest()
                showRationaleDialog = false
            },
            onDismiss = { showRationaleDialog = false }
        )
    }

    // 권한 설정 이동 다이얼로그
    if (showSettingsDialog) {
        PermissionSettingsDialog(
            onConfirm = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                )
                showSettingsDialog = false
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}