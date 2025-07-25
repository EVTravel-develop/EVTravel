package com.jeju.evtravel.ui.planner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.burnoutcrew.reorderable.*
import java.time.format.DateTimeFormatter

/**
 * 여행 일정을 편집하는 화면
 *
 * @param viewModel 플래너 뷰모델
 * @param onBackClick 뒤로가기 버튼 클릭 시 실행될 콜백
 */
@Composable
fun EditPlanScreen(
    viewModel: PlannerViewModel,
    onBackClick: () -> Unit,
    onEditDateClick: () -> Unit, // 날짜 편집 버튼 클릭 시 실행될 콜백
    onAddDestinationClick: () -> Unit // 여행지 추가 버튼 클릭 시 실행될 콜백

) {
    // 뷰모델에서 여행 시작일과 종료일을 상태로 가져옴
    val startDate = viewModel.startDate.collectAsState().value
    val endDate = viewModel.endDate.collectAsState().value
    val dayPlans by viewModel.dayPlans.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    // 날짜 포맷터
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")

    val reorderableState = rememberReorderableLazyListState(onMove = { from, to ->
        val date = selectedDate
        if (date != null) {
            viewModel.reorderPlaces(date, from.index, to.index)
        }
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 헤더 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBackClick() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
            Text("플래너", style = MaterialTheme.typography.headlineSmall)
        }

        // 여행 기간 표시 및 편집 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (startDate != null && endDate != null)
                    "${startDate.format(formatter)} ~ ${endDate.format(formatter)}"
                else "여행 기간을 선택해주세요",
                style = MaterialTheme.typography.bodyLarge
            )
            TextButton(onClick = { onEditDateClick() }) {
                Text("편집")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 날짜 리스트
        LazyColumn(
            state = reorderableState.listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .reorderable(reorderableState)
                .detectReorderAfterLongPress(reorderableState)
        ) {
            itemsIndexed(dayPlans, key = { _, dayPlan -> dayPlan.date }) { _, dayPlan ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // 날짜 박스
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (selectedDate == dayPlan.date)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                )
                            )
                            .padding(8.dp)
                            .clickable { viewModel.setSelectedDate(dayPlan.date) }
                    ) {
                        Text("${dayPlan.date} (${
                            java.time.LocalDate.parse(dayPlan.date).dayOfWeek.name.take(1)
                        })")
                    }

                    // 선택된 날짜의 장소 목록 (드래그 없이 단순 표시)
                    if (selectedDate == dayPlan.date && dayPlan.places.isNotEmpty()) {
                        Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                            dayPlan.places.forEach { place ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "• ${place.name}",
                                        modifier = Modifier.weight(1f) // 남는 공간 차지해서 X 버튼 안 밀리게
                                    )
                                    IconButton(
                                        onClick = { viewModel.removePlaceFromDate(dayPlan.date, place.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "삭제",
                                            tint = Color.Red,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "여행지 주변 충전소까지 한 번에!\n여행지를 추가하면 근처 충전소도 확인 가능해요."
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { onAddDestinationClick() }, enabled = selectedDate != null) {
                Text("여행지 추가")
            }
            Button(
                onClick = {
                    if (startDate != null && endDate != null) {
                        viewModel.saveCurrentPlan(
                            start = startDate.toString(),
                            end = endDate.toString()
                        )
                    }
                },
                enabled = startDate != null && endDate != null
            ) {
                Text("저장")
            }
        }
    }
}