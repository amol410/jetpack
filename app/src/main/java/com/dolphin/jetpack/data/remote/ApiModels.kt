package com.dolphin.jetpack.data.remote

// Response wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)

// Chapter API Models
data class ChapterResponse(
    val id: Int,
    val title: String,
    val description: String?,
    val order_index: Int,
    val topic_count: Int,
    val topics: List<TopicResponse>
)

data class TopicResponse(
    val id: Int,
    val chapter_id: Int,
    val title: String,
    val description: String?,
    val content: String?,
    val order_index: Int,
    val chapter_title: String?
)

// Note API Models
data class NoteResponse(
    val id: Int,
    val topic_id: Int,
    val title: String,
    val content: String?,
    val order_index: Int,
    val topic_title: String?,
    val chapter_id: Int?,
    val chapter_title: String?
)

// Quiz API Models
data class QuizListResponse(
    val id: Int,
    val title: String,
    val description: String?,
    val question_count: Int?
)

data class QuizDetailResponse(
    val id: Int,
    val title: String,
    val description: String?,
    val questions: List<QuestionResponse>
)

data class QuestionResponse(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String?
)

// User API Models
data class UserRegisterRequest(
    val firebase_uid: String,
    val email: String?,
    val display_name: String?,
    val photo_url: String?
)

data class UserRegisterResponse(
    val user_id: Int,
    val firebase_uid: String,
    val email: String?,
    val display_name: String?
)

data class SessionStartRequest(
    val firebase_uid: String,
    val device_id: String?,
    val device_model: String?,
    val os_version: String?,
    val app_version: String?
)

data class SessionStartResponse(
    val session_id: Int,
    val user_id: Int
)

data class SessionEndRequest(
    val session_id: Int
)

// Quiz Data Models
data class QuizAttemptRequest(
    val firebase_uid: String,
    val quiz_title: String,
    val score: Int,
    val total_questions: Int,
    val time_taken_seconds: Long,
    val percentage: Int,
    val timer_enabled: Boolean = false,
    val timer_minutes: Int = 0,
    val question_answers: List<QuestionAnswerRequest> = emptyList()
)

data class QuestionAnswerRequest(
    val question_index: Int,
    val question_text: String,
    val selected_answer: String,
    val correct_answer: String,
    val is_correct: Boolean
)

data class QuizAttemptData(
    val attempt_id: Long? = null
)

data class QuizAttemptBackend(
    val id: Long,
    val quiz_title: String,
    val date_time: String,
    val score: Int,
    val total_questions: Int,
    val time_taken_seconds: Long,
    val percentage: Int,
    val timer_enabled: Boolean,
    val timer_minutes: Int
)

data class QuizAttemptDetailData(
    val attempt: QuizAttemptBackend,
    val answers: List<QuestionAnswerBackend> = emptyList()
)

data class QuestionAnswerBackend(
    val question_index: Int,
    val question_text: String,
    val selected_answer: String,
    val correct_answer: String,
    val is_correct: Boolean
)

data class StatisticsData(
    val total_attempts: Int,
    val average_score: Double,
    val best_score: Int,
    val quiz_wise_performance: List<QuizPerformanceBackend> = emptyList(),
    val most_wrong_questions: List<WrongQuestionBackend> = emptyList(),
    val improvement_data: List<ImprovementPointBackend> = emptyList()
)

data class QuizPerformanceBackend(
    val quiz_title: String,
    val attempt_count: Int,
    val avg_score: Double
)

data class WrongQuestionBackend(
    val question_text: String,
    val wrong_count: Int
)

data class ImprovementPointBackend(
    val timestamp: Long,
    val score: Double
)
