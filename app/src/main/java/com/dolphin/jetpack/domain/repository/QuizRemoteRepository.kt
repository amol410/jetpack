package com.dolphin.jetpack.domain.repository

import com.dolphin.jetpack.domain.model.QuizAttempt
import com.dolphin.jetpack.domain.model.QuizStatistics

interface QuizRemoteRepository {
    suspend fun saveQuizAttempt(firebaseUid: String, quizAttempt: QuizAttempt): Result<Long>
    suspend fun getUserQuizAttempts(firebaseUid: String): Result<List<QuizAttempt>>
    suspend fun getQuizAttemptDetail(attemptId: Long): Result<QuizAttempt>
    suspend fun getUserStatistics(firebaseUid: String): Result<QuizStatistics>
}