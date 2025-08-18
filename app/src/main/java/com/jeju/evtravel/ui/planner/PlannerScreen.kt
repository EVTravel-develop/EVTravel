package com.jeju.evtravel.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import com.jeju.evtravel.R
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
            Image(
                painter = painterResource(id = R.drawable.ic_plan_icon),
                contentDescription = "플랜 아이콘",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(1.49161.dp)
                    .width(98.10342.dp)
                    .height(103.16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "새로운 여행을 계획해볼까요?",
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF000000),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.25.sp,
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "날짜를 선택하고 가고 싶은 여행지를 추가해, \n나만의 플랜을 만들어보세요!",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF707070),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.14.sp,
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onCreatePlanClick() },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .width(100.dp)
                    .height(44.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plan_create),
                    contentDescription = "플랜 생성 아이콘",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize() // 아이콘이 버튼 사이즈에 맞게
                )
            }
        }
    }
}