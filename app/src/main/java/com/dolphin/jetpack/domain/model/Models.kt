package com.dolphin.jetpack.domain.model

data class QuizAttempt(
    val id: Long = 0,
    val quizTitle: String,
    val dateTime: Long,
    val score: Int,
    val totalQuestions: Int,
    val timeTakenSeconds: Long,
    val percentage: Int,
    val timerEnabled: Boolean,
    val timerMinutes: Int,
    val questionAnswers: List<QuestionAnswer> = emptyList()
)
// In a file like domain/model/Quiz.kt
data class Quiz(
    val title: String,
    val questions: List<Question>
)

data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

data class QuestionAnswer(
    val id: Long = 0,
    val attemptId: Long,
    val questionIndex: Int,
    val questionText: String,
    val selectedAnswer: Int,
    val correctAnswer: Int,
    val isCorrect: Boolean
)

data class QuizState(
    val quizTitle: String,
    val currentQuestionIndex: Int,
    val answers: Map<Int, Int>,
    val startTime: Long,
    val timerEnabled: Boolean,
    val timerMinutes: Int,
    val timeRemaining: Int?
)

data class QuizStatistics(
    val totalAttempts: Int,
    val averageScore: Double,
    val bestScore: Int,
    val quizWisePerformance: List<QuizPerformance>,
    val mostWrongQuestions: List<WrongQuestion>,
    val improvementData: List<ImprovementPoint>
)

data class QuizPerformance(
    val quizTitle: String,
    val attemptCount: Int,
    val averageScore: Double
)

data class WrongQuestion(
    val questionText: String,
    val wrongCount: Int
)

data class ImprovementPoint(
    val date: Long,
    val score: Int
)

data class FilterOptions(
    val quizTitle: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val minScore: Int? = null,
    val maxScore: Int? = null
)

// Notes models
data class Chapter(
    val id: Int,
    val title: String,
    val description: String,
    val topics: List<Topic>,
    val completionPercentage: Int = 0
)

data class Topic(
    val id: Int,
    val chapterId: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val content: String = ""
)

data class Note(
    val id: Int,
    val topicId: Int,
    val title: String,
    val content: String,
    val orderIndex: Int = 0,
    val topicTitle: String? = null,
    val chapterId: Int? = null,
    val chapterTitle: String? = null
)