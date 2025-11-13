package com.dolphin.jetpack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_notes")
data class NoteEntity(
    @PrimaryKey
    val id: Int,
    val topicId: Int,
    val title: String,
    val content: String,
    val orderIndex: Int,
    val topicTitle: String?,
    val chapterId: Int?,
    val chapterTitle: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)
