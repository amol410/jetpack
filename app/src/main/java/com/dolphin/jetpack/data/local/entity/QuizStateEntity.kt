package com.dolphin.jetpack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_state")
data class QuizStateEntity(
    @PrimaryKey
    val quizTitle: String,
    val currentQuestionIndex: Int,
    val answersJson: String,
    val startTime: Long,
    val timerEnabled: Boolean,
    val timerMinutes: Int,
    val timeRemaining: Int?
)