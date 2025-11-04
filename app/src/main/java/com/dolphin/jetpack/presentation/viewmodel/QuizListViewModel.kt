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

class QuizListViewModel : ViewModel() {
    private val repository = ContentRepository()

    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
                    // Fallback to local data if available - need to convert from QuestionData to domain models
                    _quizzes.value = com.dolphin.jetpack.DataProvider.quizList.map { questionDataQuiz ->
                        com.dolphin.jetpack.domain.model.Quiz(
                            title = questionDataQuiz.title,
                            questions = questionDataQuiz.questions.map { questionDataQuestion ->
                                com.dolphin.jetpack.domain.model.Question(
                                    text = questionDataQuestion.text,
                                    options = questionDataQuestion.options,
                                    correctAnswerIndex = questionDataQuestion.correctAnswerIndex
                                )
                            }
                        )
                    }
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
}
