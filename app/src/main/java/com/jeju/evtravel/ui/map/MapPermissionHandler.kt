package com.jeju.evtravel.ui.map

import android.Manifest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
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
fun HandlePermissionRequest(permissionState: MultiplePermissionsState) {
    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted && !permissionState.shouldShowRationale) {
            permissionState.launchMultiplePermissionRequest()
        }
    }
}

@Composable
fun PermissionRationaleDialog(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = {
            focusManager.clearFocus()
            onDismiss()
        },
        title = { Text("위치 권한 필요") },
        text = { Text("현재 위치를 지도에 표시하려면 위치 권한이 필요합니다.", modifier = modifier) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("권한 요청")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionSettingsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("권한이 거부됨") },
        text = { Text("지도에서 현재 위치를 사용하려면 앱 설정에서 위치 권한을 허용해주세요.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("설정 열기")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}