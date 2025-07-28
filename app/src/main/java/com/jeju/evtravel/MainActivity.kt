package com.jeju.evtravel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jeju.evtravel.ui.planner.*

class MainActivity : ComponentActivity() {
    private val plannerViewModel by viewModels<PlannerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EVTravelApp(plannerViewModel)
        }
    }
}

@Composable
fun EVTravelApp(viewModel: PlannerViewModel) {
    val navController = rememberNavController()

    // 앱 시작 시 Firestore에서 플랜 목록 로드
    LaunchedEffect(Unit) {
        viewModel.loadPlans("somi")
    }

    val plans by viewModel.plans.collectAsState()

    // 플랜 유무에 따라 첫 화면 결정
    val startDestination = if (plans.isEmpty()) "planner" else "planList"

    NavHost(navController = navController, startDestination = startDestination) {

        // 플랜이 없을 때 보여주는 화면 (PlannerScreen)
        composable("planner") {
            PlannerScreen(
                onCreatePlanClick = {
                    navController.navigate("calendar") // 달력 화면으로 이동
                }
            )
        }

        // 플랜 목록 화면 (플랜이 있는 경우)
        composable("planList") {
            PlanListScreen(
                viewModel = viewModel,
                onBackClick = { /* 추후 수정 */ },
                onCreatePlanClick = { navController.navigate("calendar") },
                onPlanClick = { navController.navigate("editPlan") }
            )
        }

        // 방금 생성된 플랜 날짜를 강조해서 보여줄 때 (start, end 파라미터 포함)
        composable("planList/{start}/{end}") { backStackEntry ->
            val start = backStackEntry.arguments?.getString("start")
            val end = backStackEntry.arguments?.getString("end")
            PlanListScreen(
                viewModel = viewModel,
                selectedStart = start,
                selectedEnd = end,
                onBackClick = { /* 추후 수정 */ },
                onCreatePlanClick = { navController.navigate("calendar") },
                onPlanClick = { navController.navigate("editPlan") }
            )
        }

        // 여행 날짜 선택 화면
        composable("calendar") {
            CalendarScreen(
                viewModel = viewModel,
                navController = navController,
                onNextClick = {
                    navController.navigate("editPlan") // 달력 화면에서 다음 일정 추가 화면으로 이동
                }
            )
        }

        // 여행 일정 편집 화면
        composable("editPlan") {
            EditPlanScreen(
                viewModel = viewModel,
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onEditDateClick = { navController.navigate("calendar") },
                onAddDestinationClick = { navController.navigate("searchDestination") }
            )
        }

        // 여행지 검색 화면
        composable("searchDestination") {
            SearchDestinationScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}