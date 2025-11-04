// File: QuizDatabase.kt
package com.dolphin.jetpack.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dolphin.jetpack.data.local.converter.Converters
import com.dolphin.jetpack.data.local.dao.QuestionAnswerDao
import com.dolphin.jetpack.data.local.dao.QuizAttemptDao
import com.dolphin.jetpack.data.local.dao.QuizStateDao
import com.dolphin.jetpack.data.local.entity.QuestionAnswerEntity
import com.dolphin.jetpack.data.local.entity.QuizAttemptEntity
import com.dolphin.jetpack.data.local.entity.QuizStateEntity

@Database(
    entities = [
        QuizAttemptEntity::class,
        QuestionAnswerEntity::class,
        QuizStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class QuizDatabase : RoomDatabase() {

    abstract fun quizAttemptDao(): QuizAttemptDao
    abstract fun questionAnswerDao(): QuestionAnswerDao
    abstract fun quizStateDao(): QuizStateDao

    companion object {
        @Volatile
        private var INSTANCE: QuizDatabase? = null

        fun getDatabase(context: Context): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "quiz_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}