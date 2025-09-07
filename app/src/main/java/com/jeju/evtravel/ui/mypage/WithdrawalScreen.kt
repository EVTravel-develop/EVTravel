package com.jeju.evtravel.ui.mypage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeju.evtravel.viewmodel.WithdrawalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalScreen(
    viewModel: WithdrawalViewModel = hiltViewModel(),
    onWithdrawComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val user by viewModel.user
    val withdrawSuccess by viewModel.withdrawSuccess
    // TODO: ViewModel에 isProcessing 상태를 추가하고 collectAsState()로 가져와야 합니다.
    // val isProcessing by viewModel.isProcessing.collectAsState()

    // 드롭다운 상태
    var expanded by remember { mutableStateOf(false) }
    val reasons = listOf(
        "선택해주세요.",
        "정확하지 않아요",
        "알림이 너무 많이 와요",
        "여행지/액티비티 추천이 부족해서요",
        "여행지 추천이 내 취향과 맞지 않아서요",
        "기타"
    )
    var selectedReason by remember { mutableStateOf(reasons[0]) }
    var customReason by remember { mutableStateOf("") }

    LaunchedEffect(withdrawSuccess) {
        if (withdrawSuccess == true) {
            onWithdrawComplete()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "${user?.displayName ?: "회원"}님의 탈퇴 이유가 궁금해요.",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${user?.displayName ?: "회원"}님과의 이별이 너무 아쉬워요.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 드롭다운 메뉴
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedReason,
                onValueChange = {},
                readOnly = true,
                label = { Text("탈퇴 사유") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                reasons.forEach { reason ->
                    DropdownMenuItem(
                        text = { Text(reason) },
                        onClick = {
                            selectedReason = reason
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedReason == "기타") {
            OutlinedTextField(
                value = customReason,
                onValueChange = { customReason = it },
                label = { Text("탈퇴 이유를 알려주세요.") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("취소")
            }
            Spacer(modifier = Modifier.width(8.dp))

            // 버튼 활성화/비활성화 로직 추가
            val isReasonSelected = selectedReason != "선택해주세요."
            val isOtherReasonValid = selectedReason != "기타" || customReason.isNotBlank()
            val isButtonEnabled = isReasonSelected && isOtherReasonValid // && !isProcessing

            Button(
                onClick = {
                    val finalReason =
                        if (selectedReason == "기타") customReason
                        else selectedReason

                    viewModel.withdrawAccount(finalReason)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = isButtonEnabled // 활성화/비활성화 상태 적용
            ) {
                Text("탈퇴하기")
            }
        }
    }
}