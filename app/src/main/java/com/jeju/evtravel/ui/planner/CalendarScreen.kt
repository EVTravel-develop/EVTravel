package com.jeju.evtravel.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jeju.evtravel.R
import java.time.LocalDate

/**
 * 여행 일정을 선택할 수 있는 캘린더 화면
 * 사용자가 여행 시작일과 종료일을 선택하고, 이후 EditPlanScreen으로 넘어가도록 구성
 *
 * - 월별 달력 렌더링
 * - 시작일과 종료일 범위 선택 가능
 * - 선택 완료 시 PlannerViewModel에 날짜 범위 저장 및 DayPlan 초기화
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: PlannerViewModel,
    navController: NavController,
    onNextClick: () -> Unit = {}
) {
    // 사용자가 선택한 시작일과 종료일
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    
    // 표시할 월 목록 생성 (예: 현재 기준 과거 24개월 ~ 미래 24개월)
    val current = LocalDate.now().withDayOfMonth(1)
    val months = remember { List(49) { i -> current.minusMonths(24).plusMonths(i.toLong()) } }
    
    // LazyColumn의 스크롤 상태를 추적
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = 24) // 현재 월에서 시작
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        // 뒤로가기 버튼
        Box(
            modifier = Modifier
                .padding(start = 24.dp, top = 84.dp)
                .size(22.dp)
                .clickable { navController.popBackStack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 136.dp), // 좌우 패딩은 화면 전체 Column에서 제거
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 화면 헤더 텍스트
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp) // 헤더 텍스트에만 좌우 패딩 적용
            ) {
                Text(
                    text = "여행 기간을 선택해주세요",
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    fontWeight = FontWeight.Bold, // FontWeight(700) = Bold 입니다.
                    color = Color.Black,
                    letterSpacing = 0.25.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "요일별로 여행지를 추가할 수 있어요.",
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    fontWeight = FontWeight.Normal, // FontWeight(400) = Normal 입니다.
                    color = Color(0xFF707070),
                    letterSpacing = 0.13.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .background(color = Color(0xFFF7F7F7))
            )
            
            // 월별 달력 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp) // 캘린더 영역에 다시 패딩 적용
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // 요일 헤더
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("일", "월", "화", "수", "목", "금", "토").forEach { dayOfWeek ->
                        Text(
                            text = dayOfWeek,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.roboto)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF000000),
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 스크롤 가능한 월별 달력
                LazyColumn(state = lazyListState) {
                    items(items = months, key = { it.toString() }) { month ->
                        Column {
                            // 월 표시 헤더 추가
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${month.year}.${String.format("%02d", month.monthValue)}",
                                    style = TextStyle(
                                        fontSize = 20.sp,
                                        lineHeight = 24.sp,
                                        fontFamily = FontFamily(Font(R.font.roboto)),
                                        fontWeight = FontWeight(700),
                                        color = Color(0xFF000000),
                                    )
                                )
                            }
                            MonthView(
                                month = month,
                                selectedStartDate = selectedStartDate,
                                selectedEndDate = selectedEndDate,
                                onDateClick = { date ->
                                    if (selectedStartDate == null || selectedEndDate != null) {
                                        selectedStartDate = date
                                        selectedEndDate = null
                                    } else if (date.isAfter(selectedStartDate)) {
                                        selectedEndDate = date
                                    } else {
                                        selectedStartDate = date
                                        selectedEndDate = null
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // "다음" 버튼
        if (selectedStartDate != null && selectedEndDate != null) {
            Button(
                onClick = {
                    selectedStartDate?.let { start ->
                        selectedEndDate?.let { end ->
                            viewModel.setDateRange(start, end)
                            viewModel.initDayPlans(start, end)
                            onNextClick()
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                    .height(60.dp),
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0173FF),
                    disabledContainerColor = Color(0xFFE5E5E5)
                ),
                shape = RoundedCornerShape(size = 10.dp)
            ) {
                Text(
                    text = "다음",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.roboto)),
                        fontWeight = FontWeight(700),
                        color = Color.White
                    )
                )
            }
        }
    }
}

/**
 * 한 달 치 달력을 그리는 Composable
 */
@Composable
fun MonthView(
    month: LocalDate,
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    // 해당 월의 첫 번째 날의 요일 (0=월요일 ... 6=일요일)
    // 자바의 DayOfWeek는 월요일(1)부터 시작하므로 % 7을 하여 일요일(0)부터 시작하도록 조정
    val firstDayOfWeek = month.withDayOfMonth(1).dayOfWeek.value % 7
    
    val daysInMonth = month.lengthOfMonth()
    
    // 이전 달의 마지막 날짜들을 계산
    val prevMonth = month.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()
    val prevMonthDays = (daysInPrevMonth - firstDayOfWeek + 1..daysInPrevMonth).map { prevMonth.withDayOfMonth(it) }
    
    // 다음 달의 첫 날짜들을 계산
    val nextMonth = month.plusMonths(1)
    val totalGridItems = firstDayOfWeek + daysInMonth
    val nextMonthDaysCount = if (totalGridItems % 7 > 0) 7 - (totalGridItems % 7) else 0
    val nextMonthDays = (1..nextMonthDaysCount).map { nextMonth.withDayOfMonth(it) }
    
    // 모든 날짜를 하나의 리스트로 합치기
    val allDaysInView = prevMonthDays + (1..daysInMonth).map { month.withDayOfMonth(it) } + nextMonthDays
    
    // 달력 그리드 생성
    Column {
        val rows = (allDaysInView.size / 7)
        
        for (week in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (day in 0..6) {
                    val date = allDaysInView[week * 7 + day]
                    val dayOfMonth = date.dayOfMonth
                    
                    val isCurrentMonth = date.year == month.year && date.month == month.month
                    val isStart = date == selectedStartDate
                    val isEnd = date == selectedEndDate
                    val isInRange = selectedStartDate != null && selectedEndDate != null &&
                            date.isAfter(selectedStartDate) && date.isBefore(selectedEndDate)
                    
                    // 날짜 셀 UI
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .background(
                                color = if (isInRange) Color(0x1A0173FF) else Color.Transparent,
                            )
                            .clickable {
                                if (isCurrentMonth) {
                                    onDateClick(date)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    color = if (isStart || isEnd) Color(0xFF0173FF) else Color.Transparent,
                                    shape = RoundedCornerShape(size = 5.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val textColor = when {
                                isStart || isEnd -> Color.White
                                !isCurrentMonth -> Color(0xFFC2C2C2) // 이전/다음 달 날짜 색상
                                else -> Color(0xFF000000)
                            }
                            Text(
                                text = dayOfMonth.toString(),
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}