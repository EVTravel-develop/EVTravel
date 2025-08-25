package com.jeju.evtravel.ui.planner

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jeju.evtravel.R
import com.jeju.evtravel.data.model.PlanDto
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

object Variables {
    val Blue700: Color = Color(0xFF0173FF)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanListScreen(
    viewModel: PlannerViewModel,
    navController: NavController,
    selectedStart: String? = null,  // 새로 생성된 플랜의 시작일 (강조 표시용)
    selectedEnd: String? = null,    // 새로 생성된 플랜의 종료일 (강조 표시용)
    onBackClick: () -> Unit,        // 상단 뒤로가기 버튼 클릭 시 동작
    onCreatePlanClick: () -> Unit,  // "플랜 생성" 버튼 클릭 시 동작 (CalendarScreen으로 이동)
    onPlanClick: (PlanDto) -> Unit // EditPlanScreen으로 이동하는 콜백
) {
    val plans by viewModel.plans.collectAsState()
    val plansList = plans ?: emptyList()
    
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    // '편집/삭제' 바텀시트 상태 관리
    val editDeleteSheetState = rememberModalBottomSheetState()
    var showEditDeleteSheet by remember { mutableStateOf(false) }
    var selectedPlanForMenu by remember { mutableStateOf<PlanDto?>(null) }
    
    // 연/월 피커 바텀시트를 위한 상태와 스코프
    val monthPickerSheetState = rememberModalBottomSheetState()
    var showMonthPickerSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    
    LaunchedEffect(plans) {
        val currentPlans = plans
        
        if (currentPlans != null && currentPlans.isEmpty()) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != "planner") {
                navController.navigate("planner") {
                    popUpTo("planner") {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }
    
    LaunchedEffect(selectedStart) {
        val start = selectedStart?.let { LocalDate.parse(it) }
        selectedDate = start ?: LocalDate.now()
        start?.let {
            currentMonth = YearMonth.from(it)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center) // Box의 중앙에 Row를 배치
                                .clickable {
                                    scope.launch {
                                        showMonthPickerSheet = true
                                        monthPickerSheetState.show()
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy.MM")),
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight(700),
                                    color = Color(0xFF000000),
                                )
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_drop),
                                contentDescription = "날짜 선택",
                                modifier = Modifier
                                    .size(33.dp)
                                    .padding(4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            PlannerCalendarView(
                month = currentMonth,
                selectedDate = selectedDate,
                plans = plansList,
                onDateClick = { date ->
                    selectedDate = date
                }
            )
            
            Divider(thickness = 8.dp, color = Color(0xFFF5F5F5))
            
            selectedDate?.let {
                PlanListForDate(
                    modifier = Modifier.weight(1f),
                    selectedDate = it,
                    plans = plansList.filter { plan ->
                        val startDate = LocalDate.parse(plan.startDate, DateTimeFormatter.ISO_LOCAL_DATE)
                        val endDate = LocalDate.parse(plan.endDate, DateTimeFormatter.ISO_LOCAL_DATE)
                        !it.isBefore(startDate) && !it.isAfter(endDate)
                    },
                    highlightedPlan = null,
                    onMenuClick = { plan ->
                        selectedPlanForMenu = plan
                        showEditDeleteSheet = true
                    }
                )
            }
            
            Image(
                painter = painterResource(id = R.drawable.ic_add_plan),
                contentDescription = "플랜 추가",
                contentScale = ContentScale.Fit, // 이미지 비율을 유지하며 공간에 맞춤
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCreatePlanClick() }
            )
        }
    }
    
    // 연/월 피커 바텀시트
    if (showMonthPickerSheet) {
        YearMonthPickerBottomSheet(
            sheetState = monthPickerSheetState,
            currentDate = currentMonth,
            onConfirm = { newDate ->
                currentMonth = newDate
                scope.launch { monthPickerSheetState.hide() }.invokeOnCompletion {
                    if (!monthPickerSheetState.isVisible) showMonthPickerSheet = false
                }
            },
            onDismiss = {
                showMonthPickerSheet = false
            }
        )
    }
    
    // '편집/삭제' 바텀시트
    if (showEditDeleteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditDeleteSheet = false },
            sheetState = editDeleteSheetState,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            containerColor = Color.White,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(32.dp)
                        .height(4.dp)
                        .background(Color.LightGray, CircleShape)
                )
            }
        ) {
            PlanItemBottomSheetContent(
                onEditClick = {
                    selectedPlanForMenu?.let { plan ->
                        onPlanClick(plan)
                    }
                    scope.launch { editDeleteSheetState.hide() }.invokeOnCompletion {
                        showEditDeleteSheet = false
                    }
                },
                onDeleteClick = {
                    selectedPlanForMenu?.let { viewModel.deletePlan(it.id) }
                    scope.launch { editDeleteSheetState.hide() }.invokeOnCompletion {
                        showEditDeleteSheet = false
                    }
                }
            )
        }
    }
}

