package com.jeju.evtravel.ui.detail.course

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeju.evtravel.R
import com.jeju.evtravel.domain.model.Course
import com.jeju.evtravel.ui.detail.BookmarkViewModel
import com.jeju.evtravel.ui.detail.CourseCardPlaceNameTextStyle
import com.jeju.evtravel.ui.detail.CourseDescriptionTextStyle
import com.jeju.evtravel.ui.detail.CoursePlaceTitleTextStyle
import com.jeju.evtravel.ui.detail.CourseTitleTextStyle
import com.jeju.evtravel.ui.detail.DetailTitleTextStyle
import com.jeju.evtravel.ui.detail.HeaderImage
import com.jeju.evtravel.ui.theme.Variables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    course: Course,
    onBack: () -> Unit,
    onNavigateToPlace: (String) -> Unit,
    bookmarkViewModel: BookmarkViewModel = hiltViewModel()
) {
    val isBookmarked by bookmarkViewModel.isCourseBookmarked.collectAsState()

    // 화면 진입 시 북마크 상태 체크
    LaunchedEffect(key1 = course.id) {
        val placeId = course.id.toString()
        bookmarkViewModel.checkCourseBookmark(placeId)
    }

    val courseInfo = course.course_info.firstOrNull()
    Log.d("CourseDetailScreen", "courseInfo: $courseInfo")
    if (courseInfo == null) {
        Text("코스 정보를 찾을 수 없습니다.", Modifier.padding(16.dp))
        return
    }
    Log.d("CourseDetailScreen", "places: ${courseInfo.places}")

    Scaffold { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // 1. 헤더
            item {
                Box(Modifier.fillMaxWidth()) {
                    HeaderImage(" ")
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

//                    HotCourseBadge(
//                        modifier = Modifier
//                            .align(Alignment.BottomStart)
//                            .padding(start = 16.dp, bottom = 16.dp)
//                    )
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-20).dp),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 장소 이름
                            Text(
                                text = courseInfo.course_name,
                                style = CourseTitleTextStyle,
                                color = Color.Black,
                                modifier = Modifier.weight(1f) // 남은 공간을 모두 차지하여 긴 이름에도 대응
                            )
                            // 북마크 버튼
                            IconButton(
                                onClick = {
                                    bookmarkViewModel.toggleCourseBookmark(course)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                val iconRes = if (isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_empty
                                Image(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = "북마크"
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = courseInfo.course_description,
                            style = CourseDescriptionTextStyle,
                            color = Color(0xFF555555)
                        )
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

            item {
                Column(modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp)) {
                    Text(
                        text = "이 순서대로 산책해보세요!",
                        style = CoursePlaceTitleTextStyle,
                        color = Color.Black
                    )
                }
            }

            items(courseInfo.places) { placeName ->
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    CourseCard(
                        placeName = placeName,
                        onNavigateToPlace = onNavigateToPlace
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }
        }
    }
}

//@Composable
//private fun HotCourseBadge(modifier: Modifier = Modifier) {
//    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
//        Image(
//            painter = painterResource(id = R.drawable.ic_hot), // TODO: 리소스 준비 필요
//            contentDescription = "핫코스",
//            tint = Color.Red
//        )
//        Spacer(Modifier.width(4.dp))
//        Text("HOT 코스", color = Color.Red, fontWeight = FontWeight.Bold)
//    }
//}

@Composable
fun CourseCard(placeName: String, onNavigateToPlace: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.placeholder_large),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0x99000000),
                                Color(0x33000000),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = placeName,
                        style = CourseCardPlaceNameTextStyle,
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    FloatingActionButton(
                        onClick = { onNavigateToPlace(placeName) },
                        containerColor = Variables.Blue700,
                        contentColor = Color.White,
                        modifier = Modifier.size(37.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_navigate_small),
                            contentDescription = "길안내"
                        )
                    }
                }
            }
        }
    }
}