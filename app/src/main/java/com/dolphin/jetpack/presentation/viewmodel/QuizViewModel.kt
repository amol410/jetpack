package com.dolphin.jetpack.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.jetpack.domain.model.QuestionAnswer
import com.dolphin.jetpack.domain.model.QuizAttempt
import com.dolphin.jetpack.domain.model.QuizState
import com.dolphin.jetpack.domain.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class QuizUiState {
    object Idle : QuizUiState()
    object Loading : QuizUiState()
    data class StateLoaded(val state: QuizState) : QuizUiState()
    data class Error(val message: String) : QuizUiState()
}

class QuizViewModel(
    private val repository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Idle)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _hasResumeState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val hasResumeState: StateFlow<Map<String, Boolean>> = _hasResumeState.asStateFlow()

    fun checkResumeStates(quizTitles: List<String>) {
        viewModelScope.launch {
            val states = mutableMapOf<String, Boolean>()
            quizTitles.forEach { title ->
                states[title] = repository.hasQuizState(title)
            }
            _hasResumeState.value = states
        }
    }

    fun loadQuizState(quizTitle: String) {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            try {
                val state = repository.getQuizState(quizTitle)
                if (state != null) {
                    _uiState.value = QuizUiState.StateLoaded(state)
                } else {
                    _uiState.value = QuizUiState.Error("No saved state found")
                }
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveQuizState(state: QuizState) {
        viewModelScope.launch {
            try {
                repository.saveQuizState(state)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteQuizState(quizTitle: String) {
        viewModelScope.launch {
            try {
                repository.deleteQuizState(quizTitle)
                checkResumeStates(listOf(quizTitle))
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun saveQuizAttempt(
        quizTitle: String,
        score: Int,
        totalQuestions: Int,
        timeTakenSeconds: Long,
        timerEnabled: Boolean,
        timerMinutes: Int,
        questionAnswers: List<QuestionAnswer>
    ) {
        viewModelScope.launch {
            try {
                val attempt = QuizAttempt(
                    quizTitle = quizTitle,
                    dateTime = System.currentTimeMillis(),
                    score = score,
                    totalQuestions = totalQuestions,
                    timeTakenSeconds = timeTakenSeconds,
                    percentage = (score * 100) / totalQuestions,
                    timerEnabled = timerEnabled,
                    timerMinutes = timerMinutes
                )

                val attemptId = repository.saveQuizAttempt(attempt)

                // Update attemptId for all question answers
                val updatedAnswers = questionAnswers.map { it.copy(attemptId = attemptId) }
                repository.saveQuestionAnswers(updatedAnswers)

                // Delete quiz state after successful completion
                deleteQuizState(quizTitle)
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(e.message ?: "Failed to save attempt")
            }
        }
    }

    fun resetState() {
        _uiState.value = QuizUiState.Idle
    }
}