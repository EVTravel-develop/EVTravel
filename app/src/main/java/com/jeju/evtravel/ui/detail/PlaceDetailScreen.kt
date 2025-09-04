package com.jeju.evtravel.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jeju.evtravel.R
import com.jeju.evtravel.domain.model.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    place: Place,
//    tour: TourPlaceDetailUi? = null,
    onBack: () -> Unit,
    onNavigateClick: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_navigate),
                    contentDescription = "길찾기"
                )
            }
        },
        containerColor = Color.White
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // 헤더(이미지/뒤로가기)
            Box(modifier = Modifier.fillMaxWidth()) {
                // (옵션) 투어 상세 이미지가 있으면 보여주기
                /*
                AsyncImage(
                    model = tour?.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    contentScale = ContentScale.Crop
                )
                */
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

            // 상단 둥근 섹션
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-20).dp),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // ── Kakao Place 기본 정보(항상 표시) ──
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = place.name,
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(12.dp))

                        place.address?.let { addr ->
                            InfoRow(
                                leading = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_location),
                                        contentDescription = null
                                    )
                                },
                                text = addr
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        if (!place.phone.isNullOrEmpty()) {
                            InfoRow(
                                leading = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_phone),
                                        contentDescription = null
                                    )
                                },
                                text = place.phone
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        Divider()
                    }

                    // ── 스크롤 상세(투어 상세가 있을 때만 내용 풍성) ──
//                    Column(
//                        modifier = Modifier
//                            .weight(1f)
//                            .verticalScroll(rememberScrollState())
//                            .padding(16.dp)
//                    ) {
//                        Text("상세정보", style = MaterialTheme.typography.titleMedium)
//                        Spacer(Modifier.height(8.dp))
//
//                        val overview = tour?.overview
//                        if (!overview.isNullOrBlank()) {
//                            Text(overview, style = MaterialTheme.typography.bodyMedium)
//                        } else {
//                            Text(
//                                "등록된 상세 설명이 없습니다.",
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
//
//                        Spacer(Modifier.height(80.dp)) // FAB 간섭 방지
//                    }
                }
            }
        }
    }
}