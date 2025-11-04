package com.dolphin.jetpack.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.dolphin.jetpack.domain.model.Topic
import com.dolphin.jetpack.presentation.viewmodel.NotesViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonNotesScreen(
    topic: Topic,
    onBack: () -> Unit,
    notesViewModel: NotesViewModel = viewModel()
) {
    // Load notes for this topic when screen opens
    LaunchedEffect(topic.id) {
        notesViewModel.loadNotes(topic.id)
    }

    // Observe notes state
    val notes by notesViewModel.notes.collectAsState()
    val isLoadingNotes by notesViewModel.isLoadingNotes.collectAsState()
    val error by notesViewModel.error.collectAsState()

    // Handle back button press
    BackHandler {
        onBack()
    }

    // Track swipe gesture
    var offsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // If swiped more than 100dp to the right, go back
                        if (offsetX > 100) {
                            onBack()
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        // Only allow right swipe (positive values)
                        val newOffset = offsetX + dragAmount
                        if (newOffset >= 0) {
                            offsetX = newOffset
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Centered Topic Name with Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = topic.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Show loading indicator
                if (isLoadingNotes) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Notes content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Show notes if available
                    if (notes.isNotEmpty()) {
                        notes.forEach { note ->
                            // Note title
                            Text(
                                text = note.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Note content without background
                            Text(
                                text = androidx.core.text.HtmlCompat.fromHtml(
                                    note.content,
                                    androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
                                ).toString(),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                lineHeight = 24.sp,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Divider between notes
                            if (note != notes.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    } else if (!isLoadingNotes) {
                        // Fallback to topic content if no notes are available
                        if (error != null) {
                            Text(
                                text = "Failed to load notes. Showing topic description.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        } else {
                            Text(
                                text = "No notes available for this topic yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        // Show topic content as fallback
                        if (topic.content.isNotEmpty()) {
                            Text(
                                text = "Topic Content",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = topic.content,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                lineHeight = 24.sp
                            )
                        }
                    }

                    // Bottom spacing
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Floating back button overlay (doesn't take space in layout)
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
                .zIndex(10f)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
