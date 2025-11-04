package com.dolphin.jetpack.presentation.util

import com.dolphin.jetpack.domain.model.QuizAttempt
import java.text.SimpleDateFormat
import java.util.*

object HistoryExporter {

    fun exportToCsv(attempts: List<QuizAttempt>): String {
        val header = "Quiz Title,Score,Total Questions,Percentage,Date,Time Taken (s)"
        val rows = attempts.map { attempt ->
            val date = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(attempt.dateTime))
            "${attempt.quizTitle},${attempt.score},${attempt.totalQuestions},${attempt.percentage},${date},${attempt.timeTakenSeconds}"
        }
        return header + "\n" + rows.joinToString("\n")
    }
}
