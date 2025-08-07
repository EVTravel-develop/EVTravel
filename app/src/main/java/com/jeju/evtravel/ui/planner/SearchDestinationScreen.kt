package com.jeju.evtravel.ui.planner

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import com.jeju.evtravel.data.model.PlaceDto
import androidx.compose.ui.res.painterResource
import com.jeju.evtravel.R
import com.jeju.evtravel.data.model.ChargerDto
import androidx.compose.ui.graphics.Color

/**
 * 목적지 검색 화면을 구현하는 Composable 함수
 * @param onBackClick 뒤로 가기 버튼 클릭 시 호출되는 콜백 함수
 */
@Composable
fun SearchDestinationScreen(
    viewModel: PlannerViewModel,
    x: Double, // 경도
    y: Double, // 위도
    onBackClick: () -> Unit
) {
    // 검색어 상태 관리
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val searchResults by viewModel.searchResults.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var selectedPlace by remember { mutableStateOf<PlaceDto?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 헤더 (뒤로가기 버튼 + 검색 상자)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 뒤로가기 버튼
            Box(
                modifier = Modifier
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
            
            // 커스텀 검색 상자
            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = 6.dp,
                        spotColor = Color(0xA09A9A9A),
                        ambientColor = Color(0xA09A9A9A),
                        shape = RoundedCornerShape(size = 10.dp)
                    )
                    .height(51.dp)
                    .background(
                        color = Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(size = 10.dp)
                    )
                    .padding(start = 13.dp, top = 13.dp, end = 13.dp, bottom = 11.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 검색 아이콘 (포커스 상태에 따라 변경)
                    Image(
                        painter = painterResource(
                            id = if (isFocused || query.text.isNotEmpty())
                                R.drawable.ic_search_on
                            else
                                R.drawable.ic_search_off
                        ),
                        contentDescription = "search icon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(17.dp)
                    )
                    
                    // 텍스트 필드 영역
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                viewModel.searchPlaces(it.text, x, y)
                            },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.roboto)),
                                fontWeight = FontWeight(400),
                                color = Color.Black
                            ),
                            singleLine = true,
                            interactionSource = interactionSource,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Placeholder 텍스트
                        if (query.text.isEmpty()) {
                            Text(
                                text = "장소를 입력해주세요",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    lineHeight = 26.53.sp,
                                    fontFamily = FontFamily(Font(R.font.roboto)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFF949494),
                                )
                            )
                        }
                    }
                }
            }
        }
        
        // 검색 결과 표시
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            items(searchResults) { uiPlace ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable {
                            val placeChargers = uiPlace.chargers?.map {
                                ChargerDto(
                                    name = it.name,
                                    address = it.address,
                                    latitude = it.latitude,
                                    longitude = it.longitude
                                )
                            } ?: emptyList()
                            
                            selectedPlace = PlaceDto(
                                id = uiPlace.place.id,
                                name = uiPlace.place.name,
                                roadAddressName = uiPlace.place.roadAddress ?: "",
                                categoryGroupCode = "",
                                x = uiPlace.place.longitude,
                                y = uiPlace.place.latitude,
                                chargers = placeChargers
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 검색 아이콘
                    Image(
                        painter = painterResource(id = R.drawable.ic_search_off),
                        contentDescription = "search icon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(17.dp)
                    )
                    
                    // 장소명
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = uiPlace.place.name,
                            style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                fontFamily = FontFamily(Font(R.font.roboto)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF000000),
                            )
                        )
                    }
                    
                    // 충전소 아이콘
                    Image(
                        painter = painterResource(
                            id = if (uiPlace.isExpanded)
                                R.drawable.ic_charger_on
                            else
                                R.drawable.ic_charger_off
                        ),
                        contentDescription = "battery charge",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(26.dp)
                            .clickable {
                                viewModel.toggleChargerSection(uiPlace.place.id)
                            }
                    )
                    
                    // 선택 아이콘
                    Image(
                        painter = painterResource(
                            id = if (selectedPlace?.id == uiPlace.place.id)
                                R.drawable.ic_plus_on
                            else
                                R.drawable.ic_plus_off
                        ),
                        contentDescription = "select",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // 충전소 목록 (확장시)
                if (uiPlace.isExpanded && uiPlace.chargers != null) {
                    Column(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                            .shadow(
                                elevation = 4.dp,
                                spotColor = Color(0x409A9A9A),
                                ambientColor = Color(0x409A9A9A),
                                shape = RoundedCornerShape(size = 10.dp)
                            )
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFFFFFFF),
                                shape = RoundedCornerShape(size = 10.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        uiPlace.chargers.forEachIndexed { index, charger ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = charger.name,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily(Font(R.font.roboto)),
                                        fontWeight = FontWeight(400),
                                        color = Color(0xFF000000),
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            // 마지막 아이템이 아닌 경우에만 구분선 추가
                            if (index < uiPlace.chargers.size - 1) {
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
        
        // 하단 "다음" 버튼 (장소 선택시에만 표시)
        if (selectedPlace != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .width(382.dp)
                    .height(59.dp)
                    .background(
                        color = Color(0xFF007BFF), // Blue700 색상
                        shape = RoundedCornerShape(size = 10.dp)
                    )
                    .clickable {
                        val date = viewModel.selectedDate.value
                        if (date != null && selectedPlace != null) {
                            viewModel.addPlaceToDate(date, selectedPlace!!)
                            query = TextFieldValue("")
                            viewModel.searchPlaces("", x, y)
                            selectedPlace = null
                            onBackClick()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_planner_next),
                        contentDescription = "next",
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}