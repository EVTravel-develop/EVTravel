// 📁 com.jeju.evtravel.ui.planner.CalendarScreen.kt
package com.jeju.evtravel.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun CalendarScreen(
    viewModel: PlannerViewModel,
    onNextClick: () -> Unit = {}
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.dayOfWeek.value % 7

    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }

    val totalGridItems = firstDayOfWeek + daysInMonth
    val rows = (totalGridItems / 7) + if (totalGridItems % 7 > 0) 1 else 0
    val dates = List(rows * 7) { index ->
        val day = index - firstDayOfWeek + 1
        if (day in 1..daysInMonth) currentMonth.withDayOfMonth(day) else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("여행기간을 선택해주세요", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("요일별로 여행지를 추가할 수 있어요")
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "이전 달")
            }
            Text("${currentMonth.year}년 ${currentMonth.monthValue}월", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "다음 달")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                Text(it, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
            }
        }

        for (week in dates.chunked(7)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val isSelected = date != null &&
                            (date == selectedStartDate || date == selectedEndDate ||
                                    (selectedStartDate != null && selectedEndDate != null &&
                                            !date.isBefore(selectedStartDate) && !date.isAfter(selectedEndDate)))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable(enabled = date != null) {
                                if (selectedStartDate == null || selectedEndDate != null) {
                                    selectedStartDate = date
                                    selectedEndDate = null
                                } else if (date != null && date.isAfter(selectedStartDate)) {
                                    selectedEndDate = date
                                } else {
                                    selectedStartDate = date
                                    selectedEndDate = null
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date?.dayOfMonth?.toString() ?: "",
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 다음 버튼
        Button(
            onClick = {
                selectedStartDate?.let { start ->
                    selectedEndDate?.let { end ->
                        viewModel.setDateRange(start, end)
                        onNextClick()
                    }
                }
            },
            enabled = selectedStartDate != null && selectedEndDate != null
        ) {
            Text("다음")
        }
    }
}
