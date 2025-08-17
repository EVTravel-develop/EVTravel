package com.jeju.evtravel.ui.planner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.jeju.evtravel.ui.theme.Variables
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.time.LocalDate
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
    planId: String?,
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
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    
    val reorderableState = rememberReorderableLazyListState(onMove = { from, to ->
        val date = selectedDate
        if (date != null) {
            viewModel.reorderPlaces(date, from.index, to.index)
        }
    })
    var expandedPlaceId by remember { mutableStateOf<String?>(null) }
    
    // 화면 진입 시 플랜 ID가 있다면 해당 플랜을 로드합니다.
    LaunchedEffect(planId) {
        if (planId != null) {
            // ?. 를 추가하여 plans.value가 null이 아닐 때만 find를 실행합니다.
            val planToLoad = viewModel.plans.value?.find { it.id == planId }
            if (planToLoad != null) {
                viewModel.loadPlanDetails(planToLoad)
            }
        }
    }
    
    LaunchedEffect(dayPlans) {
        if (selectedDate == null && dayPlans.isNotEmpty()) {
            viewModel.setSelectedDate(dayPlans.first().date)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .padding(start = 24.dp, top = 84.dp)
                .size(22.dp)
                .clickable { onBackClick() },
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
                .padding(top = 136.dp, start = 24.dp, end = 24.dp, bottom = 16.dp) // 하단 패딩 수정
        ) {
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
                        text = "편집",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily(Font(R.font.roboto)),
                            color = Color(0xFF0173FF)
                        )
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "여행 일정",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight(700),
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    color = Color.Black
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(dayPlans) { dayPlan ->
                    val localDate = LocalDate.parse(dayPlan.date)
                    val dayOfWeek =
                        listOf("월", "화", "수", "목", "금", "토", "일")[localDate.dayOfWeek.ordinal]
                    val isSelected = selectedDate == dayPlan.date
                    Box(
                        modifier = Modifier
                            .width(86.dp)
                            .height(36.dp)
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
            
            if (!hasAnyPlace) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // 남은 공간 차지
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "여행지 주변 충전소까지 한 번에!",
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily(Font(R.font.roboto)),
                            color = Color.Black,
                            lineHeight = 24.sp
                        )
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
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(59.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(size = 10.dp)
                            )
                            .clickable { onAddDestinationClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_add_plan_spot_small),
                            contentDescription = "여행지 추가",
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = reorderableState.listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // 남은 공간을 차지하도록 weight 추가
                        .reorderable(reorderableState)
                        .detectReorderAfterLongPress(reorderableState)
                ) {
                    itemsIndexed(dayPlans, key = { _, plan -> plan.date }) { _, dayPlan ->
                        if (selectedDate == dayPlan.date && dayPlan.places.isNotEmpty()) {
                            Column {
                                dayPlan.places.forEach { place ->
                                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(60.dp)
                                                .shadow(
                                                    elevation = 4.dp,
                                                    shape = RoundedCornerShape(10.dp),
                                                    ambientColor = Color(0x40A7A7A7),
                                                    spotColor = Color(0x40A7A7A7)
                                                )
                                                .background(
                                                    Color.White,
                                                    shape = RoundedCornerShape(10.dp)
                                                ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_menu),
                                                contentDescription = "메뉴",
                                                tint = Color.Black,
                                                modifier = Modifier
                                                    .padding(start = 16.dp)
                                                    .size(18.dp)
                                            )
                                            
                                            Text(
                                                text = place.name,
                                                style = TextStyle(
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontFamily = FontFamily(Font(R.font.roboto)),
                                                    color = Color(0xFF1C1917)
                                                ),
                                                modifier = Modifier
                                                    .weight(1f) // Row 내부에서 weight 사용
                                                    .padding(start = 16.dp)
                                            )
                                            
                                            val chargerIconId = if (expandedPlaceId == place.id) {
                                                R.drawable.ic_charger_on // 선택 시 ic_charger_on
                                            } else {
                                                R.drawable.ic_charger_off // 미선택 시 ic_charger_off
                                            }
                                            
                                            Icon(
                                                painter = painterResource(id = chargerIconId),
                                                contentDescription = "충전 현황",
                                                tint = if (expandedPlaceId == place.id) Color(
                                                    0xFF000000
                                                ) else Color(0xFF9D9D9D),
                                                modifier = Modifier
                                                    .padding(end = 12.dp)
                                                    .size(20.dp)
                                                    .clickable {
                                                        expandedPlaceId =
                                                            if (expandedPlaceId == place.id) null else place.id
                                                    }
                                            )
                                            IconButton(
                                                onClick = {
                                                    viewModel.removePlaceFromDate(
                                                        dayPlan.date,
                                                        place.id
                                                    )
                                                },
                                                modifier = Modifier
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
                                        
                                        // 충전소 목록
                                        if (expandedPlaceId == place.id && !place.chargers.isNullOrEmpty()) {
                                            Column(
                                                modifier = Modifier
                                                    .padding(start = 8.dp, end = 0.dp)
                                                    .offset(y = (-4).dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .shadow(
                                                            elevation = 4.dp,
                                                            spotColor = Color(0x40A7A7A7),
                                                            ambientColor = Color(0x40A7A7A7),
                                                            shape = RoundedCornerShape(
                                                                bottomStart = 10.dp,
                                                                bottomEnd = 10.dp
                                                            )
                                                        )
                                                        .background(
                                                            color = Color(0xFFFFFFFF),
                                                            shape = RoundedCornerShape(
                                                                bottomStart = 10.dp,
                                                                bottomEnd = 10.dp
                                                            )
                                                        )
                                                        .padding(
                                                            vertical = 12.dp,
                                                            horizontal = 16.dp
                                                        )
                                                ) {
                                                    Column {
                                                        place.chargers!!.forEachIndexed { index, charger ->
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = charger.name,
                                                                    style = TextStyle(
                                                                        fontSize = 14.sp,
                                                                        fontFamily = FontFamily(
                                                                            Font(
                                                                                R.font.roboto
                                                                            )
                                                                        ),
                                                                        fontWeight = FontWeight(400),
                                                                        color = Color(0xFF000000),
                                                                    )
                                                                )
                                                            }
                                                            
                                                            // 마지막 아이템이 아닌 경우에만 구분선 추가
                                                            if (index < place.chargers.size - 1) {
                                                                Spacer(modifier = Modifier.height(8.dp))
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .height(0.5.dp)
                                                                        .background(Color(0xFFDBDBDB))
                                                                )
                                                                Spacer(modifier = Modifier.height(8.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 하단 버튼 영역을 LazyColumn과 분리하여 Column 내부에서 관리
            Spacer(modifier = Modifier.height(12.dp))
            if (hasAnyPlace) {
                // 여행지 추가 버튼
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(59.dp)
                        .background(
                            color = Color(0xFFE9E9E9),
                            shape = RoundedCornerShape(size = 10.dp)
                        )
                        .clickable { onAddDestinationClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = " + 여행지 추가하기",
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            fontFamily = FontFamily(Font(R.font.roboto)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFF9D9D9D),
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // "다음" 버튼
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(59.dp)
                        .background(
                            color = Color(0xFF0173FF),
                            shape = RoundedCornerShape(size = 10.dp)
                        )
                        .clickable {
                            if (startDate != null && endDate != null) {
                                
                                // 1. 화면 전환 로직을 onSaveComplete 라는 이름의 람다로 정의
                                val onSaveComplete = {
                                    navController.navigate("planList?start=${startDate}&end=${endDate}") {
                                        popUpTo("planList") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                                
                                // 2. ViewModel 함수를 호출하며 위에서 정의한 람다를 전달
                                viewModel.saveCurrentPlan(
                                    start = startDate.toString(),
                                    end = endDate.toString(),
                                    onSaveComplete = onSaveComplete
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "다음",
                        style = TextStyle(
                            fontSize = 17.sp,
                            lineHeight = 24.sp,
                            fontFamily = FontFamily(Font(R.font.roboto)),
                            fontWeight = FontWeight(600),
                            color = Color(0xFFFFFFFF),
                            letterSpacing = 0.21.sp,
                        )
                    )
                }
            }
        }
    }
}