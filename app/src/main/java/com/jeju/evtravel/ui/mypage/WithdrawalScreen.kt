package com.jeju.evtravel.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeju.evtravel.viewmodel.WithdrawalViewModel
import com.jeju.evtravel.ui.onboarding.LoadingDialog
import androidx.compose.ui.unit.IntSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalScreen(
    viewModel: WithdrawalViewModel = hiltViewModel(),
    onWithdrawComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val user by viewModel.user
    val withdrawSuccess by viewModel.withdrawSuccess
    val isProcessing by viewModel.isProcessing.collectAsState()

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

    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    LaunchedEffect(withdrawSuccess) {
        if (withdrawSuccess == true) {
            onWithdrawComplete()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("탈퇴하기", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${user?.displayName ?: "김별이"}님의 탈퇴 이유가 궁금해요.",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${user?.displayName ?: "김별이"}님과의 이별이 너무 아쉬워요.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size
                    }
            ) {
                OutlinedTextField(
                    value = selectedReason,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { expanded = true }
                        )
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(with(density) { textFieldSize.width.toDp() })
                        .background(Color.White)
                ) {
                    // ✅ "선택해주세요." (reasons[0])를 제외한 나머지 항목만 메뉴에 표시
                    reasons.drop(1).forEach { reason ->
                        DropdownMenuItem(
                            text = { Text(reason, modifier = Modifier.fillMaxWidth()) },
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
                    placeholder = { Text("탈퇴 이유를 알려주세요.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isReasonSelected = selectedReason != reasons[0]
                val isOtherReasonValid = selectedReason != "기타" || customReason.isNotBlank()
                val isButtonEnabled = isReasonSelected && isOtherReasonValid && !isProcessing

                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0E0E0),
                        contentColor = Color.Black
                    )
                ) {
                    Text("취소", fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        val finalReason =
                            if (selectedReason == "기타") customReason
                            else selectedReason

                        viewModel.withdrawAccount(finalReason)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0173FF),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFB0B0B0),
                        disabledContentColor = Color.White
                    ),
                    enabled = isButtonEnabled
                ) {
                    Text("탈퇴하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isProcessing) {
            LoadingDialog()
        }
    }
}