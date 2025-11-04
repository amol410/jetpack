package com.dolphin.jetpack.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.jetpack.domain.model.QuizAttempt
import com.dolphin.jetpack.domain.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _selectedAttempt = MutableStateFlow<QuizAttempt?>(null)
    val selectedAttempt: StateFlow<QuizAttempt?> = _selectedAttempt.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _historyState.value = HistoryUiState.Loading
            try {
                repository.getAllAttempts().collect { attempts ->
                    _historyState.value = if (attempts.isEmpty()) {
                        HistoryUiState.Empty
                    } else {
                        HistoryUiState.Success(attempts)
                    }
                }
            } catch (e: Exception) {
                _historyState.value = HistoryUiState.Error(e.message ?: "Unknown error")
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