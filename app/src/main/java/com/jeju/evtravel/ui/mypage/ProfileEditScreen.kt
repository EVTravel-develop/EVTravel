// com/jeju/evtravel/ui/mypage/ProfileEditScreen.kt
package com.jeju.evtravel.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jeju.evtravel.service.auth.FirestoreUserService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    navController: NavController
) {
    var nickname by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isInitialLoadDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        FirestoreUserService.getUserFromFirestore { user ->
            nickname = user?.displayName ?: ""
            isLoading = false
            isInitialLoadDone = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 정보") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        // ✅ Column과 Spacer(weight)를 사용한 레이아웃으로 변경
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 20.dp), // 좌우 여백
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 이 Spacer가 위쪽 공간을 차지하여 TextField를 아래로 밀어냅니다.
            Spacer(modifier = Modifier.weight(1f))

            // 중앙에 위치할 컨텐츠 (TextField와 로딩 바)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isInitialLoadDone) {
                    TextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        placeholder = { Text("닉네임을 입력해주세요") },
                        singleLine = true,
                        trailingIcon = {
                            if (nickname.isNotEmpty()) {
                                IconButton(onClick = { nickname = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "지우기"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFF0173FF),
                            unfocusedIndicatorColor = Color.Gray
                        )
                    )
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(20.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            // 이 Spacer가 아래쪽 공간을 차지하여 TextField를 위로, Button을 맨 아래로 밀어냅니다.
            Spacer(modifier = Modifier.weight(1f))

            // 하단 저장 버튼
            Button(
                onClick = {
                    if (nickname.isNotBlank()) {
                        isLoading = true
                        FirestoreUserService.updateDisplayName(nickname) { success ->
                            isLoading = false
                            if (success) {
                                navController.popBackStack()
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("닉네임 변경에 실패했습니다.")
                                }
                            }
                        }
                    }
                },
                enabled = nickname.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp), // 하단 여백
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0173FF),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFBDBDBD),
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    "저장",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}