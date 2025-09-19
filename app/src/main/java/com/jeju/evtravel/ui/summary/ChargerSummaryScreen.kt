package com.jeju.evtravel.ui.summary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.common.io.Files.append
import com.jeju.evtravel.R
import com.jeju.evtravel.data.util.mapStatus
import com.jeju.evtravel.domain.model.Course
import com.jeju.evtravel.domain.model.CoursePlace
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.service.ChargeTimeService
import com.jeju.evtravel.ui.detail.Block
import com.jeju.evtravel.ui.detail.CategoryChipTextStyle
import com.jeju.evtravel.ui.detail.CourseCardTitleTextStyle
import com.jeju.evtravel.ui.detail.EstimatedTimeLabelTextStyle
import com.jeju.evtravel.ui.detail.EstimatedTimeUnitStyle
import com.jeju.evtravel.ui.detail.EstimatedTimeValueStyle
import com.jeju.evtravel.ui.detail.NearbyPlaceViewModel
import com.jeju.evtravel.ui.detail.OutputTextStyle
import com.jeju.evtravel.ui.detail.PlaceTabTagTextStyle
import com.jeju.evtravel.ui.detail.PlaceTabTitleTextStyle
import com.jeju.evtravel.ui.detail.RegisterButtonTextStyle
import com.jeju.evtravel.ui.detail.RobotoFamily
import com.jeju.evtravel.ui.detail.SelectedTabTextStyle
import com.jeju.evtravel.ui.detail.StatTextStyle
import com.jeju.evtravel.ui.detail.SubtitleTextStyle
import com.jeju.evtravel.ui.detail.TitleTextStyle
import com.jeju.evtravel.ui.detail.UiNearbyPlace
import com.jeju.evtravel.ui.detail.UnselectedTabTextStyle
import com.jeju.evtravel.ui.detail.course.CourseCard
import com.jeju.evtravel.ui.detail.course.CourseViewModel
import com.jeju.evtravel.ui.detail.timeGradientBrush
import com.jeju.evtravel.ui.theme.Variables
import com.jeju.evtravel.ui.viewmodel.UserVehicleViewModel
import kotlin.math.roundToInt

