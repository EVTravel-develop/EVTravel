package com.jeju.evtravel.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.jeju.evtravel.R
import com.jeju.evtravel.ui.map.getCurrentLocation
import com.jeju.evtravel.ui.search.comp.PlaceRow
import com.jeju.evtravel.ui.search.SearchViewModel.SearchType
import com.jeju.evtravel.ui.search.SearchViewModel.SearchUiState
import com.kakao.vectormap.LatLng
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.jeju.evtravel.ui.map.MapViewModel as MapVM
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    fusedLocationClient: FusedLocationProviderClient,
    navController: NavController,
    mapViewModel: MapVM = hiltViewModel(),
    viewModel: SearchViewModel = hiltViewModel(),
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

    val DEFAULT = LatLng.from(37.5665, 126.9780)
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // 최초 진입/권한 변경 시 위치 세팅 → 파이프라인 자동 검색
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            // 현재 위치 1회 취득
            suspend fun fetch(): LatLng = suspendCoroutine { cont ->
                getCurrentLocation(context, fusedLocationClient) { loc ->
                    cont.resume(loc ?: DEFAULT)
                }
            }
            val origin = fetch()
            viewModel.setLocation(origin.longitude, origin.latitude)
        } else {
            // 권한 없으면 폴백 좌표로라도 세팅(검색 가능하게)
            viewModel.setLocation(DEFAULT.longitude, DEFAULT.latitude)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .statusBarsPadding()
    ) {
        // 상단 바
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
            }

            RoundedSearchTextField(
                value = query,
                onValueChange = { text ->
                    viewModel.updateQuery(text)
                    if (text.isBlank()) {
                        // 검색어를 모두 지우면 즉시 현재 위치 기준으로 재검색
                        viewModel.forceSearch()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(RoundedBarDefaults.Height),
                placeholder = "장소 및 충전소 검색.",
                onSearch = {
                    focusManager.clearFocus()
                    keyboard?.hide()
                    viewModel.forceSearch()
                },
                trailingClear = true
            )
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
                Text(s.message, modifier = Modifier.padding(16.dp))
            }
            is SearchUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(s.items, key = { it.id }) { place ->
                        PlaceRow(place = place) {
                            navController.navigate("place_detail/${place.id}")
                        }
                    }
                }
            }
        }
    }
}