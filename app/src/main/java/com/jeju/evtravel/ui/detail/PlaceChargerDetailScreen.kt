package com.jeju.evtravel.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jeju.evtravel.R
import com.jeju.evtravel.data.util.mapStatus
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.ui.theme.Variables

// === 파일 상단에 두어 다른 컴포저블에서도 보이도록 ===
private data class Block(
    val label: String,
    val total: Int,
    val charging: Int,       // "3"
    val available: Int       // = total - charging
)

private data class CourseCardData(
    val title: String,
    val subtitle: String,
    val imageRes: Int
)

private data class PlaceCardData(
    val title: String,
    val subtitle: String,
    val tags: List<String>,
    val imageRes: Int
)

private val RobotoFamily = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal),
    Font(R.font.roboto_bold, FontWeight.Bold)
)

val BoxTextStyle = TextStyle(
    fontFamily = RobotoFamily,
    fontWeight = FontWeight.W600,
    fontSize = 16.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.sp,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

val TitleTextStyle = TextStyle(
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
val SubtitleTextStyle = TextStyle(
    fontFamily = RobotoFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

val SelectedTabTextStyle = TextStyle(
    fontFamily = RobotoFamily,
    fontWeight = FontWeight.Bold,   // 700
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

val UnselectedTabTextStyle = TextStyle(
    fontFamily = RobotoFamily,
    fontWeight = FontWeight.Normal, // 400
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

@Composable
fun PlaceChargerDetailScreen(
    place: Place,
    isFullScreen: Boolean = false,  // 전체화면
    isLoading: Boolean = false,     // 로딩
    onRetry: () -> Unit = {},       // 새로고침/다시 시도
    onNavigateClick: () -> Unit
) {
    val chargers = place.chargerList ?: emptyList()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = screenHeight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // 타이틀
            Text(text = place.name, style = TitleTextStyle)
            Spacer(Modifier.height(12.dp))

            // 로딩
            if (isLoading) {
                LoadingCard()
                return@Column
            }

            // 로딩 완료 후 데이터가 비어있는 상태 (0, null, 빈 리스트)
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

            fun isCharging(code: String?) = code == "3" || (code?.equals("CHARGING", ignoreCase = true)==true)

            // 속도별 그룹핑
            val slow = chargers.filter { it.output.toDoubleOrNull()?.let { out -> out < 50 } == true }
            val rapid = chargers.filter {
                it.output.toDoubleOrNull()?.let { out -> out in 50.0..199.9 } == true
            }
            val ultra =
                chargers.filter { it.output.toDoubleOrNull()?.let { out -> out >= 200 } == true }


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
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Variables.Blue700,
                    contentColor = Color.White
                ),
            ) {
                Text("안내하기")
            }

            Spacer(Modifier.height(20.dp))

            // 섹션 타이틀
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

            // 탭
            var tabIndex by remember { mutableStateOf(0) }
            val tabs = listOf("추천 코스", "장소")
            TabRow(
                selectedTabIndex = tabIndex,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[tabIndex]) // 선택 탭 위치/폭에 맞춤
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // 가로 카드 리스트
                if (tabIndex == 0) {
                    // [추천 코스] — 세로 큰 카드 + 이미지 오버레이
                    val courses = listOf(
                        CourseCardData(
                            title = "탑동광장",
                            subtitle = "빛과제주의 일몰이 아름다운 장소. 해안가를 걸으며, 노을 촬영에 최적입니다.",
                            imageRes = R.drawable.jeju_course_sample
                        ),
                        CourseCardData(
                            title = "탑동광장",
                            subtitle = "빛과제주의 일몰이 아름다운 장소. 해안가를 걸으며, 노을 촬영에 최적입니다.",
                            imageRes = R.drawable.jeju_course_sample
                        )
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()  // ★ 이 영역 안에서 스크롤
                    ) {
                        items(courses) { c ->
                            WideOverlayCourseCard(item = c)
                        }
                    }
                } else {
                    // [장소] — 카테고리 칩 + 2열 카드 그리드
                    var selectedCat by remember { mutableStateOf("자연환경") }
                    val categories = listOf("자연환경", "맛집", "카페", "박물관")

                    Spacer(Modifier.height(12.dp))

                    val places = listOf(
                        PlaceCardData(
                            title = "제주시청 감성 산책 코스",
                            subtitle = "코스 포인트",
                            tags = listOf("사진 촬영", "디저트", "가벼운산책"),
                            imageRes = R.drawable.jeju_place_sample
                        ),
                        PlaceCardData(
                            title = "제주시청 감성 산책 코스",
                            subtitle = "코스 포인트",
                            tags = listOf("사진 촬영", "디저트", "가벼운산책"),
                            imageRes = R.drawable.jeju_place_sample
                        ),
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            CategoryChips(
                                categories = categories,
                                selected = selectedCat,
                                onSelect = { selectedCat = it }
                            )
                        }

                        items(places.chunked(2)) { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 왼쪽 카드
                                Box(Modifier.weight(1f)) { PlaceGridCard(item = row[0]) }

                                // 오른쪽 카드(있으면)
                                if (row.size > 1) {
                                    Box(Modifier.weight(1f)) { PlaceGridCard(item = row[1]) }
                                } else {
                                    // 홀수 개일 때 균형 맞춤
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

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                shape = MaterialTheme.shapes.large,
                color = Color(0xFFF9F9F9)),
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
private fun ChargerSummaryCard(blocks: List<Block>) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9F9F9)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            blocks.forEach { b ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = b.label,
                        style = BoxTextStyle,
                        color = Variables.Grayscale600)
                    // "충전가능 a / t"
                    Text(
                        text = "${mapStatus("2")} ${b.available} / ${b.total}",
                        style = BoxTextStyle,
                        color = Variables.Blue700
                    )
                }
            }
        }
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

/* ----------------------- 탭 전용 UI ----------------------- */

// 추천 코스: 가로 꽉찬 큰 이미지 카드(그라데이션 오버레이 + 텍스트)
@Composable
private fun WideOverlayCourseCard(item: CourseCardData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // 좌측 하단이 더 어두운 그라데이션
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0x99000000), Color(0x33000000), Color.Transparent)
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFEFEFEF))
                )
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
                        style = MaterialTheme.typography.labelMedium
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
private fun PlaceGridCard(item: PlaceCardData) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
    ) {
        Column {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(item.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(item.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.tags.joinToString(", "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}