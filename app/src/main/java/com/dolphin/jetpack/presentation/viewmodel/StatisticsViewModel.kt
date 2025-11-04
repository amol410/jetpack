package com.dolphin.jetpack.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.jetpack.domain.model.QuizStatistics
import com.dolphin.jetpack.domain.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Success(val statistics: QuizStatistics) : StatisticsUiState()
    object Empty : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}

class StatisticsViewModel(
    private val repository: QuizRepository
) : ViewModel() {

    private val _statsState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val statsState: StateFlow<StatisticsUiState> = _statsState.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _statsState.value = StatisticsUiState.Loading
            try {
                val stats = repository.getStatistics()
                _statsState.value = if (stats.totalAttempts == 0) {
                    StatisticsUiState.Empty
                } else {
                    StatisticsUiState.Success(stats)
                }
            } catch (e: Exception) {
                _statsState.value = StatisticsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        loadStatistics()
    }
}