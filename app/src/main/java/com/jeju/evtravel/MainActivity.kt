package com.jeju.evtravel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                onPlanClick = { plan ->
                    navController.navigate("editPlan/${plan.id}")
                }
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
                onPlanClick = { plan ->
                    navController.navigate("editPlan/${plan.id}")
                }
            )
        }

        // 여행 날짜 선택 화면
        composable("calendar") {
            CalendarScreen(
                viewModel = viewModel,
                navController = navController,
                onNextClick = {
                    navController.navigate("editPlan/new") // 달력 화면에서 다음 일정 추가 화면으로 이동
                }
            )
        }

        // 여행 일정 편집 화면
        composable(
            // "editPlan/new" 또는 "editPlan/기존planID" 형태의 경로를 모두 처리합니다.
            route = "editPlan/{planId}",
            arguments = listOf(navArgument("planId") { type = NavType.StringType })
        ) { backStackEntry ->
            // 경로에서 planId 값을 추출합니다.
            val planId = backStackEntry.arguments?.getString("planId")

            EditPlanScreen(
                viewModel = viewModel,
                // 추출한 planId가 "new"이면 null을, 아니라면 실제 id를 전달합니다.
                planId = if (planId == "new") null else planId,
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