package com.dolphin.jetpack.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.jetpack.data.remote.NetworkResult
import com.dolphin.jetpack.data.repository.ContentRepository
import com.dolphin.jetpack.domain.model.Quiz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizListViewModel(
    private val repository: ContentRepository
) : ViewModel() {

    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Download progress tracking
    private val _downloadingQuizzes = MutableStateFlow<Set<Int>>(emptySet())
    val downloadingQuizzes: StateFlow<Set<Int>> = _downloadingQuizzes.asStateFlow()

    init {
        loadQuizzes()
    }

    fun loadQuizzes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getQuizzes()) {
                is NetworkResult.Success -> {
                    _quizzes.value = result.data
                }
                is NetworkResult.Error -> {
                    _error.value = result.message
                    // Try to load offline cached quizzes if available
                    // The first 3 quizzes should be auto-cached
                    _quizzes.value = emptyList() // Let offline cached quizzes be shown if any
                }
                is NetworkResult.Loading -> {
                    // Already handled by _isLoading
                }
            }

            _isLoading.value = false
        }
    }

    fun retry() {
        loadQuizzes()
    }

    fun toggleQuizOffline(quizId: Int, quizTitle: String, makeOffline: Boolean) {
        viewModelScope.launch {
            try {
                if (makeOffline) {
                    // Add to downloading set
                    _downloadingQuizzes.value = _downloadingQuizzes.value + quizId
                }

                repository.toggleQuizOffline(quizId, quizTitle, makeOffline)

                // Remove from downloading set
                _downloadingQuizzes.value = _downloadingQuizzes.value - quizId
            } catch (e: Exception) {
                // Remove from downloading set on error
                _downloadingQuizzes.value = _downloadingQuizzes.value - quizId
                _error.value = "Failed to toggle offline status: ${e.message}"
            }
        }
    }

    suspend fun isQuizOffline(quizId: Int): Boolean {
        return repository.isQuizOffline(quizId)
    }

    suspend fun isQuizManuallyDownloaded(quizId: Int): Boolean {
        return repository.isQuizManuallyDownloaded(quizId)
    }
}
