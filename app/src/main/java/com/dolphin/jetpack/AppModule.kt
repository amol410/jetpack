package com.dolphin.jetpack

import android.content.Context
import com.dolphin.jetpack.data.local.database.QuizDatabase
import com.dolphin.jetpack.data.remote.QuizRemoteRepositoryImpl
import com.dolphin.jetpack.data.remote.RetrofitClient
import com.dolphin.jetpack.data.repository.ContentRepository
import com.dolphin.jetpack.data.repository.QuizRepositoryImpl
import com.dolphin.jetpack.presentation.viewmodel.HistoryViewModel
import com.dolphin.jetpack.presentation.viewmodel.NotesViewModel
import com.dolphin.jetpack.presentation.viewmodel.QuizListViewModel
import com.dolphin.jetpack.presentation.viewmodel.QuizViewModel
import com.dolphin.jetpack.presentation.viewmodel.StatisticsViewModel


object AppModule {

    private lateinit var database: QuizDatabase
    private lateinit var repository: QuizRepositoryImpl // Changed to concrete type
    private lateinit var contentRepository: ContentRepository

    fun initialize(context: Context) {
        database = QuizDatabase.getDatabase(context)
        val remoteRepository = QuizRemoteRepositoryImpl(RetrofitClient.apiService)
        repository = QuizRepositoryImpl(
            database.quizAttemptDao(),
            database.questionAnswerDao(),
            database.quizStateDao(),
            remoteRepository
        )

        contentRepository = ContentRepository(
            database.quizDao(),
            database.chapterDao(),
            database.noteDao()
        )

    }

    fun provideQuizViewModel(): QuizViewModel {
        return QuizViewModel(repository, contentRepository)
    }

    fun provideNotesViewModel(): NotesViewModel {
        return NotesViewModel(contentRepository)
    }

    fun provideQuizListViewModel(): QuizListViewModel {
        return QuizListViewModel(contentRepository)
    }

    fun provideHistoryViewModel(): HistoryViewModel {
        return HistoryViewModel(repository)
    }

    fun provideStatisticsViewModel(): StatisticsViewModel {
        return StatisticsViewModel(repository)
    }

    fun provideContentRepository(): ContentRepository {
        return contentRepository
    }

    fun updateCurrentUser(userId: String?) {
        repository.setCurrentUserId(userId)
    }
}