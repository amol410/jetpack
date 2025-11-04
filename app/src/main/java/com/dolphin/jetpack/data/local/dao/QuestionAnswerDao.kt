// File: QuestionAnswerDao.kt
package com.dolphin.jetpack.data.local.dao

import androidx.room.*
import com.dolphin.jetpack.data.local.entity.QuestionAnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionAnswerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionAnswer(answer: QuestionAnswerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionAnswers(answers: List<QuestionAnswerEntity>)

    @Query("SELECT * FROM question_answers WHERE attemptId = :attemptId ORDER BY questionIndex ASC")
    fun getQuestionAnswersByAttempt(attemptId: Long): Flow<List<QuestionAnswerEntity>>

    @Query("SELECT * FROM question_answers WHERE attemptId = :attemptId ORDER BY questionIndex ASC")
    suspend fun getQuestionAnswersByAttemptSync(attemptId: Long): List<QuestionAnswerEntity>

    @Query("SELECT * FROM question_answers WHERE isCorrect = 0")
    suspend fun getAllIncorrectAnswers(): List<QuestionAnswerEntity>

    @Query("SELECT questionText, COUNT(*) as wrongCount FROM question_answers WHERE isCorrect = 0 GROUP BY questionText ORDER BY wrongCount DESC LIMIT :limit")
    suspend fun getMostWrongQuestions(limit: Int = 10): List<WrongQuestionCount>

    @Delete
    suspend fun deleteQuestionAnswer(answer: QuestionAnswerEntity)

    @Query("DELETE FROM question_answers WHERE attemptId = :attemptId")
    suspend fun deleteQuestionAnswersByAttempt(attemptId: Long)

    @Query("DELETE FROM question_answers")
    suspend fun deleteAllQuestionAnswers()
}

data class WrongQuestionCount(
    val questionText: String,
    val wrongCount: Int
)