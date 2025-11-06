package com.dolphin.jetpack.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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

import com.dolphin.jetpack.domain.model.Quiz
import com.dolphin.jetpack.domain.model.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    modifier: Modifier = Modifier,
    quiz: Quiz,
    userAnswers: Map<Int, Int>,
    onBackToSelection: () -> Unit
) {
    val correctCount = userAnswers.count { (index, answer) ->
        quiz.questions[index].correctAnswerIndex == answer
    }
    val totalQuestions = quiz.questions.size
    val percentage = (correctCount * 100) / totalQuestions

    val resultColor = when {
        percentage >= 90 -> MaterialTheme.colorScheme.primary
        percentage >= 70 -> MaterialTheme.colorScheme.tertiary
        percentage >= 50 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    val resultMessage = when {
        percentage >= 90 -> "Excellent! 🎉"
        percentage >= 70 -> "Great Job! 👍"
        percentage >= 50 -> "Good Effort! 💪"
        else -> "Keep Practicing! 📚"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Results") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Score card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = resultColor.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = resultMessage,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = resultColor
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .background(resultColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$percentage%",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = "$correctCount/$totalQuestions",
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = quiz.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Question breakdown
            item {
                Text(
                    "Answer Review",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            itemsIndexed(quiz.questions) { index, question ->
                val userAnswer = userAnswers[index]
                val isCorrect = userAnswer == question.correctAnswerIndex
                val wasAnswered = userAnswer != null

                QuizResultQuestionCard(
                    questionNumber = index + 1,
                    question = question,
                    userAnswer = userAnswer,
                    isCorrect = isCorrect,
                    wasAnswered = wasAnswered
                )
            }

            // Back button
            item {
                Button(
                    onClick = onBackToSelection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Back to Quiz Selection")
                }
            }
        }
    }
}

@Composable
fun QuizResultQuestionCard(
    questionNumber: Int,
    question: com.dolphin.jetpack.domain.model.Question,
    userAnswer: Int?,
    isCorrect: Boolean,
    wasAnswered: Boolean
) {
    val backgroundColor = when {
        !wasAnswered -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
        isCorrect -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
    }

    val iconColor = when {
        !wasAnswered -> MaterialTheme.colorScheme.onSurfaceVariant
        isCorrect -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = if (isCorrect || !wasAnswered) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Question $questionNumber",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = question.text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            when {
                !wasAnswered -> {
                    Text(
                        text = "Not answered",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Correct answer: ${question.options[question.correctAnswerIndex]}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                isCorrect -> {
                    Text(
                        text = "✓ Your answer: ${question.options[userAnswer!!]}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                else -> {
                    Text(
                        text = "✗ Your answer: ${question.options[userAnswer!!]}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "✓ Correct answer: ${question.options[question.correctAnswerIndex]}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }


        }
    }
}