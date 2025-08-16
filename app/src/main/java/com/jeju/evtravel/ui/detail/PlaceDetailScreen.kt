package com.jeju.evtravel.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jeju.evtravel.domain.model.Place

@Composable
fun PlaceDetailScreen(
    place: Place,
    isFullScreen: Boolean = false,
    onNavigateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isFullScreen) Modifier.padding(24.dp)
                else Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
    ) {
        Text(
            text = place.name,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val slow = place.chargerList.filter {
                it.output.toDoubleOrNull()?.let { out -> out < 50 } == true
            }
            val rapid = place.chargerList.filter {
                it.output.toDoubleOrNull()?.let { out -> out in 50.0..199.9 } == true
            }
            val ultraRapid = place.chargerList.filter {
                it.output.toDoubleOrNull()?.let { out -> out >= 200 } == true
            }

            val availableSlow = slow.count { it.status == "AVAILABLE" }
            val availableRapid = rapid.count { it.status == "AVAILABLE" }
            val availableUltraRapid = ultraRapid.count { it.status == "AVAILABLE" }

            val chargerColumns = listOfNotNull(
                if (slow.isNotEmpty()) "완속" to "$availableSlow/${slow.size}" else null,
                if (rapid.isNotEmpty()) "급속" to "$availableRapid/${rapid.size}" else null,
                if (ultraRapid.isNotEmpty()) "초급속" to "$availableUltraRapid/${ultraRapid.size}" else null
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                chargerColumns.forEach { (label, countText) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "충전가능 $countText",
                            color = Color.Blue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateClick
            ) {
                Text("안내하기")
            }

            // TODO: 추후 확장 시: 추천 코스 콘텐츠 / 장소 콘텐츠
//        Spacer(modifier = Modifier.height(24.dp))
//        Text("충전 시간 동안 주변을 둘러보세요", style = MaterialTheme.typography.titleSmall)
//        Text("추천 코스", style = MaterialTheme.typography.bodyMedium)
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        LazyRow {
//            items(2) {
//                Column(
//                    modifier = Modifier
//                        .width(180.dp)
//                        .padding(end = 12.dp)
//                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.jeju_course_sample), // 예시
//                        contentDescription = null,
//                        modifier = Modifier
//                            .height(100.dp)
//                            .fillMaxWidth()
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text("제주시청 감성 산책 코스", style = MaterialTheme.typography.bodySmall)
//                    Text("사진 촬영, 디저트, 가벼운 산책", style = MaterialTheme.typography.labelSmall)
//                }
//            }
//        }
        }
    }
}