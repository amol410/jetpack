package com.dolphin.jetpack.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dolphin.jetpack.domain.model.Quiz
import com.dolphin.jetpack.presentation.viewmodel.QuizListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSelectionScreen(
    modifier: Modifier = Modifier,
    onQuizSelected: (Quiz, Boolean, Int) -> Unit,
    hasResumeState: Map<String, Boolean>,
    onResumeQuiz: (Quiz) -> Unit,
    onSettingsClick: () -> Unit = {},
    quizListViewModel: QuizListViewModel
) {
    var showTimerDialog by remember { mutableStateOf(false) }
    var selectedQuiz by remember { mutableStateOf<Quiz?>(null) }

    // Observe ViewModel state
    val quizzes by quizListViewModel.quizzes.collectAsState()
    val isLoading by quizListViewModel.isLoading.collectAsState()
    val error by quizListViewModel.error.collectAsState()

    // Track offline status for each quiz
    var offlineStatusMap by remember { mutableStateOf<Map<Int, Boolean>>(emptyMap()) }

    // Track downloading quizzes
    val downloadingQuizzes by quizListViewModel.downloadingQuizzes.collectAsState()

    // Load manually downloaded status for all quizzes
    LaunchedEffect(quizzes) {
        if (quizzes.isNotEmpty()) {
            val statusMap = mutableMapOf<Int, Boolean>()
            quizzes.forEach { quiz ->
                statusMap[quiz.id] = quizListViewModel.isQuizManuallyDownloaded(quiz.id)
            }
            offlineStatusMap = statusMap
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Select Quiz", 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                actions = {
                    // Retry button when there's an error
                    if (error != null) {
                        IconButton(onClick = { quizListViewModel.retry() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Retry"
                            )
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // Show loading indicator
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Show error message with retry option
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Failed to load quizzes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error ?: "Unknown error",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Showing local quizzes instead",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Show quizzes list (either from API or fallback local data)
            if (!isLoading) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(quizzes.size) { index ->
                        val quiz = quizzes[index]
                        QuizCard(
                            quiz = quiz,
                            quizId = quiz.id,
                            hasResumeState = hasResumeState[quiz.title] == true,
                            isOffline = offlineStatusMap[quiz.id] ?: false,
                            isDownloading = downloadingQuizzes.contains(quiz.id),
                            onQuizClick = {
                                selectedQuiz = quiz
                                showTimerDialog = true
                            },
                            onResumeClick = {
                                onResumeQuiz(quiz)
                            },
                            onOfflineToggle = { id, makeOffline ->
                                quizListViewModel.toggleQuizOffline(id, quiz.title, makeOffline)
                                // Update local state immediately for responsiveness
                                offlineStatusMap = offlineStatusMap + (id to makeOffline)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showTimerDialog && selectedQuiz != null) {
        val totalQuestions = selectedQuiz!!.questionCount
        var selectedMinutes by remember { mutableIntStateOf(10) } // Default to 10 minutes
        
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { 
                Text(
                    "Start Quiz", 
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                ) 
            },
            text = {
                Column {
                    Text(
                        "How much time do you need to solve $totalQuestions questions?", 
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        listOf(10, 20, 30).forEach { minutes ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMinutes = minutes }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedMinutes == minutes,
                                    onClick = { selectedMinutes = minutes }
                                )
                                Text(
                                    text = "$minutes minutes",
                                    fontSize = 16.sp,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            showTimerDialog = false
                            onQuizSelected(selectedQuiz!!, true, selectedMinutes) // Always use timer with selected minutes
                        },
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text("START QUIZ", fontWeight = FontWeight.Medium)
                    }
                }
            },
            dismissButton = null, // Remove the dismiss button (No Timer option)
            shape = MaterialTheme.shapes.medium
        )
        
        // Update the selectedQuiz's timerMinutes when the minutes change
        LaunchedEffect(selectedMinutes) {
            // This will set the initial value for timerMinutes in MainActivity
        }
    }


}

@Composable
fun QuizCard(
    quiz: Quiz,
    quizId: Int,
    hasResumeState: Boolean,
    isOffline: Boolean,
    isDownloading: Boolean = false,
    onQuizClick: () -> Unit,
    onResumeClick: () -> Unit,
    onOfflineToggle: (Int, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onQuizClick)
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quiz.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Offline toggle button with progress
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        // Show circular progress indicator while downloading
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        }

                        IconButton(
                            onClick = {
                                if (!isDownloading) {
                                    onOfflineToggle(quizId, !isOffline)
                                }
                            },
                            enabled = !isDownloading
                        ) {
                            Icon(
                                imageVector = when {
                                    isDownloading -> Icons.Default.CloudDownload
                                    isOffline -> Icons.Default.CloudDone
                                    else -> Icons.Default.CloudDownload
                                },
                                contentDescription = when {
                                    isDownloading -> "Downloading..."
                                    isOffline -> "Remove from offline"
                                    else -> "Save for offline"
                                },
                                tint = when {
                                    isDownloading -> MaterialTheme.colorScheme.primary
                                    isOffline -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${quiz.questionCount} questions",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (hasResumeState) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onResumeClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        "Resume Quiz",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}