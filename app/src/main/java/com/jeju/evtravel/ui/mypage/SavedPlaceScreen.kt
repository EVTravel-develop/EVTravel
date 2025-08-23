package com.jeju.evtravel.viewmodel

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jeju.evtravel.ui.mypage.SavedItemRow


@OptIn(ExperimentalMaterial3Api::class)   // ✅ Experimental API 사용 허용
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
            CenterAlignedTopAppBar(
                title = { Text("저장된 장소") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(bookmarks.value) { bookmark ->
                SavedItemRow(
                    imageUrl = bookmark.image_url ?: "",
                    title = bookmark.place_name,
                    description = bookmark.description
                )
            }
        }
    }
}
