package com.dolphin.jetpack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quizTitle: String,
    val dateTime: Long,
    val score: Int,
    val totalQuestions: Int,
    val timeTakenSeconds: Long,
    val percentage: Int,
    val timerEnabled: Boolean,
    val timerMinutes: Int
)