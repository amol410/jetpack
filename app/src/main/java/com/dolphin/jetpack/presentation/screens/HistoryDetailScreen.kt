// File: HistoryDetailScreen.kt
package com.dolphin.jetpack.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolphin.jetpack.domain.model.QuizAttempt
import com.dolphin.jetpack.presentation.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    viewModel: HistoryViewModel,
    attemptId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedAttempt by viewModel.selectedAttempt.collectAsState()

    LaunchedEffect(attemptId) {
        viewModel.loadAttemptDetail(attemptId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
            selectedAttempt?.let { attempt ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Section
                    item {
                        AttemptHeaderCard(attempt)
                    }

                    // Statistics Section
                    item {
                        AttemptStatisticsCard(attempt)
                    }

                    // Questions Section
                    item {
                        Text(
                            "Question Breakdown",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    itemsIndexed(attempt.questionAnswers) { index, answer ->
                        QuestionAnswerCard(
                            questionNumber = index + 1,
                            questionAnswer = answer
                        )
                    }
                }
            } ?: run {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun AttemptHeaderCard(attempt: QuizAttempt) {
    val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    val scoreColor = when {
        attempt.percentage >= 90 -> MaterialTheme.colorScheme.primary
        attempt.percentage >= 70 -> MaterialTheme.colorScheme.tertiary
        attempt.percentage >= 50 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = scoreColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = attempt.quizTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dateFormat.format(Date(attempt.dateTime)),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(scoreColor),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${attempt.percentage}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "${attempt.score}/${attempt.totalQuestions}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun AttemptStatisticsCard(attempt: QuizAttempt) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Statistics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            StatRow("Correct Answers", "${attempt.score}", MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            val incorrect = attempt.questionAnswers.count { !it.isCorrect }
            StatRow("Incorrect Answers", "$incorrect", MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))

            val unanswered = attempt.totalQuestions - attempt.questionAnswers.size
            StatRow("Unanswered", "$unanswered", MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            val minutes = attempt.timeTakenSeconds / 60
            val seconds = attempt.timeTakenSeconds % 60
            StatRow(
                "Time Taken",
                if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s",
                MaterialTheme.colorScheme.primary
            )

            if (attempt.timerEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                StatRow("Timer Mode", "${attempt.timerMinutes} minutes", MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun QuestionAnswerCard(
    questionNumber: Int,
    questionAnswer: com.dolphin.jetpack.domain.model.QuestionAnswer
) {
    val backgroundColor = if (questionAnswer.isCorrect) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    }

    val iconColor = if (questionAnswer.isCorrect) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Icon(
                imageVector = if (questionAnswer.isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = if (questionAnswer.isCorrect) "Correct" else "Incorrect",
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Question $questionNumber",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = questionAnswer.questionText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (!questionAnswer.isCorrect) {
                    Text(
                        text = "Your answer: Option ${questionAnswer.selectedAnswer + 1}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Correct answer: Option ${questionAnswer.correctAnswer + 1}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Correct! Option ${questionAnswer.correctAnswer + 1}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp)
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}