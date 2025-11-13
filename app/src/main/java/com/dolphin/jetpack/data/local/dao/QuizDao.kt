package com.dolphin.jetpack.data.local.dao

import androidx.room.*
import com.dolphin.jetpack.data.local.entity.QuizEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    @Query("SELECT * FROM offline_quizzes WHERE isOffline = 1 ORDER BY title ASC")
    fun getAllOfflineQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM offline_quizzes WHERE isOffline = 1 ORDER BY title ASC")
    suspend fun getAllOfflineQuizzesList(): List<QuizEntity>

    @Query("SELECT * FROM offline_quizzes WHERE id = :quizId")
    suspend fun getQuizById(quizId: Int): QuizEntity?

    @Query("SELECT * FROM offline_quizzes WHERE title = :title")
    suspend fun getQuizByTitle(title: String): QuizEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM offline_quizzes WHERE id = :quizId AND isOffline = 1)")
    suspend fun isQuizOffline(quizId: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM offline_quizzes WHERE id = :quizId AND manuallyDownloaded = 1)")
    suspend fun isQuizManuallyDownloaded(quizId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<QuizEntity>)

    @Query("UPDATE offline_quizzes SET isOffline = :isOffline WHERE id = :quizId")
    suspend fun updateOfflineStatus(quizId: Int, isOffline: Boolean)

    @Delete
    suspend fun deleteQuiz(quiz: QuizEntity)

    @Query("DELETE FROM offline_quizzes WHERE id = :quizId")
    suspend fun deleteQuizById(quizId: Int)

    @Query("DELETE FROM offline_quizzes")
    suspend fun deleteAllQuizzes()

    @Query("SELECT COUNT(*) FROM offline_quizzes WHERE isOffline = 1")
    suspend fun getOfflineQuizCount(): Int
}
