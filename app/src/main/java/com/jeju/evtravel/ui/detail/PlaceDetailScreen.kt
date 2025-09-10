package com.jeju.evtravel.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeju.evtravel.R
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.ui.theme.Variables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    place: Place,
//    tour: TourPlaceDetailUi? = null,
    onBack: () -> Unit,
    onNavigateClick: () -> Unit = {},
    viewModel: SummarizePlaceViewModel = viewModel(),
    bookmarkViewModel: BookmarkViewModel = hiltViewModel()
) {
    val summaryText by viewModel.summaryText.collectAsState()
    val isBookmarked by bookmarkViewModel.isPlaceBookmarked.collectAsState()

    // 화면 진입 시 북마크 상태 체크
    LaunchedEffect(key1 = place.id) {
        bookmarkViewModel.checkPlaceBookmark(place.id)
    }

    LaunchedEffect(key1 = place.id) {
        val cachedSummary = viewModel.getCachedSummary()
        if (cachedSummary.isNullOrBlank() || cachedSummary == "AI 작성 중...") {
            val x = place.longitude?.toString() ?: "0.0"
            val y = place.latitude?.toString() ?: "0.0"

            if (x == "0.0" || y == "0.0" || x.isBlank() || y.isBlank()) {
                viewModel.setSummaryText("유효한 좌표 정보가 없어 AI 요약을 불러올 수 없습니다.")
            } else {
                viewModel.fetchPlaceSummary(
                    placeName = place.name,
                    x = x,
                    y = y
                )
            }
        }
    }

    Scaffold(
        floatingActionButton = { ActionFAB(onClick = onNavigateClick) }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(Color.White),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 헤더(이미지/뒤로가기)
            item {
                Box(Modifier.fillMaxWidth()) {
//                    HeaderImage(imageUrl = " ")
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(3.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            }

            // 상단 둥근 섹션
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-15).dp),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = Color.White
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 장소 이름
                            Text(
                                text = place.name,
                                style = DetailTitleTextStyle,
                                modifier = Modifier.weight(1f) // 남은 공간을 모두 차지하여 긴 이름에도 대응
                            )
                            // 북마크 버튼
                            IconButton(
                                onClick = {
                                    bookmarkViewModel.togglePlaceBookmark(place)
                                },
                                modifier = Modifier.size(24.dp) // 버튼 크기 조정
                            ) {
                                val iconRes = if (isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_empty
                                Image(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = "북마크"
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))

                        val addr = place.roadAddress?.takeIf { it.isNotBlank() } ?: place.address
                        if (!addr.isNullOrBlank()) {
                            InfoRow(iconRes = R.drawable.ic_location, text = addr)
                            Spacer(Modifier.height(12.dp))
                        }
                        if (!place.phone.isNullOrEmpty()) {
                            InfoRow(iconRes = R.drawable.ic_phone, text = place.phone)
                            Spacer(Modifier.height(18.dp))
                        }

                        AiDescriptionBox(text = summaryText)
                    }
                }
            }

            // overview가 있는 경우 추가
//            item {
//                Divider(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(7.dp),
//                    color = Variables.Grayscale50,
//                    thickness = 7.dp
//                )
//            }
//            item {
//                Column(
//                    modifier = Modifier
//                        .padding(16.dp)
//                ) {
//                    Text("상세 설명", style = DetailInfoTextStyle)
//                    Spacer(Modifier.height(8.dp))
//
//                    val overview = tour?.overview
//                    if (!overview.isNullOrBlank()) {
//                        Text(overview,
//                             style = DetailOverviewTextStyle,
//                             color = Color(0xFF5E5E5E)
//                        )
//                    } else {
//                        Text(
//                            "등록된 상세 설명이 없습니다.",
//                            style = DetailOverviewTextStyle,
//                            color = Color(0xFF5E5E5E)
//                        )
//                    }
//
//                    Spacer(Modifier.height(80.dp)) // FAB 간섭 방지
//                }
//            }
        }
    }
}

@Composable
private fun AiDescriptionBox(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEBF4FF), shape = RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, Color(0xFFEBF4FF)), shape = RoundedCornerShape(12.dp))
            .padding(start = 7.dp, end = 7.dp, top = 6.dp, bottom = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_ai),
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("AI 요약", style = AiTitleTextStyle, color = Variables.Blue700)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            // 왼쪽 인용구 라인
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF8BBFFF))
            )
            Spacer(Modifier.width(8.dp))
            // 본문 텍스트
            Text(
                text,
                style = AiSummaryTextStyle,
                color = Color(0xFF535353)
            )
        }
    }
}