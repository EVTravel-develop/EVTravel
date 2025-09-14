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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.jeju.evtravel.domain.model.Course
import com.jeju.evtravel.domain.model.Place
import com.jeju.evtravel.navigation.BottomNavigationBar
import com.jeju.evtravel.ui.detail.ChargerDetailScreen
import com.jeju.evtravel.ui.detail.ErrorScreen
import com.jeju.evtravel.ui.detail.PlaceDetailScreen
import com.jeju.evtravel.ui.detail.SummarizePlaceViewModel
import com.jeju.evtravel.ui.detail.TourPlaceDetailViewModel
import com.jeju.evtravel.ui.detail.course.CourseDetailScreen
import com.jeju.evtravel.ui.detail.course.CourseViewModel
import com.jeju.evtravel.ui.map.KakaoMapScreen
import com.jeju.evtravel.ui.map.MapViewModel
import com.jeju.evtravel.ui.mypage.MyPageScreen
import com.jeju.evtravel.ui.mypage.ProfileEditScreen
import com.jeju.evtravel.ui.mypage.SavedCourseScreen
import com.jeju.evtravel.ui.mypage.SavedPlaceScreen
import com.jeju.evtravel.ui.onboarding.OnboardingScreen
import com.jeju.evtravel.ui.planner.CalendarScreen
import com.jeju.evtravel.ui.planner.EditPlanScreen
import com.jeju.evtravel.ui.planner.PlanListScreen
import com.jeju.evtravel.ui.planner.PlannerScreen
import com.jeju.evtravel.ui.planner.PlannerViewModel
import com.jeju.evtravel.ui.planner.SearchDestinationScreen
import com.jeju.evtravel.ui.search.SearchScreen
import com.jeju.evtravel.ui.search.SearchViewModel
import com.jeju.evtravel.ui.splash.SplashScreen
import com.jeju.evtravel.ui.mypage.WithdrawalScreen
import com.kakao.vectormap.utils.MapUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val plannerViewModel by viewModels<PlannerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (BuildConfig.DEBUG) {
            Log.d("KeyHash", MapUtils.getHashKey(this))
        }

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
                    val searchViewModel: SearchViewModel = hiltViewModel()
                    SearchScreen(
                        fusedLocationClient = fusedLocationClient,
                        navController = navController,
                        mapViewModel = mapViewModel,
                        viewModel = searchViewModel,
                    )
                }

                // 장소 상세
                composable(
                    route = "placeDetail/{placeId}",
                    arguments = listOf(navArgument("placeId"){ type = NavType.StringType })
                ) { backStackEntry ->
                    val cached = navController.previousBackStackEntry
                        ?.savedStateHandle?.get<Place>("cachedPlace")

                    val summarizeViewModel: SummarizePlaceViewModel = if (navController.previousBackStackEntry != null) {
                        hiltViewModel(navController.previousBackStackEntry!!)
                    } else {
                        // null일 경우 새로운 ViewModel 인스턴스를 생성하거나 다른 처리를 합니다.
                        // 여기서는 현재 백 스택에 연결된 ViewModel을 사용하도록 변경
                        hiltViewModel(backStackEntry)
                    }

                    if (cached != null) {
                        PlaceDetailScreen(
                            place = cached,
                            onBack = { navController.popBackStack() },
                            fusedLocationClient = fusedLocationClient,
                            viewModel = summarizeViewModel
                        )
                    } else {
                        val vm: TourPlaceDetailViewModel = hiltViewModel(backStackEntry)
                        val ui = vm.state.collectAsState().value
                        when {
                            ui.loading -> CircularProgressIndicator()
                            ui.error != null -> ErrorScreen(ui.error) { vm.reload() }
                            ui.data != null -> PlaceDetailScreen(
                                place = ui.data,
                                onBack = { navController.popBackStack() },
                                fusedLocationClient = fusedLocationClient,
                                viewModel = summarizeViewModel
                            )
                        }
                    }
                }
                // 충전소 상세
                composable("chargerDetail/{placeId}") { backStackEntry ->
                    val cached = navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<Place>("cachedPlace")

                    val place = cached
                        ?: mapViewModel.selectedPlace.collectAsState().value
                        ?: run {
                            navController.popBackStack()
                            return@composable
                        }

                    ChargerDetailScreen(
                        place = place,
                        onBack = { navController.popBackStack() },
                        fusedLocationClient = fusedLocationClient,
                    )
                }

                composable(
                    route = "courseDetail/{courseId}",
                    arguments = listOf(navArgument("courseId"){ type = NavType.StringType })
                ) { backStackEntry ->
                    val course = navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<Course>("selectedCourse")

                    if (course != null) {
                        CourseDetailScreen(
                            course = course,
                            onBack = { navController.popBackStack() },
                            onNavigateToPlace = { }
                        )
                    }
                }

                // 마이페이지
                composable("my") {
                    MyPageScreen(
                        onEditProfileClick = { navController.navigate("profileEdit") },
                        onSavedPlaceClick = { navController.navigate("saved_places") },
                        onSavedCourseClick = { navController.navigate("saved_courses") },
                        onDeleteAccountClick = { navController.navigate("withdrawal") } // ✅ 탈퇴하기 클릭 시 이동
                    )
                }

                composable("profileEdit") {
                    ProfileEditScreen(navController = navController)
                }
                // 저장된 장소
                composable("saved_places") {
                    SavedPlaceScreen(navController = navController)
                }

                // 저장된 코스
                composable("saved_courses") {
                    SavedCourseScreen(navController = navController)
                }

                // ✅ 회원 탈퇴 화면 추가
                composable("withdrawal") {
                    WithdrawalScreen(
                        onWithdrawComplete = {
                            // 탈퇴 성공 시 온보딩 화면으로 이동 (모든 백스택 제거)
                            navController.navigate("onboarding") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        },
                        onCancel = {
                            // 취소 시 이전 화면(마이페이지)으로 돌아감
                            navController.popBackStack()
                        }
                    )
                }


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
                            // 새 플랜 생성을 위해 ViewModel 상태 초기화
                            plannerViewModel.clearPlanDetails()
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
                        onCreatePlanClick = {
                            // 새 플랜 생성을 위해 ViewModel 상태 초기화
                            plannerViewModel.clearPlanDetails()
                            navController.navigate("calendar")
                        },
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
                                if (location != null) {
                                    x = location.longitude
                                    y = location.latitude
                                } else {
                                    // 위치 정보를 가져올 수 없는 경우 기본 좌표 설정
                                    x = 126.53112475064323
                                    y = 33.499545786637974
                                }
                            }.addOnFailureListener {
                                // 위치 가져오기 실패시 기본 좌표 설정
                                x = 126.53112475064323
                                y = 33.499545786637974
                            }
                        } catch (e: SecurityException) {
                            Log.e("Location", "Location permission not granted: ${e.message}")
                            // 권한 없을 때도 기본 좌표 설정
                            x = 126.53112475064323
                            y = 33.499545786637974
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