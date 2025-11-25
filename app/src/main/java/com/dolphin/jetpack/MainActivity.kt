package com.dolphin.jetpack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.dolphin.jetpack.fcm.FCMTokenManager
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.dolphin.jetpack.domain.model.QuestionAnswer
import com.dolphin.jetpack.domain.model.Quiz
import com.dolphin.jetpack.presentation.screens.*
import com.dolphin.jetpack.presentation.viewmodel.AuthState
import com.dolphin.jetpack.presentation.viewmodel.AuthViewModel
import com.dolphin.jetpack.presentation.viewmodel.HistoryViewModel
import com.dolphin.jetpack.presentation.viewmodel.NotesViewModel
import com.dolphin.jetpack.presentation.viewmodel.QuizListState
import com.dolphin.jetpack.presentation.viewmodel.QuizListViewModel
import com.dolphin.jetpack.presentation.viewmodel.QuizUiState
import com.dolphin.jetpack.presentation.viewmodel.QuizViewModel
import com.dolphin.jetpack.presentation.viewmodel.StatisticsViewModel
import com.dolphin.jetpack.ui.theme.JetpackTheme
import com.dolphin.jetpack.util.ThemePreferences

enum class Screen {
    Login,
    EmailAuth,
    QuizSelection,
    TimerSettings,
    QuizInProgress,
    QuizResult,
    History,
    HistoryDetail,
    Statistics,
    Notes,
    LessonNotes,
    Settings,
    Material3Showcase
}

enum class BottomNavItem {
    Quizzes, Notes, History, Statistics
}

class MainActivity : ComponentActivity() {
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AppModule
        AppModule.initialize(applicationContext)

        // Initialize Firebase Analytics
        analytics = FirebaseAnalytics.getInstance(this)
        com.dolphin.jetpack.util.AnalyticsHelper.initialize(applicationContext)

