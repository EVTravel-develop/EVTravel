package com.jeju.evtravel

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.jeju.evtravel.navigation.BottomNavigationBar
import com.jeju.evtravel.ui.detail.PlaceDetailScreen
import com.jeju.evtravel.ui.map.KakaoMapScreen
import com.jeju.evtravel.ui.planner.*
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.utils.MapUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
                    MainScreen(
                        fusedLocationClient = fusedLocationClient,
                        plannerViewModel = plannerViewModel
                    )
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
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    val currentRoute = navBackStackEntry?.destination?.route
    
    // 하단 네비게이션 바를 보여줄 라우트 목록
    val bottomBarRoutes = setOf("map", "planner_initial", "my")
    
    // 현재 라우트가 하단 네비게이션 바를 보여줘야 하는지 여부
    val shouldShowBottomBar = currentRoute in bottomBarRoutes
    
    // 앱 시작 시 딱 한번만 플랜 목록을 로드합니다.
    LaunchedEffect(Unit) {
        plannerViewModel.loadPlans("somi") // 사용자 ID는 실제 값으로 변경 예정
    }
    
    Scaffold(
        bottomBar = {
            // 조건에 따라 하단 네비게이션 바를 표시하거나 숨깁니다.
            if (shouldShowBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = "map") {
                // 홈 탭: 지도 화면
                composable("map") {
                    KakaoMapScreen(
                        fusedLocationClient = fusedLocationClient,
                        navController = navController
                    )
                }
                
                // 마이 탭
                composable("my") {
                    Text(text = "마이 페이지")
                }
                
                // 장소 상세 화면
                composable("place_detail/{placeId}") { backStackEntry ->
                    val placeId =
                        backStackEntry.arguments?.getString("placeId") ?: return@composable
                    PlaceDetailScreen(placeId)
                }
                
                // --- 플래너 관련 화면들 ---
                
                // 플래너 탭의 분기점 역할. UI 없음.
                composable("planner") {
                    val plans by plannerViewModel.plans.collectAsState()
                    // 데이터 로드가 완료되었는지 확인하여 한 번만 실행되도록 함
                    // (ViewModel에 isloading 같은 상태 추가를 권장합니다.)
                    // 여기서는 plans가 초기 상태(null 또는 empty list)가 아닌 경우를 로드 완료로 간주합니다.
                    
                    // LaunchedEffect를 사용하여 컴포지션이 완료된 후 navigate를 실행합니다.
                    LaunchedEffect(plans) {
                        if (plans.isEmpty()) {
                            navController.navigate("planner_initial") {
                                popUpTo("planner") { inclusive = true }
                            }
                        } else {
                            navController.navigate("planList") {
                                popUpTo("planner") { inclusive = true }
                            }
                        }
                    }
                }
                
                // 플랜이 없을 때 보여주는 초기 화면 (하단바 보임)
                composable("planner_initial") {
                    PlannerScreen(
                        onCreatePlanClick = {
                            navController.navigate("calendar")
                        }
                    )
                }
                
                // 플랜 목록 화면 (하단바 숨김)
                composable(
                    // 'planList' 뒤에 쿼리 파라미터 형식으로 선택적 인자를 정의합니다.
                    route = "planList?start={start}&end={end}",
                    arguments = listOf(
                        navArgument("start") {
                            type = NavType.StringType
                            nullable = true // start 인자는 필수가 아님
                        },
                        navArgument("end") {
                            type = NavType.StringType
                            nullable = true // end 인자는 필수가 아님
                        }
                    )
                ) { backStackEntry ->
                    // 인자를 추출합니다. 값이 전달되지 않으면 null이 됩니다.
                    val start = backStackEntry.arguments?.getString("start")
                    val end = backStackEntry.arguments?.getString("end")
                    
                    PlanListScreen(
                        viewModel = plannerViewModel,
                        selectedStart = start, // 추출한 값을 PlanListScreen에 전달
                        selectedEnd = end,     // 추출한 값을 PlanListScreen에 전달
                        onBackClick = { /* 추후 수정 */ },
                        onCreatePlanClick = { navController.navigate("calendar") },
                        onPlanClick = { plan ->
                            navController.navigate("editPlan/${plan.id}")
                        }
                    )
                }
                
                // 여행 날짜 선택 화면 (하단바 숨김)
                composable("calendar") {
                    CalendarScreen(
                        viewModel = plannerViewModel,
                        navController = navController,
                        onNextClick = {
                            navController.navigate("editPlan/new")
                        }
                    )
                }
                
                // 여행 일정 편집 화면 (하단바 숨김)
                composable(
                    route = "editPlan/{planId}",
                    arguments = listOf(navArgument("planId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val planId = backStackEntry.arguments?.getString("planId")
                    EditPlanScreen(
                        viewModel = plannerViewModel,
                        planId = if (planId == "new") null else planId,
                        navController = navController,
                        onBackClick = { navController.popBackStack() },
                        onEditDateClick = { navController.navigate("calendar") },
                        onAddDestinationClick = { navController.navigate("searchDestination") }
                    )
                }
                
                // 여행지 검색 화면 (하단바 숨김)
                composable("searchDestination") {
                    var x by remember { mutableStateOf<Double?>(null) }
                    var y by remember { mutableStateOf<Double?>(null) }
                    
                    LaunchedEffect(Unit) {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    x = it.longitude
                                    y = it.latitude
                                }
                            }
                        } catch (e: SecurityException) {
                            Log.e("Location", "Location permission not granted: ${e.message}")
                        }
                    }
                    
                    if (x != null && y != null) {
                        SearchDestinationScreen(
                            viewModel = plannerViewModel,
                            x = x!!,
                            y = y!!,
                            onBackClick = { navController.popBackStack() }
                        )
                    } else {
                        // 위치 가져오는 중일 때 로딩 표시
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}