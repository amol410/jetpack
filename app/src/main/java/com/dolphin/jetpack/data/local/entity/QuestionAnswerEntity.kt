package com.dolphin.jetpack.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "question_answers",
    foreignKeys = [
        ForeignKey(
            entity = QuizAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["attemptId", "userId"])]
)
data class QuestionAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val attemptId: Long,
    val userId: String, // Firebase user ID
    val questionIndex: Int,
    val questionText: String,
    val selectedAnswer: Int,
    val correctAnswer: Int,
    val isCorrect: Boolean
)