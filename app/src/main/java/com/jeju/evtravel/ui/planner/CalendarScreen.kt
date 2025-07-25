package com.jeju.evtravel.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDate

/**
 * 여행 일정을 선택할 수 있는 캘린더 화면 컴포저블
 * 
 * @param viewModel 플래너 뷰모델
 * @param navController 네비게이션 컨트롤러
 * @param onNextClick 다음 버튼 클릭 시 실행될 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: PlannerViewModel,
    navController: NavController,
    onNextClick: () -> Unit = {}
) {
    // 현재 표시 중인 월 (매월 1일로 설정)
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    // 현재 월의 일수
    val daysInMonth = currentMonth.lengthOfMonth()
    // 현재 월의 첫 날 요일 (0=일요일, 1=월요일, ..., 6=토요일)
    val firstDayOfWeek = currentMonth.dayOfWeek.value % 7

    // 선택된 시작일과 종료일 상태
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }

    // 캘린더 그리드를 위한 계산
    val totalGridItems = firstDayOfWeek + daysInMonth  // 첫 주 빈 칸 + 일수
    val rows = (totalGridItems / 7) + if (totalGridItems % 7 > 0) 1 else 0  // 필요한 행 수 계산
    
    // 캘린더에 표시할 날짜 리스트 생성 (null은 빈 칸)
    val dates = List(rows * 7) { index ->
        val day = index - firstDayOfWeek + 1
        if (day in 1..daysInMonth) currentMonth.withDayOfMonth(day) else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "이전 달"
                    )
                }
                Text(
                    "${currentMonth.year}년 ${currentMonth.monthValue}월",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "다음 달"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                    Text(
                        it,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall
                    )
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

            Button(
                onClick = {
                    selectedStartDate?.let { start ->
                        selectedEndDate?.let { end ->
                            viewModel.setDateRange(start, end)
                            viewModel.initDayPlans(start, end) // 날짜별 일정 초기화
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
}