@Composable
fun ChargerSummaryScreen(
    place: Place,
    isFullScreen: Boolean = false,
    isLoading: Boolean = false,
    onRetry: () -> Unit,
    onNavigateClick: () -> Unit,
    onPlaceClick: (Place) -> Unit = {},
    onCourseClick: (Course) -> Unit,
    nearbyVm: NearbyPlaceViewModel = hiltViewModel(),
    courseVm: CourseViewModel = hiltViewModel(),
    onExpandToDetail: () -> Unit,
    onNavigateToPlaceInCourse: (CoursePlace) -> Unit,
    onCoursePlaceClick: (CoursePlace) -> Unit,
    onNavigateToVehicleInfo: () -> Unit, // 네비게이션 콜백 추가
    userVehicleVm: UserVehicleViewModel = hiltViewModel(), // ViewModel 주입
) {
    val chargers = place.chargerList ?: emptyList()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val courseUiState = courseVm.state.collectAsState().value

    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("추천 코스", "장소")

    val uiState = nearbyVm.state.collectAsState().value

    // 예상 차량 충전 시간
    val userVehicleInfo by userVehicleVm.vehicleInfo.collectAsState() // ViewModel 상태 구독

    // 충전 시간 계산 로직
    val estimatedTimeString by remember(userVehicleInfo, place.chargerList) {
        derivedStateOf {
            val info = userVehicleInfo
            val chargers = place.chargerList
            if (info == null || chargers.isNullOrEmpty()) {
                return@derivedStateOf null // 정보 없으면 null
            }

            // 1. 선호하는 속도의 충전기 찾기
            val preferredChargers = chargers.filter { charger ->
                when (info.preferredSpeed) {
                    "초급속" -> (charger.output.toDoubleOrNull() ?: 0.0) >= 200
                    "급속" -> (charger.output.toDoubleOrNull() ?: 0.0) in 50.0..199.9
                    "완속" -> (charger.output.toDoubleOrNull() ?: 0.0) < 50
                    else -> false
                }
            }

            // 2. 사용할 충전기 결정 (선호 충전기가 있으면 그 중 가장 빠른 것, 없으면 전체에서 가장 빠른 것)
            val targetCharger = (if (preferredChargers.isNotEmpty()) {
                preferredChargers
            } else {
                chargers
            }).maxByOrNull { it.output.toDoubleOrNull() ?: 0.0 }


            if (targetCharger == null) return@derivedStateOf null

            // 충전 시간 계산
            val chargerKw = targetCharger.output.toDoubleOrNull() ?: return@derivedStateOf null
            try {
                // [수정] 목표 충전량을 현재 배터리 상태에 따라 동적으로 결정
                val targetSoc = if (info.currentSoc < 80) 80.0 else 100.0

                val timeInHours = ChargeTimeService().estimateChargeTime(
                    carModel = info.carModel,
                    chargerKw = chargerKw,
                    currentSoc = info.currentSoc.toDouble(),
                    targetSoc = targetSoc // 동적으로 결정된 목표치 사용
                )
                formatHoursToHHmm(timeInHours)
            } catch (e: Exception) {
                "계산 불가"
            }
        }
    }

    LaunchedEffect(tabIndex, place.longitude, place.latitude) {
        if (tabIndex == 1) {
            nearbyVm.selectTourPlace(place)
        }
    }

    // 새로운 LaunchedEffect를 추가하여 place.id가 변경될 때마다 코스를 다시 조회
    LaunchedEffect(place.id) {
        val pid = place.id.toLongOrNull()
        if (pid == null) {
            return@LaunchedEffect
        }
        courseVm.fetchCoursesForPlace(pid)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = screenHeight)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // --- 상단 타이틀 ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = place.name,
                        style = TitleTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onExpandToDetail)
                    )
                    Text(
                        text = if (userVehicleInfo == null) "차량 등록" else "차량정보수정",
                        style = RegisterButtonTextStyle,
                        color = Variables.Blue700,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToVehicleInfo)
                    )
                }

                val timeStringValue = estimatedTimeString

                // --- 충전 예상 시간 표시 ---
                if (timeStringValue != null) {
                    Spacer(Modifier.height(12.dp))
                    Column {
                        Text(
                            text = "충전 예상 시간",
                            style = EstimatedTimeLabelTextStyle,
                            color = Variables.Grayscale300
                        )

                        Text(
                            text = buildAnnotatedString {
                                // "1시간 35분" 과 같은 문자열을 숫자와 단위로 분리
                                val parts = timeStringValue.split(Regex("(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)"))
                                parts.forEach { part ->
                                    if (part.all { it.isDigit() }) {
                                        // 숫자 부분 스타일
                                        withStyle(style = EstimatedTimeValueStyle.toSpanStyle().copy(brush = timeGradientBrush)) {
                                            append(part)
                                        }
                                    } else {
                                        // 단위(시간, 분) 부분 스타일
                                        withStyle(style = EstimatedTimeUnitStyle.toSpanStyle().copy(brush = timeGradientBrush)) {
                                            append(part)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // --- 로딩 / 에러 / 데이터 ---
                if (isLoading) {
                    LoadingCard()
                    Spacer(Modifier.height(18.dp))
                    return@Column
                }

                if (chargers.isEmpty()) {
                    EmptyChargerCard(
                        title = "충전기 정보를 찾지 못했어요",
                        subtitle = "이 장소에 등록된 충전기가 없거나, 잠시 정보를 불러오지 못했을 수 있어요.",
                        primaryText = "다시 시도",
                        onPrimary = onRetry,
                        secondaryText = "다른 장소 보기",
                        onSecondary = { }
                    )
                    return@Column
                }

                fun isCharging(code: String?) =
                    code == "3" || (code?.equals("CHARGING", ignoreCase = true) == true)

                // 속도별 그룹핑
                val slow =
                    chargers.filter { it.output.toDoubleOrNull()?.let { out -> out < 50 } == true }
                val rapid = chargers.filter {
                    it.output.toDoubleOrNull()?.let { out -> out in 50.0..199.9 } == true
                }
                val ultra =
                    chargers.filter {
                        it.output.toDoubleOrNull()?.let { out -> out >= 200 } == true
                    }


                // === 사용 가능 = 총 개수 - 충전중 개수 ===
                val blocks = buildList {
                    if (rapid.isNotEmpty()) {
                        val total = rapid.size
                        val charging = rapid.count { isCharging(it.status) }
                        add(Block("급속", total, charging, total - charging))
                    }
                    if (slow.isNotEmpty()) {
                        val total = slow.size
                        val charging = slow.count { isCharging(it.status) }
                        add(Block("완속", total, charging, total - charging))
                    }
                    if (ultra.isNotEmpty()) {
                        val total = ultra.size
                        val charging = ultra.count { isCharging(it.status) }
                        add(Block("초급속", total, charging, total - charging))
                    }
                }

                // 속도별 집계 카드
                ChargerSummaryCard(blocks)

                Spacer(Modifier.height(18.dp))

                // 안내하기 버튼
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(47.dp),
                    onClick = onNavigateClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Variables.Blue700,
                        contentColor = Color.White
                    ),
                ) {
                    Text("안내하기")
                }
                Spacer(Modifier.height(18.dp))
            }
        }

        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .background(Variables.Grayscale50)
            )
            Spacer(Modifier.height(16.dp))
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "충전 시간 동안 주변을 둘러보세요",
                    style = TextStyle(
                        fontFamily = RobotoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 0.001.em,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.None
                        )
                    )
                )
                Spacer(Modifier.height(12.dp))
            }
        }
        item {
            Column(Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = tabIndex,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[tabIndex])
                                .height(2.dp),
                            color = Variables.Blue700
                        )
                    },
                    containerColor = Color.White,
                    divider = { Divider(color = Color(0x11000000), thickness = 1.dp) }
                ) {
                    tabs.forEachIndexed { i, title ->
                        Tab(
                            selected = tabIndex == i,
                            onClick = { tabIndex = i },
                            text = {
                                Text(
                                    text = title,
                                    style = if (tabIndex == i) SelectedTabTextStyle else UnselectedTabTextStyle
                                )
                            },
                            selectedContentColor = Color.Black,
                            unselectedContentColor = Color(0xFFD9D9D9)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
        when (tabIndex) {
            // --- 추천 코스 탭 ---
            0 -> {
                when {
                    courseUiState.loading -> {
                        // 로딩 상태는 하나의 아이템으로 표시
                        item {
                            Column(Modifier.padding(horizontal = 16.dp)) {
                                LoadingCard()
                            }
                        }
                    }

                    courseUiState.data == null -> {
                        // 데이터 없는 상태도 하나의 아이템으로 표시
                        item {
                            Column(Modifier.padding(horizontal = 16.dp)) {
                                EmptyChargerCard(
                                    title = "추천 코스를 찾지 못했어요",
                                    subtitle = "잠시 후 다시 시도해주세요.",
                                    primaryText = "다시 시도",
                                    onPrimary = { /* TODO: reload logic */ }
                                )
                            }
                        }
                    }

                    else -> {
                        val courseInfo = courseUiState.data.course_info.firstOrNull()
                        if (courseInfo != null) {

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp) // 패딩을 여기에 적용
                                        .background(Color.White, RoundedCornerShape(16.dp))
                                        .clickable { onCourseClick(courseUiState.data) }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = courseInfo.course_name,
                                        style = CourseCardTitleTextStyle,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Filled.ArrowForward,
                                        contentDescription = "코스 상세 보기",
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            items(courseInfo.places) { placeInCourse ->
                                // 각 카드에 좌우 패딩을 줍니다.
                                Column(Modifier.padding(horizontal = 16.dp)) {
                                    CourseCard(
                                        place = placeInCourse,
                                        onNavigateToPlace = onNavigateToPlaceInCourse,
                                        onCardClick = onCoursePlaceClick
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- 장소 탭 ---
            1 -> {
                // '장소' 탭의 내용은 복잡하므로 하나의 item으로 묶어서 처리합니다.
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        val categories = listOf("자연환경", "맛집", "카페", "박물관")

                        Spacer(Modifier.height(12.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CategoryChips(
                                categories = categories,
                                selected = uiState.selectedCategory,
                                onSelect = { label -> nearbyVm.selectCategory(label) }
                            )
                            Spacer(Modifier.height(12.dp))

                            when {
                                uiState.loading && uiState.items.isEmpty() -> {
                                    LoadingCard()
                                }
                                uiState.error != null && uiState.items.isEmpty() -> {
                                    EmptyChargerCard(
                                        title = "주변 장소를 불러오지 못했어요",
                                        subtitle = "네트워크 상태를 확인해주세요.",
                                        primaryText = "다시 시도",
                                        onPrimary = { nearbyVm.selectTourPlace(place) }
                                    )
                                }
                                uiState.items.isEmpty() -> {
                                    EmptyChargerCard(
                                        title = "반경 내 추천 장소가 없어요",
                                        subtitle = "반경을 넓히거나 다른 카테고리를 선택해보세요.",
                                        primaryText = "다시 시도",
                                        onPrimary = { nearbyVm.selectTourPlace(place) }
                                    )
                                }
                                else -> {
                                    uiState.items.chunked(2).forEach { row ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                        ) {
                                            Box(Modifier.weight(1f)) {
                                                PlaceGridCard(
                                                    item = row[0],
                                                    onClick = { clicked ->
                                                        uiState.itemsRaw
                                                            .firstOrNull { it.id == clicked.id }
                                                            ?.let { onPlaceClick(it) }
                                                    }
                                                )
                                            }
                                            if (row.size > 1) {
                                                Box(Modifier.weight(1f)) {
                                                    PlaceGridCard(
                                                        item = row[1],
                                                        onClick = { clicked ->
                                                            uiState.itemsRaw
                                                                .firstOrNull { it.id == clicked.id }
                                                                ?.let { onPlaceClick(it) }
                                                        }
                                                    )
                                                }
                                            } else {
                                                Spacer(Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 시간 포맷팅 헬퍼 함수
private fun formatHoursToHHmm(hours: Double): String {
    if (hours <= 0) return "0분"
    val totalMinutes = (hours * 60).roundToInt()
    val h = totalMinutes / 60
    val m = totalMinutes % 60

    return when {
        h > 0 && m > 0 -> "${h}시간 ${m}분"
        h > 0 -> "${h}시간"
        else -> "${m}분"
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                shape = MaterialTheme.shapes.large,
                color = Color(0xFFF9F9F9)
            ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9F9F9))
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Variables.Blue700)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ChargerSummaryCard(
    blocks: List<Block>
) {
    val rapid = blocks.firstOrNull { it.label == "급속" }
    val slow = blocks.firstOrNull { it.label == "완속" }
    val ultra = blocks.firstOrNull { it.label == "초급속" }

    // 존재하는 항목만
    val present = listOfNotNull(rapid, slow, ultra)

    // 상단 표시 쌍 결정
    val topPair: List<Block> = when (present.size) {
        3 -> listOfNotNull(rapid, slow)                   // 세 개면 상단은 급속/완속 고정
        2 -> orderPairForTwo(present)                     // 두 개면 있는 두 개를 상단에
        1 -> present                                      // 하나면 단독 중앙
        else -> emptyList()
    }

    // 하단(초급속) 표시는 "세 개 모두 있는 경우"에만
    val showUltraBottom = (present.size == 3 && ultra != null)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ───── 상단 영역 ─────
            when (topPair.size) {
                2 -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)     // divider가 내용 높이에 맞게
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SummaryColumn(
                            topPair[0],
                            Modifier
                                .weight(1f)
                        )
                        VerticalDivider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 12.dp),
                            thickness = 0.5.dp,
                            color = Color(0xFFDBDBDB)
                        )
                        SummaryColumn(
                            topPair[1],
                            Modifier
                                .weight(1f)
                        )
                    }
                }
                1 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        SummaryColumn(topPair[0])
                    }
                }
                else -> {}
            }

            // ───── 하단(초급속) 중앙 배치: 세 개 모두 있을 때만 ─────
            if (showUltraBottom) {
                Spacer(Modifier.height(6.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = ultra!!.label,
                        style = OutputTextStyle,
                        color = Variables.Grayscale600
                    )
                    Text(
                        text = "${mapStatus("2")} ${ultra.available} / ${ultra.total}",
                        style = StatTextStyle,
                        color = Variables.Blue700
                    )
                }
            }
        }
    }
}

/**
 * 두 개만 있을 때의 상단 좌/우 배치 규칙:
 * - (급속, 완속) -> [급속, 완속]
 * - (급속, 초급속) -> [급속, 초급속]   // 초급속은 오른쪽
 * - (완속, 초급속) -> [완속, 초급속]   // 초급속은 오른쪽
 */
private fun orderPairForTwo(present: List<Block>): List<Block> {
    val names = present.map { it.label }.toSet()
    val map = present.associateBy { it.label }
    return when {
        names.containsAll(setOf("급속", "완속"))   -> listOf(map.getValue("급속"), map.getValue("완속"))
        names.containsAll(setOf("급속", "초급속")) -> listOf(map.getValue("급속"), map.getValue("초급속"))
        names.containsAll(setOf("완속", "초급속")) -> listOf(map.getValue("완속"), map.getValue("초급속"))
        else -> present // 혹시 모를 예외
    }
}

@Composable
private fun SummaryColumn(b: Block, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = b.label, style = OutputTextStyle, color = Variables.Grayscale600)
        Text(
            text = "${mapStatus("2")} ${b.available} / ${b.total}",
            style = StatTextStyle,
            color = Variables.Blue700
        )
    }
}

@Composable
private fun EmptyChargerCard(
    title: String,
    subtitle: String,
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9F9F9)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = TitleTextStyle)
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = SubtitleTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Variables.Blue700,
                        contentColor = Color.White
                    ),
                    onClick = onPrimary,
                    shape = RoundedCornerShape(12.dp)) {
                    Text(primaryText)
                }
                if (secondaryText != null && onSecondary != null) {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF1F1F1),
                            contentColor = Variables.Blue700
                        ),
                        onClick = onSecondary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(secondaryText)
                    }
                }
            }
        }
    }
}

