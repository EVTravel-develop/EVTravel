package com.jeju.evtravel.ui.map

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.jeju.evtravel.R
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.ui.search.ClickableSearchBar
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import com.kakao.vectormap.label.LabelTextStyle

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
fun KakaoMapScreen(
    fusedLocationClient: FusedLocationProviderClient,
    viewModel: MapViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val permissionState = rememberLocationPermissionState()
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }

    var selectedPlace by remember { mutableStateOf<Place?>(null) }      // 선택된 장소
    var selectedLabel by remember { mutableStateOf<Label?>(null) } // 선택된 마커 저장
    var placeList by remember { mutableStateOf<List<Place>>(emptyList()) }  // 주변 장소 목록

    val uiState by viewModel.uiState.collectAsState()

    HandlePermissionRequest(
        permissionState = permissionState,
        onPermissionDenied = {
            Toast.makeText(
                context,
                "'정확한 위치' 사용 권한을 허용해주세요.",
                Toast.LENGTH_SHORT
            ).show()
        }
    )

    // 현재 위치 정보 가져오기 + 주변 검색
    LaunchedEffect(permissionState.allPermissionsGranted, kakaoMap) {
        if (permissionState.allPermissionsGranted && kakaoMap != null) {
            getCurrentLocation(context, fusedLocationClient) { location: LatLng? ->
                location?.let { loc ->
                    currentLatLng = loc
                    viewModel.lastUserLocation = loc
                    viewModel.lastCenter = loc

                    viewModel.searchNearby(
                        query = "전기차 충전소",
                        longitude = loc.longitude,
                        latitude = loc.latitude,
                        radius = 500
                    )
                }
            }
        }
    }

    // 카메라 이동 종료 시 중심 저장
    LaunchedEffect(kakaoMap) {
        kakaoMap?.let { map ->
            map.setOnCameraMoveEndListener { _, cameraPosition, _ ->
                viewModel.lastCenter = cameraPosition.position
            }
        }
    }

    // 마커 클릭 리스너
    LaunchedEffect(kakaoMap) {
        kakaoMap?.setOnLabelClickListener { _, _, label ->
            val id = label.tag as? String
            Log.d("MapClick", "Clicked label with tag: $id")
            val place = placeList.find { it.id == id }
            Log.d("MapClick", "Matched place: ${place?.name}")

            // 마커 중복 처리 방지
            if (label == selectedLabel) {
                return@setOnLabelClickListener true
            }

            // 이전 선택된 라벨 스타일/텍스트 초기화
            selectedLabel?.let { prevLabel ->
                val defaultTextStyle = LabelTextStyle.from(40, 0xFF000000.toInt())  // 기본 텍스트 스타일
                val defaultStyle = LabelStyle.from(R.drawable.blue_marker)
                    .setTextStyles(defaultTextStyle)
                val defaultStyles = LabelStyles.from(defaultStyle)
                val emptyText = LabelTextBuilder().setTexts("")

                try {
                    prevLabel.changeStylesAndText(defaultStyles, emptyText)
                } catch (e: Exception) {
                    Log.w("MapClick", "기존 라벨 복원 중 오류 발생: ${e.message}")
                }
            }

            // 현재 클릭된 라벨 스타일/텍스트 변경
            place?.let {
                val textStyle = LabelTextStyle.from(40, 0xFF000000.toInt())
                val selectedStyle = LabelStyle.from(R.drawable.blue_marker_selected)
                    .setTextStyles(textStyle)
                val selectedStyles = LabelStyles.from(selectedStyle)
                val textBuilder = LabelTextBuilder().setTexts(it.name)

                label.changeStylesAndText(selectedStyles, textBuilder)

                // 지도 중심 이동
                val latLng = LatLng.from(it.latitude, it.longitude)

                val zoomLevel = kakaoMap?.zoomLevel
                Log.d("zoomLevel", "zoomLevel: $zoomLevel")

                // 지도 상태 저장
                viewModel.lastCenter = latLng
                viewModel.lastZoomLevel = zoomLevel
                viewModel.lastSelectedPlaceId = it.id
                viewModel.isMapRestored = false

                val cameraUpdate = CameraUpdateFactory.newCenterPosition(latLng, zoomLevel ?: 16)
                // 애니메이션 설정 : 단위 ms
//                val cameraAnimation = CameraAnimation.from(200)
                kakaoMap?.moveCamera(cameraUpdate)

                selectedLabel = label
                selectedPlace = it

                navController.navigate("place_detail/${it.id}")
            }
            true
        }
    }

    // 검색된 장소 마커 추가 및 지도 상태 복원 처리
    LaunchedEffect(uiState) {
        if (uiState is MapUiState.Success && kakaoMap != null) {
            kakaoMap?.labelManager?.layer?.removeAll()
            val places = (uiState as MapUiState.Success).places
            placeList = places
            kakaoMap?.addMarkers(currentLatLng, places)

            if (!viewModel.isMapRestored) {
                viewModel.lastCenter?.let { center ->
                    viewModel.lastZoomLevel?.let { zoom ->
                        kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(center, zoom))
                    }
                }

                val placeId = viewModel.lastSelectedPlaceId
                val place = places.find { it.id == placeId }
                place?.let {
                    val label = kakaoMap?.labelManager?.layer?.getLabel(it.id)
                    val textStyle = LabelTextStyle.from(40, 0xFF000000.toInt())
                    val selectedStyle = LabelStyle.from(R.drawable.blue_marker_selected)
                        .setTextStyles(textStyle)
                    label?.changeStylesAndText(LabelStyles.from(selectedStyle), LabelTextBuilder().setTexts(it.name))
                    selectedLabel = label
                    selectedPlace = it
                }

                viewModel.isMapRestored = true
            }
        }
    }

    // 카메라 이동 종료 후 '현재 위치로 재검색' 버튼 활성
    LaunchedEffect(kakaoMap) {
        kakaoMap?.setOnCameraMoveEndListener { _, _, _ -> viewModel.isMapRestored = false }
    }


    // 기본 화면 구성
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        KakaoMapView(onMapReady = { kakaoMap = it })

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            ClickableSearchBar(
                placeholder = "주변 장소 및 충전소를 검색해주세요.",
                onClick = { navController.navigate("search") }
            )
        }

//        FloatingActionButton(
//            onClick = { viewModel.searchAroundCenter() },
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(16.dp)
//        ) {
//            Icon(Icons.Default.Search, contentDescription = "현재 지도 위치로 재검색")
//        }
    }
}