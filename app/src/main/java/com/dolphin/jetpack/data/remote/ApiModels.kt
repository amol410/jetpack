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
