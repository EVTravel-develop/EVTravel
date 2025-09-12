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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jeju.evtravel.R
import com.jeju.evtravel.data.util.mapStatus
import com.jeju.evtravel.domain.model.Course
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.ui.detail.Block
import com.jeju.evtravel.ui.detail.CategoryChipTextStyle
import com.jeju.evtravel.ui.detail.CourseCardTitleTextStyle
import com.jeju.evtravel.ui.detail.NearbyPlaceViewModel
import com.jeju.evtravel.ui.detail.OutputTextStyle
import com.jeju.evtravel.ui.detail.PlaceTabTagTextStyle
import com.jeju.evtravel.ui.detail.PlaceTabTitleTextStyle
import com.jeju.evtravel.ui.detail.RobotoFamily
import com.jeju.evtravel.ui.detail.SelectedTabTextStyle
import com.jeju.evtravel.ui.detail.StatTextStyle
import com.jeju.evtravel.ui.detail.SubtitleTextStyle
import com.jeju.evtravel.ui.detail.TitleTextStyle
import com.jeju.evtravel.ui.detail.UiNearbyPlace
import com.jeju.evtravel.ui.detail.UnselectedTabTextStyle
import com.jeju.evtravel.ui.detail.course.CourseCard
import com.jeju.evtravel.ui.detail.course.CourseViewModel
import com.jeju.evtravel.ui.theme.Variables

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
    onExpandToDetail: () -> Unit
) {
    val chargers = place.chargerList ?: emptyList()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val courseUiState = courseVm.state.collectAsState().value

    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("추천 코스", "장소")

    val uiState = nearbyVm.state.collectAsState().value

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
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                // --- 상단 타이틀 ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onExpandToDetail),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = place.name,
                        style = TitleTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painterResource(id = R.drawable.ic_right),
                        contentDescription = "상세 보기",
                        tint = Color.Black,
                        modifier = Modifier.padding(start = 8.dp)
                    )
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
                        onSecondary = onNavigateClick
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
                        .padding(bottom = 16.dp),
                    onClick = onNavigateClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Variables.Blue700,
                        contentColor = Color.White
                    ),
                ) {
                    Text("안내하기")
                }
                Spacer(Modifier.height(10.dp))
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
        item {
            Column(Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
            ) {
                // 🔹 스크롤을 없애고 Column으로 변경
                if (tabIndex == 0) {
                    when {
                        courseUiState.loading -> {
                            LoadingCard()
                        }

                        courseUiState.data == null -> {
                            EmptyChargerCard(
                                title = "추천 코스를 찾지 못했어요",
                                subtitle = "잠시 후 다시 시도해주세요.",
                                primaryText = "다시 시도",
                                onPrimary = { /* reload */ }
                            )
                        }

                        else -> {
                            val courseInfo = courseUiState.data.course_info.firstOrNull()
                            if (courseInfo != null) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // ─── 상단 코스 이름 ───
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
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

                                    // ─── 하단 장소 카드 ───
                                    courseInfo.places.forEach { placeName ->
                                        CourseCard(
                                            placeName = placeName,
                                            onNavigateToPlace = { onNavigateClick() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // [장소] — 카테고리 칩 + 2열 카드 그리드
                    val categories = listOf("자연환경", "맛집", "카페", "박물관")

                    Spacer(Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        CategoryChips(
                            categories = categories,
                            selected = uiState.selectedCategory,
                            onSelect = { label -> nearbyVm.selectCategory(label) }
                        )

                        Spacer(Modifier.height(12.dp))

                        // 상태별 UI
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
    Box(
        modifier = Modifier
//            .size(width = 184.dp, height = 209.dp)  이미지가 추가 하면 적용
            .width(184.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(10.dp),          // border-radius
                clip = false
            )
            .background(Color.White, RoundedCornerShape(10.dp))
            .clickable { onClick(item) }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // gap
        ) {
            val ctx = LocalContext.current
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(108.dp)
                        .clip(RoundedCornerShape(10.dp)),   // 이미지도 radius 적용
                    contentScale = ContentScale.Crop
                )
            }
            Text(item.title, style = PlaceTabTitleTextStyle, maxLines = 1)
            if (item.tags != null) {
                Text(
                    text = item.tags,
                    style = PlaceTabTagTextStyle,
                    color = Variables.Grayscale400,
                    maxLines = 1
                )
            }
        }
    }
}