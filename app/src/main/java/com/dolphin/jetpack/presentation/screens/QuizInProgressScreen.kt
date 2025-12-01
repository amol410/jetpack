package com.dolphin.jetpack.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolphin.jetpack.domain.model.Quiz
import com.dolphin.jetpack.domain.model.QuizState
import com.dolphin.jetpack.presentation.viewmodel.QuizUiState
import com.dolphin.jetpack.presentation.viewmodel.QuizViewModel
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
    var visitedQuestions by remember { mutableStateOf<Set<Int>>(setOf(0)) }
    var markedForReview by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isSummaryDrawerOpen by remember { mutableStateOf(false) }
    val questionCount = quiz.questions.size
    // Position the handle just below the progress line near the top of the screen
    val handleTopOffset = 48.dp

    fun persistCurrentSelection() {
        selectedOption?.let {
            answers = answers + (currentQuestionIndex to it)
        }
    }

    // Check for loaded state
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        if (uiState is QuizUiState.StateLoaded) {
            val state = (uiState as QuizUiState.StateLoaded).state
            currentQuestionIndex = state.currentQuestionIndex
            answers = state.answers
            selectedOption = state.answers[currentQuestionIndex]
            startTime = state.startTime
            visitedQuestions = state.answers.keys + state.currentQuestionIndex
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

    LaunchedEffect(currentQuestionIndex) {
        visitedQuestions = visitedQuestions + currentQuestionIndex
    }

    val currentQuestion = quiz.questions[currentQuestionIndex]

    val attemptedCount = answers.size
    val notVisitedCount = (questionCount - visitedQuestions.size).coerceAtLeast(0)
    val notAnsweredCount = visitedQuestions.count { it !in answers }
    val markedCount = markedForReview.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            quiz.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Question ${currentQuestionIndex + 1} of ${quiz.questions.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            markedForReview = if (markedForReview.contains(currentQuestionIndex)) {
                                markedForReview - currentQuestionIndex
                            } else {
                                markedForReview + currentQuestionIndex
                            }
                        }
                    ) {
                        val isMarked = markedForReview.contains(currentQuestionIndex)
                        Icon(
                            imageVector = if (isMarked) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Mark for review"
                        )
                    }

                    if (timerEnabled) {
                        val minutes = timeRemaining / 60
                        val seconds = timeRemaining % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeRemaining < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(end = 16.dp)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Progress indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = (currentQuestionIndex + 1).toFloat() / quiz.questions.size,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${currentQuestionIndex + 1}/${quiz.questions.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question with mark indicator
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentQuestion.text,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
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
                                defaultElevation = if (isSelected) 6.dp else 2.dp
                            ),
                            shape = MaterialTheme.shapes.small,
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
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
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
                            persistCurrentSelection()
                            if (currentQuestionIndex > 0) {
                                currentQuestionIndex--
                                selectedOption = answers[currentQuestionIndex]
                            }
                        },
                        enabled = currentQuestionIndex > 0,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            "Previous",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            persistCurrentSelection()

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
                        enabled = true,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            if (currentQuestionIndex == quiz.questions.size - 1) "Finish" else "Next",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right-edge handle to open the drawer
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = handleTopOffset, end = 6.dp)
                    .clickable { isSummaryDrawerOpen = true }
            ) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(112.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Open summary drawer",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Slide-in drawer that reuses the summary UI
            AnimatedVisibility(
                visible = isSummaryDrawerOpen,
                modifier = Modifier.matchParentSize(),
                enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }) + fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .fillMaxWidth(0.75f),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                        tonalElevation = 6.dp
                    ) {
                        SummarySheetContent(
                            quizTitle = quiz.title,
                            questionCount = questionCount,
                            attempted = attemptedCount,
                            notVisited = notVisitedCount,
                            notAnswered = notAnsweredCount,
                            marked = markedCount,
                            markedForReview = markedForReview,
                            answers = answers,
                            visitedQuestions = visitedQuestions,
                            currentQuestionIndex = currentQuestionIndex,
                            onQuestionSelected = { index ->
                                persistCurrentSelection()
                                currentQuestionIndex = index
                                selectedOption = answers[index]
                                isSummaryDrawerOpen = false
                            },
                            onSubmit = {
                                persistCurrentSelection()
                                val timeTaken = (System.currentTimeMillis() - startTime) / 1000
                                viewModel.deleteQuizState(quiz.title)
                                onQuizFinished(answers, timeTaken)
                            },
                            onClose = { isSummaryDrawerOpen = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummarySheetContent(
    quizTitle: String,
    questionCount: Int,
    attempted: Int,
    notVisited: Int,
    notAnswered: Int,
    marked: Int,
    markedForReview: Set<Int>,
    answers: Map<Int, Int>,
    visitedQuestions: Set<Int>,
    currentQuestionIndex: Int,
    onQuestionSelected: (Int) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit
) {
    val statusColors = mapOf(
        "Attempted" to Color(0xFF4CAF50),
        "Not Visited" to Color(0xFF9E9E9E),
        "Not Answered" to Color(0xFFFFA000),
        "Marked" to Color(0xFFFFC107),
        "Current" to MaterialTheme.colorScheme.primary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClose() }
    ) {
        Text(
            text = quizTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LegendRow(
            items = listOf(
                "Attempted" to attempted,
                "Not Visited" to notVisited,
                "Not Answered" to notAnswered,
                "Marked" to marked
            ),
            colors = statusColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Grid",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
        ) {
            items((0 until questionCount).toList()) { index ->
                val isCurrent = index == currentQuestionIndex
                val isMarked = markedForReview.contains(index)
                val isAttempted = answers.containsKey(index)
                val isVisited = visitedQuestions.contains(index)
                val statusColor = when {
                    isCurrent -> statusColors.getValue("Current")
                    isMarked -> statusColors.getValue("Marked")
                    isAttempted -> statusColors.getValue("Attempted")
                    isVisited -> statusColors.getValue("Not Answered")
                    else -> statusColors.getValue("Not Visited")
                }

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(44.dp)
                        .border(
                            width = 1.dp,
                            color = statusColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onQuestionSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Submit Test",
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LegendRow(
    items: List<Pair<String, Int>>,
    colors: Map<String, Color>
) {
    Column {
        items.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { (label, count) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(
                                    color = colors[label] ?: MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$label ($count)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
