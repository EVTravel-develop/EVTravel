// com/jeju/evtravel/ui/mypage/SavedCourseScreen.kt

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
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jeju.evtravel.R
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
                val loadMoreThreshold = 5
                if (lastVisibleItem >= totalItems - loadMoreThreshold) {
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(
        topBar = {
            // 👇 [수정] CenterAlignedTopAppBar 를 TopAppBar 로 변경
            TopAppBar(
                title = { Text("저장된 코스") },
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
                val imageModel = if (bookmark.imageUrl.isNullOrEmpty()) {
                    R.drawable.jeju_place_sample // URL이 없으면 리소스 ID 사용
                } else {
                    bookmark.imageUrl // URL이 있으면 URL 사용
                }

                SavedItemRow(
                    imageUrl = imageModel,
                    title = bookmark.course_name,
                    description = bookmark.course_description
                )
            }
        }
    }
}