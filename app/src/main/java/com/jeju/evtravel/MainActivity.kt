// com/jeju/evtravel/MainActivity.kt
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.jeju.evtravel.data.service.GuestLoginService
import com.jeju.evtravel.navigation.BottomNavigationBar
import com.jeju.evtravel.ui.map.KakaoMapScreen
import com.jeju.evtravel.ui.mypage.*
import com.jeju.evtravel.ui.onboarding.OnboardingScreen
import com.jeju.evtravel.ui.planner.*
import com.jeju.evtravel.ui.splash.SplashScreen
import com.kakao.vectormap.utils.MapUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val plannerViewModel by viewModels<PlannerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("KeyHash", MapUtils.getHashKey(this))

        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        fusedLocationClient = fusedLocationClient,
                        plannerViewModel = plannerViewModel,
                        searchViewModel = hiltViewModel()
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    fusedLocationClient: FusedLocationProviderClient,
    plannerViewModel: PlannerViewModel,
    searchViewModel: SearchViewModel
) {
    val navController = rememberNavController()
    val mapViewModel: MapViewModel = hiltViewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = setOf("map", "planner", "planner_initial", "my", "planList")

    val shouldShowBottomBar = bottomBarRoutes.any { routePrefix ->
        currentRoute?.startsWith(routePrefix) == true
    }

    // ✅ FirebaseAuth 상태 감지
    val auth = FirebaseAuth.getInstance()
    var userId by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf("게스트") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    // Auth Listener
    LaunchedEffect(Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            userId = user?.uid
            userName = user?.displayName ?: "게스트"
            profileImageUrl = user?.photoUrl?.toString()
        }
    }


    // 앱 시작 시 플랜 목록 로드 (로그인 된 경우만)
    LaunchedEffect(userId) {
        userId?.let { plannerViewModel.loadPlans(it) }
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = "SplashScreen") {

                // ✅ 스플래시
                composable("SplashScreen") {
                    SplashScreen(navController = navController)
                }

                // ✅ 온보딩
                composable("onboarding") {
                    OnboardingScreen(
                        onGuestClick = { success ->
                            if (success) {
                                navController.navigate("map") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        },
                        onKakaoClick = { success ->
                            if (success) {
                                navController.navigate("map") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        }
                    )
                }


                // 지도
                composable("map") {
                    KakaoMapScreen(
                        fusedLocationClient = fusedLocationClient,
                        viewModel = mapViewModel,
                        navController = navController
                    )
                }

                // 검색 탭: 지도 화면 검색바 -> 검색 화면
                composable(route = "search") {
                    SearchScreen(
                        fusedLocationClient = fusedLocationClient,
                        navController = navController,
                        mapViewModel = mapViewModel,
                        viewModel = searchViewModel,
                    )
                }

                // 플래너 탭: EVTravelApp (플래너 관련 NavHost)
                composable("plannerTab") {
                    EVTravelApp(
                        viewModel = plannerViewModel,
                        fusedLocationClient = fusedLocationClient
                    )
                }

                // 마이페이지
                composable("my") {
                    MyPageScreen(
                        onEditProfileClick = {
                            navController.navigate("profileEdit")
                        }
                    )
                }

                composable("profileEdit") {
                    ProfileEditScreen(navController = navController)
                }

                // --- 플래너 관련 화면들 ---

                // 플래너 탭의 분기점 역할. UI 없음.
                composable("planner") {
                    val plans by plannerViewModel.plans.collectAsState()

                    // plans 상태가 변경될 때마다 실행
                    LaunchedEffect(plans) {
                        // plans가 null이 아닐 때(로딩 완료)만 네비게이션 실행
                        plans?.let { planList ->
                            if (planList.isEmpty()) {
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

                    // plans가 null일 때(로딩 중) 로딩 인디케이터 표시
                    if (plans == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
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
                        navController = navController,
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