package com.jeju.evtravel.ui.mypage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jeju.evtravel.R
import com.jeju.evtravel.viewmodel.SavedPlaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPlaceScreen(
    navController: NavController,
    viewModel: SavedPlaceViewModel = hiltViewModel()
) {
    val bookmarks = viewModel.bookmarks.collectAsState()
    val listState = rememberLazyListState()

    // 화면 들어올 때 첫 로드
    LaunchedEffect(Unit) {
        viewModel.loadBookmarks()
    }

    // 스크롤 끝에 도달하면 자동으로 loadMore 실행
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (lastVisibleItem >= totalItems - 1) {
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(
        topBar = {
            // 👇 [수정] CenterAlignedTopAppBar 를 TopAppBar 로 변경
            TopAppBar(
                title = { Text("저장된 장소") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                // 👇 [추가] 배경색을 흰색으로 지정
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(bookmarks.value) { bookmark ->
                val imageModel = if (bookmark.image_url.isNullOrEmpty()) {
                    R.drawable.jeju_place_sample // URL이 null이거나 비어있으면 리소스 ID 사용
                } else {
                    bookmark.image_url // URL이 있으면 URL 사용
                }

                SavedItemRow(
                    imageUrl = imageModel,
                    title = bookmark.place_name,
                    description = bookmark.description
                )
            }
        }
    }
}