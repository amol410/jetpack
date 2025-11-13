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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Settings
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

    Scaffold(
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

                // Login and EmailAuth screens are handled in QuizApp composable
                Screen.Login, Screen.EmailAuth -> {
                    // These screens are not used in MainQuizApp
                    // They are handled at a higher level in QuizApp
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