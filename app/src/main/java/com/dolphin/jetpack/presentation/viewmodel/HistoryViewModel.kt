package com.dolphin.jetpack.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.jetpack.domain.model.QuizAttempt
import com.dolphin.jetpack.domain.repository.QuizRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val attempts: List<QuizAttempt>) : HistoryUiState()
    object Empty : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class HistoryViewModel(
    private val repository: QuizRepository
) : ViewModel() {

    private val _historyState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val historyState: StateFlow<HistoryUiState> = _historyState.asStateFlow()

    private val _detailError = MutableStateFlow<String?>(null)
    val detailError: StateFlow<String?> = _detailError.asStateFlow()

    // For optimistic UI updates
    private var lastLocalHistory: List<QuizAttempt> = emptyList()

    private val _selectedAttempt = MutableStateFlow<QuizAttempt?>(null)
    val selectedAttempt: StateFlow<QuizAttempt?> = _selectedAttempt.asStateFlow()

    // Cache of quiz title -> Quiz for option text resolution
    private val _currentQuiz = MutableStateFlow<com.dolphin.jetpack.domain.model.Quiz?>(null)
    val currentQuiz: StateFlow<com.dolphin.jetpack.domain.model.Quiz?> = _currentQuiz.asStateFlow()

    init {
        // Load initial data with optimistic approach
        viewModelScope.launch {
            try {
                val localAttempts = repository.getAllAttempts().first()
                lastLocalHistory = localAttempts
                _historyState.value = if (localAttempts.isEmpty()) {
                    HistoryUiState.Empty
                } else {
                    HistoryUiState.Success(localAttempts)
                }
            } catch (e: Exception) {
                _historyState.value = HistoryUiState.Empty
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _historyState.value = HistoryUiState.Loading
            try {
                val attempts = repository.getAllAttempts().first()
                val deduplicated = attempts
                    .distinctBy { "${it.quizTitle}_${it.dateTime}_${it.score}_${it.totalQuestions}" }
                    .sortedByDescending { it.dateTime }

                lastLocalHistory = deduplicated
                _historyState.value = if (deduplicated.isEmpty()) {
                    HistoryUiState.Empty
                } else {
                    HistoryUiState.Success(deduplicated)
                }
            } catch (e: Exception) {
                if (lastLocalHistory.isNotEmpty()) {
                    _historyState.value = HistoryUiState.Success(lastLocalHistory)
                } else {
                    _historyState.value = HistoryUiState.Empty
                }
            }
        }
    }

    fun loadRemoteHistory(firebaseUid: String) {
        viewModelScope.launch {
            android.util.Log.d("HistoryViewModel", "=== Loading remote history ===")
            android.util.Log.d("HistoryViewModel", "Firebase UID: $firebaseUid")

            // Optimistic: show local data first
            try {
                val localAttempts = repository.getAllAttempts().first()
                val deduplicated = localAttempts
                    .distinctBy { "${it.quizTitle}_${it.dateTime}_${it.score}_${it.totalQuestions}" }
                    .sortedByDescending { it.dateTime }

                lastLocalHistory = deduplicated
                if (deduplicated.isNotEmpty()) {
                    _historyState.value = HistoryUiState.Success(deduplicated)
                }
            } catch (e: Exception) {
                _historyState.value = HistoryUiState.Loading
            }

            // Fetch from backend
            try {
                val remoteResult = repository.syncAllQuizAttempts(firebaseUid)
                remoteResult.fold(
                    onSuccess = { attempts ->
                        lastLocalHistory = attempts
                        _historyState.value = if (attempts.isEmpty()) {
                            HistoryUiState.Empty
                        } else {
                            HistoryUiState.Success(attempts)
                        }
                    },
                    onFailure = { error ->
                        if (lastLocalHistory.isNotEmpty()) {
                            _historyState.value = HistoryUiState.Success(lastLocalHistory)
                        } else {
                            val isNoData = error.message?.contains("no data", true) == true ||
                                error.message?.contains("not found", true) == true ||
                                error.message?.contains("empty", true) == true
                            _historyState.value = if (isNoData) {
                                HistoryUiState.Empty
                            } else {
                                HistoryUiState.Error("Failed to load data: ${error.message}")
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                if (lastLocalHistory.isNotEmpty()) {
                    _historyState.value = HistoryUiState.Success(lastLocalHistory)
                } else {
                    val isNoData = e.message?.contains("no data", true) == true ||
                        e.message?.contains("not found", true) == true ||
                        e.message?.contains("empty", true) == true
                    _historyState.value = if (isNoData) {
                        HistoryUiState.Empty
                    } else {
                        HistoryUiState.Error("Failed to load data: ${e.message}")
                    }
                }
            }
        }
    }

    fun loadAttemptDetail(attemptId: Long) {
        viewModelScope.launch {
            try {
                _detailError.value = null
                android.util.Log.d("HistoryViewModel", "Loading attempt detail: $attemptId")

                // Show cached attempt immediately
                val cached = lastLocalHistory.find { it.id == attemptId }
                    ?: (historyState.value as? HistoryUiState.Success)?.attempts?.find { it.id == attemptId }
                if (cached != null) {
                    _selectedAttempt.value = cached
                }

                // Try local DB with answers
                val localAttempt = repository.getAttemptById(attemptId)
                if (localAttempt != null) {
                    _selectedAttempt.value = localAttempt
                    loadQuizForAttempt(localAttempt.quizTitle)
                    _detailError.value = null
                    // If we have no answers locally, try fetching detail from backend for this attempt
                    if (localAttempt.questionAnswers.isEmpty()) {
                        fetchRemoteAttemptDetail(attemptId)
                    }
                } else {
                    // Fetch from backend
                    fetchRemoteAttemptDetail(attemptId)
                }
            } catch (e: Exception) {
                _detailError.value = e.message ?: "Unexpected error loading details"
                if (_selectedAttempt.value == null) {
                    _selectedAttempt.value = lastLocalHistory.find { it.id == attemptId }
                }
            }
        }
    }

    fun deleteAttempt(attemptId: Long) {
        viewModelScope.launch {
            val currentList = (historyState.value as? HistoryUiState.Success)?.attempts ?: lastLocalHistory

            // Optimistically update UI and cache
            val updatedList = currentList.filterNot { it.id == attemptId }
            lastLocalHistory = updatedList
            _historyState.value = if (updatedList.isEmpty()) HistoryUiState.Empty else HistoryUiState.Success(updatedList)

            try {
                repository.deleteAttempt(attemptId)
                getCurrentUser()?.let { user ->
                    val result = repository.deleteRemoteAttempt(user.uid, attemptId)
                    result.onFailure { error ->
                        // Revert on remote failure so state matches backend
                        lastLocalHistory = currentList
                        _historyState.value = if (currentList.isEmpty()) {
                            HistoryUiState.Empty
                        } else {
                            HistoryUiState.Success(currentList)
                        }
                        _historyState.value = HistoryUiState.Error(error.message ?: "Failed to delete from cloud")
                    }
                }
            } catch (e: Exception) {
                // Revert to previous list on failure
                lastLocalHistory = currentList
                _historyState.value = HistoryUiState.Error(e.message ?: "Failed to delete")
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                repository.deleteAllAttempts()
                _historyState.value = HistoryUiState.Empty
            } catch (e: Exception) {
                _historyState.value = HistoryUiState.Error(e.message ?: "Failed to clear history")
            }
        }
    }

    private fun getCurrentUser(): FirebaseUser? = FirebaseAuth.getInstance().currentUser

    private fun loadQuizForAttempt(quizTitle: String) {
        viewModelScope.launch {
            try {
                // Look up the quiz in the cached local content repository via ContentRepository (through QuizViewModel normally)
                // Here we approximate by using the saved attempt and local history quizzes; if not found, leave null.
                val localQuiz = com.dolphin.jetpack.data.local.DataProvider.quizList.find { it.title == quizTitle }
                _currentQuiz.value = localQuiz
            } catch (_: Exception) {
                _currentQuiz.value = null
            }
        }
    }

    private suspend fun fetchRemoteAttemptDetail(attemptId: Long) {
        val user = getCurrentUser()
        if (user == null) {
            _detailError.value = "Sign in to load quiz details from cloud"
            return
        }
        val remoteResult = repository.getRemoteAttemptDetail(user.uid, attemptId)
        remoteResult.fold(
            onSuccess = { attempt ->
                _selectedAttempt.value = attempt
                loadQuizForAttempt(attempt.quizTitle)
                _detailError.value = null
            },
            onFailure = { error ->
                _detailError.value = error.message ?: "Failed to load quiz details"
            }
        )
    }
}
