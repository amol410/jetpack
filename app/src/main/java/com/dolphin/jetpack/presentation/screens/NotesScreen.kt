package com.dolphin.jetpack.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolphin.jetpack.domain.model.Chapter
import com.dolphin.jetpack.domain.model.Topic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    selectedChapterId: Int = 1,
    onChapterSelected: (Int) -> Unit = {},
    onContinueLesson: (Topic) -> Unit,
    notesViewModel: com.dolphin.jetpack.presentation.viewmodel.NotesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // Use a state variable to persist the selected chapter across recompositions
    var localSelectedChapterId by rememberSaveable { mutableStateOf(selectedChapterId) }

    // Update local state when the prop changes
    LaunchedEffect(selectedChapterId) {
        localSelectedChapterId = selectedChapterId
    }
    var showChapterList by remember { mutableStateOf(false) }

    // Observe ViewModel state
    val chapters by notesViewModel.chapters.collectAsState()
    val isLoading by notesViewModel.isLoading.collectAsState()
    val error by notesViewModel.error.collectAsState()

    // Calculate overall course completion based on all topics across all chapters (dynamically)
    val totalTopics = chapters.sumOf { chapter -> chapter.topics.size }
    val completedTopics = chapters.sumOf { chapter ->
        chapter.topics.count { topic -> topic.isCompleted }
    }
    val overallCompletionPercentage = if (totalTopics > 0) {
        (completedTopics.toFloat() / totalTopics * 100).toInt()
    } else 0

    // Find the currently selected chapter based on the ID
    val selectedChapter = chapters.find { it.id == localSelectedChapterId }

    // Initialize with first chapter if none is selected
    LaunchedEffect(chapters) {
        if (localSelectedChapterId == 0 && chapters.isNotEmpty()) {
            localSelectedChapterId = chapters.first().id
            onChapterSelected(localSelectedChapterId) // Notify parent of the change
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Show loading indicator
        if (isLoading && chapters.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Show error message
        if (error != null && chapters.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Failed to load chapters",
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
                Button(onClick = { notesViewModel.retry() }) {
                    Text("Retry")
                }
            }
        }

        // Show content when chapters are loaded
        if (chapters.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "JETPACK COMPOSE COURSE",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            // Show error indicator if there's an error but we have fallback data
                            if (error != null) {
                                Text(
                                    text = "Using offline data",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chapter Selection Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Chapter ${selectedChapter?.id ?: 1}:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedChapter?.title ?: "",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Chapters Dropdown Button
                    OutlinedButton(
                        onClick = { showChapterList = !showChapterList },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (showChapterList)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                Color.Transparent
                        )
                    ) {
                        Text("Chapters")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (showChapterList)
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.ArrowDropDown,
                            contentDescription = "Toggle chapters"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content Area - Show either chapter list or topic list
                if (showChapterList) {
                    ChapterListView(
                        chapters = chapters,
                        selectedChapter = selectedChapter,
                        onChapterSelected = { chapter ->
                            localSelectedChapterId = chapter.id
                            onChapterSelected(chapter.id) // Notify parent of the change
                            showChapterList = false
                        }
                    )
                } else {
                    selectedChapter?.let { chapter ->
                        TopicListView(
                            chapter = chapter,
                            onContinueLesson = onContinueLesson
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterListView(
    chapters: List<Chapter>,
    selectedChapter: Chapter?,
    onChapterSelected: (Chapter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        // View Course Summary Link
        TextButton(
            onClick = { /* TODO: Navigate to course summary */ },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "View Course summary →",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        chapters.forEach { chapter ->
            ChapterCard(
                chapter = chapter,
                isSelected = chapter.id == selectedChapter?.id,
                onClick = { onChapterSelected(chapter) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ChapterCard(
    chapter: Chapter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${chapter.id} ${chapter.title}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Progress Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            ) {
                Text(
                    text = "${chapter.completionPercentage}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TopicListView(
    chapter: Chapter,
    onContinueLesson: (Topic) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
    ) {
        chapter.topics.forEachIndexed { index, topic ->
            TopicItem(
                topic = topic,
                topicNumber = index + 1,
                onClick = { onContinueLesson(topic) }
            )

            if (index < chapter.topics.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TopicItem(
    topic: Topic,
    topicNumber: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Number and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Topic number without background
                Text(
                    text = "$topicNumber.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (topic.isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(end = 16.dp)
                )

                // Topic title
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                    if (topic.isCompleted) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Completed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Right side: Checkmark for completed or arrow for not completed
            if (topic.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Start",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(270f) // Rotate to point right
                )
            }
        }
    }
}