        // Log app open event
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)

        // Request notification permission
        FCMTokenManager.requestNotificationPermission(this)

        // Get FCM token
        lifecycleScope.launch {
            FCMTokenManager.getToken()
        }

        enableEdgeToEdge()
        setContent {
            val themePreferences = remember { ThemePreferences(applicationContext) }
            val isDarkMode by themePreferences.isDarkModeFlow.collectAsState(initial = false)

            JetpackTheme(darkTheme = isDarkMode) {
                QuizApp()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // This will be called by Facebook SDK callback manager
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizApp() {
    // AuthViewModel
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    var showEmailAuth by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Initialize UserSyncManager
    LaunchedEffect(Unit) {
        authViewModel.initializeUserSync(context)
    }

    // Show login screen if not authenticated
    when (authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AuthState.Unauthenticated, is AuthState.Error -> {
            if (showEmailAuth) {
                EmailAuthScreen(
                    viewModel = authViewModel,
                    onBackClick = { showEmailAuth = false }
                )
            } else {
                LoginScreen(
                    viewModel = authViewModel,
                    onEmailAuthClick = { showEmailAuth = true }
                )
            }
        }
        is AuthState.Authenticated -> {
            showEmailAuth = false
            // Update current user ID in repository
            val userId = (authState as AuthState.Authenticated).user.uid
            AppModule.updateCurrentUser(userId)
            MainQuizApp(authViewModel = authViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainQuizApp(authViewModel: AuthViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.QuizSelection) }
    var selectedBottomNav by remember { mutableStateOf(BottomNavItem.Quizzes) }
    var selectedQuiz by remember { mutableStateOf<com.dolphin.jetpack.domain.model.Quiz?>(null) }
    var userAnswers by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var timerEnabled by remember { mutableStateOf(false) }
    var timerMinutes by remember { mutableStateOf(10) }
    var showExitDialog by remember { mutableStateOf(false) }
    var selectedAttemptId by remember { mutableStateOf(0L) }
    var selectedTopic by remember { mutableStateOf<com.dolphin.jetpack.domain.model.Topic?>(null) }
    var selectedChapterId by rememberSaveable { mutableStateOf(1) } // Added chapter selection state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Pager state for swipe navigation - Order: Notes, Quizzes, History, Statistics
    val pagerState = rememberPagerState(
        initialPage = 1, // Start with Quizzes
        pageCount = { 4 }
    )
    val coroutineScope = rememberCoroutineScope()

    // Get context for AdMob initialization
    val context = androidx.compose.ui.platform.LocalContext.current

    // ViewModels
    val quizViewModel: QuizViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AppModule.provideQuizViewModel() as T
            }
        }
    )

    // Initialize AdMob
    LaunchedEffect(Unit) {
        quizViewModel.initializeAdMob(context)
    }

    val historyViewModel: HistoryViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AppModule.provideHistoryViewModel() as T
            }
        }
    )

    val statsViewModel: StatisticsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AppModule.provideStatisticsViewModel() as T
            }
        }
    )

    // Sync pager state with bottom nav selection
    LaunchedEffect(pagerState.currentPage) {
        selectedBottomNav = when (pagerState.currentPage) {
            0 -> BottomNavItem.Notes
            1 -> BottomNavItem.Quizzes
            2 -> BottomNavItem.History
            3 -> BottomNavItem.Statistics
            else -> BottomNavItem.Quizzes
        }
        currentScreen = when (pagerState.currentPage) {
            0 -> Screen.Notes
            1 -> Screen.QuizSelection
            2 -> Screen.History
            3 -> Screen.Statistics
            else -> Screen.QuizSelection
        }
    }

    // Check for resume states - observe quiz list state
    val quizListState by quizViewModel.quizListState.collectAsState()
    LaunchedEffect(quizListState) {
        if (quizListState is QuizListState.Success) {
            val quizzes = (quizListState as QuizListState.Success).quizzes
            val quizTitles = quizzes.map { it.title }
            quizViewModel.checkResumeStates(quizTitles)
        }
    }

    // Handle back press within Quizzes tab (for internal navigation)
    BackHandler(enabled = currentScreen != Screen.QuizSelection && selectedBottomNav == BottomNavItem.Quizzes) {
        when (currentScreen) {
            Screen.QuizInProgress -> showExitDialog = true
            Screen.QuizResult -> {
                currentScreen = Screen.QuizSelection
                selectedQuiz = null
                userAnswers = emptyMap()
            }
            Screen.LessonNotes -> {
                currentScreen = Screen.Notes
            }
            else -> {}
        }
    }

    // Handle back press on History, Statistics tabs - navigate to Notes instead of going to background
    BackHandler(enabled = selectedBottomNav in listOf(BottomNavItem.History, BottomNavItem.Statistics)) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0) // Navigate to Notes tab (page 0)
        }
    }

    // Handle back press on Quizzes tab when at QuizSelection screen - navigate to Notes instead of going to background
    BackHandler(enabled = selectedBottomNav == BottomNavItem.Quizzes && currentScreen == Screen.QuizSelection) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0) // Navigate to Notes tab (page 0)
        }
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            onDismiss = { showExitDialog = false },
            onConfirm = {
                showExitDialog = false
                currentScreen = Screen.QuizSelection
                selectedQuiz = null
                userAnswers = emptyMap()
                timerEnabled = false
                timerMinutes = 10 // Reset to default
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.6f) // 60% of screen width
            ) {
                DrawerContent(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        currentScreen = screen
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    },
                    onSignOut = {
                        authViewModel.signOut()
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentScreen in listOf(Screen.Notes, Screen.QuizSelection, Screen.History, Screen.Statistics)) {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (currentScreen) {
                                    Screen.Notes -> "Notes"
                                    Screen.QuizSelection -> "Quizzes"
                                    Screen.History -> "History"
                                    Screen.Statistics -> "Statistics"
                                    else -> "Jetpack"
                                },
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            when (currentScreen) {
                                Screen.QuizSelection -> {
                                    IconButton(onClick = {
                                        currentScreen = Screen.Settings
                                    }) {
                                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                                    }
                                }
                                Screen.Statistics -> {
                                    IconButton(onClick = {
                                        statsViewModel.refresh()
                                    }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                                    }
                                }
                                Screen.History -> {
                                    HistoryScreenActions(historyViewModel = historyViewModel)
                                }
                                else -> {}
                            }
                        }
                    )
                }
            },
            bottomBar = {
            if (currentScreen in listOf(Screen.Notes, Screen.QuizSelection, Screen.History, Screen.Statistics)) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Notes") },
                        label = { Text("Notes") },
                        selected = selectedBottomNav == BottomNavItem.Notes,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Grade, contentDescription = "Quizzes") },
                        label = { Text("Quizzes") },
                        selected = selectedBottomNav == BottomNavItem.Quizzes,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        selected = selectedBottomNav == BottomNavItem.History,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Statistics") },
                        label = { Text("Statistics") },
                        selected = selectedBottomNav == BottomNavItem.Statistics,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(3)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            // Show detail screens outside of pager
            when (currentScreen) {
                Screen.TimerSettings -> {
                    // Redirect to quiz selection if somehow accessed
                    LaunchedEffect(Unit) {
                        currentScreen = Screen.QuizSelection
                    }
                }

                Screen.QuizInProgress -> {
                    selectedQuiz?.let { quiz ->
                        // Get activity context for ad showing
                        val activityContext = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
                        QuizInProgressScreen(
                            modifier = Modifier.fillMaxSize(),
                            quiz = quiz,
                            timerEnabled = timerEnabled,
                            timerMinutes = timerMinutes,
                            viewModel = quizViewModel,
                            onQuizFinished = { answers, timeTaken ->
                                userAnswers = answers

                                // Save quiz attempt
                                val questions = quiz.questions
                                val correctCount = answers.count { (index, answer) ->
                                    questions.getOrNull(index)?.correctAnswerIndex == answer
                                }

                                val questionAnswers = answers.mapNotNull { (index, answer) ->
                                    questions.getOrNull(index)?.let { question ->
                                        QuestionAnswer(
                                            attemptId = 0L,
                                            questionIndex = index,
                                            questionText = question.text,
                                            selectedAnswer = answer,
                                            correctAnswer = question.correctAnswerIndex,
                                            isCorrect = question.correctAnswerIndex == answer
                                        )
                                    }
                                }

                                // Save quiz attempt
                                quizViewModel.saveQuizAttempt(
                                    quizTitle = quiz.title,
                                    score = correctCount,
                                    totalQuestions = questions.size,
                                    timeTakenSeconds = timeTaken,
                                    timerEnabled = timerEnabled,
                                    timerMinutes = timerMinutes,
                                    questionAnswers = questionAnswers
                                )

                                // Show interstitial ad before navigating to results
                                activityContext?.let { activity ->
                                    quizViewModel.showInterstitialAd(activity) {
                                        // After ad is dismissed (or if no ad available), navigate to results
                                        currentScreen = Screen.QuizResult
                                    }
                                } ?: run {
                                    // If no activity context, navigate directly
                                    currentScreen = Screen.QuizResult
                                }
                            }
                        )
                    } ?: run {
                        // If selectedQuiz is null, navigate back to selection
                        currentScreen = Screen.QuizSelection
                    }
                }

                Screen.QuizResult -> {
                    selectedQuiz?.let { quiz ->
                        QuizResultScreen(
                            modifier = Modifier.fillMaxSize(),
                            quiz = quiz,
                            userAnswers = userAnswers,
                            onBackToSelection = {
                                currentScreen = Screen.QuizSelection
                                selectedQuiz = null
                                userAnswers = emptyMap()
                                statsViewModel.refresh()
                            }
                        )
                    } ?: run {
                        // If selectedQuiz is null, navigate back to selection
                        currentScreen = Screen.QuizSelection
                    }
                }

                Screen.LessonNotes -> {
                    selectedTopic?.let { topic ->
                        LessonNotesScreen(
                            topic = topic,
                            onBack = {
                                currentScreen = Screen.Notes
                            }
                        )
                    }
                }

                Screen.HistoryDetail -> {
                    HistoryDetailScreen(
                        viewModel = historyViewModel,
                        attemptId = selectedAttemptId,
                        onBack = {
                            currentScreen = Screen.History
                        }
                    )
                }

                // Main tab screens - use HorizontalPager
                Screen.Notes, Screen.QuizSelection, Screen.History, Screen.Statistics -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true // Enable swipe gestures
                    ) { page ->
                        when (page) {
                            0 -> {
                                // Notes tab - Lazy loading with optimistic UI
                                val notesViewModel: NotesViewModel = viewModel(
                                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                            @Suppress("UNCHECKED_CAST")
                                            return AppModule.provideNotesViewModel() as T
                                        }
                                    }
                                )

                                // Load content when screen is accessed
                                LaunchedEffect(currentScreen) {
                                    if (currentScreen == Screen.Notes) {
                                        notesViewModel.loadChaptersWithOptimisticUpdate()
                                    }
                                }
                                
                                NotesScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    selectedChapterId = selectedChapterId,
                                    onChapterSelected = { chapterId ->
                                        selectedChapterId = chapterId
                                        notesViewModel.loadChaptersWithOptimisticUpdate() // Refresh when chapter changes
                                    },
                                    onContinueLesson = { topic ->
                                        selectedTopic = topic
                                        currentScreen = Screen.LessonNotes
                                    },
                                    notesViewModel = notesViewModel
                                )
                            }
                            1 -> {
                                // Quizzes tab - Lazy loading with optimistic UI
                                val quizListViewModel: QuizListViewModel = viewModel(
                                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                            @Suppress("UNCHECKED_CAST")
                                            return AppModule.provideQuizListViewModel() as T
                                        }
                                    }
                                )

                                // Check resume states when screen is accessed (lazy loading)
                                val quizzesFromList by quizListViewModel.quizzes.collectAsState()
                                LaunchedEffect(currentScreen, quizzesFromList) {
                                    if (currentScreen == Screen.QuizSelection && quizzesFromList.isNotEmpty()) {
                                        val quizTitles = quizzesFromList.map { it.title }
                                        quizViewModel.checkResumeStates(quizTitles)
                                    }
                                }

                                QuizSelectionScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onQuizSelected = { quiz, withTimer, minutes ->
                                        // Load full quiz details before starting
                                        quizViewModel.loadQuizDetails(quiz.id) { fullQuiz ->
                                            if (fullQuiz != null) {
                                                selectedQuiz = fullQuiz
                                                timerEnabled = withTimer
                                                timerMinutes = minutes
                                                currentScreen = Screen.QuizInProgress
                                                userAnswers = emptyMap()
                                            }
                                        }
                                    },
                                    hasResumeState = quizViewModel.hasResumeState.collectAsState().value,
                                    onResumeQuiz = { quiz ->
                                        // Load full quiz details before resuming
                                        quizViewModel.loadQuizDetails(quiz.id) { fullQuiz ->
                                            if (fullQuiz != null) {
                                                selectedQuiz = fullQuiz
                                                quizViewModel.loadQuizState(fullQuiz.title)
                                            }
                                        }
                                    },
                                    onSettingsClick = {
                                        currentScreen = Screen.Settings
                                    },
                                    quizListViewModel = quizListViewModel
                                )

                                // Handle resume state loading
                                val quizUiState by quizViewModel.uiState.collectAsState()
                                LaunchedEffect(quizUiState) {
                                    if (quizUiState is QuizUiState.StateLoaded) {
                                        currentScreen = Screen.QuizInProgress
                                    }
                                }
                            }
                            2 -> {
                                // History tab
                                // Load remote history when screen is accessed with optimistic UI
                                LaunchedEffect(Unit) { // Only run once when the composable is first created
                                    val firebaseUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                    if (firebaseUid != null) {
                                        historyViewModel.loadRemoteHistory(firebaseUid)
                                    } else {
                                        historyViewModel.loadHistory()
                                    }
                                }
                                
                                HistoryScreen(
                                    viewModel = historyViewModel,
                                    onAttemptClick = { attemptId ->
                                        selectedAttemptId = attemptId
                                        currentScreen = Screen.HistoryDetail
                                    }
                                )
                            }
                            3 -> {
                                // Statistics tab
                                // Load remote statistics when screen is accessed with optimistic UI
                                LaunchedEffect(Unit) { // Only run once when the composable is first created
                                    val firebaseUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                    if (firebaseUid != null) {
                                        statsViewModel.loadRemoteStatistics(firebaseUid)
                                    } else {
                                        statsViewModel.loadStatistics()
                                    }
                                }
                                
                                StatisticsScreen(
                                    viewModel = statsViewModel
                                )
                            }
                        }
                    }
                }

                // Settings screen
                Screen.Settings -> {
                    SettingsScreen(
                        onBack = { currentScreen = Screen.QuizSelection },
                        authViewModel = authViewModel
                    )
                }

                // Material3 Showcase screen
                Screen.Material3Showcase -> {
                    Material3ShowcaseScreen(
                        onBackClick = { currentScreen = Screen.QuizSelection }
                    )
                }

                // Login and EmailAuth screens are handled in QuizApp composable
                Screen.Login, Screen.EmailAuth -> {
                    // These screens are not used in MainQuizApp
                    // They are handled at a higher level in QuizApp
                }
            }
        }
    }
    }
}

