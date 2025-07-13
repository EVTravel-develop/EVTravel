package com.jeju.evtravel.ui.planner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue

/**
 * 목적지 검색 화면을 구현하는 Composable 함수
 * @param onBackClick 뒤로 가기 버튼 클릭 시 호출되는 콜백 함수
 */
@Composable
fun SearchDestinationScreen(
    viewModel: PlannerViewModel,
    onBackClick: () -> Unit
) {
    // 검색어 상태 관리
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val searchResults by viewModel.searchResults.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // 상단 헤더: 뒤로 가기 버튼과 타이틀 표시
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 뒤로 가기 버튼
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
            // 플래너 타이틀
            Text("플래너", style = MaterialTheme.typography.headlineSmall)
        }

        // 헤더와 검색창 사이 여백
        Spacer(modifier = Modifier.height(16.dp))

        // 검색 입력창: 사용자가 목적지를 입력할 수 있는 텍스트 필드
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.searchPlaces(it.text)
            },
            placeholder = { Text("장소를 입력해주세요") },
            modifier = Modifier.fillMaxWidth()
        )

        // 검색 결과 표시
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(searchResults) { place ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(place.name, style = MaterialTheme.typography.bodyLarge)
                        Text(place.address, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = {
                        // TODO: 장소 추가 및 일정으로 이동
                    }) {
                        Text("추가")
                    }
                }
            }
        }
    }
}
