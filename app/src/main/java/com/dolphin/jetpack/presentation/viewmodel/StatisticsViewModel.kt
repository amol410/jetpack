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
    
    // For optimistic UI updates
    private var lastLocalStats: QuizStatistics? = null

    init {
        // Load initial data with optimistic approach
        viewModelScope.launch {
            try {
                val stats = repository.getStatistics()
                lastLocalStats = stats
                _statsState.value = if (stats.totalAttempts == 0) {
                    StatisticsUiState.Empty
                } else {
                    StatisticsUiState.Success(stats)
                }
            } catch (e: Exception) {
                // If local data fails, default to Empty state instead of Error
                // since there's simply no data yet
                _statsState.value = StatisticsUiState.Empty
            }
        }
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _statsState.value = StatisticsUiState.Loading
            try {
                val stats = repository.getStatistics()
                lastLocalStats = stats
                _statsState.value = if (stats.totalAttempts == 0) {
                    StatisticsUiState.Empty
                } else {
                    StatisticsUiState.Success(stats)
                }
            } catch (e: Exception) {
                // If loading fails but we have cached data, show it
                lastLocalStats?.let { stats ->
                    if (stats.totalAttempts > 0) {
                        _statsState.value = StatisticsUiState.Success(stats)
                    } else {
                        _statsState.value = StatisticsUiState.Empty
                    }
                } ?: run {
                    // No data available, show empty state instead of error
                    _statsState.value = StatisticsUiState.Empty
                }
            }
        }
    }

    fun loadRemoteStatistics(firebaseUid: String) {
        viewModelScope.launch {
            // First, immediately show local data if available (optimistic UI)
            try {
                val localStats = repository.getStatistics()
                lastLocalStats = localStats
                if (localStats.totalAttempts > 0) {
                    // Show local data immediately while fetching remote data in background
                    _statsState.value = StatisticsUiState.Success(localStats)
                }
            } catch (e: Exception) {
                // If even local data fails, show loading state
                _statsState.value = StatisticsUiState.Loading
            }
            
            // Then, fetch remote data in the background
            try {
                val remoteResult = repository.syncUserStatistics(firebaseUid)
                remoteResult.fold(
                    onSuccess = { stats ->
                        // Update with fresh remote data
                        _statsState.value = if (stats.totalAttempts == 0) {
                            StatisticsUiState.Empty
                        } else {
                            StatisticsUiState.Success(stats)
                        }
                    },
                    onFailure = { error ->
                        // If remote fails, check if it's a "no data" scenario or actual error
                        lastLocalStats?.let { stats ->
                            if (stats.totalAttempts > 0) {
                                _statsState.value = StatisticsUiState.Success(stats)
                            } else {
                                // Check if error message indicates "no data" vs actual error
                                val isNoDataError = error.message?.contains("no data", ignoreCase = true) == true ||
                                                   error.message?.contains("not found", ignoreCase = true) == true ||
                                                   error.message?.contains("user not found", ignoreCase = true) == true ||
                                                   error.message?.contains("empty", ignoreCase = true) == true

                                _statsState.value = if (isNoDataError) {
                                    StatisticsUiState.Empty
                                } else {
                                    StatisticsUiState.Error("Failed to load statistics: ${error.message}")
                                }
                            }
                        } ?: run {
                            // Check if error message indicates "no data" vs actual error
                            val isNoDataError = error.message?.contains("no data", ignoreCase = true) == true ||
                                               error.message?.contains("not found", ignoreCase = true) == true ||
                                               error.message?.contains("user not found", ignoreCase = true) == true ||
                                               error.message?.contains("empty", ignoreCase = true) == true

                            _statsState.value = if (isNoDataError) {
                                StatisticsUiState.Empty
                            } else {
                                StatisticsUiState.Error("Failed to load statistics: ${error.message}")
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                // If remote sync fails, keep showing local data or show appropriate state
                lastLocalStats?.let { stats ->
                    if (stats.totalAttempts > 0) {
                        _statsState.value = StatisticsUiState.Success(stats)
                    } else {
                        // Check if exception indicates "no data" vs actual error
                        val isNoDataError = e.message?.contains("no data", ignoreCase = true) == true ||
                                           e.message?.contains("not found", ignoreCase = true) == true ||
                                           e.message?.contains("user not found", ignoreCase = true) == true ||
                                           e.message?.contains("empty", ignoreCase = true) == true

                        _statsState.value = if (isNoDataError) {
                            StatisticsUiState.Empty
                        } else {
                            StatisticsUiState.Error("Failed to load statistics: ${e.message}")
                        }
                    }
                } ?: run {
                    // Check if exception indicates "no data" vs actual error
                    val isNoDataError = e.message?.contains("no data", ignoreCase = true) == true ||
                                       e.message?.contains("not found", ignoreCase = true) == true ||
                                       e.message?.contains("user not found", ignoreCase = true) == true ||
                                       e.message?.contains("empty", ignoreCase = true) == true

                    _statsState.value = if (isNoDataError) {
                        StatisticsUiState.Empty
                    } else {
                        StatisticsUiState.Error("Failed to load statistics: ${e.message}")
                    }
                }
            }
        }
    }

    fun refresh() {
        loadStatistics()
    }
}