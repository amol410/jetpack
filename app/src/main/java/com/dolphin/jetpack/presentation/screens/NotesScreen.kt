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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolphin.jetpack.domain.model.Chapter
import com.dolphin.jetpack.domain.model.Topic
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    selectedChapterId: Int = 1,
    onChapterSelected: (Int) -> Unit = {},
    onContinueLesson: (Topic) -> Unit,
    notesViewModel: com.dolphin.jetpack.presentation.viewmodel.NotesViewModel
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

    // Find the currently selected chapter based on the ID
    val selectedChapter = chapters.find { it.id == localSelectedChapterId }

    // Initialize with first chapter if none is selected or selected chapter doesn't exist
    LaunchedEffect(chapters) {
        if (chapters.isNotEmpty() && selectedChapter == null) {
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

        // Show error message - only when no chapters loaded
        if (error != null && chapters.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "No connection",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Internet Connection",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please check your internet connection",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { notesViewModel.retry() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }

        // Show content when chapters are loaded
        if (chapters.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
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
                                .padding(horizontal = 24.dp)
                                .padding(top = 16.dp, bottom = 8.dp)
                        ) {
                            Text(
                                text = "JETPACK COMPOSE COURSE",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Content Area - Show either chapter list or topic list
                    if (showChapterList) {
                        ChapterListView(
                            chapters = chapters,
                            selectedChapter = selectedChapter,
                            onChapterSelected = { chapter ->
                                localSelectedChapterId = chapter.id
                                onChapterSelected(chapter.id) // Notify parent of the change
                                showChapterList = false
                            },
                            viewModel = notesViewModel
                        )
                    } else {
                        // Show only topics with a change chapter button on the right
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Just the button to change chapter, aligned to the right
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { showChapterList = true },
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text("Chapters")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select chapter"
                                    )
                                }
                            }

                            // Topics only - no chapter name
                            selectedChapter?.let { chapter ->
                                TopicListView(
                                    chapter = chapter,
                                    onContinueLesson = onContinueLesson
                                )
                            }
                        }
                    }
                }

                // Offline indicator at the bottom
                if (error != null && chapters.isNotEmpty()) {
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
    }
}

@Composable
fun ChapterListView(
    chapters: List<Chapter>,
    selectedChapter: Chapter?,
    onChapterSelected: (Chapter) -> Unit,
    viewModel: com.dolphin.jetpack.presentation.viewmodel.NotesViewModel
) {
    // Track manually downloaded status for each chapter
    var offlineStatusMap by remember { mutableStateOf<Map<Int, Boolean>>(emptyMap()) }
    val coroutineScope = rememberCoroutineScope()

    // Track downloading chapters
    val downloadingChapters by viewModel.downloadingChapters.collectAsState()

    // Load manually downloaded status for all chapters
    LaunchedEffect(chapters) {
        if (chapters.isNotEmpty()) {
            val statusMap = mutableMapOf<Int, Boolean>()
            chapters.forEach { chapter ->
                statusMap[chapter.id] = viewModel.isChapterManuallyDownloaded(chapter.id)
            }
            offlineStatusMap = statusMap
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        chapters.forEach { chapter ->
            ChapterCard(
                chapter = chapter,
                isSelected = chapter.id == selectedChapter?.id,
                isOffline = offlineStatusMap[chapter.id] ?: false,
                isDownloading = downloadingChapters.contains(chapter.id),
                onClick = { onChapterSelected(chapter) },
                onOfflineToggle = { makeOffline ->
                    viewModel.toggleChapterOffline(chapter.id, chapter.title, makeOffline)
                    // Update local state immediately for responsiveness
                    coroutineScope.launch {
                        offlineStatusMap = offlineStatusMap + (chapter.id to makeOffline)
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ChapterCard(
    chapter: Chapter,
    isSelected: Boolean,
    isOffline: Boolean,
    isDownloading: Boolean = false,
    onClick: () -> Unit,
    onOfflineToggle: (Boolean) -> Unit
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
                    text = "${chapter.id} ${chapter.title}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Offline toggle button with download progress
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Show circular progress indicator while downloading
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }

                IconButton(
                    onClick = {
                        if (!isDownloading) {
                            onOfflineToggle(!isOffline)
                        }
                    },
                    enabled = !isDownloading
                ) {
                    Icon(
                        imageVector = when {
                            isDownloading -> Icons.Default.CloudDownload
                            isOffline -> Icons.Default.CloudDone
                            else -> Icons.Default.CloudDownload
                        },
                        contentDescription = when {
                            isDownloading -> "Downloading..."
                            isOffline -> "Remove from offline"
                            else -> "Save for offline"
                        },
                        tint = when {
                            isDownloading -> MaterialTheme.colorScheme.primary
                            isOffline -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
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

