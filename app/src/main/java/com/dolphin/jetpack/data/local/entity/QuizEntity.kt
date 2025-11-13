package com.dolphin.jetpack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.dolphin.jetpack.domain.model.Question

@Entity(tableName = "offline_quizzes")
@TypeConverters(QuizTypeConverters::class)
data class QuizEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String?,
    val questions: List<Question>,
    val isOffline: Boolean = true,
    val manuallyDownloaded: Boolean = false, // true = user clicked download, false = auto-cached
    val lastUpdated: Long = System.currentTimeMillis()
)

class QuizTypeConverters {
    @TypeConverter
    fun fromQuestionList(value: List<Question>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toQuestionList(value: String): List<Question> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
