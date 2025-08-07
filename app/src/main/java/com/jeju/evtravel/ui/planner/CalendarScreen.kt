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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    // 현재 표시 중인 월 (매월 1일을 기준으로 초기화)
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    
    // 현재 월의 일 수
    val daysInMonth = currentMonth.lengthOfMonth()
    
    // 현재 월의 첫 번째 날의 요일 (0=일요일, ... 6=토요일)
    val firstDayOfWeek = currentMonth.dayOfWeek.value % 7
    
    // 사용자가 선택한 시작일과 종료일
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    
    // 한 달을 표시하기 위해 필요한 그리드 아이템 수 계산 (빈칸 + 날짜)
    val totalGridItems = firstDayOfWeek + daysInMonth
    // 전체 그리드 아이템 수를 7로 나누어 필요한 행 수 계산
    val rows = (totalGridItems / 7) + if (totalGridItems % 7 > 0) 1 else 0
    // 각 그리드 셀에 표시할 날짜 (빈칸은 null)
    val dates = List(rows * 7) { index ->
        val day = index - firstDayOfWeek + 1
        if (day in 1..daysInMonth) currentMonth.withDayOfMonth(day) else null
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        
        // 뒤로가기
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
                .padding(top = 136.dp, start = 24.dp, end = 24.dp, bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // 화면 헤더 텍스트
            Column(modifier = Modifier.fillMaxWidth()) { //왼쪽 정렬
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
            
            // 월 변경 버튼과 현재 월 표시
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
            
            // 요일 헤더
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                    Text(
                        it,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            // 주 단위로 날짜 그리드 출력
            for (week in dates.chunked(7)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        val isSelected = date != null &&
                                (date == selectedStartDate || date == selectedEndDate ||
                                        (selectedStartDate != null && selectedEndDate != null &&
                                                !date.isBefore(selectedStartDate) && !date.isAfter(
                                            selectedEndDate
                                        )))
                        
                        // 각 날짜 셀
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
                                    // 날짜 선택 로직: 시작일과 종료일 설정
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
            
            // "다음" 버튼: 선택된 날짜 범위를 ViewModel에 저장 후 EditPlanScreen으로 이동
            Box(
                modifier = Modifier
                    .fillMaxWidth()       // 전체 폭을 차지
                    .height(90.dp)        // 전체 높이를 차지
                    .clickable(           // 클릭도 Box 전체에
                        enabled = selectedStartDate != null && selectedEndDate != null
                    ) {
                        selectedStartDate?.let { start ->
                            selectedEndDate?.let { end ->
                                viewModel.setDateRange(start, end)
                                viewModel.initDayPlans(start, end)
                                onNextClick()
                            }
                        }
                    },
                contentAlignment = Alignment.Center  // 아이콘 중앙 정렬
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_planner_next),
                    contentDescription = "다음",
                    tint = Color.Unspecified,
                    modifier = Modifier.matchParentSize() // Box 크기 그대로 채우기
                )
            }
        }
    }
}