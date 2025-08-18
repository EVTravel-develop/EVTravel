package com.jeju.evtravel.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jeju.evtravel.data.util.mapStatus
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.R

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

@Composable
fun PlaceChargerDetailScreen(
    place: Place,
    isFullScreen: Boolean = false,
    isLoading: Boolean = false,     // 로딩
    onRetry: () -> Unit = {},       // 새로고침/다시 시도
    onNavigateClick: () -> Unit
) {
    val chargers = place.chargerList ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // 타이틀
        Text(text = place.name, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        // 로딩
        if (isLoading) {
            LoadingCard()
            return
        }

        // 2) 로딩은 끝났는데 데이터가 비어있는 상태 (0, null, 빈 리스트)
        if (chargers.isEmpty()) {
            EmptyChargerCard(
                title = "충전기 정보를 찾지 못했어요",
                subtitle = "이 장소에 등록된 충전기가 없거나, 잠시 정보를 불러오지 못했을 수 있어요.",
                primaryText = "다시 시도",
                onPrimary = onRetry,
                secondaryText = "다른 장소 보기",
                onSecondary = onNavigateClick
            )
            return
        }

        fun isCharging(code: String?) = code == "3" || code.equals("CHARGING", ignoreCase = true)

        // 속도별 그룹핑
        val slow = chargers.filter { it.output.toDoubleOrNull()?.let { out -> out < 50 } == true }
        val rapid = chargers.filter { it.output.toDoubleOrNull()?.let { out -> out in 50.0..199.9 } == true }
        val ultra = chargers.filter { it.output.toDoubleOrNull()?.let { out -> out >= 200 } == true }


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

        Spacer(Modifier.height(16.dp))

        // 안내하기 버튼
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("안내하기")
        }

        Spacer(Modifier.height(20.dp))

        // 섹션 타이틀
        Text(
            text = "충전 시간 동안 주변을 둘러보세요",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
        )

        Spacer(Modifier.height(12.dp))

        // 탭
        var tabIndex by remember { mutableStateOf(0) }
        val tabs = listOf("추천 코스", "장소")
        TabRow(selectedTabIndex = tabIndex, indicator = {}, divider = { Divider(color = Color(0x11000000)) }) {
            tabs.forEachIndexed { i, title ->
                Tab(
                    selected = tabIndex == i,
                    onClick = { tabIndex = i },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (tabIndex == i) FontWeight.SemiBold else FontWeight.Normal
                            )
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // 가로 카드 리스트
        if (tabIndex == 0) {
            val sample = listOf(
                CourseCardData(
                    title = "제주시청 감성 산책 코스",
                    subtitle = "사진 촬영, 디저트, 가벼운 산책",
                    imageRes = R.drawable.jeju_course_sample
                ),
                CourseCardData(
                    title = "제주시청 감성 산책 코스",
                    subtitle = "사진 촬영, 디저트, 가벼운 산책",
                    imageRes = R.drawable.jeju_course_sample
                )
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sample) { item -> CourseCard(item) }
            }
        } else {
            Text(
                text = "주변 장소가 곧 제공됩니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ChargerSummaryCard(blocks: List<Block>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            blocks.forEach { b ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(b.label, style = MaterialTheme.typography.bodySmall)
                    // "충전가능 a / t"
                    Text(
                        text = "${mapStatus("2")} ${b.available} / ${b.total}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.Blue
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
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onPrimary, shape = RoundedCornerShape(12.dp)) {
                    Text(primaryText)
                }
                if (secondaryText != null && onSecondary != null) {
                    OutlinedButton(onClick = onSecondary, shape = RoundedCornerShape(12.dp)) {
                        Text(secondaryText)
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCard(item: CourseCardData) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = item.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
