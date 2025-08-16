package com.jeju.evtravel.ui.map

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.jeju.evtravel.R
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.ui.detail.PlaceDetailScreen
import com.jeju.evtravel.ui.search.ClickableSearchBar
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import com.kakao.vectormap.label.LabelTextStyle
import kotlinx.coroutines.launch

/**
 * 카카오 맵을 표시하는 컴포저블.
 *
 * 사용자에게 카카오 맵을 표시하고, 현재 위치를 표시하는 마커를 추가할 수 있습니다.
 * 카카오 맵을 표시하려면 [FusedLocationProviderClient]를 전달하여야 합니다.
 *
 * @param fusedLocationClient FusedLocationProviderClient instance
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KakaoMapScreen(
    fusedLocationClient: FusedLocationProviderClient,
    viewModel: MapViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val permissionState = rememberLocationPermissionState()
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedLabel by remember { mutableStateOf<Label?>(null) } // 선택된 마커 저장
    var placeList by remember { mutableStateOf<List<Place>>(emptyList()) }  // 주변 장소 목록

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )

    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    /** 권한 요청 거부 시 메시지 출력 */
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

    /** 현재 위치 정보 가져오기 + 주변 검색 */
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
                        radius = 2000
                    )
                }
            }
        }
    }

    /** 마커 클릭 리스너 */
    LaunchedEffect(kakaoMap) {
        kakaoMap?.setOnLabelClickListener { _, _, label ->
            val id = label.tag as? String
            Log.d("MapClick", "Clicked label with tag: $id")
            val place = placeList.find { it.id == id }
            Log.d("MapClick", "Matched place: ${place?.name}")
            Log.d("MapClick", "tag=$id, hasPlaces=${placeList.isNotEmpty()}")

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

                // 지도 중심 이동 및 상태 저장
                val latLng = LatLng.from(it.latitude, it.longitude)
                val zoomLevel = kakaoMap?.zoomLevel
                Log.d("zoomLevel", "zoomLevel: $zoomLevel")
                viewModel.lastCenter = latLng
                viewModel.lastZoomLevel = zoomLevel
                viewModel.lastSelectedPlaceId = it.id
                viewModel.isMapRestored = false

                val cameraUpdate = CameraUpdateFactory.newCenterPosition(latLng, zoomLevel ?: 16)
                val cameraAnimation = CameraAnimation.from(200) // 애니메이션 설정 : 단위 ms
                kakaoMap?.moveCamera(cameraUpdate, cameraAnimation)

                selectedLabel = label

                viewModel.selectPlace(it)
            }
            true
        }
    }

    // 검색된 장소 마커 추가 및 지도 상태 복원 처리
    LaunchedEffect(uiState) {
        Log.d("UIState", "state=$uiState, markers=${placeList.size}")
        if (uiState is MapUiState.Success && kakaoMap != null) {
            kakaoMap?.labelManager?.layer?.removeAll()
            val places = (uiState as MapUiState.Success).places
            placeList = places
            kakaoMap?.addMarkers(currentLatLng, places)

            if (!viewModel.isMapRestored) {
                // 카메라 복원
                viewModel.lastCenter?.let { center ->
                    viewModel.lastZoomLevel?.let { zoom ->
                        kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(center, zoom))
                    }
                }
                // 라벨 스타일 복원
                val placeId = viewModel.lastSelectedPlaceId
                val place = places.find { it.id == placeId }
                place?.let {
                    val label = kakaoMap?.labelManager?.layer?.getLabel(it.id)
                    val textStyle = LabelTextStyle.from(40, 0xFF000000.toInt())
                    val selectedStyle = LabelStyle.from(R.drawable.blue_marker_selected)
                        .setTextStyles(textStyle)
                    label?.changeStylesAndText(LabelStyles.from(selectedStyle), LabelTextBuilder().setTexts(it.name))
                    selectedLabel = label
                }
                viewModel.isMapRestored = true
            }
        }
    }

    // 카메라 이동 종료 후 '현재 위치로 재검색' 버튼 활성
    LaunchedEffect(kakaoMap) {
        kakaoMap?.let { map ->
            kakaoMap?.setOnCameraMoveEndListener { _, cameraPos, _ ->
                viewModel.lastCenter = cameraPos.position
                viewModel.isMapRestored = false
            }
        }
    }

    // 하단 시트 열기
    LaunchedEffect(selectedPlace) {
        Log.d("KakaoMap_selectedPlace_Sheet", "selectedPlace=${selectedPlace?.id} | ${selectedPlace?.name}")
        if (selectedPlace != null) {
            Log.d("KakaoMap_selectedPlace_Sheet", "show() 호출")
            sheetState.partialExpand()
        } else {
            sheetState.hide()
        }
    }

    BackHandler(enabled = sheetState.currentValue != SheetValue.Hidden) {
        coroutineScope.launch {
            when (sheetState.currentValue) {
                SheetValue.Expanded -> sheetState.partialExpand()
                SheetValue.PartiallyExpanded -> {
                    sheetState.hide()
                    viewModel.clearSelection()
                }
                else -> viewModel.clearSelection()
            }
        }
    }

    // 기본 화면 구성 + 하단 시트 구성
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetPeekHeight = if (selectedPlace != null) 300.dp else 0.dp,
        sheetSwipeEnabled = selectedPlace != null,
        sheetContent = {
            if (selectedPlace != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        PlaceDetailScreen(
                            place = selectedPlace!!,
                            isFullScreen = false,
                            onNavigateClick = {

                            }
                        )
                    }
                    Spacer(Modifier.padding(top = 8.dp))
                }
            } else {
                // 선택이 없으면 비워두거나 간단한 플레이스홀더
                Box(Modifier.fillMaxWidth())
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding()
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


            if (!viewModel.isMapRestored) {
                FloatingActionButton(
                    onClick = { viewModel.searchAroundCenter() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "현재 지도 위치로 재검색")
                }
            }
        }
    }
}