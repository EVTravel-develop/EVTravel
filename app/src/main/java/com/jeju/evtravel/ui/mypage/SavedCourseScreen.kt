package com.jeju.evtravel.ui.mypage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jeju.evtravel.viewmodel.SavedCourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCourseScreen(
    navController: NavController,
    viewModel: SavedCourseViewModel = hiltViewModel()
) {
    val bookmarks = viewModel.bookmarks.collectAsState()
    val listState = rememberLazyListState()

    // 첫 로드
    LaunchedEffect(Unit) {
        viewModel.loadBookmarks()
    }

    // 무한 스크롤 감지
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
                title = { Text("저장된 코스") },
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
                    imageUrl = "https://picsum.photos/200/100", // TODO: 코스 썸네일 있으면 교체
                    title = "코스 ID: ${bookmark.course_id}",
                    description = "북마크한 UID: ${bookmark.uid}"
                )
            }
        }
    }
}
