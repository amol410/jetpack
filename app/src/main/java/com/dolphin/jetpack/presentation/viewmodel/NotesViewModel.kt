package com.dolphin.jetpack.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.jetpack.data.remote.NetworkResult
import com.dolphin.jetpack.data.repository.ContentRepository
import com.dolphin.jetpack.domain.model.Chapter
import com.dolphin.jetpack.domain.model.Topic
import com.dolphin.jetpack.domain.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: ContentRepository
) : ViewModel() {

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedTopic = MutableStateFlow<Topic?>(null)
    val selectedTopic: StateFlow<Topic?> = _selectedTopic.asStateFlow()

    private val _isLoadingTopic = MutableStateFlow(false)
    val isLoadingTopic: StateFlow<Boolean> = _isLoadingTopic.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isLoadingNotes = MutableStateFlow(false)
    val isLoadingNotes: StateFlow<Boolean> = _isLoadingNotes.asStateFlow()

    private val _isUsingOfflineData = MutableStateFlow(false)
    val isUsingOfflineData: StateFlow<Boolean> = _isUsingOfflineData.asStateFlow()

    // Download progress tracking
    private val _downloadingChapters = MutableStateFlow<Set<Int>>(emptySet())
    val downloadingChapters: StateFlow<Set<Int>> = _downloadingChapters.asStateFlow()

    // For optimistic UI updates
    private var lastLoadedChapters: List<Chapter> = emptyList()
    private var lastLoadedTopic: Topic? = null
    private var lastLoadedNotes: List<Note> = emptyList()

    init {
        // Load chapters with optimistic approach
        loadChaptersWithOptimisticUpdate()
    }

    fun loadChaptersWithOptimisticUpdate() {
        viewModelScope.launch {
            // First, try to show local data if available (optimistic UI)
            // For now, we'll still proceed with the API call as there's no local storage implemented for chapters
            _isLoading.value = true
            _error.value = null
            _isUsingOfflineData.value = false

            when (val result = repository.getChapters()) {
                is NetworkResult.Success -> {
                    lastLoadedChapters = result.data
                    _chapters.value = result.data
                    // Check if we got data from cache by seeing if there was a network error
                    // The repository returns Success even when loading from cache during network errors
                }
                is NetworkResult.Error -> {
                    _error.value = result.message
                    _isUsingOfflineData.value = true
                    // Show empty list - no hardcoded fallback
                    _chapters.value = emptyList()
                }
                is NetworkResult.Loading -> {
                    // Already handled by _isLoading
                }
            }

            _isLoading.value = false
        }
    }

    fun loadChapters() {
        // For backward compatibility, call the new optimistic load
        loadChaptersWithOptimisticUpdate()
    }

    // Local fallback data
    // Removed hardcoded chapters - no longer used
    @Deprecated("Hardcoded chapters removed")
    private fun getLocalChapters(): List<Chapter> {
        return listOf(
            Chapter(
                id = 1,
                title = "Compose Basics",
                description = "Learn the fundamentals of Jetpack Compose",
                completionPercentage = 0,
                topics = listOf(
                    Topic(1, 1, "GETTING STARTED", "Introduction to Jetpack Compose and setup", false,
                        "Welcome to the Jetpack Compose course!"),
                    Topic(2, 1, "COMPOSABLE FUNCTIONS", "Understanding @Composable functions", false,
                        "Composable functions are the heart of Jetpack Compose."),
                    Topic(3, 1, "MODIFIERS", "Using modifiers to customize UI", false,
                        "Modifiers are your secret weapon for customizing the look and feel."),
                    Topic(4, 1, "STATE MANAGEMENT", "Managing state with remember and mutableState", false,
                        "UI that doesn't change isn't very interesting.")
                )
            ),
            Chapter(
                id = 2,
                title = "Layouts & UI",
                description = "Build beautiful UIs with Compose layouts",
                completionPercentage = 0,
                topics = listOf(
                    Topic(6, 2, "ROW AND COLUMN", "Creating layouts with Row and Column", false,
                        "Learn how to arrange your composables."),
                    Topic(7, 2, "BOX LAYOUT", "Stacking components with Box", false,
                        "Sometimes you need to place composables on top of each other."),
                    Topic(8, 2, "LAZY LISTS", "Building scrollable lists with LazyColumn", false,
                        "Displaying long lists of items efficiently."),
                    Topic(9, 2, "MATERIAL DESIGN", "Using Material3 components", false,
                        "Jetpack Compose comes with Material Design components.")
                )
            ),
            Chapter(
                id = 3,
                title = "State & Navigation",
                description = "Advanced state management and navigation",
                completionPercentage = 0,
                topics = listOf(
                    Topic(11, 3, "STATE HOISTING", "Lifting state up in Compose", false,
                        "As your UI grows in complexity, managing state can become tricky."),
                    Topic(12, 3, "VIEW MODELS", "Using ViewModels with Compose", false,
                        "Learn how to integrate ViewModel with Jetpack Compose."),
                    Topic(13, 3, "NAVIGATION", "Implementing navigation in Compose apps", false,
                        "Most apps have more than one screen.")
                )
            )
        )
    }

    fun loadTopic(topicId: Int) {
        viewModelScope.launch {
            _isLoadingTopic.value = true

            when (val result = repository.getTopic(topicId)) {
                is NetworkResult.Success -> {
                    lastLoadedTopic = result.data
                    _selectedTopic.value = result.data
                }
                is NetworkResult.Error -> {
                    _error.value = result.message
                    // If remote API fails, try to show last loaded data
                    if (lastLoadedTopic != null && lastLoadedTopic?.id == topicId) {
                        _selectedTopic.value = lastLoadedTopic
                    }
                }
                is NetworkResult.Loading -> {
                    // Already handled by _isLoadingTopic
                }
            }

            _isLoadingTopic.value = false
        }
    }

    fun loadNotes(topicId: Int) {
        viewModelScope.launch {
            _isLoadingNotes.value = true
            _error.value = null

            when (val result = repository.getNotes(topicId)) {
                is NetworkResult.Success -> {
                    lastLoadedNotes = result.data
                    _notes.value = result.data
                    // Note: Repository returns Success even when loading from cache
                    // We can't distinguish between online and offline success here
                }
                is NetworkResult.Error -> {
                    _error.value = result.message
                    // If remote API fails, try to show last loaded data
                    if (lastLoadedNotes.isNotEmpty()) {
                        _notes.value = lastLoadedNotes
                    } else {
                        _notes.value = emptyList()
                    }
                }
                is NetworkResult.Loading -> {
                    // Already handled by _isLoadingNotes
                }
            }

            _isLoadingNotes.value = false
        }
    }

    fun retry() {
        loadChapters()
    }

    // Offline Management
    fun toggleChapterOffline(chapterId: Int, chapterTitle: String, makeOffline: Boolean) {
        viewModelScope.launch {
            try {
                if (makeOffline) {
                    // Add to downloading set
                    _downloadingChapters.value = _downloadingChapters.value + chapterId
                }

                repository.toggleChapterOffline(chapterId, chapterTitle, makeOffline)

                // Remove from downloading set
                _downloadingChapters.value = _downloadingChapters.value - chapterId

                // Reload chapters to reflect the change
                loadChaptersWithOptimisticUpdate()
            } catch (e: Exception) {
                // Remove from downloading set on error
                _downloadingChapters.value = _downloadingChapters.value - chapterId
                _error.value = "Failed to toggle offline status: ${e.message}"
            }
        }
    }

    suspend fun isChapterOffline(chapterId: Int): Boolean {
        return repository.isChapterOffline(chapterId)
    }

    suspend fun isChapterManuallyDownloaded(chapterId: Int): Boolean {
        return repository.isChapterManuallyDownloaded(chapterId)
    }
}
