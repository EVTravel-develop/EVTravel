package com.jeju.evtravel.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.ui.summary.ChargerSummaryScreen
import com.jeju.evtravel.ui.summary.PlaceSummaryScreen

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

@Composable
fun PlaceSheetScreen(
    place: Place,
    kind: SummaryKind,
    isFullScreen: Boolean = false,
    isLoading: Boolean = false,
    onRetry: () -> Unit,
    onNavigateClick: () -> Unit,
    onPlaceClick: (Place) -> Unit = {},
    onOpenPlaceDetail: (Place) -> Unit,
    onOpenChargerDetail: (Place) -> Unit
) {
    when (kind) {
        SummaryKind.PLACE -> {
            PlaceSummaryScreen(
                place = place,
                onNavigateClick = onNavigateClick,
                onExpandToDetail = { onOpenPlaceDetail(place) }
            )
        }
        SummaryKind.CHARGER -> {
            ChargerSummaryScreen(
                place = place,
                isFullScreen = isFullScreen,
                isLoading = isLoading,
                onRetry = onRetry,
                onNavigateClick = onNavigateClick,
                onPlaceClick = onPlaceClick,
                onExpandToDetail = { onOpenChargerDetail(place) }
            )
        }
    }
}