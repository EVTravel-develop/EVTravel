// com/jeju/evtravel/ui/mypage/MyPageScreen.kt
package com.jeju.evtravel.ui.mypage

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jeju.evtravel.R
import com.jeju.evtravel.domain.model.User
import com.jeju.evtravel.service.auth.FirestoreUserService

@Composable
fun MyPageScreen(
    onEditProfileClick: () -> Unit = {},
    onSavedCourseClick: () -> Unit = {},
    onSavedPlaceClick: () -> Unit = {}
) {
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        FirestoreUserService.getUserFromFirestore { fetchedUser ->
            user = fetchedUser
        }
    }

    Log.d("MyPageScreen", "현재 user 상태: $user")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp)
    ) {
        // 🔹 프로필 영역
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (user?.imageUrl != null) {
                Log.d("MyPageScreen", "프로필 이미지 URL: ${user?.imageUrl}")
                AsyncImage(
                    model = user?.imageUrl,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Log.d("MyPageScreen", "기본 프로필 사용")
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Default Profile Image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = user?.displayName ?: "제주도여행자",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    Log.d("MyPageScreen", "프로필 편집 클릭됨")
                    onEditProfileClick()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0173FF))
            ) {
                Text("프로필 편집", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))

        // 🔹 즐겨찾기 영역
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "즐겨찾기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            MyPageItem(
                icon = Icons.Default.Star,
                text = "저장된 코스",
                onClick = {
                    Log.d("MyPageScreen", "저장된 코스 클릭")
                    onSavedCourseClick()   // ✅ uid 넘길 필요 없음
                }
            )

            MyPageItem(
                icon = Icons.Default.Place,
                text = "저장된 장소",
                onClick = {
                    Log.d("MyPageScreen", "저장된 장소 클릭")
                    onSavedPlaceClick()    // ✅ uid 넘길 필요 없음
                }
            )
        }
    }
}

