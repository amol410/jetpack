// File: QuizAttemptDao.kt
package com.dolphin.jetpack.data.local.dao

import androidx.room.*
import com.dolphin.jetpack.data.local.entity.QuizAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAttemptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    @Query("SELECT * FROM quiz_attempts WHERE userId = :userId ORDER BY dateTime DESC")
    fun getAllAttemptsByUserId(userId: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE userId = :userId")
    suspend fun getAllAttemptsSyncByUserId(userId: String): List<QuizAttemptEntity>

    @Query("SELECT * FROM quiz_attempts ORDER BY dateTime DESC")
    fun getAllAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts")
    suspend fun getAllAttemptsSync(): List<QuizAttemptEntity>

    @Query("SELECT * FROM quiz_attempts WHERE id = :attemptId")
    suspend fun getAttemptById(attemptId: Long): QuizAttemptEntity?

    @Query("SELECT * FROM quiz_attempts WHERE userId = :userId AND quizTitle = :quizTitle ORDER BY dateTime DESC")
    fun getAttemptsByQuizForUser(userId: String, quizTitle: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE userId = :userId AND dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime DESC")
    fun getAttemptsByDateRangeForUser(userId: String, startDate: Long, endDate: Long): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE userId = :userId AND percentage >= :minPercentage AND percentage <= :maxPercentage ORDER BY dateTime DESC")
    fun getAttemptsByScoreRangeForUser(userId: String, minPercentage: Int, maxPercentage: Int): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE quizTitle = :quizTitle ORDER BY dateTime DESC")
    fun getAttemptsByQuiz(quizTitle: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime DESC")
    fun getAttemptsByDateRange(startDate: Long, endDate: Long): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE percentage >= :minPercentage AND percentage <= :maxPercentage ORDER BY dateTime DESC")
    fun getAttemptsByScoreRange(minPercentage: Int, maxPercentage: Int): Flow<List<QuizAttemptEntity>>

    @Delete
    suspend fun deleteAttempt(attempt: QuizAttemptEntity)

    @Query("DELETE FROM quiz_attempts WHERE id = :attemptId")
    suspend fun deleteAttemptById(attemptId: Long)

    @Query("DELETE FROM quiz_attempts WHERE userId = :userId")
    suspend fun deleteAllAttemptsForUser(userId: String)

    @Query("DELETE FROM quiz_attempts")
    suspend fun deleteAllAttempts()

    @Query("SELECT COUNT(*) FROM quiz_attempts WHERE userId = :userId")
    suspend fun getTotalAttemptsForUser(userId: String): Int

    @Query("SELECT COUNT(*) FROM quiz_attempts")
    suspend fun getTotalAttempts(): Int

    @Query("SELECT AVG(percentage) FROM quiz_attempts WHERE userId = :userId")
    suspend fun getAverageScoreForUser(userId: String): Double?

    @Query("SELECT AVG(percentage) FROM quiz_attempts")
    suspend fun getAverageScore(): Double?

    @Query("SELECT MAX(percentage) FROM quiz_attempts WHERE userId = :userId")
    suspend fun getBestScoreForUser(userId: String): Int?

    @Query("SELECT MAX(percentage) FROM quiz_attempts")
    suspend fun getBestScore(): Int?

    @Query("SELECT quizTitle, COUNT(*) as count FROM quiz_attempts WHERE userId = :userId GROUP BY quizTitle")
    suspend fun getQuizAttemptCountsForUser(userId: String): List<QuizAttemptCount>

    @Query("SELECT quizTitle, COUNT(*) as count FROM quiz_attempts GROUP BY quizTitle")
    suspend fun getQuizAttemptCounts(): List<QuizAttemptCount>

    @Query("SELECT quizTitle, AVG(percentage) as avgScore FROM quiz_attempts WHERE userId = :userId GROUP BY quizTitle")
    suspend fun getAverageScoreByQuizForUser(userId: String): List<QuizAvgScore>

    @Query("SELECT quizTitle, AVG(percentage) as avgScore FROM quiz_attempts GROUP BY quizTitle")
    suspend fun getAverageScoreByQuiz(): List<QuizAvgScore>
}

data class QuizAttemptCount(
    val quizTitle: String,
    val count: Int
)

data class QuizAvgScore(
    val quizTitle: String,
    val avgScore: Double
)
