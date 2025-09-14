package com.jeju.evtravel.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeju.evtravel.R
import com.jeju.evtravel.data.service.GuestLoginService
import com.jeju.evtravel.service.auth.KakaoLoginService

@Composable
fun OnboardingScreen(
    onGuestClick: (Boolean) -> Unit,
    onKakaoClick: (Boolean) -> Unit
) {
    // 수정된 데이터 구조에 맞게 페이지 리스트를 초기화합니다.
    val pages = listOf(
        OnboardingPage("충전소를\n", "소중한 추억", "으로", "충전소를 클릭하면 주변 관광지를 확인할 수 있어요.", R.drawable.onboarding_1),
        OnboardingPage("여행지 준비를\n", "철저하게", "", "플래너에 관광지와 관광지 주변 충전소를\n한 번에 담아보세요.", R.drawable.onboarding_2),
        OnboardingPage("에디터 선정 맞춤 코스로\n", "좋은 경험을", "", "충전시간동안 추천 코스를 따라 여행해보세요.", R.drawable.onboarding_3)
    )

    val pagerState = rememberPagerState(initialPage = 0) { pages.size }
    val context = LocalContext.current
    var showLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 텍스트 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 60.dp)
            ) {
                // 인디케이터
                Row(
                    modifier = Modifier.padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    repeat(pages.size) { index ->
                        val selected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (selected) Color(0xFF0173FF) else Color(0xFFE0E0E0))
                        )
                    }
                }

                val currentPageContent = pages[pagerState.currentPage]

                // 세분화된 데이터로 AnnotatedString 생성
                val annotatedTitle = buildAnnotatedString {
                    append(currentPageContent.titleNormalPart1)
                    withStyle(style = SpanStyle(color = Color(0xFF0173FF))) {
                        append(currentPageContent.titleBluePart)
                    }
                    append(currentPageContent.titleNormalPart2)
                }

                Text(
                    text = annotatedTitle,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = currentPageContent.subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 21.sp
                )
            }

            // 중앙 이미지 페이저
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(state = pagerState) { page ->
                    Image(
                        painter = painterResource(id = pages[page].imageRes),
                        contentDescription = "Onboarding image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                }
            }

            // 하단 버튼 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE812)),
                    onClick = {
                        showLoading = true
                        KakaoLoginService.kakaoLogin(context) { success ->
                            showLoading = false
                            onKakaoClick(success)
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_kakao),
                            contentDescription = "Kakao Icon",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "카카오톡으로 로그인",
                            color = Color.Black.copy(alpha = 0.85f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        showLoading = true
                        GuestLoginService.guestLogin { success ->
                            showLoading = false
                            onGuestClick(success)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Text("비회원", color = Color(0xFF757575), fontSize = 16.sp)
                }
            }
        }

        if (showLoading) {
            LoadingDialog() // LoadingDialog Composable이 있다고 가정합니다.
        }
    }
}