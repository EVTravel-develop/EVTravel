package com.jeju.evtravel.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.jeju.evtravel.R
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.ui.map.getCurrentLocation
import com.jeju.evtravel.ui.theme.Variables
import com.jeju.evtravel.utils.NavigationAppBottomSheet
import com.kakao.vectormap.LatLng
import java.net.URLEncoder

data class ChargerRowUi(
    val label: String,      // 예) "100 kWDC콤보"
//    val priceText: String,  // 예) "347.2원/kWh"
    val available: Int,
    val total: Int
)

fun isCharging(status: String): Boolean =
    status == "3" || status.equals("CHARGING", ignoreCase = true)

private fun prettyType(code: String?): String {
    // null, 공백, "0" 방어
    val c = code?.trim()?.padStart(2, '0') ?: return ""
    return when (c) {
        "01" -> "DC차데모"
        "02" -> "AC완속"
        "03" -> "DC차데모+AC3상"
        "04" -> "DC콤보"
        "05" -> "DC차데모+DC콤보"
        "06" -> "DC차데모+AC3상+DC콤보"
        "07" -> "AC3상"
        "08" -> "DC콤보(완속)"
        "09" -> "NACS"
        "10" -> "DC콤보+NACS"
        else -> ""
    }
}

private fun kwBucket(output: String?): String? {
    val kw = output?.toDoubleOrNull()?.toInt() ?: return null
    return when {
        kw >= 200 -> "${kw} kW"          // 초급속 등
        kw >= 50  -> "${kw} kW"          // 급속
        kw > 0    -> "${kw} kW"          // 완속
        else      -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChargerDetailScreen(
    fusedLocationClient: FusedLocationProviderClient,
    place: Place,
    onBack: () -> Unit,
    bookmarkViewModel: BookmarkViewModel = hiltViewModel()
) {
    val chargers = place.chargerList ?: emptyList()
    val isBookmarked by bookmarkViewModel.isPlaceBookmarked.collectAsState()

    // 길 안내 관련 상태 변수 및 로직 추가
    var showNavAppBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var startLocation by remember { mutableStateOf<LatLng?>(null) }
    var destinationAddress by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val onNavigate: () -> Unit = {
        // ✅ getAvailableNavigationApps 호출 및 분기문을 제거합니다.
        getCurrentLocation(context, fusedLocationClient) { loc ->
            startLocation = loc

            // ✅ Geocoder 대신 목적지 이름(place.name)을 바로 사용하도록 단순화합니다.
            destinationAddress = URLEncoder.encode(place.name, "UTF-8")
            showNavAppBottomSheet = true
        }
    }

    // 화면 진입 시 북마크 상태 체크
    LaunchedEffect(key1 = place.id) {
        bookmarkViewModel.checkPlaceBookmark(place.id)
    }

    // 샘플 변환: 출력(W)과 커넥터를 합쳐 라벨링하고 가격/가용수 계산
    val rows: List<ChargerRowUi> = chargers
        .groupBy { ch ->
            val typeLabel = prettyType(ch.chargerType)              // <- chargerType 기반
            val kwLabel = kwBucket(ch.output)                       // <- (옵션) 출력 병기
            // 라벨 규칙: "타입 + (있으면) 공백 + kW"
            if (kwLabel != null) "$kwLabel$typeLabel" else typeLabel
        }
        .map { (label, list) ->
            val total = list.size
            val avail = list.count { !isCharging(it.status) }
            ChargerRowUi(
                label = label,
                available = avail,
                total = total
            )
        }
        // 정렬: 개수 많은 순(원하면 다른 기준 추가 가능)
        .sortedByDescending { it.total }

    Scaffold(
        floatingActionButton = { ActionFAB(onClick = onNavigate) }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(Color.White),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 헤더(이미지/뒤로가기)
            item {
                Box(Modifier.fillMaxWidth()) {
//                    HeaderImage(imageUrl = " ")
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(3.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            }
            // 상단 둥근 섹션
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-15).dp),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = Color.White
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        // ─ 기본 정보 ─
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = place.name,
                                    style = DetailTitleTextStyle
                                )
                                IconButton(
                                    onClick = {
                                        bookmarkViewModel.togglePlaceBookmark(place)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    val iconRes =
                                        if (isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_empty
                                    Image(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = "북마크"
                                    )
                                }
                            }
                            Spacer(Modifier.height(20.dp))

                            val hours = chargers.firstOrNull()?.usageTime.orEmpty()
                            val location = chargers.firstOrNull()?.location
                                ?.takeIf { !it.isNullOrBlank() && it.lowercase() != "null" }
                                .orEmpty()
                            val addr =
                                place.roadAddress?.takeIf { it.isNotBlank() } ?: place.address
                            val fullAddress =
                                if (!addr.isNullOrBlank())
                                    if (location.isNotEmpty()) "${addr}, $location" else addr!!
                                else
                                    location

                            if (hours.isNotBlank()) {
                                InfoRow(iconRes = R.drawable.ic_clock, text = hours)
                                Spacer(Modifier.height(12.dp))
                            }
                            if (fullAddress.isNotBlank()) {
                                InfoRow(iconRes = R.drawable.ic_location, text = fullAddress)
                                Spacer(Modifier.height(12.dp))
                            }
                            if (!place.phone.isNullOrBlank()) {
                                InfoRow(iconRes = R.drawable.ic_phone, text = place.phone)
                                Spacer(Modifier.height(18.dp))
                            }
                        }

                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp),
                            color = Variables.Grayscale50,
                            thickness = 7.dp
                        )
                        Spacer(Modifier.height(18.dp))

                        SectionTitle(
                            title = "충전기 정보",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(12.dp))

                        // ─ 행들 (Surface 안에서 렌더링: 한 화면처럼 보이게) ─
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rows.forEach { r -> ChargerRow(r) }

                            if (rows.isEmpty()) {
                                Text(
                                    "등록된 충전기 정보가 없습니다.",
                                    style = DetailOutputTextStyle,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(40.dp)) // 하단 여유
                    }
                }
            }
        }
    }

    // 길 안내 앱 선택 바텀 모달 시트
    if (showNavAppBottomSheet) {
        NavigationAppBottomSheet(
            context = context,
            startLocation = startLocation,
            endLocation = LatLng.from(place.latitude, place.longitude),
            destinationAddress = destinationAddress,
            onDismiss = { showNavAppBottomSheet = false }
        )
    }
}

@Composable
private fun ChargerRow(row: ChargerRowUi) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
//                    text = row.label + " | " + row.priceText,
                    text = row.label + " | ",
                    style = DetailOutputTextStyle
                )
            }
            val canUse = row.available > 0
            val rightText = if (canUse) "충전가능 ${row.available}/${row.total}"
            else "충전불가 ${row.available}/${row.total}"
            val rightColor = if (canUse) Variables.Blue700 else Color(0xFFD92B2B)
            Text(text = rightText, color = rightColor, style = DetailStatTextStyle)
        }
    }
}
