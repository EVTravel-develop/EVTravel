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
    onDeleteAccountClick: () -> Unit = {}
) {
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        FirestoreUserService.getUserFromFirestore { fetchedUser ->
            user = fetchedUser
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 🔹 프로필 영역
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!user?.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = user?.imageUrl,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
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
                text = user?.displayName ?: "제주도 여행자_3664",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onEditProfileClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0173FF))
            ) {
                Text("프로필 편집", color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ 1. 프로필과 즐겨찾기 사이의 구분 블록 추가
        Divider(color = Color(0xFFF5F5F5), thickness = 8.dp)

        // 🔹 즐겨찾기
        Text(
            "즐겨찾기",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
        )

        // 저장된 코스
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSavedCourseClick() }
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

        // 저장된 장소
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSavedPlaceClick() }
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

        // ✅ 2. 즐겨찾기와 탈퇴하기 사이의 구분 블록 추가
        Divider(color = Color(0xFFF5F5F5), thickness = 8.dp)

        // 🔹 탈퇴하기
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDeleteAccountClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("탈퇴하기", fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}