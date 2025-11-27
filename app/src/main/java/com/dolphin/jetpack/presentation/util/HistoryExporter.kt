package com.dolphin.jetpack.presentation.util

import com.dolphin.jetpack.domain.model.QuizAttempt
import java.text.SimpleDateFormat
import java.util.*

object HistoryExporter {

    fun exportToCsv(attempts: List<QuizAttempt>): String {
        val header = "Quiz Title,Score,Total Questions,Percentage,Date,Time Taken (s)"
        val rows = attempts.map { attempt ->
            // Create local SimpleDateFormat instance for thread safety
            val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = try {
                dateFormatter.format(Date(attempt.dateTime))
            } catch (e: Exception) {
                "Invalid Date"
            }
            "${attempt.quizTitle},${attempt.score},${attempt.totalQuestions},${attempt.percentage},${date},${attempt.timeTakenSeconds}"
        }
        return header + "\n" + rows.joinToString("\n")
    }
}