@Composable
fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Exit Quiz?") },
        text = { Text("Are you sure you want to exit? Your progress will be lost.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Exit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Continue Quiz")
            }
        }
    )
}

@Composable
fun DrawerContent(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Jetpack",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 32.dp, vertical = 12.dp)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

        // Tools menu items
        DrawerMenuItem(
            icon = Icons.Default.Palette,
            label = "Material3 Showcase",
            selected = currentScreen == Screen.Material3Showcase,
            onClick = { onNavigate(Screen.Material3Showcase) }
        )

        DrawerMenuItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            selected = currentScreen == Screen.Settings,
            onClick = { onNavigate(Screen.Settings) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Sign out button at bottom
        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Out")
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun HistoryScreenActions(historyViewModel: HistoryViewModel) {
    var showFilterDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val historyState by historyViewModel.historyState.collectAsState()

    Row {
        IconButton(onClick = { showFilterDialog = true }) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter")
        }
        IconButton(onClick = {
            if (historyState is com.dolphin.jetpack.presentation.viewmodel.HistoryUiState.Success) {
                val attempts = (historyState as com.dolphin.jetpack.presentation.viewmodel.HistoryUiState.Success).attempts
                val csvData = com.dolphin.jetpack.presentation.util.HistoryExporter.exportToCsv(attempts)
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, csvData)
                    type = "text/csv"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
        }) {
            Icon(Icons.Default.Share, contentDescription = "Export")
        }
    }
}