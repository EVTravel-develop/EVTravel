package com.jeju.evtravel.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
    val pages = listOf(
        OnboardingPage("충전소를 소중한 추억으로", "djkaskfakfdjskajdkfajdkfjas", R.drawable.onboarding_1),
        OnboardingPage("여행지 준비를 철저하게", "djsakfakfdjfdjajczpwkjakdfjas", R.drawable.onboarding_2),
        OnboardingPage("에디터 선정 맞춤 코스로 좋은 경험을", "dkgakfakfdjajd22wjkajdkfjas", R.drawable.onboarding_3)
    )

    val pagerState = rememberPagerState(initialPage = 0) { pages.size }
    val context = LocalContext.current
    var showLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // ✅ 전체 흰 배경
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp) // ✅ 이제 Column 안에서만 padding 적용
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val content = pages[page]

                Box(modifier = Modifier.fillMaxSize()) {
                    // 이미지
                    Image(
                        painter = painterResource(id = content.imageRes),
                        contentDescription = "Onboarding image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .fillMaxHeight(0.6f)
                    )

                    // 인디케이터 + 텍스트
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 24.dp, start = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            repeat(pages.size) { index ->
                                val selected = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .size(if (selected) 10.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selected) Color(0xFF0173FF) else Color.LightGray
                                        )
                                )
                            }
                        }

                        Text(
                            text = content.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = content.subtitle,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // 버튼
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
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
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "카카오톡으로 로그인",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        showLoading = true
                        GuestLoginService.guestLogin { success ->
                            showLoading = false
                            onGuestClick(success)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Text("비회원", color = Color.Black, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ✅ 로딩 모달
        if (showLoading) {
            LoadingDialog()
        }
    }
}