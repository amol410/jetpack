package com.dolphin.jetpack.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSettingsScreen(
    modifier: Modifier = Modifier,
    selectedMinutes: Int,
    onMinutesSelected: (Int) -> Unit,
    onBack: () -> Unit,
    quiz: com.dolphin.jetpack.domain.model.Quiz? = null
) {
    val timeOptions = listOf(10, 20, 30) // Only 10, 20, 30 minute options
    val totalQuestions = quiz?.questions?.size ?: 3 // Default to 3 if quiz is null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Quiz Duration (Quiz has $totalQuestions questions)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Select quiz duration:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(timeOptions) { minutes ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMinutesSelected(minutes) },
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (minutes == selectedMinutes) 8.dp else 2.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (minutes == selectedMinutes)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$minutes minutes",
                            fontSize = 18.sp,
                            fontWeight = if (minutes == selectedMinutes) FontWeight.Bold else FontWeight.Normal
                        )
                        if (minutes == selectedMinutes) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}