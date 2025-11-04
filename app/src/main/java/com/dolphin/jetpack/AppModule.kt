package com.dolphin.jetpack

import android.content.Context
import com.dolphin.jetpack.data.local.database.QuizDatabase
import com.dolphin.jetpack.data.repository.QuizRepositoryImpl
import com.dolphin.jetpack.domain.repository.QuizRepository
import com.dolphin.jetpack.presentation.viewmodel.HistoryViewModel
import com.dolphin.jetpack.presentation.viewmodel.QuizViewModel
import com.dolphin.jetpack.presentation.viewmodel.StatisticsViewModel

object AppModule {

    private lateinit var database: QuizDatabase
    private lateinit var repository: QuizRepository

    fun initialize(context: Context) {
        database = QuizDatabase.getDatabase(context)
        repository = QuizRepositoryImpl(
            database.quizAttemptDao(),
            database.questionAnswerDao(),
            database.quizStateDao()
        )
    }

    fun provideQuizViewModel(): QuizViewModel {
        return QuizViewModel(repository)
    }

    fun provideHistoryViewModel(): HistoryViewModel {
        return HistoryViewModel(repository)
    }

    fun provideStatisticsViewModel(): StatisticsViewModel {
        return StatisticsViewModel(repository)
    }
}