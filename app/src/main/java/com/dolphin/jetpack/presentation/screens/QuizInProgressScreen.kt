package com.dolphin.jetpack.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolphin.jetpack.domain.model.Quiz
import com.dolphin.jetpack.domain.model.QuizState
import com.dolphin.jetpack.presentation.viewmodel.QuizViewModel
import com.dolphin.jetpack.presentation.viewmodel.QuizUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizInProgressScreen(
    modifier: Modifier = Modifier,
    quiz: Quiz,
    timerEnabled: Boolean,
    timerMinutes: Int,
    viewModel: QuizViewModel,
    onQuizFinished: (Map<Int, Int>, Long) -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var answers by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var timeRemaining by remember { mutableStateOf(timerMinutes * 60) }

    // Check for loaded state
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is QuizUiState.StateLoaded) {
            val state = (uiState as QuizUiState.StateLoaded).state
            currentQuestionIndex = state.currentQuestionIndex
            answers = state.answers
            selectedOption = state.answers[currentQuestionIndex]
            startTime = state.startTime
            if (state.timeRemaining != null) {
                timeRemaining = state.timeRemaining
            }
            viewModel.resetState()
        }
    }

    // Timer countdown
    LaunchedEffect(timerEnabled, timeRemaining) {
        if (timerEnabled && timeRemaining > 0) {
            delay(1000)
            timeRemaining--
            if (timeRemaining == 0) {
                val timeTaken = (System.currentTimeMillis() - startTime) / 1000
                viewModel.deleteQuizState(quiz.title)
                onQuizFinished(answers, timeTaken)
            }
        }
    }

    // Auto-save state periodically
    LaunchedEffect(currentQuestionIndex, answers) {
        delay(2000) // Save every 2 seconds
        val state = QuizState(
            quizTitle = quiz.title,
            currentQuestionIndex = currentQuestionIndex,
            answers = answers,
            startTime = startTime,
            timerEnabled = timerEnabled,
            timerMinutes = timerMinutes,
            timeRemaining = if (timerEnabled) timeRemaining else null
        )
        viewModel.saveQuizState(state)
    }

    val currentQuestion = quiz.questions[currentQuestionIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("${quiz.title}")
                        Text(
                            "Question ${currentQuestionIndex + 1} of ${quiz.questions.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                },
                actions = {
                    if (timerEnabled) {
                        val minutes = timeRemaining / 60
                        val seconds = timeRemaining % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeRemaining < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Progress indicator
                LinearProgressIndicator(
                    progress = (currentQuestionIndex + 1).toFloat() / quiz.questions.size,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Question
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(
                        text = currentQuestion.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Options
                currentQuestion.options.forEachIndexed { index, option ->
                    val isSelected = selectedOption == index

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .selectable(
                                selected = isSelected,
                                onClick = { selectedOption = index }
                            ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isSelected) 8.dp else 2.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedOption = index }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentQuestionIndex > 0) {
                            currentQuestionIndex--
                            selectedOption = answers[currentQuestionIndex]
                        }
                    },
                    enabled = currentQuestionIndex > 0
                ) {
                    Text("Previous")
                }

                Button(
                    onClick = {
                        // Save answer if selected
                        selectedOption?.let {
                            answers = answers + (currentQuestionIndex to it)
                        }

                        if (currentQuestionIndex < quiz.questions.size - 1) {
                            currentQuestionIndex++
                            selectedOption = answers[currentQuestionIndex]
                        } else {
                            // Quiz finished
                            val timeTaken = (System.currentTimeMillis() - startTime) / 1000
                            viewModel.deleteQuizState(quiz.title)
                            onQuizFinished(answers, timeTaken)
                        }
                    },
                    enabled = selectedOption != null
                ) {
                    Text(
                        if (currentQuestionIndex == quiz.questions.size - 1) "Finish" else "Next"
                    )
                }
            }
        }
    }
}