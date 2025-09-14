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
    onNavigateToPlaceInCourse: (coursePlace: CoursePlace) -> Unit,
    onCoursePlaceClick: (CoursePlace) -> Unit
) {

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
                onNavigateToPlaceInCourse = onNavigateToPlaceInCourse,
                onCoursePlaceClick = onCoursePlaceClick
            )
        }
    }
}