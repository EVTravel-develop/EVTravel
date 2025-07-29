package com.jeju.evtravel.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jeju.evtravel.R
import com.jeju.evtravel.ui.theme.Variables
import java.time.format.DateTimeFormatter
import org.burnoutcrew.reorderable.*

/**
 * 여행 일정을 편집하는 화면
 *
 * @param viewModel 플래너 뷰모델
 * @param onBackClick 뒤로가기 버튼 클릭 시 실행될 콜백
 */
@Composable
fun EditPlanScreen(
    viewModel: PlannerViewModel,
    navController: NavController,
    onBackClick: () -> Unit,
    onEditDateClick: () -> Unit, // 날짜 편집 버튼 클릭 시 실행될 콜백
    onAddDestinationClick: () -> Unit // 여행지 추가 버튼 클릭 시 실행될 콜백

) {
    // 뷰모델에서 여행 시작일과 종료일을 상태로 가져옴
    val startDate = viewModel.startDate.collectAsState().value
    val endDate = viewModel.endDate.collectAsState().value
    val dayPlans by viewModel.dayPlans.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val hasAnyPlace = dayPlans.any { it.places.isNotEmpty() }
    // 날짜 포맷터
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")

    val reorderableState = rememberReorderableLazyListState(onMove = { from, to ->
        val date = selectedDate
        if (date != null) {
            viewModel.reorderPlaces(date, from.index, to.index)
        }
    })

    LaunchedEffect(dayPlans) {
        if (selectedDate == null && dayPlans.isNotEmpty()) {
            viewModel.setSelectedDate(dayPlans.first().date)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(84.dp))

        // 상단 헤더 (뒤로가기)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBackClick() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
        }

        // 여행 기간 + 편집
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "여행 기간",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    color = Color.Black
                )
            )
            TextButton(onClick = { onEditDateClick() }) {
                Text(
                    "편집",
                    style = TextStyle(color = Color(0xFF2563EB), fontSize = 16.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (startDate != null && endDate != null) {
            Text(
                text = "${startDate.format(formatter)} ~ ${endDate.format(formatter)}",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    color = Color.Black
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 여행 일정 제목
        Text(
            text = "여행 일정",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.roboto)),
                color = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 날짜 선택 바 (LazyRow)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dayPlans) { dayPlan ->
                val localDate = java.time.LocalDate.parse(dayPlan.date)
                val dayOfWeek = when (localDate.dayOfWeek) {
                    java.time.DayOfWeek.MONDAY -> "월"
                    java.time.DayOfWeek.TUESDAY -> "화"
                    java.time.DayOfWeek.WEDNESDAY -> "수"
                    java.time.DayOfWeek.THURSDAY -> "목"
                    java.time.DayOfWeek.FRIDAY -> "금"
                    java.time.DayOfWeek.SATURDAY -> "토"
                    java.time.DayOfWeek.SUNDAY -> "일"
                }
                val isSelected = selectedDate == dayPlan.date

                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(24.dp)
                        .background(
                            color = if (isSelected) Variables.Blue700 else Color(0xFFF4F4F5),
                            shape = RoundedCornerShape(5.dp)
                        )
                        .clickable { viewModel.setSelectedDate(dayPlan.date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${localDate.dayOfMonth}일 ($dayOfWeek)",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.roboto)),
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) Color.White else Variables.Grayscale300
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 선택된 날짜의 장소 리스트 (Frame 스타일 적용)
        LazyColumn(
            state = reorderableState.listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .reorderable(reorderableState)
                .detectReorderAfterLongPress(reorderableState)
        ) {
            itemsIndexed(dayPlans, key = { _, plan -> plan.date }) { _, dayPlan ->
                if (selectedDate == dayPlan.date && dayPlan.places.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        dayPlan.places.forEach { place ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(vertical = 6.dp)
                                    .background(Color.White, shape = RoundedCornerShape(10.dp))
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(10.dp),
                                        ambientColor = Color(0x40A7A7A7),
                                        spotColor = Color(0x40A7A7A7)
                                    )
                            ) {
                                // 메뉴 아이콘 (왼쪽)
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_menu),
                                    contentDescription = "메뉴",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = 12.dp)
                                        .size(18.dp)
                                )

                                // 장소명 (중앙)
                                Text(
                                    text = place.name,
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily(Font(R.font.roboto)),
                                        color = Color(0xFF1C1917)
                                    ),
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = 48.dp)
                                )

                                // 충전소 현황 아이콘 (오른쪽 중간)
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_battery_charge),
                                    contentDescription = "충전 현황",
                                    tint = Color(0xFF9D9D9D),
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 56.dp)
                                        .size(20.dp)
                                )

                                // 삭제 버튼 (맨 오른쪽 X)
                                IconButton(
                                    onClick = { viewModel.removePlaceFromDate(dayPlan.date, place.id) },
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 16.dp)
                                        .size(20.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = "삭제",
                                        tint = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!hasAnyPlace) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "여행지 주변 충전소까지 한 번에!",
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    color = Color.Black,
                    lineHeight = 24.sp
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "여행지를 추가하고, 근처 전기차 충전소까지\n담으러 가볼까요?",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    color = Color(0xFF707070),
                    lineHeight = 19.5.sp
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // 여행지 추가 버튼
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable { onAddDestinationClick() }
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                "여행지 추가",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    color = Color(0xFF2563EB)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 저장 버튼
        Button(
            onClick = {
                if (startDate != null && endDate != null) {
                    viewModel.saveCurrentPlan(startDate.toString(), endDate.toString())
                    navController.navigate("planList/${startDate}/${endDate}") {
                        popUpTo("planList") { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            enabled = startDate != null && endDate != null
        ) {
            Text("저장")
        }
    }
}