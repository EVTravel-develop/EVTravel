package com.jeju.evtravel.ui.planner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    onBackClick: () -> Unit
) {
    // 뷰모델에서 여행 시작일과 종료일을 상태로 가져옴
    val startDate = viewModel.startDate.collectAsState().value
    val endDate = viewModel.endDate.collectAsState().value

    // 날짜 포맷터
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")

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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
            Text(
                "플래너",
                style = MaterialTheme.typography.headlineSmall
            )
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
            TextButton(onClick = { /* TODO: 다시 캘린더로 이동 */ }) {
                Text("편집")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 날짜별 버튼
        startDate?.let { start ->
            endDate?.let { end ->
                val days = buildList {
                    var date = start
                    while (!date.isAfter(end)) {
                        add(date)
                        date = date.plusDays(1)
                    }
                }

                println("생성된 날짜 목록: $days") // 디버그용 로그

                Row {
                    days.forEach { date ->
                        Text(
                            text = "${date.dayOfMonth}일 (${date.dayOfWeek.name.take(1)})",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline))
                                .padding(8.dp)
                        )
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
            Button(onClick = { /* TODO: 여행지 추가 */ }) {
                Text("여행지 추가")
            }
            Button(
                onClick = {
                    if (startDate != null && endDate != null) {
                        val days = generateSequence(startDate) { current ->
                            val next = current.plusDays(1)
                            if (!next.isAfter(endDate)) next else null
                        }.toList()

                        viewModel.saveCurrentPlan(
                            start = startDate.toString(), // "2025-07-04"
                            end = endDate.toString(),
                            days = days.map { it.toString() }
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