package com.dolphin.jetpack.presentation.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.jetpack.ads.InterstitialAdManager
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

    // For optimistic UI updates
    private var lastKnownResumeStates: Map<String, Boolean> = emptyMap()

    // AdMob Interstitial Ad Manager
    private var adManager: InterstitialAdManager? = null

    // Your AdMob Interstitial Ad Unit ID
    private val adUnitId = "ca-app-pub-6038618318911032/1779472799"

    fun checkResumeStates(quizTitles: List<String>) {
        viewModelScope.launch {
            // First, use the last known states immediately (optimistic UI)
            if (lastKnownResumeStates.isNotEmpty()) {
                val mergedStates = mutableMapOf<String, Boolean>()
                quizTitles.forEach { title ->
                    // Use last known state if available, otherwise query
                    mergedStates[title] = lastKnownResumeStates[title] ?: repository.hasQuizState(title)
                }
                _hasResumeState.value = mergedStates
            }
            
            // Then, update with fresh data from repository
            val freshStates = mutableMapOf<String, Boolean>()
            quizTitles.forEach { title ->
                freshStates[title] = repository.hasQuizState(title)
            }
            _hasResumeState.value = freshStates
            lastKnownResumeStates = freshStates
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

                // Sync with backend if user is authenticated
                val currentUser = getCurrentUser()
                if (currentUser != null) {
                    repository.syncQuizAttempt(currentUser.uid, attempt)
                }

                // Delete quiz state after successful completion
                deleteQuizState(quizTitle)
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(e.message ?: "Failed to save attempt")
            }
        }
    }

    private fun getCurrentUser(): com.google.firebase.auth.FirebaseUser? {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    }

    fun resetState() {
        _uiState.value = QuizUiState.Idle
    }

    /**
     * Initialize AdMob SDK and create ad manager instance
     * Call this from the Activity's onCreate or from a composable
     */
    fun initializeAdMob(context: Context) {
        if (adManager == null) {
            InterstitialAdManager.initialize(context) {
                adManager = InterstitialAdManager(context.applicationContext, adUnitId)
                // Preload the first ad
                adManager?.loadAd()
            }
        }
    }

    /**
     * Check if an ad is ready to show
     */
    fun isAdReady(): Boolean {
        return adManager?.isAdReady() ?: false
    }

    /**
     * Load an interstitial ad
     */
    fun loadAd() {
        adManager?.loadAd()
    }

    /**
     * Show the interstitial ad with a callback for when it's dismissed
     * @param activity The activity context
     * @param onAdDismissed Callback invoked when ad is dismissed or failed to show
     */
    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (adManager?.isAdReady() == true) {
            adManager?.showAd(activity, onAdDismissed = {
                // After ad is dismissed, load the next ad
                adManager?.loadAd()
                onAdDismissed()
            })
        } else {
            // Ad not ready, just call the callback immediately
            onAdDismissed()
        }
    }

    override fun onCleared() {
        super.onCleared()
        adManager?.destroy()
    }
}