package com.jeju.evtravel.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.jeju.evtravel.R
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    data: TourPlaceDetailUi,
    onBack: () -> Unit,
    onNavigateClick: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateClick) {
                Icon(painterResource(id = R.drawable.ic_navigate), contentDescription = "길찾기")
            }
        },
        containerColor = Color.White
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // 1) 헤더 이미지 + 뒤로가기
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = data.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "뒤로가기"
                    )
                }
            }

            // 2) 상단 둥근 섹션
            Surface(
                modifier = Modifier
                    .fillMaxSize()  // 화면 꽉 채우기
                    .offset(y = (-20).dp),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // 고정 영역 (제목 + 기본정보)
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(data.title, style = TextStyle(
                            fontFamily = RobotoFamily,
                            fontWeight = FontWeight.Bold,

                        ))
                        Spacer(Modifier.height(12.dp))
                        data.address?.let {
                            InfoRow(
                                leading = { Icon(painterResource(id = R.drawable.ic_location), contentDescription = null) },
                                text = it
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        if (data.tel == null && data.tel == "") {
                            data.tel?.let {
                                InfoRow(
                                    leading = { Icon(painterResource(id = R.drawable.ic_phone), contentDescription = null) },
                                    text = it
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Divider()
                    }

                    // 상세정보만 스크롤되도록 처리
                    Column(
                        modifier = Modifier
                            .weight(1f)  // 남는 공간 차지
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("상세정보", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        if (!data.overview.isNullOrBlank()) {
                            Text(data.overview, style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text(
                                "등록된 상세 설명이 없습니다.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(80.dp)) // FAB 간섭 방지
                    }
                }
            }
        }
    }
}

/* ----------------- 재사용 요소 ----------------- */

@Composable
private fun InfoRow(
    leading: @Composable () -> Unit,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
            leading()
        }
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("다시 시도") }
        }
    }
}
