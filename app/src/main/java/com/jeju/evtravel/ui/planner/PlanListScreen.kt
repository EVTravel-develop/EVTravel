package com.jeju.evtravel.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jeju.evtravel.data.model.PlanDto
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanListScreen(
    viewModel: PlannerViewModel,
    navController: NavController,
    selectedStart: String? = null,  // 새로 생성된 플랜의 시작일 (강조 표시용)
    selectedEnd: String? = null,    // 새로 생성된 플랜의 종료일 (강조 표시용)
    onBackClick: () -> Unit,        // 상단 뒤로가기 버튼 클릭 시 동작
    onCreatePlanClick: () -> Unit,  // "플랜 생성" 버튼 클릭 시 동작 (CalendarScreen으로 이동)
    onPlanClick: (PlanDto) -> Unit  // 특정 플랜 아이템 클릭 시 동작 (EditPlanScreen으로 이동 등)
) {
    val plansState by viewModel.plans.collectAsState()
    
    // 목록이 비어있게 되면(삭제 후), PlannerScreen으로 이동시킵니다.
    LaunchedEffect(plansState) {
        // 데이터 로딩이 완료되었고(null이 아님) 목록이 비어있을 경우
        if (plansState != null && plansState!!.isEmpty()) {
            navController.navigate("planner_initial") {
                // 뒤로가기 시 다시 빈 목록 화면으로 돌아오지 않도록 스택에서 제거
                popUpTo("planList") { inclusive = true }
            }
        }
    }
    
    // 화면 진입 시 Firestore에서 플랜 목록 불러오기
    LaunchedEffect(Unit) {
        viewModel.loadPlans("somi")
    }
    
    // null일 경우 빈 리스트로 처리하여 NullPointerException 방지
    val plans = plansState ?: emptyList()
    
    // 방금 생성한 플랜을 찾아서 강조 표시 (startDate ~ endDate 일치하는 경우)
    val newPlan = plans.find { it.startDate == selectedStart && it.endDate == selectedEnd }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("플래너") },  // 화면 상단 제목
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    // 우측 상단 "플랜 생성" 버튼
                    Button(onClick = onCreatePlanClick) {
                        Text("플랜 생성")
                    }
                }
            )
        }
    ) { innerPadding ->
        // 로딩 중(null)일 때 로딩 인디케이터를 보여줍니다.
        if (plansState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (plans.isEmpty()) {
            // 플랜이 없을 때 중앙 안내 메시지 표시
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("저장된 여행 플랜이 없습니다.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // 플랜 목록 표시
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding
            ) {
                items(plans) { plan ->
                    PlanItem(
                        plan = plan,
                        isHighlighted = (plan == newPlan),              // 새로 생성된 플랜 강조 표시
                        onPlanClick = { onPlanClick(plan) },            // 클릭 시 상세 화면으로 이동
                        onDeleteClick = { viewModel.deletePlan(plan.id) } // 삭제 버튼 클릭 시 삭제
                    )
                }
            }
        }
    }
}

// 플랜 목록의 개별 아이템 UI 컴포넌트
@Composable
fun PlanItem(
    plan: PlanDto,                              // 표시할 플랜 데이터
    isHighlighted: Boolean = false,             // 새로 생성된 플랜인지 여부 (강조 색상 적용)
    onPlanClick: () -> Unit,                    // 아이템 클릭 시 동작
    onDeleteClick: () -> Unit                   // 삭제 메뉴 클릭 시 동작
) {
    var expanded by remember { mutableStateOf(false) } // DropdownMenu 확장 여부 상태
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                if (isHighlighted) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .clickable { onPlanClick() }, // 아이템 전체 클릭 가능
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 플랜의 시작일 ~ 종료일 표시
        Text("${plan.startDate} ~ ${plan.endDate}")
        
        // 더보기 (⋮) 메뉴 - 삭제 기능 포함
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "더보기")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("삭제하기") },
                    onClick = {
                        expanded = false
                        onDeleteClick() // 삭제 콜백 실행
                    }
                )
            }
        }
    }
}