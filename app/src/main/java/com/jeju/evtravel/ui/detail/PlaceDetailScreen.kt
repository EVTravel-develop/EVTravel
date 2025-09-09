package com.jeju.evtravel.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeju.evtravel.R
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.ui.summary.AiSummaryBox
import com.jeju.evtravel.ui.theme.Variables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    place: Place,
//    tour: TourPlaceDetailUi? = null,
    onBack: () -> Unit,
    onNavigateClick: () -> Unit = {},
    viewModel: SummarizePlaceViewModel = viewModel()
) {
    LaunchedEffect(key1 = place.id) {
        // 좌표 값이 유효한지 확인
        val isValidCoordinates = place.longitude != 0.0 && place.latitude != 0.0
        if (isValidCoordinates) {
            val x = place.longitude.toString()
            val y = place.latitude.toString()

            viewModel.fetchPlaceSummary(
                placeName = place.name,
                x = x,
                y = y
            )
        } else {
            viewModel.setSummaryText("유효한 좌표 정보가 없어 AI 요약을 불러올 수 없습니다.")
        }
    }

    // ViewModel의 summaryText 상태를 관찰합니다.
    val summaryText by viewModel.summaryText.collectAsState()

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
                    HeaderImage(imageUrl = " ")
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
                        .offset(y = (-20).dp),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = Color.White
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(text = place.name, style = DetailTitleTextStyle)
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

                        AiSummaryBox(text = summaryText)
                    }
                }
            }

            item {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                    color = Variables.Grayscale50,
                    thickness = 7.dp
                )
            }
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