package com.jeju.evtravel.ui.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jeju.evtravel.R
import com.jeju.evtravel.service.ChargeTimeService
import com.jeju.evtravel.ui.detail.RobotoFamily
import com.jeju.evtravel.ui.theme.Variables
import com.jeju.evtravel.ui.viewmodel.UserVehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleInfoScreen(
    onSave: () -> Unit, // 뒤로 가기 및 저장 후 돌아가기용 콜백
    vm: UserVehicleViewModel
) {
    // ChargeTimeService에서 차종 목록을 가져옵니다.
    val carModels = remember { ChargeTimeService().getCarModels() }
    val speeds = listOf("급속", "완속", "초급속")

    var selectedCarModel by remember { mutableStateOf(vm.vehicleInfo.value?.carModel ?: "") }
    var currentSoc by remember { mutableStateOf(vm.vehicleInfo.value?.currentSoc?.toString() ?: "") }
    var selectedSpeed by remember { mutableStateOf(vm.vehicleInfo.value?.preferredSpeed ?: "") }

    var carModelExpanded by remember { mutableStateOf(false) }
    var speedExpanded by remember { mutableStateOf(false) }

    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        containerColor = Color.White,
        unfocusedBorderColor = Variables.Grayscale200, // 기본 테두리 색
        focusedBorderColor = Variables.Blue700    // 포커스 시 테두리 색
    )

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { /* 타이틀은 content 영역에 표시하므로 비워둡니다. */ },
                navigationIcon = {
                    IconButton(onClick = onSave) {
                        Icon(
                            painterResource(id = R.drawable.ic_back),
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text("차량 정보를 입력해주세요", style = TitleTextStyle)
            Spacer(Modifier.height(24.dp))

            // --- 차종 선택 ---
            Text("차종", style = LabelTextStyle)
            ExposedDropdownMenuBox(
                expanded = carModelExpanded,
                onExpandedChange = { carModelExpanded = !carModelExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCarModel,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dropdown),
                            contentDescription = "메뉴 열기"
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "차량 모델을 선택해주세요.",
                            style = DropDownLabelTextStyle
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
                    expanded = carModelExpanded,
                    onDismissRequest = { carModelExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    carModels.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = model,
                                    style = DropDownLabelTextStyle,
                                    color = Color.Black
                                )
                            },
                            onClick = {
                                selectedCarModel = model
                                carModelExpanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.Black
                            )
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // --- 현재 배터리 입력 ---
            Text("현재 배터리 (%)", style = LabelTextStyle)
            OutlinedTextField(
                value = currentSoc,
                onValueChange = { soc ->
                    // 숫자만 입력받도록 처리
                    val digits = soc.filter { it.isDigit() }.take(3)
                    val v = digits.toIntOrNull()
                    currentSoc = when {
                        v == null -> ""
                        v > 100 -> "100"
                        else -> v.toString()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "현재 배터리 용량을 입력해주세요.",
                        style = DropDownLabelTextStyle
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = textFieldColors
            )
            Spacer(Modifier.height(16.dp))

            // --- 충전기 속도 ---
            Text("충전기 속도", style = LabelTextStyle)
            ExposedDropdownMenuBox(
                expanded = speedExpanded,
                onExpandedChange = { speedExpanded = !speedExpanded }
            ) {
                OutlinedTextField(
                    value = selectedSpeed,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dropdown),
                            contentDescription = "메뉴 열기"
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "원하는 충전기를 선택해주세요.",
                            style = DropDownLabelTextStyle
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
                    expanded = speedExpanded,
                    onDismissRequest = { speedExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    speeds.forEach { speed ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = speed,
                                    style = DropDownLabelTextStyle,
                                    color = Color.Black
                                )
                            },
                            onClick = {
                                selectedSpeed = speed
                                speedExpanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.Black
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // --- 저장 버튼 ---
            Button(
                enabled = selectedCarModel.isNotBlank() &&
                        selectedSpeed.isNotBlank() &&
                        (currentSoc.toIntOrNull()?.let { it in 0..100 } == true),
                onClick = {
                    val socInt = currentSoc.toIntOrNull() ?: 0
                    if (selectedCarModel.isNotBlank() && currentSoc.isNotBlank() && selectedSpeed.isNotBlank()) {
                        vm.updateVehicleInfo(selectedCarModel, socInt, selectedSpeed)
                        onSave()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(59.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Variables.Blue700
                )
            ) {
                Text(
                    text = "저장",
                    style = ButtonLabelTextStyle,
                    color = Color.White
                )
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}

// ChargeTimeService에 차종 목록을 반환하는 함수 추가
fun ChargeTimeService.getCarModels(): List<String> {
    return this.batteryCapacityMap.keys.sorted()
}

// TextStyle 정의 (변경 없음)
private val TitleTextStyle = TextStyle(
    fontFamily = RobotoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.0125.em,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

private val LabelTextStyle = TextStyle(
    fontFamily = RobotoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.0125.em,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

private val DropDownLabelTextStyle = TextStyle(
    fontFamily = RobotoFamily,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.001.em,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

private val ButtonLabelTextStyle = TextStyle(
    fontFamily = RobotoFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.0125.em,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)