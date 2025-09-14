package com.jeju.evtravel.ui.detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.jeju.evtravel.domain.model.Course
import com.jeju.evtravel.domain.model.CoursePlace
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.ui.summary.ChargerSummaryScreen
import com.jeju.evtravel.ui.summary.PlaceSummaryScreen
import com.kakao.vectormap.LatLng

// 충전소 상세로
fun openChargerDetail(navController: NavController, place: Place) {
    navController.currentBackStackEntry?.savedStateHandle?.set("cachedPlace", place)
    navController.navigate("chargerDetail/${place.id}") {
        launchSingleTop = true
    }
}

// 장소 상세로
fun openPlaceDetail(navController: NavController, place: Place) {
    navController.currentBackStackEntry?.savedStateHandle?.set("cachedPlace", place)
    navController.navigate("placeDetail/${place.id}") {
        launchSingleTop = true
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlaceSheetScreen(
    fusedLocationClient: FusedLocationProviderClient,
    place: Place,
    kind: SummaryKind,
    isFullScreen: Boolean = false,
    isLoading: Boolean = false,
    onRetry: () -> Unit,
    onNavigateClick: () -> Unit,
    onOpenPlaceDetail: (Place) -> Unit,
    onOpenChargerDetail: (Place) -> Unit,
    onCourseClick: (Course) -> Unit,
    onNavigateToPlaceInCourse: (coursePlace: CoursePlace) -> Unit
) {

    // 다이얼로그 상태 변수
//    var showNavAppBottomSheet by remember { mutableStateOf(false) }
//    val context = LocalContext.current
//    var startLocation by remember { mutableStateOf<LatLng?>(null) }
//    val coroutineScope = rememberCoroutineScope()
//    var destinationAddress by remember { mutableStateOf("") }

    // "안내하기" 버튼을 눌렀을 때 실행될 함수
//    val onNavigate: () -> Unit = let@{
//        if (place.latitude == null || place.longitude == null) {
//            Toast.makeText(context, "좌표 정보가 없어 길 안내를 시작할 수 없습니다.", Toast.LENGTH_SHORT).show()
//            return@let
//        }
//        val availableApps = getAvailableNavigationApps(context)
//        if (availableApps.isEmpty()) {
//            Toast.makeText(context, "설치된 길 안내 앱이 없습니다.", Toast.LENGTH_SHORT).show()
//        } else {
//            getCurrentLocation(context, fusedLocationClient) { loc ->
//                startLocation = loc
//
//                coroutineScope.launch {
//                    val geocoder = Geocoder(context, Locale.KOREAN)
//                    try {
//                        val addresses = withContext(kotlinx.coroutines.Dispatchers.IO) {
//                            geocoder.getFromLocation(
//                                place.latitude,
//                                place.longitude,
//                                1
//                            )
//                        }
//                        if (addresses != null && addresses.isNotEmpty()) {
//                            val addressLine = addresses[0].getAddressLine(0)
//                            val cleanedAddress = addressLine.replace("대한민국 ", "")
//                            destinationAddress = URLEncoder.encode(cleanedAddress, "UTF-8")
//                        } else {
//                            // 주소를 찾지 못하면 장소 이름을 기본값으로 사용
//                            destinationAddress = URLEncoder.encode(place.name, "UTF-8")
//                        }
//                    } catch (e: Exception) {
//                        Log.e("Geocoder", "주소 변환 실패", e)
//                        // 실패 시에도 장소 이름을 기본값으로 사용
//                        destinationAddress = URLEncoder.encode(place.name, "UTF-8")
//                    }
//
//                    // ✅ 주소 변환이 완료되면 시트를 띄웁니다.
//                    showNavAppBottomSheet = true
//                }
//            }
//        }
//    }

    when (kind) {
        SummaryKind.PLACE -> {
            PlaceSummaryScreen(
                place = place,
                onNavigateClick = onNavigateClick,
                onExpandToDetail = { onOpenPlaceDetail(place) },
            )
        }

        SummaryKind.CHARGER -> {
            ChargerSummaryScreen(
                place = place,
                isFullScreen = isFullScreen,
                isLoading = isLoading,
                onRetry = onRetry,
                onNavigateClick = onNavigateClick,
                onPlaceClick = onOpenPlaceDetail,
                onExpandToDetail = { onOpenChargerDetail(place) },
                onCourseClick = onCourseClick,
                onNavigateToPlaceInCourse = onNavigateToPlaceInCourse
            )
        }
    }

    // 길 안내 앱 선택 바텀 모달 시트
//    if (showNavAppBottomSheet) {
//        NavigationAppBottomSheet(
//            context = context,
//            startLocation = startLocation,
//            endLocation = LatLng.from(place.latitude, place.longitude),
//            destinationAddress = destinationAddress,
//            onDismiss = { showNavAppBottomSheet = false }
//        )
//    }
}