@Composable
fun PlanListForDate(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    plans: List<PlanDto>,
    highlightedPlan: Pair<String, String>?,
    onMenuClick: (PlanDto) -> Unit
) {
    // 날짜 포맷 지정: "yyyy년 M월 d일"
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)
    
    if (plans.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "선택된 날짜에 해당하는 플랜이 없습니다.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    } else {
        // [수정됨] 날짜 헤더와 리스트를 함께 보여주기 위해 Column 추가
        Column(
            modifier = modifier.padding(top = 24.dp, start = 24.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 날짜 헤더 Text 추가
            Text(
                text = selectedDate.format(dateFormatter),
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    // fontFamily = FontFamily(Font(R.font.roboto)), // 폰트 리소스 필요
                    fontWeight = FontWeight(700),
                    color = Color(0xFF000000),
                )
            )
            
            // 2. 기존 플랜 목록 LazyColumn
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = plans, key = { it.id }) { plan ->
                    val isHighlighted = highlightedPlan?.let {
                        it.first == plan.startDate && it.second == plan.endDate
                    } ?: false
                    PlanItem(
                        plan = plan,
                        isHighlighted = isHighlighted,
                        onMenuClick = { onMenuClick(plan) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlannerCalendarView(
    month: YearMonth,
    selectedDate: LocalDate?,
    plans: List<PlanDto>,
    onDateClick: (LocalDate) -> Unit
) {
    val allDaysInView = remember(month) {
        val firstDayOfMonth = month.atDay(1)
        val firstDayOfWeek = java.time.DayOfWeek.SUNDAY
        var dayOfWeek = firstDayOfMonth.dayOfWeek
        var daysToSubtract = dayOfWeek.value - firstDayOfWeek.value
        if (daysToSubtract < 0) daysToSubtract += 7
        val firstVisibleDate = firstDayOfMonth.minusDays(daysToSubtract.toLong())
        
        val days = mutableListOf<LocalDate>()
        repeat(42) {
            days.add(firstVisibleDate.plusDays(it.toLong()))
        }
        days
    }
    
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        val rows = (allDaysInView.size / 7)
        for (week in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (day in 0..6) {
                    val date = allDaysInView[week * 7 + day]
                    val isCurrentMonth = date.year == month.year && date.month == month.month
                    val isSelected = date == selectedDate
                    
                    val planInfo = remember(date, plans) {
                        var isStart = false
                        var isEnd = false
                        var isWithin = false
                        for (plan in plans) {
                            val startDate = LocalDate.parse(plan.startDate)
                            val endDate = LocalDate.parse(plan.endDate)
                            if (date.isEqual(startDate)) isStart = true
                            if (date.isEqual(endDate)) isEnd = true
                            if (date.isAfter(startDate) && date.isBefore(endDate)) isWithin = true
                        }
                        Triple(isStart, isEnd, isWithin)
                    }
                    
                    DayCellWithPlan(
                        date = date,
                        isCurrentMonth = isCurrentMonth,
                        isSelected = isSelected,
                        planInfo = planInfo,
                        onClick = { onDateClick(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.DayCellWithPlan(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    planInfo: Triple<Boolean, Boolean, Boolean>,
    onClick: (LocalDate) -> Unit
) {
    val (isPlanStart, isPlanEnd, isWithinPlan) = planInfo
    val hasPlan = isPlanStart || isPlanEnd || isWithinPlan
    
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clickable(enabled = isCurrentMonth) {
                onClick(date)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = if (isSelected) Variables.Blue700 else Color.Transparent,
                        shape = RoundedCornerShape(size = 10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    color = when {
                        isSelected -> Color.White
                        !isCurrentMonth -> Color(0xFFC2C2C2)
                        else -> Color(0xFF000000)
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
            
            if (hasPlan && isCurrentMonth) {
                Spacer(modifier = Modifier.height(2.dp))
                PlanIndicator(isStart = isPlanStart, isEnd = isPlanEnd)
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PlanIndicator(isStart: Boolean, isEnd: Boolean) {
    val isSingleDay = isStart && isEnd
    
    if (isSingleDay) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .fillMaxWidth(0.5f)
                    .background(color = Color(0xFFE6F1FF), shape = CircleShape)
            )
            Row(
                modifier = Modifier.fillMaxWidth(0.5f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).background(color = Variables.Blue700, shape = CircleShape))
                Box(modifier = Modifier.size(6.dp).background(color = Variables.Blue700, shape = CircleShape))
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        ) {
            when {
                !isStart && !isEnd -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE6F1FF))
                    )
                }
                isStart -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight()
                            .background(Color(0xFFE6F1FF))
                    )
                }
                isEnd -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight()
                            .background(Color(0xFFE6F1FF))
                    )
                }
            }
            
            if (isStart) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(6.dp)
                        .background(color = Variables.Blue700, shape = CircleShape)
                )
            }
            if (isEnd) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(6.dp)
                        .background(color = Variables.Blue700, shape = CircleShape)
                )
            }
        }
    }
}

