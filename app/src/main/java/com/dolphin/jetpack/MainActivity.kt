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
import androidx.compose.foundation.layout.*
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
import com.dolphin.jetpack.domain.model.QuestionAnswer
import com.dolphin.jetpack.domain.model.Quiz
import com.dolphin.jetpack.presentation.screens.*
import com.dolphin.jetpack.presentation.viewmodel.AuthState
import com.dolphin.jetpack.presentation.viewmodel.AuthViewModel
import com.dolphin.jetpack.presentation.viewmodel.HistoryViewModel
import com.dolphin.jetpack.presentation.viewmodel.QuizUiState
import com.dolphin.jetpack.presentation.viewmodel.QuizViewModel
import com.dolphin.jetpack.presentation.viewmodel.StatisticsViewModel
import com.dolphin.jetpack.ui.theme.JetpackTheme

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
    LessonNotes
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
            JetpackTheme {
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
            MainQuizApp(authViewModel = authViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    // ViewModels
    val quizViewModel: QuizViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AppModule.provideQuizViewModel() as T
            }
        }
    )

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

    // Check for resume states
    LaunchedEffect(Unit) {
        val quizTitles = com.dolphin.jetpack.DataProvider.quizList.map { it.title }
        quizViewModel.checkResumeStates(quizTitles)
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
                            selectedBottomNav = BottomNavItem.Notes
                            currentScreen = Screen.Notes
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Grade, contentDescription = "Quizzes") },
                        label = { Text("Quizzes") },
                        selected = selectedBottomNav == BottomNavItem.Quizzes,
                        onClick = {
                            selectedBottomNav = BottomNavItem.Quizzes
                            currentScreen = Screen.QuizSelection
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        selected = selectedBottomNav == BottomNavItem.History,
                        onClick = {
                            selectedBottomNav = BottomNavItem.History
                            currentScreen = Screen.History
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Statistics") },
                        label = { Text("Statistics") },
                        selected = selectedBottomNav == BottomNavItem.Statistics,
                        onClick = {
                            selectedBottomNav = BottomNavItem.Statistics
                            currentScreen = Screen.Statistics
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.QuizSelection -> {
                    QuizSelectionScreen(
                        modifier = Modifier.fillMaxSize(),
                        onQuizSelected = { quiz, withTimer, minutes ->
                            selectedQuiz = quiz
                            timerEnabled = true // Always enable timer since No Timer option is removed
                            timerMinutes = minutes // Set the selected minutes
                            currentScreen = Screen.QuizInProgress // Always go to quiz with timer
                            userAnswers = emptyMap()
                        },
                        hasResumeState = quizViewModel.hasResumeState.collectAsState().value,
                        onResumeQuiz = { quiz ->
                            selectedQuiz = quiz
                            quizViewModel.loadQuizState(quiz.title)
                        },
                        onSignOut = {
                            authViewModel.signOut()
                        }
                    )

                    // Handle resume state loading
                    val quizUiState by quizViewModel.uiState.collectAsState()
                    LaunchedEffect(quizUiState) {
                        if (quizUiState is QuizUiState.StateLoaded) {
                            currentScreen = Screen.QuizInProgress
                        }
                    }
                }

                Screen.TimerSettings -> {
                    // Redirect to quiz selection if somehow accessed
                    LaunchedEffect(Unit) {
                        currentScreen = Screen.QuizSelection
                    }
                }



                Screen.QuizInProgress -> {
                    QuizInProgressScreen(
                        modifier = Modifier.fillMaxSize(),
                        quiz = selectedQuiz!!,
                        timerEnabled = timerEnabled,
                        timerMinutes = timerMinutes,
                        viewModel = quizViewModel,
                        onQuizFinished = { answers, timeTaken ->
                            userAnswers = answers

                            // Save quiz attempt
                            val questions = selectedQuiz!!.questions
                            val correctCount = answers.count { (index, answer) ->
                                questions[index].correctAnswerIndex == answer
                            }

                            val questionAnswers = answers.map { (index, answer) ->
                                QuestionAnswer(
                                    attemptId = 0L,
                                    questionIndex = index,
                                    questionText = questions[index].text,
                                    selectedAnswer = answer,
                                    correctAnswer = questions[index].correctAnswerIndex,
                                    isCorrect = questions[index].correctAnswerIndex == answer
                                )
                            }

                            quizViewModel.saveQuizAttempt(
                                quizTitle = selectedQuiz!!.title,
                                score = correctCount,
                                totalQuestions = questions.size,
                                timeTakenSeconds = timeTaken,
                                timerEnabled = timerEnabled,
                                timerMinutes = timerMinutes,
                                questionAnswers = questionAnswers
                            )

                            currentScreen = Screen.QuizResult
                        }
                    )
                }

                Screen.QuizResult -> {
                    QuizResultScreen(
                        modifier = Modifier.fillMaxSize(),
                        quiz = selectedQuiz!!,
                        userAnswers = userAnswers,
                        onBackToSelection = {
                            currentScreen = Screen.QuizSelection
                            selectedQuiz = null
                            userAnswers = emptyMap()
                            statsViewModel.refresh()
                        }
                    )
                }

                Screen.Notes -> {
                    NotesScreen(
                        modifier = Modifier.fillMaxSize(),
                        selectedChapterId = selectedChapterId,
                        onChapterSelected = { chapterId ->
                            selectedChapterId = chapterId
                        },
                        onContinueLesson = { topic ->
                            selectedTopic = topic
                            currentScreen = Screen.LessonNotes
                        }
                    )
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

                Screen.History -> {
                    HistoryScreen(
                        viewModel = historyViewModel,
                        onAttemptClick = { attemptId ->
                            selectedAttemptId = attemptId
                            currentScreen = Screen.HistoryDetail
                        }
                    )
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

                Screen.Statistics -> {
                    StatisticsScreen(
                        viewModel = statsViewModel
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