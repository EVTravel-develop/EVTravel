package com.jeju.evtravel.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.ui.theme.Variables

data class ChargerRowUi(
    val label: String,      // 예) "100 kWDC콤보"
//    val priceText: String,  // 예) "347.2원/kWh"
    val available: Int,
    val total: Int
)

fun isCharging(status: String): Boolean =
    status == "3" || status.equals("CHARGING", ignoreCase = true)

@Composable
fun ChargerDetailScreen(
    place: Place,                       // place.chargerList 사용
    onNavigateClick: () -> Unit
) {
    val chargers = place.chargerList ?: emptyList()

    // 샘플 변환: 출력(W)과 커넥터를 합쳐 라벨링하고 가격/가용수 계산
    val rows: List<ChargerRowUi> = chargers
        .groupBy { ch ->
            // 출력 버킷 + 커넥터명(있으면)로 묶기
            val kw = ch.output?.toDoubleOrNull()?.toInt() ?: 0
            val bucket = when {
                kw >= 100 -> "${kw} kWDC콤보"
                kw in 50..99 -> "${kw} kWDC콤보"
                else -> "${kw} kWAC"
            }
            bucket
        }
        .map { (bucket, list) ->
            val total = list.size
            val avail = list.count { !isCharging(it.status) }
//            val price = list.firstOrNull()?.price // 프로젝트 필드명에 맞게 조정
            ChargerRowUi(
                label = bucket,
//                priceText = if (price != null) "${price}원/kWh" else "-원/kWh",
                available = avail,
                total = total
            )
        }
        .sortedByDescending { it.total }

    Scaffold(
        floatingActionButton = { ActionFAB(onClick = onNavigateClick) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            HeaderImage(imageUrl = " ")
            Spacer(Modifier.height(12.dp))

            Text(
                text = place.name,
                style = DetailTitleTextStyle,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(10.dp))

            InfoCard(
                hours = chargers.firstOrNull()?.usageTime ?: "24시간 이용가능",
                address = place.address,
                phone = place.phone
            )

            Spacer(Modifier.height(18.dp))

            SectionTitle(
                title = "충전기 정보",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(10.dp))

            // 행 카드들
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rows.forEach { r ->
                    ChargerRow(r)
                }

                if (rows.isEmpty()) {
                    Text(
                        "등록된 충전기 정보가 없습니다.",
                        style = DetailSubtitleTextStyle,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ChargerRow(row: ChargerRowUi) {
    // 스크린샷처럼 왼쪽: 사양/요금, 오른쪽: 상태(색상)
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
                    style = DetailLabelTextStyle.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            val canUse = row.available > 0
            val rightText = if (canUse) "충전가능 ${row.available}/${row.total}"
            else "충전불가 ${row.available}/${row.total}"
            val rightColor = if (canUse) Variables.Blue700 else Color(0xFFD92B2B)
            Text(text = rightText, color = rightColor, style = DetailLabelTextStyle)
        }
    }
}
