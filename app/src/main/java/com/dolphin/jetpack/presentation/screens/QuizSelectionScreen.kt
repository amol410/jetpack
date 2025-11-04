package com.dolphin.jetpack.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
    onSignOut: () -> Unit = {},
    quizListViewModel: QuizListViewModel = viewModel()
) {
    var showTimerDialog by remember { mutableStateOf(false) }
    var selectedQuiz by remember { mutableStateOf<Quiz?>(null) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    // Observe ViewModel state
    val quizzes by quizListViewModel.quizzes.collectAsState()
    val isLoading by quizListViewModel.isLoading.collectAsState()
    val error by quizListViewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Quiz") },
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
                    IconButton(onClick = { showSignOutDialog = true }) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Sign Out"
                        )
                    }
                }
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
                    items(quizzes) { quiz ->
                        QuizCard(
                            quiz = quiz,
                            hasResumeState = hasResumeState[quiz.title] == true,
                            onQuizClick = {
                                selectedQuiz = quiz
                                showTimerDialog = true
                            },
                            onResumeClick = {
                                onResumeQuiz(quiz)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showTimerDialog && selectedQuiz != null) {
        val totalQuestions = selectedQuiz!!.questions.size
        var selectedMinutes by remember { mutableIntStateOf(10) } // Default to 10 minutes
        
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Start Quiz", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("How much time do you need to solve $totalQuestions questions?", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        listOf(10, 20, 30).forEach { minutes ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMinutes = minutes }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedMinutes == minutes,
                                    onClick = { selectedMinutes = minutes }
                                )
                                Text(
                                    text = "$minutes minutes",
                                    fontSize = 16.sp,
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
                        }
                    ) {
                        Text("START QUIZ", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = null // Remove the dismiss button (No Timer option)
        )
        
        // Update the selectedQuiz's timerMinutes when the minutes change
        LaunchedEffect(selectedMinutes) {
            // This will set the initial value for timerMinutes in MainActivity
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            icon = {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun QuizCard(
    quiz: Quiz,
    hasResumeState: Boolean,
    onQuizClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onQuizClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = quiz.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${quiz.questions.size} questions",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (hasResumeState) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onResumeClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Resume Quiz")
                }
            }
        }
    }
}