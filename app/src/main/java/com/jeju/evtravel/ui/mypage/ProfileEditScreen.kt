// com/jeju/evtravel/ui/mypage/ProfileEditScreen.kt
package com.jeju.evtravel.ui.mypage

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val coroutineScope = rememberCoroutineScope() // ✅ 추가

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 정보") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // 무조건 my 페이지로 이동
                            navController.navigate("my") {
                                popUpTo("my") { inclusive = true }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (nickname.isNotBlank()) {
                                isLoading = true
                                FirestoreUserService.updateDisplayName(nickname) { success ->
                                    isLoading = false
                                    if (success) {
                                        navController.navigate("my") {
                                            popUpTo("my") { inclusive = true }
                                        }
                                    } else {
                                        // ❌ LaunchedEffect → ✅ coroutineScope.launch 로 교체
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("닉네임 변경 실패")
                                            navController.navigate("home") {
                                                popUpTo("home") { inclusive = true }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        Text("저장")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
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
                modifier = Modifier.fillMaxWidth()
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(20.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

