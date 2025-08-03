package com.jeju.evtravel

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.jeju.evtravel.navigation.BottomNavigationBar
import com.jeju.evtravel.ui.map.KakaoMapScreen
import com.jeju.evtravel.ui.planner.*
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.utils.MapUtils

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val plannerViewModel by viewModels<PlannerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        // 카카오 키 해시 출력
        Log.d("KeyHash", MapUtils.getHashKey(this))
        // Kakao SDK 초기화
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        super.onCreate(savedInstanceState)

        // 현재 위치 정보를 가져 오기 위한 fusedLocationClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 모든 화면을 관리하는 MainScreen 컴포저블을 호출
                    MainScreen(fusedLocationClient, plannerViewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    fusedLocationClient: FusedLocationProviderClient,
    plannerViewModel: PlannerViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = "map") {
                // 홈 탭: 지도 화면
                composable("map") {
                    KakaoMapScreen(fusedLocationClient)
                }

                // 플래너 탭: EVTravelApp (플래너 관련 NavHost)
                composable("plannerTab") {
                    EVTravelApp(plannerViewModel)
                }

                // 마이 탭
                composable("my") {
                    Text(text = "마이 페이지")
                }
            }
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
        composable(
            route = "planList/{start}/{end}",
            arguments = listOf(
                navArgument("start") { type = NavType.StringType },
                navArgument("end") { type = NavType.StringType }
            )
        ) { backStackEntry ->
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