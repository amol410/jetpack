// File: StatisticsScreen.kt
package com.dolphin.jetpack.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolphin.jetpack.domain.model.QuizStatistics
import com.dolphin.jetpack.fcm.FCMTokenManager
import com.dolphin.jetpack.presentation.components.LineChart
import com.dolphin.jetpack.presentation.viewmodel.StatisticsUiState
import com.dolphin.jetpack.presentation.viewmodel.StatisticsViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier
) {
    val statsState by viewModel.statsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
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
            when (val state = statsState) {
                is StatisticsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is StatisticsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Overall Statistics
                        item {
                            Text(
                                "Overall Performance",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            OverallStatsCard(state.statistics)
                        }

                        // Quiz-wise Performance
                        item {
                            Text(
                                "Quiz-wise Performance",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(state.statistics.quizWisePerformance) { performance ->
                            QuizPerformanceCard(performance)
                        }

                        // Most Wrong Questions
                        if (state.statistics.mostWrongQuestions.isNotEmpty()) {
                            item {
                                Text(
                                    "Need More Practice",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            items(state.statistics.mostWrongQuestions.take(5)) { wrongQ ->
                                WrongQuestionCard(wrongQ)
                            }
                        }

                        // Improvement Chart
                        if (state.statistics.improvementData.isNotEmpty()) {
                            item {
                                Text(
                                    "Progress Over Time",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            item {
                                ImprovementChartCard(state.statistics)
                            }
                        }

                        // Developer Tools Section
                        item {
                            Text(
                                "Developer Tools",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        item {
                            DeveloperToolsCard()
                        }
                    }
                }
                is StatisticsUiState.Empty -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No statistics available",
                            fontSize = 20.sp,
                            color = Color.Gray
                        )
                        Text(
                            "Complete a quiz to see your statistics",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                is StatisticsUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OverallStatsCard(statistics: QuizStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCircle(
                    value = "${statistics.totalAttempts}",
                    label = "Total Quizzes",
                    color = Color(0xFF2196F3)
                )

                StatCircle(
                    value = "${statistics.averageScore.toInt()}%",
                    label = "Average Score",
                    color = Color(0xFFFF9800)
                )

                StatCircle(
                    value = "${statistics.bestScore}%",
                    label = "Best Score",
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun StatCircle(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}

@Composable
fun QuizPerformanceCard(performance: com.dolphin.jetpack.domain.model.QuizPerformance) {
    val scoreColor = when {
        performance.averageScore >= 90 -> Color(0xFF4CAF50)
        performance.averageScore >= 70 -> Color(0xFF2196F3)
        performance.averageScore >= 50 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = performance.quizTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${performance.attemptCount} attempts",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(scoreColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${performance.averageScore.toInt()}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }
        }
    }
}

@Composable
fun WrongQuestionCard(wrongQuestion: com.dolphin.jetpack.domain.model.WrongQuestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = wrongQuestion.questionText,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFF44336)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${wrongQuestion.wrongCount}×",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ImprovementChartCard(statistics: QuizStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Your Progress",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val chartData = statistics.improvementData.mapIndexed { index, point ->
                (index.toFloat() to point.score.toFloat())
            }

            LineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                data = chartData
            )
        }
    }
}

@Composable
fun DeveloperToolsCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fcmToken by remember { mutableStateOf("Loading...") }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        fcmToken = FCMTokenManager.getToken() ?: "Failed to get token"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // FCM Token Section
            Text(
                "FCM Device Token",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fcmToken,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    color = Color.Gray
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("FCM Token", fcmToken)
                        clipboard.setPrimaryClip(clip)
                        snackbarMessage = "Token copied to clipboard"
                        showSnackbar = true
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, "Copy token")
                }
            }

            HorizontalDivider()

            // Test Crashlytics
            OutlinedButton(
                onClick = {
                    FirebaseCrashlytics.getInstance().log("Test crash button clicked")
                    throw RuntimeException("Test Crash from Developer Tools")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.BugReport, "Test crash")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Crashlytics (Will crash app)")
            }

            // Test Notification Permission
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val hasPermission = FCMTokenManager.hasNotificationPermission(context)
                        snackbarMessage = if (hasPermission) {
                            "Notification permission granted"
                        } else {
                            "Notification permission not granted"
                        }
                        showSnackbar = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Notifications, "Check permission")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Check Notification Permission")
            }

            // Subscribe to Topic
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val success = FCMTokenManager.subscribeToTopic("quiz_updates")
                        snackbarMessage = if (success) {
                            "Subscribed to quiz_updates topic"
                        } else {
                            "Failed to subscribe to topic"
                        }
                        showSnackbar = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Notifications, "Subscribe")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Subscribe to 'quiz_updates' Topic")
            }
        }
    }

    if (showSnackbar) {
        LaunchedEffect(snackbarMessage) {
            kotlinx.coroutines.delay(2000)
            showSnackbar = false
        }

        Snackbar(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(snackbarMessage)
        }
    }
}