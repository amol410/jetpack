// File: StatisticsScreen.kt
package com.dolphin.jetpack.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Warning
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
import com.dolphin.jetpack.ui.theme.*
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Custom illustration using Compose shapes
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "No Statistics Available",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "You haven't solved any quizzes yet, who knows?",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { viewModel.refresh() }
                        ) {
                            Text("Refresh")
                        }
                    }
                }
                is StatisticsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Custom illustration using Compose shapes
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Failed to Load Statistics",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        
                        if (!state.message.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "No statistics data available: ${state.message}. Try again!",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Check your connection and try again",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { viewModel.refresh() }
                        ) {
                            Text("Try Again")
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
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Overall Performance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCircle(
                    value = "${statistics.totalAttempts}",
                    label = "Total Quizzes",
                    color = InfoBlue,
                    darkColor = InfoBlueDark
                )

                StatCircle(
                    value = "${statistics.averageScore.toInt()}%",
                    label = "Average Score",
                    color = WarningYellow,
                    darkColor = WarningYellowDark
                )

                StatCircle(
                    value = "${statistics.bestScore}%",
                    label = "Best Score",
                    color = SuccessGreen,
                    darkColor = SuccessGreenDark
                )
            }
        }
    }
}

@Composable
fun StatCircle(value: String, label: String, color: Color, darkColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .border(width = 2.dp, color = PrimaryLight, shape = CircleShape)
                .clip(CircleShape)
                .background(color = color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = darkColor,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun QuizPerformanceCard(performance: com.dolphin.jetpack.domain.model.QuizPerformance) {
    val scoreColor = when {
        performance.averageScore >= 90 -> MaterialTheme.colorScheme.primary
        performance.averageScore >= 70 -> MaterialTheme.colorScheme.tertiary
        performance.averageScore >= 50 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = performance.quizTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${performance.attemptCount} attempts",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color = scoreColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${performance.averageScore.toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun WrongQuestionCard(wrongQuestion: com.dolphin.jetpack.domain.model.WrongQuestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
        ),
        shape = MaterialTheme.shapes.small
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
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${wrongQuestion.wrongCount}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun ImprovementChartCard(statistics: QuizStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Your Progress",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleLarge,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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