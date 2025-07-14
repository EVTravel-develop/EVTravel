package com.jeju.evtravel.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.jeju.evtravel.R
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun KakaoMapScreen(fusedLocationClient: FusedLocationProviderClient) {
    val context = LocalContext.current
    var kakaoMap: KakaoMap? by remember { mutableStateOf(null) }
    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }
    val zoomLevel = 15 // 기본 줌 레벨 설정

    // 위치 권한 요청
    val permissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 권한 요청 트리거를 위한 LaunchedEffect
    // 컴포저블이 화면에 나타날 때 한 번만 권한 요청 다이얼로그를 띄우기 위함
    LaunchedEffect(Unit) { // Unit 키를 사용하여 최초 한 번만 실행
        // 권한이 모두 허용되지 않았고, 이전에 '다시 묻지 않음'을 선택하지 않은 경우
        if (!permissionState.allPermissionsGranted && !permissionState.shouldShowRationale) {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    // 위치 업데이트를 위한 LaunchedEffect
    // 위치 데이터 업데이트 및 맵 이동/마커 표시를 위한 LaunchedEffect
    // LaunchedEffect는 컴포저블이 화면에 나타날 때 실행
    LaunchedEffect(permissionState.allPermissionsGranted, kakaoMap) {
        if (permissionState.allPermissionsGranted && kakaoMap != null) {
            // 권한이 모두 부여되면 위치 가져오기 시도
            getCurrentLocation(context, fusedLocationClient) { latLng ->
                currentLatLng = latLng
                kakaoMap?.let { map ->
                    latLng?.let {
                        // 현재 위치로 맵 이동 및 마커 표시
                        map.moveCamera(CameraUpdateFactory.newCenterPosition(it, zoomLevel))

                        // 기존 마커 제거 및 새 마커 추가
                        map.labelManager?.let { labelManager ->
                            labelManager.layer?.removeAll() // 기존 마커 제거
                            val labelStyle = LabelStyle.from(R.drawable.current_location) // Custom marker icon (res/drawable에 추가)
                            val labelOptions = LabelOptions.from(it)
                                .setStyles(*arrayOf(labelStyle))
//                            val label = labelManager.layer?.addLabel(labelOptions)
//                            label?.setTag("current_location")
                            labelManager.layer?.addLabel(labelOptions)
                        }
                    }
                }
            }
        }
        else if (!permissionState.shouldShowRationale) {
            // 권한이 부여되지 않았고, 'shouldShowRationale'이 false인 경우
            // (즉, 처음 요청하거나 "다시 묻지 않음"을 선택하지 않은 경우)
            // 바로 시스템 권한 요청 다이얼로그 띄우기
            // 이 로직은 LaunchedEffect 내부에서만 실행
            permissionState.launchMultiplePermissionRequest()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 카카오 맵 이름 표시
        Text(text = "카카오 맵", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        if (permissionState.allPermissionsGranted) {
            // 권한이 부여된 경우 MapView 표시
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        start(
                            object : MapLifeCycleCallback() {
                                override fun onMapDestroy() {
                                    // 맵 파괴 시 처리
                                }

                                // 지도 생명 주기 콜백: 지도 로딩 중 에러가 발생했을 때 호출
                                override fun onMapError(exception: Exception?) {
                                    // 맵 초기화 완료 시 처리
                                    // 이 시점에 KakaoMap 객체를 얻을 수 있으며, 필요시 초기 설정 가능
                                    exception?.printStackTrace() // 에러 로그 출력
                                }
                            },
                            object : KakaoMapReadyCallback() {
                                override fun onMapReady(map: KakaoMap) {
                                    kakaoMap = map
                                    // 맵이 준비되면 현재 위치로 이동 및 마커 표시
                                    if (currentLatLng == null) {
                                        map.moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(37.5665, 126.9780), zoomLevel)) // 서울 시청 등 기본 좌표, 줌 레벨 15
                                    }
                                }
                            })
                    }
                }
            )
        } else {
            // 권한을 거부했지만, 다시 설명할 필요가 있는 경우 (rationale)
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        start(
                            object : MapLifeCycleCallback() {
                                override fun onMapDestroy() {
                                    // 맵 파괴 시 처리
                                }

                                // 지도 생명 주기 콜백: 지도 로딩 중 에러가 발생했을 때 호출
                                override fun onMapError(exception: Exception?) {
                                    // 맵 초기화 완료 시 처리
                                    // 이 시점에 KakaoMap 객체를 얻을 수 있으며, 필요시 초기 설정 가능
                                    exception?.printStackTrace() // 에러 로그 출력
                                }
                            },
                            object : KakaoMapReadyCallback() {
                                override fun onMapReady(map: KakaoMap) {
                                    kakaoMap = map
                                    // 맵이 준비되면 현재 위치로 이동 및 마커 표시
                                    if (currentLatLng == null) { // 권한이 없어 currentLatLng가 없다면 기본 위치로 이동
                                        map.moveCamera(
                                            CameraUpdateFactory.newCenterPosition(
                                                LatLng.from(
                                                    37.5665,
                                                    126.9780
                                                )
                                            )
                                        ) // 서울 시청 등 기본 좌표
                                    }
                                }
                            }
                        )
                    }
                }
            )

        // 현재 위치로 이동 버튼
        // 맵 위에 오버레이되도록 BoxScope 사용
        FloatingActionButton(
            onClick = {
                kakaoMap?.let { map ->
                    currentLatLng?.let { latLng ->
                        // 현재 위치 정보가 있을 때만 이동
                        map.moveCamera(CameraUpdateFactory.newCenterPosition(latLng, zoomLevel)) // 줌 레벨 15로 이동
                        // 마커도 다시 표시
                        map.labelManager?.let { labelManager ->
                            labelManager.layer?.removeAll() // 기존 마커 제거
                            val labelStyle = LabelStyle.from(R.drawable.current_location)
                            val labelOptions = LabelOptions.from(latLng)
                                .setStyles(*arrayOf(labelStyle))
                            labelManager.layer?.addLabel(labelOptions)
                        }
                    } ?: run {
                        // 현재 위치 정보를 가져오지 못했을 때 (예: 권한 없음),
                        // 사용자에게 권한 요청을 유도하는 메시지 등을 띄울 수 있음.
                        // 여기서는 간단히 Logcat에 출력 (실제 앱에서는 Snackbar 등 사용)
                        println("현재 위치 정보를 가져올 수 없습니다. 권한을 확인해주세요.")
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.End) // 우측 하단에 배치
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = "현재 위치") // Icons.Filled.LocationOn 임포트 필요
        }

        // 권한 상태에 따른 UI 메시지 표시 (맵 위에 오버레이되거나 맵 아래에 표시)
        if (!permissionState.allPermissionsGranted) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.CenterHorizontally) // 맵 상단에 오버레이
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)) // 배경색 추가
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (permissionState.shouldShowRationale) {
                    Text("현재 위치를 지도에 표시하기 위해 '정확한 위치' 사용 권한이 필요합니다.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                        Text("권한 다시 요청")
                    }
                } else {
                    Text(
                        "지도에 현재 위치를 표시하려면 설정에서 '정확한 위치' 권한을 허용해주세요.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    }) {
                        Text("설정으로 이동")
                    }
                }
                }
            }
        }
    }
}

// 현재 위치를 가져오는 함수
private fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationResult: (LatLng?) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // 권한이 없는 경우, 이 함수는 호출 X
        // 하지만 혹시 모를 상황을 위해 로그를 남기거나 예외 처리
        onLocationResult(null)
        return
    }
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationResult(LatLng.from(location.latitude, location.longitude))
            } else {
                // 마지막으로 알려진 위치가 없는 경우 (GPS가 꺼져있거나 새로 시작한 경우)
                // 추가적인 위치 업데이트 요청 로직이 필요
                // 여기서는 간단히 null을 반환
                onLocationResult(null)
            }
        }
        .addOnFailureListener { e ->
            // 위치 가져오기 실패
            e.printStackTrace()
            onLocationResult(null)
        }
}