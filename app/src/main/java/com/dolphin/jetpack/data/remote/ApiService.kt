package com.dolphin.jetpack.data.remote

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Content APIs
    @GET("get_chapters.php")
    suspend fun getChapters(): Response<ApiResponse<List<ChapterResponse>>>

    @GET("get_topic.php")
    suspend fun getTopic(@Query("id") topicId: Int): Response<ApiResponse<TopicResponse>>

    @GET("get_notes.php")
    suspend fun getNotes(@Query("topic_id") topicId: Int): Response<ApiResponse<List<NoteResponse>>>

    @GET("get_quizzes.php")
    suspend fun getQuizzes(): Response<ApiResponse<List<QuizListResponse>>>

    @GET("get_quiz.php")
    suspend fun getQuiz(@Query("id") quizId: Int): Response<ApiResponse<QuizDetailResponse>>

    // User APIs
    @POST("user_register.php")
    suspend fun registerUser(@Body request: UserRegisterRequest): Response<ApiResponse<UserRegisterResponse>>

    @POST("session_start.php")
    suspend fun startSession(@Body request: SessionStartRequest): Response<ApiResponse<SessionStartResponse>>

    @POST("session_end.php")
    suspend fun endSession(@Body request: SessionEndRequest): Response<ApiResponse<Any>>

    @GET("get_user_stats.php")
    suspend fun getUserStats(@Query("firebase_uid") firebaseUid: String): Response<ApiResponse<Any>>

    // Quiz data APIs
    @POST("save_quiz_attempt.php")
    suspend fun saveQuizAttempt(@Body request: QuizAttemptRequest): Response<ApiResponse<QuizAttemptData>>

    @GET("get_user_quiz_attempts.php")
    suspend fun getUserQuizAttempts(
        @Query("firebase_uid") firebaseUid: String,
        @Query("quiz_title") quizTitle: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<ApiResponse<List<QuizAttemptBackend>>>

    @GET("get_quiz_attempt_detail.php")
    suspend fun getQuizAttemptDetail(@Query("attempt_id") attemptId: Long): Response<ApiResponse<QuizAttemptDetailData>>

    @GET("get_user_statistics.php")
    suspend fun getUserStatistics(@Query("firebase_uid") firebaseUid: String): Response<ApiResponse<StatisticsData>>
}