// 장소: 카테고리 칩
@Composable
private fun CategoryChips(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        categories.forEach { c ->
            val isSelected = selected == c
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(c) },
                label = {
                    Text(
                        c,
                        style = CategoryChipTextStyle
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    // 비선택 상태
                    containerColor = Color(0xFFF1F1F1),
                    labelColor = Variables.Grayscale300,
                    // 선택 상태
                    selectedContainerColor = Variables.Blue700,
                    selectedLabelColor = Color.White,
                ),
                border = BorderStroke(0.dp, Color.Transparent)
            )
        }
    }
}

// 장소: 2열 카드
@Composable
private fun PlaceGridCard(
    item: UiNearbyPlace,
    onClick: (UiNearbyPlace) -> Unit
) {
    // ✅ 카드 전체를 감싸는 Column. 그림자, 둥근 모서리, 배경색, 클릭 효과를 적용합니다.
    Column(
        modifier = Modifier
            .size(width = 184.dp, height = 199.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(10.dp))
            .background(Color.White, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp)) // 자식 요소들이 부모의 둥근 모서리를 넘어가지 않도록 클리핑
            .clickable { onClick(item) }
    ) {
        // 1. 이미지 영역
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.imageUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(id = R.drawable.placeholder_large),
            error = painterResource(id = R.drawable.placeholder_large), // 에러 시에도 동일한 샘플 이미지 표시
            contentDescription = item.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(122.dp), // 이미지 높이를 고정합니다.
            contentScale = ContentScale.Crop
        )

        // 2. 텍스트 정보 영역
        Column(
            modifier = Modifier
                .fillMaxSize() // 남은 공간을 모두 채웁니다.
                .padding(horizontal = 12.dp, vertical = 12.dp), // 텍스트 영역 내부 패딩
            verticalArrangement = Arrangement.spacedBy(6.dp) // 텍스트 사이의 간격
        ) {
            // 메인 타이틀
            Text(
                text = item.title,
                style = PlaceTabTitleTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 태그 정보
            if (item.tags != null) {
                Text(
                    text = item.tags,
                    style = PlaceTabTagTextStyle,
                    color = Variables.Grayscale400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}