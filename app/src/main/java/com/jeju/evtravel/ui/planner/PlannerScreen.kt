package com.jeju.evtravel.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 플래너 메인 화면 컴포저블
 * 새로운 여행 계획을 생성할 수 있는 시작 화면을 표시합니다.
 *
 * @param onCreatePlanClick 플랜 생성 버튼 클릭 시 실행될 콜백
 */
@Composable
fun PlannerScreen(
    onCreatePlanClick: () -> Unit
) {
    // 전체 화면을 차지하는 박스
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),  // 좌우 여백 24dp
        contentAlignment = Alignment.Center  // 중앙 정렬
    ) {
        // 세로로 정렬된 컨텐츠
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "새로운 여행을 계획해볼까요?",
                fontSize = 18.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "날짜를 선택하고 가고 싶은 여행지를 추가해,\n나만의 플랜을 만들어보세요",
                fontSize = 16.sp,
                color = Color.Gray,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onCreatePlanClick() },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text("플랜 생성", color = Color.Black)
            }
        }
    }
}