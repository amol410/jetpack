package com.dolphin.jetpack.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.core.text.HtmlCompat
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.RelativeSizeSpan
import android.text.style.AbsoluteSizeSpan

/**
 * Converts HTML string to AnnotatedString with proper formatting
 * Supports: bold, italic, headers (h1-h6), and other HTML tags
 */
private fun htmlToAnnotatedString(html: String, baseFontSize: Float = 16f): AnnotatedString {
    val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)

    return buildAnnotatedString {
        append(spanned.toString())

        // Apply all spans from the HTML
        spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)

            when (span) {
                is StyleSpan -> {
                    when (span.style) {
                        android.graphics.Typeface.BOLD -> {
                            addStyle(
                                style = SpanStyle(fontWeight = FontWeight.Bold),
                                start = start,
                                end = end
                            )
                        }
                        android.graphics.Typeface.ITALIC -> {
                            addStyle(
                                style = SpanStyle(fontWeight = FontWeight.Normal, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                start = start,
                                end = end
                            )
                        }
                        android.graphics.Typeface.BOLD_ITALIC -> {
                            addStyle(
                                style = SpanStyle(fontWeight = FontWeight.Bold, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                start = start,
                                end = end
                            )
                        }
                    }
                }
                is RelativeSizeSpan -> {
                    // Handle relative size changes (e.g., from <h1>, <h2>, etc.)
                    val newSize = baseFontSize * span.sizeChange
                    addStyle(
                        style = SpanStyle(fontSize = newSize.sp),
                        start = start,
                        end = end
                    )
                }
                is AbsoluteSizeSpan -> {
                    // Handle absolute size changes
                    val size = if (span.dip) {
                        span.size.toFloat()
                    } else {
                        // Convert px to sp (approximate)
                        span.size / 1.5f
                    }
                    addStyle(
                        style = SpanStyle(fontSize = size.sp),
                        start = start,
                        end = end
                    )
                }
            }
        }
    }
}

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

                            // Note content with HTML formatting preserved
                            Text(
                                text = htmlToAnnotatedString(note.content, baseFontSize = 16f),
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
                                text = "No notes available. Showing topic description.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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

                    // Bottom spacing to account for offline indicator
                    Spacer(modifier = Modifier.height(if (error != null && notes.isNotEmpty()) 60.dp else 24.dp))
                }

                // Offline indicator at the bottom
                if (error != null && notes.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Offline",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Using offline data",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Floating back button overlay (doesn't take space in layout)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
                .zIndex(10f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
