// File: QuizDatabase.kt
package com.dolphin.jetpack.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dolphin.jetpack.data.local.converter.Converters
import com.dolphin.jetpack.data.local.dao.ChapterDao
import com.dolphin.jetpack.data.local.dao.NoteDao
import com.dolphin.jetpack.data.local.dao.QuestionAnswerDao
import com.dolphin.jetpack.data.local.dao.QuizAttemptDao
import com.dolphin.jetpack.data.local.dao.QuizDao
import com.dolphin.jetpack.data.local.dao.QuizStateDao
import com.dolphin.jetpack.data.local.entity.ChapterEntity
import com.dolphin.jetpack.data.local.entity.NoteEntity
import com.dolphin.jetpack.data.local.entity.QuestionAnswerEntity
import com.dolphin.jetpack.data.local.entity.QuizAttemptEntity
import com.dolphin.jetpack.data.local.entity.QuizEntity
import com.dolphin.jetpack.data.local.entity.QuizStateEntity
import com.dolphin.jetpack.data.local.entity.QuizTypeConverters
import com.dolphin.jetpack.data.local.entity.ChapterTypeConverters

@Database(
    entities = [
        QuizAttemptEntity::class,
        QuestionAnswerEntity::class,
        QuizStateEntity::class,
        QuizEntity::class,
        ChapterEntity::class,
        NoteEntity::class
    ],
    version = 6,  // Updated version for manuallyDownloaded column in both Chapter and Quiz
    exportSchema = false
)
@TypeConverters(Converters::class, QuizTypeConverters::class, ChapterTypeConverters::class)
abstract class QuizDatabase : RoomDatabase() {

    abstract fun quizAttemptDao(): QuizAttemptDao
    abstract fun questionAnswerDao(): QuestionAnswerDao
    abstract fun quizStateDao(): QuizStateDao
    abstract fun quizDao(): QuizDao
    abstract fun chapterDao(): ChapterDao
    abstract fun noteDao(): NoteDao

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
                    .fallbackToDestructiveMigration() // For simplicity in this case
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}