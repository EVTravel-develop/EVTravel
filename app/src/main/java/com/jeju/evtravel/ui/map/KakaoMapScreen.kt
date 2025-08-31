package com.jeju.evtravel.ui.map

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.jeju.evtravel.R
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.ui.detail.PlaceChargerDetailScreen
import com.jeju.evtravel.ui.search.ClickableSearchBar
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelLayerOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import com.kakao.vectormap.label.LabelTextStyle
import com.kakao.vectormap.GestureType
import kotlinx.coroutines.launch
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.jeju.evtravel.ui.theme.Variables
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.*

private const val REQUERY_DISTANCE_M = 250.0
val DEFAULT_CENTER = LatLng.from(33.4995, 126.5311) // 기본 좌표
private const val DEFAULT_ZOOM = 15

/**
 * 카카오 지도 화면을 구성하는 컴포저블.
 *
 * - 지도 준비(onMapReady) 시 레이어를 분리(현재 위치/장소)하고 ArrowController 를 연결한다.
 * - 현재 위치 획득 후 지도 중심 이동 및 위치 마커(한 장짜리 에셋) 회전을 유지한다.
 * - 장소 검색 결과가 갱신되면 장소 라벨을 다시 그린 후 마지막 선택된 라벨을 재강조한다.
 * - 라벨 클릭 시 선택 스타일/텍스트를 적용하고 카메라를 이동한다.
 * - 하단 시트는 선택 상태에 따라 부분확장/숨김을 제어한다.
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KakaoMapScreen(
    fusedLocationClient: FusedLocationProviderClient,
    viewModel: MapViewModel,
    navController: NavController
) {
    /** Compose / 상태 준비 */
    val context = LocalContext.current
    val permissionState = rememberLocationPermissionState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()

    var selectedLabel by remember { mutableStateOf<Label?>(null) } // 현재 선택된 장소 라벨
    var placeList by remember { mutableStateOf<List<Place>>(emptyList()) } // 현재 화면의 장소 목록
    var placesLayer by remember { mutableStateOf<LabelLayer?>(null) }      // 장소 라벨 레이어
    var locationLayer by remember { mutableStateOf<LabelLayer?>(null) }    // 현재 위치(한 장짜리 마커) 레이어
    val labelByPlaceId = remember { mutableMapOf<String, Label>() }
    var arrowController by remember { mutableStateOf<ArrowController?>(null) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    val restoredOnce = remember(kakaoMap) { mutableStateOf(false) }
    var isGestureMove by remember { mutableStateOf(false) }
    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }
    var isLabelHighlighted by remember { mutableStateOf(false) }

    val headingFlow = remember { headingFlow(context) }
    val headingDeg by headingFlow.collectAsState(initial = 0f)

    /** 상세 패널 상태 */
    val isDetailLoading by viewModel.isSelectedPlaceLoading.collectAsState()
    val detailError by viewModel.selectedPlaceError.collectAsState()

    /** 하단 시트 상태 */
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(sheetState)
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val isFullScreen = sheetState.currentValue == SheetValue.Expanded
    val expandLatchPx = with(density) { 56.dp.toPx() }
    val corner by animateDpAsState(
        targetValue = if (isFullScreen) 0.dp else 16.dp,
        label = "sheetCorner"
    )
    /** GPS 오류 상태 */
    var gpsErrorMessage by remember { mutableStateOf<String?>(null) }
    /** 재 검색 버튼 상태 */
    var showRequery by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameBackFromSettings by remember { mutableStateOf(false) }

    // 위치/GPS 설정 화면 런처
    var gpsMode by remember { mutableStateOf(false) } // GPS 버튼 눌러 위치 이동을 했는지 여부

    val openLocationSettings = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // 결과 콜백은 단순히 flag만 남기고, 실제 처리는 ON_RESUME에서
        cameBackFromSettings = true
    }

    // 앱 권한 화면(앱 상세 설정) 런처
    val openAppPermissionSettings = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        cameBackFromSettings = true
    }

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

    /** 라벨 클릭 리스너: 장소 라벨 선택/강조 + 카메라 이동 + 상태 저장 */
    LaunchedEffect(kakaoMap) {
        kakaoMap?.setOnLabelClickListener { _, _, label ->
            val id = label.tag as? String ?: return@setOnLabelClickListener true
            val place = placeList.find { it.id == id } ?: return@setOnLabelClickListener true

            // 이미 선택된 라벨이면 무시
            if (label == selectedLabel && isLabelHighlighted) {
                return@setOnLabelClickListener true
            }

            // 이전 선택된 라벨이면 재강조하고 무시
            if (label == selectedLabel && !isLabelHighlighted) {
                runCatching { label.applySelectedStyle(place.name) }
                isLabelHighlighted = true
                // sheetState.partialExpand()
                return@setOnLabelClickListener true
            }

            // 다른 라벨로 전환: 이전 라벨 -> 새 라벨 강조
            if (label != selectedLabel) {
                selectedLabel?.let { runCatching { it.applyDefaultStyle() } }
                runCatching { label.applySelectedStyle(place.name) }
                selectedLabel = label
                isLabelHighlighted = true
            }

            // 공통 상태/카메라 갱신
            val latLng = LatLng.from(place.latitude, place.longitude)
            val zoomLevel = kakaoMap?.zoomLevel
            viewModel.lastCenter = latLng
            viewModel.lastZoomLevel = zoomLevel
            viewModel.lastSelectedPlaceId = place.id
            viewModel.isMapRestored = false

            kakaoMap?.moveCamera(
                CameraUpdateFactory.newCenterPosition(latLng, zoomLevel ?: 16),
                CameraAnimation.from(200)
            )

            viewModel.selectPlace(place)
            showRequery = false

            true
        }
    }

    /** 시트 드래그 오프셋 감지: 시트가 거의 꼭대기에 오면 Expanded 로 고정 */
    LaunchedEffect(sheetState, selectedPlace) {
        snapshotFlow { runCatching { sheetState.requireOffset() }.getOrNull() }
            .collect { offsetPx ->
                if (selectedPlace != null && offsetPx != null && offsetPx <= expandLatchPx) {
                    sheetState.expand()
                }
            }
    }

    /**
     * 현재 위치 획득 & 초기 카메라 이동 & 주변 검색 & 현재 위치 마커(한 장짜리) 표시
     *
     * - 검색 화면에서 되돌아온 경우에는 자동 초기화 스킵
     * - 최초 진입 시 현재 위치로 카메라 이동, 한 장짜리 마커를 붙이고 주변 검색
     */
    LaunchedEffect(kakaoMap) {
//        val map = kakaoMap ?: return@LaunchedEffect
//
//        // 권한이 없으면 기본 위치로 이동
//        if (!permissionState.allPermissionsGranted) {
//            fallbackToDefaultCenter(viewModel, map, arrowController)
//            showRequery = false
//            return@LaunchedEffect
//        }
//
//        if (viewModel.skipAutoCenterOnce || viewModel.lastCenter != null) {
//            viewModel.skipAutoCenterOnce = false
//            viewModel.lastCenter?.let { c ->
//                viewModel.searchNearby("제주 전기차 충전소", c.longitude, c.latitude, 2000)
//                viewModel.markSearched(c, kakaoMap?.zoomLevel)
//                showRequery = false
//            }
//            return@LaunchedEffect
//        }
        val map = kakaoMap ?: return@LaunchedEffect
        // 항상 DEFAULT_CENTER로 이동 + 주변 검색, GPS 모드 아님
        fallbackToDefaultCenter(viewModel, map, arrowController)
        showRequery = false
        gpsMode = false

//        getCurrentLocation(context, fusedLocationClient) { loc ->
//            if (loc == null) {
//                // 위치 획득 실패 시에도 폴백
//                fallbackToDefaultCenter(viewModel, map, arrowController)
//                showRequery = false
//            } else {
//                viewModel.lastUserLocation = loc
//                viewModel.lastCenter = loc
//
//                val zoom = kakaoMap?.zoomLevel ?: 15
//                kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(loc, zoom))
//
//                // 현재 위치 마커(한 장짜리) 부착
//                arrowController?.attachOrMove(loc)
//
//                // 주변 검색
//                viewModel.searchNearby("제주 전기차 충전소", loc.longitude, loc.latitude, 2000)
//                viewModel.markSearched(loc, kakaoMap?.zoomLevel)
//                showRequery = false
//            }
//        }
    }

    /** 검색에서 돌아온 경우: lastCenter로 카메라 이동 + 주변 검색 */
    LaunchedEffect(kakaoMap, viewModel.lastCenter) {
        if (!gpsMode) return@LaunchedEffect

        val map = kakaoMap ?: return@LaunchedEffect
        val center = viewModel.lastCenter ?: return@LaunchedEffect

        // 맵이 새로 갱신 때마다 1회만 복원
        if (restoredOnce.value) return@LaunchedEffect

        val zoom = viewModel.lastZoomLevel ?: 16
        map.moveCamera(CameraUpdateFactory.newCenterPosition(center, zoom))

        // 선택한 지점을 기준으로 주변 검색 실행
        viewModel.searchNearby("제주 전기차 충전소", center.longitude, center.latitude, 2000)
        viewModel.markSearched(center, kakaoMap?.zoomLevel)
        showRequery = false

        restoredOnce.value = true
    }

    /** Heading(단말 나침반) 값이 변하면 즉시 현재 위치 마커 회전을 갱신 */
    LaunchedEffect(headingDeg) {
        arrowController?.onHeadingOrCameraChanged()
    }

    /** 카메라 이동/회전 종료 시에도 현재 위치 마커 회전을 보정하고 지도 상태를 저장 */
    LaunchedEffect(kakaoMap, selectedPlace) {
        // 이동 시작: 제스처 여부 기록
        kakaoMap?.setOnCameraMoveStartListener { _, gestureType ->
            // 사용자 드래그/핀치면 true, 코드/애니메이션이면 false
            isGestureMove = (gestureType != GestureType.Unknown)
        }

        // 이동 종료: 제스처로 이동한 경우에만 배너 노출 판단
        kakaoMap?.setOnCameraMoveEndListener { _, cameraPos, gestureType ->
            // 지도 상태 저장/보정
            viewModel.lastCenter = cameraPos.position
            viewModel.isMapRestored = false
            arrowController?.onHeadingOrCameraChanged()

            val isUserGesture = isGestureMove || (gestureType != GestureType.Unknown)
            if (isUserGesture) {
                val last = viewModel.lastSearchCenter
                val curr = cameraPos.position
                val movedFar = last == null || distanceMeters(last, curr) > REQUERY_DISTANCE_M
                val zoomChanged = viewModel.lastSearchZoomLevel?.let { zl ->
                    (kakaoMap?.zoomLevel ?: zl) != zl
                } ?: false

                // 상세 시트가 닫혀 있을 때만 표시
                showRequery = (selectedPlace == null) && (movedFar || zoomChanged)
            }

            // 다음 이동을 위해 초기화
            isGestureMove = false
        }
    }

    /**
     * 주변 장소 라벨 갱신 + 마지막 선택 라벨 강조 복원
     *
     * - 장소 레이어를 비우고 새로 그린 뒤, viewModel.lastSelectedPlaceId 가 있으면
     *   해당 라벨을 찾아 선택 스타일/텍스트를 다시 적용한다.
     */
    LaunchedEffect(uiState, kakaoMap, placesLayer) {
        if (uiState is MapUiState.Success && kakaoMap != null) {
            val places = (uiState as MapUiState.Success).places
            placeList = places

            // 장소 라벨 갱신
            placesLayer?.removeAll()
            labelByPlaceId.clear()

            places.forEach { place ->
                val lbl = placesLayer?.addLabel(place.toLabelOptions())
                if (lbl != null) labelByPlaceId[place.id] = lbl
            }

            // 선택 라벨 복원
            val selId = viewModel.lastSelectedPlaceId
            if (selId != null) {
                val selPlace = places.firstOrNull { it.id == selId }
                val selLabel = labelByPlaceId[selId]
                if (selPlace != null && selLabel != null) {
                    val textStyle = LabelTextStyle.from(28, 0xFF000000.toInt())
                    val selectedStyle =
                        LabelStyle.from(R.drawable.ev_marker_selected).setTextStyles(textStyle)
                    selLabel.changeStylesAndText(
                        LabelStyles.from(selectedStyle),
                        LabelTextBuilder().setTexts(selPlace.name)
                    )
                    selectedLabel = selLabel
                    isLabelHighlighted = true
                } else {
                    selectedLabel = null
                    viewModel.lastSelectedPlaceId = null
                    isLabelHighlighted = false // (선택/강조 모두 해제 상태)
                }
            } else {
                selectedLabel = null
                isLabelHighlighted = false
            }
        }
    }

    /** 현재 위치 재점검 & 초기화 */
    DisposableEffect(kakaoMap) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (cameBackFromSettings) {
                    cameBackFromSettings = false

                    val granted =
                        permissionState.allPermissionsGranted // or permissionState.status.isGranted
                    val gpsOn = isLocationEnabled(context)

//                    if (granted && gpsOn) {
//                        gpsErrorMessage = null
//
//                        // 돌아오자마자 현재 위치 재획득 + 지도/마커/검색 재초기화
//                        getCurrentLocation(context, fusedLocationClient) { loc ->
//                            if (loc != null) {
//                                viewModel.lastUserLocation = loc
//                                viewModel.lastCenter = loc
//                                viewModel.isMapRestored = false
//
//                                kakaoMap?.moveCamera(
//                                    CameraUpdateFactory.newCenterPosition(
//                                        loc,
//                                        kakaoMap?.zoomLevel ?: 15
//                                    )
//                                )
//                                arrowController?.attachOrMove(loc)
//
//                                viewModel.searchNearby("제주 전기차 충전소", loc.longitude, loc.latitude, 2000)
//                                viewModel.markSearched(loc, kakaoMap?.zoomLevel)
//                                showRequery = false
//                            } else {
//                                // 여전히 못가져오면 폴백
//                                fallbackToDefaultCenter(viewModel, kakaoMap, arrowController)
//                            }
//                        }
//                    } else {
//                        // 아직 조건 미충족 → 안내 유지/재요청
//                        gpsErrorMessage = when {
//                            !granted -> "정확한 위치 권한이 필요합니다. 설정에서 권한을 허용해 주세요."
//                            !gpsOn -> "GPS가 꺼져 있습니다. 설정에서 위치 서비스를 켜주세요."
//                            else -> null
//                        }
//                        // 필요 시 권한 자동 재요청 트리거 (accompanist)
//                        // if (!granted) permissionState.launchPermissionRequest()
//                    }
                    // 조건 충족해도 자동 이동/검색은 하지 않음. (오직 GPS 버튼에서만)
                    gpsErrorMessage = when {
                        !granted -> "정확한 위치 권한이 필요합니다. 설정에서 권한을 허용해 주세요."
                        !gpsOn   -> "GPS가 꺼져 있습니다. 설정에서 위치 서비스를 켜주세요."
                        else     -> null // 문제 없으면 메시지 제거만
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { arrowController?.detach() }
    }

    /** 선택 상태에 따른 시트 단계 제어 (선택 시 부분 확장, 해제 시 숨김) */
    LaunchedEffect(selectedPlace) {
        if (selectedPlace != null) {
            sheetState.partialExpand()
        } else {
            sheetState.hide()
        }
    }

    /**
     * 뒤로가기 핸들링
     *
     * - Expanded -> Partial
     * - Partial -> Hide + 선택 해제
     * - Hidden  -> 선택 해제
     */
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

    /**
     * 화면 레이아웃 및 하단 시트 구성
     *
     * - onMapReady 에서 레이어 분리(현재 위치 / 장소) 및 ArrowController 연결
     * - 상단 검색바, 하단 FAB(현재 지도 위치로 재검색)
     */
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetShape = RoundedCornerShape(topStart = corner, topEnd = corner),
        sheetContainerColor = Color.White,
        sheetTonalElevation = if (isFullScreen) 0.dp else BottomSheetDefaults.Elevation,
        sheetShadowElevation = if (isFullScreen) 0.dp else BottomSheetDefaults.Elevation,
        sheetDragHandle = { if (!isFullScreen) TinyHandle() },
        sheetPeekHeight = if (selectedPlace != null) 460.dp else 0.dp,
        sheetSwipeEnabled = selectedPlace != null,
        sheetContent = {
            if (selectedPlace != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isFullScreen) Modifier.statusBarsPadding() else Modifier)
                        .navigationBarsPadding()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    PlaceChargerDetailScreen(
                        place = selectedPlace!!,
                        isFullScreen = isFullScreen,
                        isLoading = isDetailLoading,
                        onRetry = { selectedPlace?.id?.let { viewModel.fetchCharger(it, forceRefresh = true) } },
                        onNavigateClick = { coroutineScope.launch { sheetState.hide() } },
                        onPlaceClick = { ui ->
                            navController.navigate("placeTourDetail/${ui.id}")
                        }
                    )

                    detailError?.let { msg ->
                        Spacer(Modifier.height(8.dp))
                        AssistChip(
                            onClick = { selectedPlace?.let(viewModel::selectPlace) }, // 다시 시도
                            label = { Text(msg) }
                        )
                    }
                }
            } else {
                Box(Modifier.fillMaxWidth())
            }
        }
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            /** 지도 준비: 레이어 생성 + ArrowController 연결 */
            KakaoMapView(onMapReady = { map ->
                kakaoMap = map

                val lm = map.labelManager ?: return@KakaoMapView
                // 현재 위치(한 장짜리 마커) 전용 레이어: 위로 보이도록 zOrder↑
                locationLayer = lm.addLayer(
                    LabelLayerOptions.from("layer_location").setZOrder(5000)
                )
                // 장소 라벨 전용 레이어
                placesLayer = lm.addLayer(
                    LabelLayerOptions.from("layer_places").setZOrder(2000)
                )

                arrowController = ArrowController(
                    map = map,
                    headingProvider = { headingDeg },
                    iconBiasDeg = 0f,      // 에셋 기본 방향이 북(↑)이면 0f
                    layer = locationLayer  // 현재 위치 마커는 locationLayer 에만 그린다
                )

                map.setOnMapClickListener { _, _, _, _ ->
                    if (isLabelHighlighted) {
                        selectedLabel?.let { runCatching { it.applyDefaultStyle() } }
                        isLabelHighlighted = false
                    }
                }
            })

            /** 상단 검색바 */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 0.dp)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                ClickableSearchBar(
                    placeholder = "주변 장소 및 충전소를 검색해주세요.",
                    onClick = { navController.navigate("search") }
                )
            }

            /** 상단 검색바 하단 재검색 버튼 */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 68.dp),   // 검색바 바로 아래로 배치 되게 조정
                contentAlignment = Alignment.TopCenter
            ) {
                AnimatedVisibility(visible = showRequery) {
                    AssistChip(
                        modifier = Modifier.size(width = 120.dp, height = 28.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White,
                            labelColor = Color.Black
                        ),
                        onClick = {
                            val center = viewModel.lastCenter ?: return@AssistChip
                            val zoom = kakaoMap?.zoomLevel
                            // 현재 지도 중심으로 재검색
                            viewModel.searchNearby("제주 전기차 충전소", center.longitude, center.latitude, 2000)
                            // 검색 기준 갱신해 다음부터 배너가 사라지도록
                            viewModel.markSearched(center, zoom)
                            showRequery = false
                        },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(0.4.dp, Color.Black),
                        label = {
                            Text(
                                "이 지역 재검색",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Refresh,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            if (gpsErrorMessage != null) {
                AlertDialog(
                    onDismissRequest = { gpsErrorMessage = null },
                    title = { Text("위치 사용 불가") },
                    text = { Text(gpsErrorMessage!!) },
                    containerColor = Color.White,
                    confirmButton = {
                        TextButton(onClick = {
                            gpsErrorMessage = null
                            // 권한이 없는 케이스면 앱 권한 화면으로
                            if (!permissionState.allPermissionsGranted) {
                                val uri = Uri.fromParts("package", context.packageName, null)
                                openAppPermissionSettings.launch(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
                                )
                            } else {
                                // 권한은 있는데 위치 서비스가 꺼져있는 케이스
                                openLocationSettings.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            }
                        }) {
                            Text("설정 열기", style = TextStyle(color = Variables.Blue700, fontWeight = FontWeight.SemiBold))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { gpsErrorMessage = null
                        }) { Text("닫기", style = TextStyle(color = Variables.Blue700, fontWeight = FontWeight.SemiBold)) }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .background(color = Color.Transparent)
                    .fillMaxSize()
            ) {
                /** GPS 버튼(현재 위치로 이동) */
                FloatingActionButton(
                    onClick = {
                        // 권한 체크
                        if (!permissionState.allPermissionsGranted) {
                            gpsErrorMessage = "정확한 위치 권한이 필요합니다. 설정에서 권한을 허용해 주세요."
                            fallbackToDefaultCenter(viewModel, kakaoMap, arrowController)
                            showRequery = false
                            return@FloatingActionButton
                        }

                        // 위치(GPS) 설정 체크
                        if (!isLocationEnabled(context)) {
                            gpsErrorMessage = "GPS가 꺼져 있습니다. 설정에서 위치 서비스를 켜주세요."
                            fallbackToDefaultCenter(viewModel, kakaoMap, arrowController)
                            showRequery = false
                            return@FloatingActionButton
                        }

                        // 현재 위치 가져와서 지도/마커/검색 갱신
                        getCurrentLocation(context, fusedLocationClient) { loc ->
                            if (loc == null) {
                                gpsErrorMessage = "현재 위치를 가져올 수 없습니다. 잠시 후 다시 시도해 주세요."
                                fallbackToDefaultCenter(viewModel, kakaoMap, arrowController)
                                showRequery = false
                                gpsMode = false
                            } else {
                                gpsMode = true
                                currentLatLng = loc
                                viewModel.lastUserLocation = loc
                                viewModel.lastCenter = loc
                                viewModel.isMapRestored = false

                                coroutineScope.launch { sheetState.hide() }

                                kakaoMap?.moveCamera(
                                    CameraUpdateFactory.newCenterPosition(
                                        loc,
                                        15
                                    )
                                )
                                arrowController?.attachOrMove(loc)

                                // 주변 충전소 재탐색(요구사항: 현재 위치 기준 충전소 검은색 마커들)
                                viewModel.searchNearby("제주 전기차 충전소", loc.longitude, loc.latitude, 2000)
                                viewModel.markSearched(loc, kakaoMap?.zoomLevel)
                                showRequery = false
                            }
                        }
                    },
                    containerColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(16.dp),
                    ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "현재 위치로 이동"
                    )
                }
            }
        }
    }
}

/**
 * 하단 시트 Drag Handle(얇은 바) 컴포저블
 */
@Composable
private fun TinyHandle(
    thickness: Dp = 4.dp,         // 바 두께
    length: Dp = 36.dp,           // 바 길이
    topPadding: Dp = 10.dp,       // 바와 시트 상단 간격
    cornerRadius: Dp = 2.dp,
    color: Color = Color.Gray // 배경과 대비되는 색
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(top = topPadding, bottom = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .height(thickness)
                .width(length)
                .clip(RoundedCornerShape(cornerRadius))
                .background(color)
        )
    }
}

private fun isLocationEnabled(context: android.content.Context): Boolean {
    val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

private fun distanceMeters(a: LatLng, b: LatLng): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLng = Math.toRadians(b.longitude - a.longitude)
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val aa = sin(dLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2.0)
    val c = 2 * atan2(sqrt(aa), sqrt(1 - aa))
    return R * c
}

/** 현재 위치를 못 구했을 때 기본 위치(제주시청)로 이동 + 주변 검색 */
private fun fallbackToDefaultCenter(
    viewModel: MapViewModel,
    kakaoMap: KakaoMap?,
    arrowController: ArrowController?,
    searchRadius: Int = 2000
) {
    // 현재 위치 마커는 숨김 (실제 현재 위치가 아니므로)
    arrowController?.detach()

    // 카메라 이동
    kakaoMap?.moveCamera(
        CameraUpdateFactory.newCenterPosition(DEFAULT_CENTER, DEFAULT_ZOOM)
    )

    // 상태 기록
    viewModel.lastCenter = DEFAULT_CENTER
    viewModel.lastZoomLevel = DEFAULT_ZOOM
    viewModel.isMapRestored = false
    viewModel.markSearched(DEFAULT_CENTER, kakaoMap?.zoomLevel)

    // 주변 충전소 검색
    viewModel.searchNearby(
        query = "제주 전기차 충전소",
        longitude = DEFAULT_CENTER.longitude,
        latitude = DEFAULT_CENTER.latitude,
        radius = searchRadius
    )
}

private fun Label.applySelectedStyle(placeName: String) {
    val textStyle = LabelTextStyle.from(28, 0xFF000000.toInt())
    val selectedStyle = LabelStyle.from(R.drawable.ev_marker_selected).setTextStyles(textStyle)
    val textBuilder = LabelTextBuilder().setTexts(placeName)
    changeStylesAndText(LabelStyles.from(selectedStyle), textBuilder)
}

private fun Label.applyDefaultStyle() {
    val defaultTextStyle = LabelTextStyle.from(28, 0xFF000000.toInt())
    val defaultStyle = LabelStyle.from(R.drawable.ev_marker).setTextStyles(defaultTextStyle)
    val emptyText = LabelTextBuilder().setTexts("")
    changeStylesAndText(LabelStyles.from(defaultStyle), emptyText)
}