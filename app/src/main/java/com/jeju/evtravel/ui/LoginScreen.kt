package com.jeju.evtravel.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onKakaoLogin: () -> Unit = {},
    onGuestLogin: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("로그인 화면입니다.", fontSize = 24.sp)

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onKakaoLogin) {
                Text("카카오 로그인")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(onClick = onGuestLogin) {
                Text("비회원으로 시작")
            }
        }
    }
}
