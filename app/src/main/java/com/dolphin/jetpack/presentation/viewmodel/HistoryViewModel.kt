package com.dolphin.jetpack.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.jetpack.domain.model.QuizAttempt
import com.dolphin.jetpack.domain.repository.QuizRepository
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
    
    // For optimistic UI updates
    private var lastLocalHistory: List<QuizAttempt> = emptyList()

    private val _selectedAttempt = MutableStateFlow<QuizAttempt?>(null)
    val selectedAttempt: StateFlow<QuizAttempt?> = _selectedAttempt.asStateFlow()

    init {
        // Load initial data with optimistic approach
        viewModelScope.launch {
            // Immediately show local data if available
            try {
                val localAttempts = repository.getAllAttempts().first()
                lastLocalHistory = localAttempts
                _historyState.value = if (localAttempts.isEmpty()) {
                    HistoryUiState.Empty
                } else {
                    HistoryUiState.Success(localAttempts)
                }
            } catch (e: Exception) {
                // If local data fails, default to Empty state instead of Error
                // since there's simply no data yet
                _historyState.value = HistoryUiState.Empty
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _historyState.value = HistoryUiState.Loading
            try {
                val attempts = repository.getAllAttempts().first() // Get the first emission
                lastLocalHistory = attempts
                _historyState.value = if (attempts.isEmpty()) {
                    HistoryUiState.Empty
                } else {
                    HistoryUiState.Success(attempts)
                }
            } catch (e: Exception) {
                // If loading fails but we have cached data, show it
                if (lastLocalHistory.isNotEmpty()) {
                    _historyState.value = HistoryUiState.Success(lastLocalHistory)
                } else {
                    // No data available, show empty state instead of error
                    _historyState.value = HistoryUiState.Empty
                }
            }
        }
    }

    fun loadRemoteHistory(firebaseUid: String) {
        viewModelScope.launch {
            android.util.Log.d("HistoryViewModel", "========================================")
            android.util.Log.d("HistoryViewModel", "📥 LOADING REMOTE HISTORY")
            android.util.Log.d("HistoryViewModel", "   - Firebase UID: $firebaseUid")

            // First, immediately show local data if available (optimistic UI)
            try {
                val localAttempts = repository.getAllAttempts().first()
                lastLocalHistory = localAttempts
                android.util.Log.d("HistoryViewModel", "   - Local attempts found: ${localAttempts.size}")
                if (localAttempts.isNotEmpty()) {
                    // Show local data immediately while fetching remote data in background
                    _historyState.value = HistoryUiState.Success(localAttempts)
                    android.util.Log.d("HistoryViewModel", "   - Showing local data first")
                }
            } catch (e: Exception) {
                android.util.Log.e("HistoryViewModel", "   - Error loading local data: ${e.message}")
                // If even local data fails, show loading state
                _historyState.value = HistoryUiState.Loading
            }

            // Then, fetch remote data in the background
            android.util.Log.d("HistoryViewModel", "📡 Fetching from backend...")
            try {
                val remoteResult = repository.syncAllQuizAttempts(firebaseUid)
                remoteResult.fold(
                    onSuccess = { attempts ->
                        android.util.Log.d("HistoryViewModel", "✅ Backend sync SUCCESS!")
                        android.util.Log.d("HistoryViewModel", "   - Remote attempts: ${attempts.size}")
                        // Update with fresh remote data
                        _historyState.value = if (attempts.isEmpty()) {
                            android.util.Log.d("HistoryViewModel", "   - No attempts in backend")
                            HistoryUiState.Empty
                        } else {
                            android.util.Log.d("HistoryViewModel", "   - Displaying ${attempts.size} attempts")
                            HistoryUiState.Success(attempts)
                        }
                        android.util.Log.d("HistoryViewModel", "========================================")
                    },
                    onFailure = { error ->
                        android.util.Log.e("HistoryViewModel", "❌ Backend sync FAILED: ${error.message}")
                        // If remote fails, check if it's a "no data" scenario or actual error
                        if (lastLocalHistory.isNotEmpty()) {
                            // Show local data if available
                            android.util.Log.d("HistoryViewModel", "   - Showing local data as fallback")
                            _historyState.value = HistoryUiState.Success(lastLocalHistory)
                        } else {
                            // Check if error message indicates "no data" vs actual error
                            val isNoDataError = error.message?.contains("no data", ignoreCase = true) == true ||
                                               error.message?.contains("not found", ignoreCase = true) == true ||
                                               error.message?.contains("user not found", ignoreCase = true) == true ||
                                               error.message?.contains("empty", ignoreCase = true) == true

                            _historyState.value = if (isNoDataError) {
                                android.util.Log.d("HistoryViewModel", "   - No data found (expected)")
                                HistoryUiState.Empty
                            } else {
                                android.util.Log.e("HistoryViewModel", "   - Actual error occurred")
                                HistoryUiState.Error("Failed to load data: ${error.message}")
                            }
                        }
                        android.util.Log.d("HistoryViewModel", "========================================")
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("HistoryViewModel", "💥 EXCEPTION: ${e.message}", e)
                // If remote sync fails, keep showing local data or show appropriate state
                if (lastLocalHistory.isNotEmpty()) {
                    android.util.Log.d("HistoryViewModel", "   - Showing local data as fallback")
                    _historyState.value = HistoryUiState.Success(lastLocalHistory)
                } else {
                    // Check if exception indicates "no data" vs actual error
                    val isNoDataError = e.message?.contains("no data", ignoreCase = true) == true ||
                                       e.message?.contains("not found", ignoreCase = true) == true ||
                                       e.message?.contains("user not found", ignoreCase = true) == true ||
                                       e.message?.contains("empty", ignoreCase = true) == true

                    _historyState.value = if (isNoDataError) {
                        android.util.Log.d("HistoryViewModel", "   - No data found (expected)")
                        HistoryUiState.Empty
                    } else {
                        android.util.Log.e("HistoryViewModel", "   - Actual error occurred")
                        HistoryUiState.Error("Failed to load data: ${e.message}")
                    }
                }
                android.util.Log.d("HistoryViewModel", "========================================")
            }
        }
    }

    fun loadAttemptDetail(attemptId: Long) {
        viewModelScope.launch {
            try {
                val attempt = repository.getAttemptById(attemptId)
                _selectedAttempt.value = attempt
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteAttempt(attemptId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteAttempt(attemptId)
                loadHistory()
            } catch (e: Exception) {
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
}