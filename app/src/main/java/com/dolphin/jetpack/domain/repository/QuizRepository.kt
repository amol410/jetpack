package com.dolphin.jetpack.domain.repository

import com.dolphin.jetpack.domain.model.*
import kotlinx.coroutines.flow.Flow

interface QuizRepository {

    // Quiz Attempts
    suspend fun saveQuizAttempt(attempt: QuizAttempt): Long
    fun getAllAttempts(): Flow<List<QuizAttempt>>
    suspend fun getAttemptById(id: Long): QuizAttempt?
    fun getAttemptsByQuiz(quizTitle: String): Flow<List<QuizAttempt>>
    fun getFilteredAttempts(filter: FilterOptions): Flow<List<QuizAttempt>>
    suspend fun deleteAttempt(id: Long)
    suspend fun deleteAllAttempts()

    // Question Answers
    suspend fun saveQuestionAnswers(answers: List<QuestionAnswer>)
    fun getQuestionAnswersByAttempt(attemptId: Long): Flow<List<QuestionAnswer>>
    suspend fun getQuestionAnswersByAttemptSync(attemptId: Long): List<QuestionAnswer>

    // Quiz State (Resume)
    suspend fun saveQuizState(state: QuizState)
    suspend fun getQuizState(quizTitle: String): QuizState?
    suspend fun hasQuizState(quizTitle: String): Boolean
    suspend fun deleteQuizState(quizTitle: String)
    suspend fun deleteAllQuizStates()

    // Statistics
    suspend fun getStatistics(): QuizStatistics
    suspend fun getTotalAttempts(): Int
    suspend fun getAverageScore(): Double
    suspend fun getBestScore(): Int
    suspend fun getQuizWisePerformance(): List<QuizPerformance>
    suspend fun getMostWrongQuestions(limit: Int = 10): List<WrongQuestion>
    suspend fun getImprovementData(): List<ImprovementPoint>

    // Export
    suspend fun exportToCSV(): String

    // Remote sync operations
    suspend fun syncQuizAttempt(firebaseUid: String, attempt: QuizAttempt): Result<Long>
    suspend fun syncAllQuizAttempts(firebaseUid: String): Result<List<QuizAttempt>>
    suspend fun syncUserStatistics(firebaseUid: String): Result<QuizStatistics>
}