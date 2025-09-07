package com.jeju.evtravel.ui.mypage

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jeju.evtravel.domain.model.User
import com.jeju.evtravel.service.auth.FirestoreUserService

@Composable
fun MyPageScreen(
    onEditProfileClick: () -> Unit = {},
    onSavedCourseClick: () -> Unit = {},
    onSavedPlaceClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {} // '탈퇴하기' 클릭 핸들러 추가
) {
    var user by remember { mutableStateOf<User?>(null) }

    // Firestore에서 사용자 정보를 가져옵니다.
    LaunchedEffect(Unit) {
        FirestoreUserService.getUserFromFirestore { fetchedUser ->
            user = fetchedUser
        }
    }

    Log.d("MyPageScreen", "현재 user 상태: $user")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // 전체 배경을 흰색으로 설정
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 🔹 프로필 영역
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!user?.imageUrl.isNullOrEmpty()) {
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
                // 이미지가 없을 때 기본 아이콘을 보여주는 Box
                Log.d("MyPageScreen", "기본 프로필 아이콘 사용")
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Profile Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                Text("프로필 편집", color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color(0xFFE0E0E0))

        // 🔹 즐겨찾기
        Text(
            "즐겨찾기",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // 저장된 코스
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Log.d("MyPageScreen", "저장된 코스 클릭")
                    onSavedCourseClick()
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = "저장된 코스", tint = Color(0xFF424242))
            Spacer(modifier = Modifier.width(12.dp))
            Text("저장된 코스", fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFE0E0E0))

        // 저장된 장소
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Log.d("MyPageScreen", "저장된 장소 클릭")
                    onSavedPlaceClick()
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "저장된 장소", tint = Color(0xFF424242))
            Spacer(modifier = Modifier.width(12.dp))
            Text("저장된 장소", fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        Divider(color = Color(0xFFE0E0E0))

        // 🔹 탈퇴하기
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    Log.d("MyPageScreen", "탈퇴하기 클릭")
                    onDeleteAccountClick()
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("탈퇴하기", fontSize = 16.sp, color = Color.Red)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        Divider(color = Color(0xFFE0E0E0))
    }
}