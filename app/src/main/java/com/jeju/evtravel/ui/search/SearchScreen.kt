package com.jeju.evtravel.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.jeju.evtravel.R
import com.jeju.evtravel.ui.map.getCurrentLocation
import com.jeju.evtravel.ui.search.SearchViewModel.SearchType
import com.jeju.evtravel.ui.search.SearchViewModel.SearchUiState
import com.jeju.evtravel.ui.search.comp.PlaceRow
import com.kakao.vectormap.LatLng
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.jeju.evtravel.ui.map.MapViewModel

/**
 * 검색 화면 Composable 함수.
 *
 * @param fusedLocationClient 위치 서비스 클라이언트
 * @param navController 네비게이션 컨트롤러
 * @param mapViewModel 맵 뷰모델
 * @param viewModel 검색 뷰모델
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    fusedLocationClient: FusedLocationProviderClient,
    navController: NavController,
    mapViewModel: MapViewModel,
    viewModel: SearchViewModel,
) {
    //권한 “상태만” 확인(요청은 안 함)
    val permissionState: MultiplePermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()
    val type by viewModel.type.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }

    val DEFAULT_LOCATION = LatLng.from(33.4996, 126.5312)
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    // 최초 진입/권한 변경 시 위치 세팅 → 파이프라인 자동 검색
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            // 현재 위치 1회 취득
            suspend fun fetch(): LatLng = suspendCoroutine { cont ->
                getCurrentLocation(context, fusedLocationClient) { loc ->
                    cont.resume(loc ?: DEFAULT_LOCATION)
                }
            }
            val origin = fetch()
            viewModel.setLocation(origin.longitude, origin.latitude)
        } else {
            // 권한 없으면 폴백 좌표로라도 세팅(검색 가능하게)
            viewModel.setLocation(DEFAULT_LOCATION.longitude, DEFAULT_LOCATION.latitude)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // 상단 바
        Box (
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            // 뒤로가기 버튼
            Box(
                modifier = Modifier
                    .padding(start = 24.dp, top = 22.dp)
                    .size(22.dp)
                    .clickable { navController.navigateUp() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 검색 박스
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 62.dp, end = 24.dp) // 뒤로가기 여백 고려
                    .offset(y = 7.dp)                  // 수직 정렬 맞춤
                    .shadow(
                        elevation = 6.dp,
                        spotColor = Color(0xA09A9A9A),
                        ambientColor = Color(0xA09A9A9A),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .height(51.dp)
                    .background(
                        color = Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(start = 13.dp, top = 13.dp, end = 13.dp, bottom = 11.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 검색 아이콘 (포커스/입력 유무에 따라 on/off)
                    val isFocused = interactionSource.collectIsFocusedAsState().value
                    Image(
                        painter = painterResource(
                            id = if (isFocused || query.isNotEmpty())
                                R.drawable.ic_search_on
                            else
                                R.drawable.ic_search_off
                        ),
                        contentDescription = "search icon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(17.dp)
                    )

                    // 입력 영역
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = query,
                            onValueChange = { text ->
                                viewModel.updateQuery(text)
                                if (text.isBlank()) {
                                    // 기존 동작 유지: 모두 지우면 즉시 현재 위치 기준 재검색
                                    viewModel.forceSearch()
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.roboto)),
                                fontWeight = FontWeight.W400,
                                color = Color.Black
                            ),
                            interactionSource = interactionSource,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus()
                                    keyboard?.hide()
                                    viewModel.forceSearch()
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (query.isEmpty()) {
                            Text(
                                text = "장소 및 충전소 검색.",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    lineHeight = 26.53.sp,
                                    fontFamily = FontFamily(Font(R.font.roboto)),
                                    fontWeight = FontWeight.W400,
                                    color = Color(0xFF949494),
                                )
                            )
                        }
                    }
                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "지우기",
                            tint = Color(0xFF949494),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    viewModel.updateQuery("")
                                    viewModel.forceSearch() // 지울 때 즉시 재검색 원하면 유지
                                }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // 장소 / 충전소 토글
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = type == SearchType.PLACE,
                onClick = { viewModel.setType(SearchType.PLACE) },
                label = { Text("장소") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(34.dp)
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = type == SearchType.CHARGER,
                onClick = { viewModel.setType(SearchType.CHARGER) },
                label = { Text("충전소") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(34.dp)
            )
        }

        Divider(Modifier.padding(top = 8.dp))

        // 결과 리스트
        when (val s = uiState) {
            is SearchUiState.Idle -> Unit
            is SearchUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    CircularProgressIndicator(Modifier.padding(top = 24.dp))
                }
            }

            is SearchUiState.Error -> {
                Text("알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도하세요.", modifier = Modifier.padding(16.dp))
            }

            is SearchUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(s.items, key = { it.id }) { place ->
                        PlaceRow(place = place) {
                            mapViewModel.focusAndSelect(place)
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }
}