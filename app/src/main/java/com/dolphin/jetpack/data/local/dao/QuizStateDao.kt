// File: QuizStateDao.kt
package com.dolphin.jetpack.data.local.dao

import androidx.room.*
import com.dolphin.jetpack.data.local.entity.QuizStateEntity

@Dao
interface QuizStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizState(state: QuizStateEntity)

    @Query("SELECT * FROM quiz_state WHERE quizTitle = :quizTitle")
    suspend fun getQuizState(quizTitle: String): QuizStateEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM quiz_state WHERE quizTitle = :quizTitle)")
    suspend fun hasQuizState(quizTitle: String): Boolean

    @Query("SELECT * FROM quiz_state")
    suspend fun getAllQuizStates(): List<QuizStateEntity>

    @Delete
    suspend fun deleteQuizState(state: QuizStateEntity)

    @Query("DELETE FROM quiz_state WHERE quizTitle = :quizTitle")
    suspend fun deleteQuizStateByTitle(quizTitle: String)

    @Query("DELETE FROM quiz_state")
    suspend fun deleteAllQuizStates()
}