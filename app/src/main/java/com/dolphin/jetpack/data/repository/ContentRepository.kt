package com.dolphin.jetpack.data.repository

import android.util.Log
import com.dolphin.jetpack.data.remote.NetworkResult
import com.dolphin.jetpack.data.remote.RetrofitClient
import com.dolphin.jetpack.domain.model.Chapter
import com.dolphin.jetpack.domain.model.Topic
import com.dolphin.jetpack.domain.model.Note
import com.dolphin.jetpack.domain.model.Question
import com.dolphin.jetpack.domain.model.Quiz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentRepository {
    private val api = RetrofitClient.apiService
    private val TAG = "ContentRepository"

    suspend fun getChapters(): NetworkResult<List<Chapter>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching chapters from API...")
            val response = api.getChapters()

            if (response.isSuccessful && response.body()?.success == true) {
                val chapters = response.body()?.data?.map { chapterResponse ->
                    Chapter(
                        id = chapterResponse.id,
                        title = chapterResponse.title,
                        description = chapterResponse.description ?: "",
                        topics = chapterResponse.topics.map { topicResponse ->
                            Topic(
                                id = topicResponse.id,
                                chapterId = topicResponse.chapter_id,
                                title = topicResponse.title,
                                description = topicResponse.description ?: "",
                                isCompleted = false,
                                content = topicResponse.content ?: ""
                            )
                        },
                        completionPercentage = 0
                    )
                } ?: emptyList()

                Log.d(TAG, "Successfully fetched ${chapters.size} chapters")
                NetworkResult.Success(chapters)
            } else {
                val errorMsg = response.body()?.message ?: "Failed to fetch chapters"
                Log.e(TAG, "Error: $errorMsg")
                NetworkResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Network error occurred")
        }
    }

    suspend fun getTopic(topicId: Int): NetworkResult<Topic> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching topic $topicId from API...")
            val response = api.getTopic(topicId)

            if (response.isSuccessful && response.body()?.success == true) {
                val topicResponse = response.body()?.data
                if (topicResponse != null) {
                    val topic = Topic(
                        id = topicResponse.id,
                        chapterId = topicResponse.chapter_id,
                        title = topicResponse.title,
                        description = topicResponse.description ?: "",
                        isCompleted = false,
                        content = topicResponse.content ?: ""
                    )
                    Log.d(TAG, "Successfully fetched topic: ${topic.title}")
                    NetworkResult.Success(topic)
                } else {
                    Log.e(TAG, "Topic data is null")
                    NetworkResult.Error("Topic not found")
                }
            } else {
                val errorMsg = response.body()?.message ?: "Failed to fetch topic"
                Log.e(TAG, "Error: $errorMsg")
                NetworkResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Network error occurred")
        }
    }

    suspend fun getNotes(topicId: Int): NetworkResult<List<Note>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching notes for topic $topicId from API...")
            val response = api.getNotes(topicId)

            if (response.isSuccessful && response.body()?.success == true) {
                val notes = response.body()?.data?.map { noteResponse ->
                    Note(
                        id = noteResponse.id,
                        topicId = noteResponse.topic_id,
                        title = noteResponse.title,
                        content = noteResponse.content ?: "",
                        orderIndex = noteResponse.order_index,
                        topicTitle = noteResponse.topic_title,
                        chapterId = noteResponse.chapter_id,
                        chapterTitle = noteResponse.chapter_title
                    )
                } ?: emptyList()

                Log.d(TAG, "Successfully fetched ${notes.size} notes")
                NetworkResult.Success(notes)
            } else {
                val errorMsg = response.body()?.message ?: "Failed to fetch notes"
                Log.e(TAG, "Error: $errorMsg")
                NetworkResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Network error occurred")
        }
    }

    suspend fun getQuizzes(): NetworkResult<List<Quiz>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching quizzes list from API...")
            val response = api.getQuizzes()

            if (response.isSuccessful && response.body()?.success == true) {
                val quizList = response.body()?.data ?: emptyList()
                Log.d(TAG, "Found ${quizList.size} quizzes, fetching details...")

                // Fetch full quiz details for each quiz
                val quizzes = quizList.mapNotNull { quizResponse ->
                    try {
                        val detailResponse = api.getQuiz(quizResponse.id)
                        if (detailResponse.isSuccessful && detailResponse.body()?.success == true) {
                            val quizDetail = detailResponse.body()?.data
                            quizDetail?.let {
                                Quiz(
                                    title = it.title,
                                    questions = it.questions.map { questionResponse ->
                                        Question(
                                            text = questionResponse.text,
                                            options = questionResponse.options,
                                            correctAnswerIndex = questionResponse.correctAnswerIndex
                                        )
                                    }
                                )
                            }
                        } else {
                            Log.w(TAG, "Failed to fetch quiz ${quizResponse.id}")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching quiz ${quizResponse.id}: ${e.message}")
                        null
                    }
                }

                Log.d(TAG, "Successfully loaded ${quizzes.size} complete quizzes")
                NetworkResult.Success(quizzes)
            } else {
                val errorMsg = response.body()?.message ?: "Failed to fetch quizzes"
                Log.e(TAG, "Error: $errorMsg")
                NetworkResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Network error occurred")
        }
    }

    suspend fun getQuiz(quizId: Int): NetworkResult<Quiz> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching quiz $quizId from API...")
            val response = api.getQuiz(quizId)

            if (response.isSuccessful && response.body()?.success == true) {
                val quizResponse = response.body()?.data
                if (quizResponse != null) {
                    val quiz = Quiz(
                        title = quizResponse.title,
                        questions = quizResponse.questions.map { questionResponse ->
                            Question(
                                text = questionResponse.text,
                                options = questionResponse.options,
                                correctAnswerIndex = questionResponse.correctAnswerIndex
                            )
                        }
                    )
                    Log.d(TAG, "Successfully fetched quiz: ${quiz.title} with ${quiz.questions.size} questions")
                    NetworkResult.Success(quiz)
                } else {
                    Log.e(TAG, "Quiz data is null")
                    NetworkResult.Error("Quiz not found")
                }
            } else {
                val errorMsg = response.body()?.message ?: "Failed to fetch quiz"
                Log.e(TAG, "Error: $errorMsg")
                NetworkResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Network error occurred")
        }
    }
}
