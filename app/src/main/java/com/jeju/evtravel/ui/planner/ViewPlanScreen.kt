package com.jeju.evtravel.ui.planner

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.jeju.evtravel.R
import com.jeju.evtravel.ui.theme.Variables
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController

/**
 * 여행 일정을 조회하는 화면 (읽기 전용)
 *
 * @param viewModel 플래너 뷰모델
 * @param onBackClick 뒤로가기 버튼 클릭 시 실행될 콜백
 */
@Composable
fun ViewPlanScreen(
    viewModel: PlannerViewModel,
    planId: String?,
    onBackClick: () -> Unit,
) {
    val TAG = "PlannerDebug"
    
    // 뷰모델에서 여행 시작일과 종료일을 상태로 가져옴
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val dayPlans by viewModel.dayPlans.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    // 날짜 포맷터
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    
    // 충전소 목록 확장 상태
    var expandedPlaceId by remember { mutableStateOf<String?>(null) }
    
    // 화면 진입 시 플랜 ID가 있다면 해당 플랜을 로드합니다.
    LaunchedEffect(planId) {
        Log.d(
            TAG,
            "ViewPlanScreen: LaunchedEffect triggered. Received planId is '$planId'."
        )
        
        if (planId != null) {
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
                .padding(top = 136.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            // "여행 기간" 헤더
            Text(
                text = "여행 기간",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    color = Color.Black
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            startDate?.let { start ->
                endDate?.let { end ->
                    Text(
                        text = "${start.format(formatter)} ~ ${end.format(formatter)}",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.roboto)),
                            color = Color.Black
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // "여행 일정" 헤더
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
            
            // 날짜 선택 버튼 LazyRow
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
            
            // 여행지 목록
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 남은 공간을 차지하도록 weight 추가
            ) {
                dayPlans.find { it.date == selectedDate }?.let { dayPlan ->
                    items(dayPlan.places) { place ->
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
                                // "메뉴" 아이콘 대신 더미 Box 또는 다른 아이콘 사용
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
                                        .weight(1f)
                                        .padding(start = 16.dp)
                                )
                                
                                val chargerIconId = if (expandedPlaceId == place.id) {
                                    R.drawable.ic_charger_on
                                } else {
                                    R.drawable.ic_charger_off
                                }
                                
                                Icon(
                                    painter = painterResource(id = chargerIconId),
                                    contentDescription = "충전 현황",
                                    tint = if (expandedPlaceId == place.id) Color(0xFF000000) else Color(0xFF9D9D9D),
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .size(20.dp)
                                        .clickable {
                                            expandedPlaceId =
                                                if (expandedPlaceId == place.id) null else place.id
                                        }
                                )
                            }
                            
                            // 충전소 목록 (EditPlanScreen과 동일)
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
                                                                Font(R.font.roboto)
                                                            ),
                                                            fontWeight = FontWeight(400),
                                                            color = Color(0xFF000000),
                                                        )
                                                    )
                                                }
                                                
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