@Composable
fun PlanItem(
    plan: PlanDto,
    isHighlighted: Boolean = false,
    onMenuClick: (PlanDto) -> Unit
) {
    Row(
        modifier = Modifier
            .shadow(elevation = 4.dp, spotColor = Color(0xA09A9A9A), ambientColor = Color(0xA09A9A9A))
            .fillMaxWidth() // .width(382.dp) 대신 사용
            .heightIn(min = 60.dp) // .height(60.dp) 대신 사용
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 10.dp))
            .padding(start = 17.dp, top = 18.dp, end = 9.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plan),
                contentDescription = "플랜 아이콘",
                tint = Color(0xFF0173FF)
            )
            Text(
                text = "${plan.startDate.replace("-", ".")} - ${plan.endDate.replace("-", ".")}",
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                fontSize = 14.sp
            )
        }
        
        Image(
            painter = painterResource(id = R.drawable.ic_list_menu),
            contentDescription = "더보기",
            modifier = Modifier
                .padding(1.dp)
                .width(24.dp)
                .height(24.dp)
                .clickable { onMenuClick(plan) }
        )
    }
}

@Composable
fun PlanItemBottomSheetContent(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "편집하기",
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onEditClick)
                .padding(vertical = 12.dp, horizontal = 24.dp),
            style = TextStyle(
                fontSize = 17.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight(500),
                color = Color(0xFF000000),
                letterSpacing = 0.25.sp,
                textAlign = TextAlign.Center
            )
        )
        
        Text(
            text = "삭제하기",
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onDeleteClick)
                .padding(vertical = 12.dp, horizontal = 24.dp),
            style = TextStyle(
                fontSize = 17.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight(500),
                color = Color(0xFFFF1010),
                letterSpacing = 0.25.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun YearMonthPickerBottomSheet(
    sheetState: SheetState,
    currentDate: YearMonth,
    onConfirm: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
) {
    val Grayscale50 = Color(0xFFF5F5F5)
    val Grayscale300 = Color(0xFFC4C4C4)
    val GrayscaleBlack = Color(0xFF000000)
    
    val currentYear = YearMonth.now().year
    val years = ((currentYear - 5)..(currentYear + 10)).map { it.toString() }
    val months = (1..12).map { it.toString() }
    
    val initialYearIndex = years.indexOf(currentDate.year.toString()).coerceAtLeast(0)
    val yearListState = rememberLazyListState(initialFirstVisibleItemIndex = initialYearIndex)
    
    val initialMonthIndex = months.indexOf(currentDate.monthValue.toString()).coerceAtLeast(0)
    val monthListState = rememberLazyListState(initialFirstVisibleItemIndex = initialMonthIndex)
    
    val getCenteredIndex = { state: LazyListState ->
        val layoutInfo = state.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty()) -1
        else {
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            visibleItems.minByOrNull { abs((it.offset + it.size / 2) - viewportCenter) }?.index ?: -1
        }
    }
    
    val centeredYearIndex by remember { derivedStateOf { getCenteredIndex(yearListState) } }
    val centeredMonthIndex by remember { derivedStateOf { getCenteredIndex(monthListState) } }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 17.dp)
                    .width(50.dp)
                    .height(4.dp)
                    .background(Color.LightGray, CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(274.dp)
                        .height(34.dp)
                        .background(
                            color = Grayscale50,
                            shape = RoundedCornerShape(size = 10.dp)
                        )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelPicker(
                        state = yearListState,
                        items = years.map { "${it}년" },
                        centeredIndex = centeredYearIndex,
                        modifier = Modifier,
                        textModifier = Modifier.width(80.dp),
                        activeColor = GrayscaleBlack,
                        inactiveColor = Grayscale300
                    )
                    WheelPicker(
                        state = monthListState,
                        items = months.map { "${it}월" },
                        centeredIndex = centeredMonthIndex,
                        modifier = Modifier,
                        textModifier = Modifier.width(50.dp),
                        activeColor = GrayscaleBlack,
                        inactiveColor = Grayscale300
                    )
                }
            }
            
            Image(
                painter = painterResource(id = R.drawable.ic_ok),
                contentDescription = "완료",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(size = 10.dp))
                    .clickable {
                        val selectedYear = years.getOrElse(centeredYearIndex) { currentDate.year.toString() }.toInt()
                        val selectedMonth = months.getOrElse(centeredMonthIndex) { currentDate.monthValue.toString() }.toInt()
                        onConfirm(YearMonth.of(selectedYear, selectedMonth))
                    }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPicker(
    state: LazyListState,
    items: List<String>,
    centeredIndex: Int,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    activeColor: Color,
    inactiveColor: Color
) {
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    
    LazyColumn(
        state = state,
        modifier = modifier.height(150.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(vertical = 60.dp)
    ) {
        items(items.size) { index ->
            Text(
                text = items[index],
                style = TextStyle(
                    fontSize = 19.sp,
                    lineHeight = 20.sp,
                    // fontFamily = FontFamily(Font(R.font.roboto)), // 폰트 리소스 필요
                    fontWeight = FontWeight(500),
                    color = if (centeredIndex == index) activeColor else inactiveColor,
                    letterSpacing = 0.25.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = textModifier.padding(vertical = 4.dp)
            )
        }
    }
}