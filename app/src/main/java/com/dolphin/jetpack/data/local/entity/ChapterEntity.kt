package com.dolphin.jetpack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.dolphin.jetpack.domain.model.Topic

@Entity(tableName = "offline_chapters")
@TypeConverters(ChapterTypeConverters::class)
data class ChapterEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String?,
    val orderIndex: Int,
    val topicCount: Int,
    val topics: List<Topic>,
    val isOffline: Boolean = true,
    val manuallyDownloaded: Boolean = false, // true = user clicked download, false = auto-cached
    val lastUpdated: Long = System.currentTimeMillis()
)

class ChapterTypeConverters {
    @TypeConverter
    fun fromTopicList(value: List<Topic>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toTopicList(value: String): List<Topic> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
