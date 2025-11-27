package com.dolphin.jetpack.data.remote

import com.dolphin.jetpack.domain.model.*
import com.dolphin.jetpack.domain.repository.QuizRemoteRepository
import javax.inject.Inject

class QuizRemoteRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : QuizRemoteRepository {
    
    override suspend fun saveQuizAttempt(
        firebaseUid: String,
        quizAttempt: QuizAttempt
    ): Result<Long> {
        return try {
            android.util.Log.d("QuizRemoteRepo", "════════════════════════════════════════")
            android.util.Log.d("QuizRemoteRepo", "🌐 BACKEND API CALL: saveQuizAttempt")
            android.util.Log.d("QuizRemoteRepo", "════════════════════════════════════════")
            android.util.Log.d("QuizRemoteRepo", "📋 Request Parameters:")
            android.util.Log.d("QuizRemoteRepo", "   - Firebase UID: $firebaseUid")
            android.util.Log.d("QuizRemoteRepo", "   - Quiz Title: ${quizAttempt.quizTitle}")
            android.util.Log.d("QuizRemoteRepo", "   - Score: ${quizAttempt.score}/${quizAttempt.totalQuestions}")
            android.util.Log.d("QuizRemoteRepo", "   - Percentage: ${quizAttempt.percentage}%")
            android.util.Log.d("QuizRemoteRepo", "   - Time Taken: ${quizAttempt.timeTakenSeconds}s")
            android.util.Log.d("QuizRemoteRepo", "   - Timer Enabled: ${quizAttempt.timerEnabled}")
            android.util.Log.d("QuizRemoteRepo", "   - Question Answers: ${quizAttempt.questionAnswers.size}")

            val questionAnswers = quizAttempt.questionAnswers.map { answer ->
                QuestionAnswerRequest(
                    question_index = answer.questionIndex,
                    question_text = answer.questionText,
                    selected_answer = answer.selectedAnswer.toString(), // Convert Int to String
                    correct_answer = answer.correctAnswer.toString(), // Convert Int to String
                    is_correct = answer.isCorrect
                )
            }

            val request = QuizAttemptRequest(
                firebase_uid = firebaseUid,
                quiz_title = quizAttempt.quizTitle,
                score = quizAttempt.score,
                total_questions = quizAttempt.totalQuestions,
                time_taken_seconds = quizAttempt.timeTakenSeconds,
                percentage = quizAttempt.percentage,
                timer_enabled = quizAttempt.timerEnabled,
                timer_minutes = quizAttempt.timerMinutes,
                question_answers = questionAnswers
            )

            android.util.Log.d("QuizRemoteRepo", "────────────────────────────────────────")
            android.util.Log.d("QuizRemoteRepo", "📡 Sending POST request to:")
            android.util.Log.d("QuizRemoteRepo", "   URL: https://jetpack.dolphincoder.com/api/save_quiz_attempt.php")
            android.util.Log.d("QuizRemoteRepo", "   Method: POST")
            android.util.Log.d("QuizRemoteRepo", "   Content-Type: application/json")
            android.util.Log.d("QuizRemoteRepo", "────────────────────────────────────────")

            val startTime = System.currentTimeMillis()
            val response = apiService.saveQuizAttempt(request)
            val duration = System.currentTimeMillis() - startTime

            android.util.Log.d("QuizRemoteRepo", "────────────────────────────────────────")
            android.util.Log.d("QuizRemoteRepo", "📥 Response Received (${duration}ms):")
            android.util.Log.d("QuizRemoteRepo", "   - HTTP Status: ${response.code()}")
            android.util.Log.d("QuizRemoteRepo", "   - Is Successful: ${response.isSuccessful}")
            android.util.Log.d("QuizRemoteRepo", "   - Response Body Success: ${response.body()?.success}")
            android.util.Log.d("QuizRemoteRepo", "   - Response Message: ${response.body()?.message}")
            android.util.Log.d("QuizRemoteRepo", "   - Response Data: ${response.body()?.data}")

            if (response.isSuccessful && response.body()?.success == true) {
                val attemptId = response.body()?.data?.attempt_id ?: -1
                android.util.Log.d("QuizRemoteRepo", "════════════════════════════════════════")
                android.util.Log.d("QuizRemoteRepo", "✅ SUCCESS: Backend sync completed!")
                android.util.Log.d("QuizRemoteRepo", "   - Attempt ID: $attemptId")
                android.util.Log.d("QuizRemoteRepo", "════════════════════════════════════════")
                Result.success(attemptId)
            } else {
                val errorMsg = response.body()?.message ?: "Failed to save quiz attempt"
                val errorBody = try {
                    response.errorBody()?.string() ?: "No error body"
                } catch (e: Exception) {
                    "Error reading error body: ${e.message}"
                }

                android.util.Log.e("QuizRemoteRepo", "════════════════════════════════════════")
                android.util.Log.e("QuizRemoteRepo", "❌ FAILED: Backend sync failed!")
                android.util.Log.e("QuizRemoteRepo", "   - HTTP Status: ${response.code()}")
                android.util.Log.e("QuizRemoteRepo", "   - Error Message: $errorMsg")
                android.util.Log.e("QuizRemoteRepo", "   - Response Success Flag: ${response.body()?.success}")
                android.util.Log.e("QuizRemoteRepo", "   - Error Body: $errorBody")
                android.util.Log.e("QuizRemoteRepo", "════════════════════════════════════════")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("QuizRemoteRepo", "════════════════════════════════════════")
            android.util.Log.e("QuizRemoteRepo", "💥 EXCEPTION during backend sync!")
            android.util.Log.e("QuizRemoteRepo", "   - Exception Type: ${e.javaClass.simpleName}")
            android.util.Log.e("QuizRemoteRepo", "   - Message: ${e.message}")
            android.util.Log.e("QuizRemoteRepo", "   - Cause: ${e.cause?.message ?: "None"}")

            // Check for common network errors
            when (e) {
                is java.net.UnknownHostException -> {
                    android.util.Log.e("QuizRemoteRepo", "   - Type: NETWORK ERROR - Cannot resolve hostname")
                    android.util.Log.e("QuizRemoteRepo", "   - Possible causes: No internet, DNS issue, server down")
                }
                is java.net.SocketTimeoutException -> {
                    android.util.Log.e("QuizRemoteRepo", "   - Type: TIMEOUT ERROR - Server took too long to respond")
                }
                is java.net.ConnectException -> {
                    android.util.Log.e("QuizRemoteRepo", "   - Type: CONNECTION ERROR - Cannot connect to server")
                }
                is javax.net.ssl.SSLException -> {
                    android.util.Log.e("QuizRemoteRepo", "   - Type: SSL ERROR - Certificate or encryption issue")
                }
            }

            android.util.Log.e("QuizRemoteRepo", "   - Stack Trace:", e)
            android.util.Log.e("QuizRemoteRepo", "════════════════════════════════════════")
            Result.failure(e)
        }
    }
    
    override suspend fun getUserQuizAttempts(firebaseUid: String): Result<List<QuizAttempt>> {
        return try {
            android.util.Log.d("QuizRemoteRepo", "════════════════════════════════════════")
            android.util.Log.d("QuizRemoteRepo", "📥 GET USER QUIZ ATTEMPTS")
            android.util.Log.d("QuizRemoteRepo", "   - Firebase UID: $firebaseUid")
            android.util.Log.d("QuizRemoteRepo", "   - API: get_user_quiz_attempts.php")

            val response = apiService.getUserQuizAttempts(firebaseUid)

            android.util.Log.d("QuizRemoteRepo", "   - HTTP Status: ${response.code()}")
            android.util.Log.d("QuizRemoteRepo", "   - Is Successful: ${response.isSuccessful}")
            android.util.Log.d("QuizRemoteRepo", "   - Success Flag: ${response.body()?.success}")

            if (response.isSuccessful && response.body()?.success == true) {
                val backendAttempts = response.body()?.data ?: emptyList()
                android.util.Log.d("QuizRemoteRepo", "   - Backend attempts: ${backendAttempts.size}")

                val attempts = backendAttempts.map { backend ->
                    // Convert backend date string to timestamp
                    val dateTime = try {
                        // Assuming date format is "YYYY-MM-DD HH:MM:SS"
                        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        formatter.parse(backend.date_time)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis() // fallback
                    }

                    QuizAttempt(
                        id = backend.id,
                        quizTitle = backend.quiz_title,
                        dateTime = dateTime,
                        score = backend.score,
                        totalQuestions = backend.total_questions,
                        timeTakenSeconds = backend.time_taken_seconds,
                        percentage = backend.percentage,
                        timerEnabled = backend.timer_enabled == 1,  // Convert 0/1 to boolean
                        timerMinutes = backend.timer_minutes,
                        questionAnswers = emptyList() // We'll fetch this separately if needed
                    )
                }

                android.util.Log.d("QuizRemoteRepo", "✅ SUCCESS: Fetched ${attempts.size} quiz attempts")
                android.util.Log.d("QuizRemoteRepo", "════════════════════════════════════════")
                Result.success(attempts)
            } else {
                val errorMsg = response.body()?.message ?: "Failed to get quiz attempts"
                android.util.Log.e("QuizRemoteRepo", "❌ FAILED: $errorMsg")
                android.util.Log.e("QuizRemoteRepo", "   - Response code: ${response.code()}")
                android.util.Log.e("QuizRemoteRepo", "════════════════════════════════════════")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("QuizRemoteRepo", "💥 EXCEPTION: ${e.message}", e)
            android.util.Log.e("QuizRemoteRepo", "════════════════════════════════════════")
            Result.failure(e)
        }
    }
    
    override suspend fun getQuizAttemptDetail(attemptId: Long): Result<QuizAttempt> {
        return try {
            val response = apiService.getQuizAttemptDetail(attemptId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: return Result.failure(Exception("No data returned"))
                val backendAttempt = data.attempt
                val backendAnswers = data.answers
                
                val dateTime = try {
                    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    formatter.parse(backendAttempt.date_time)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis() // fallback
                }
                
                val questionAnswers = backendAnswers.map { answer ->
                    QuestionAnswer(
                        id = 0, // Backend ID is not used in the local model
                        attemptId = attemptId,
                        questionIndex = answer.question_index,
                        questionText = answer.question_text,
                        selectedAnswer = answer.selected_answer.toIntOrNull() ?: -1, // Convert String to Int
                        correctAnswer = answer.correct_answer.toIntOrNull() ?: -1, // Convert String to Int
                        isCorrect = answer.is_correct == 1  // Convert 0/1 to boolean
                    )
                }

                Result.success(
                    QuizAttempt(
                        id = backendAttempt.id,
                        quizTitle = backendAttempt.quiz_title,
                        dateTime = dateTime,
                        score = backendAttempt.score,
                        totalQuestions = backendAttempt.total_questions,
                        timeTakenSeconds = backendAttempt.time_taken_seconds,
                        percentage = backendAttempt.percentage,
                        timerEnabled = backendAttempt.timer_enabled == 1,  // Convert 0/1 to boolean
                        timerMinutes = backendAttempt.timer_minutes,
                        questionAnswers = questionAnswers
                    )
                )
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get quiz attempt detail"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserStatistics(firebaseUid: String): Result<QuizStatistics> {
        return try {
            val response = apiService.getUserStatistics(firebaseUid)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: return Result.failure(Exception("No statistics data returned"))
                
                val quizPerformance = data.quiz_wise_performance.map { perf ->
                    QuizPerformance(
                        quizTitle = perf.quiz_title,
                        attemptCount = perf.attempt_count,
                        averageScore = perf.avg_score
                    )
                }
                
                val wrongQuestions = data.most_wrong_questions.map { wrong ->
                    WrongQuestion(
                        questionText = wrong.question_text,
                        wrongCount = wrong.wrong_count
                    )
                }
                
                val improvementData = data.improvement_data.map { point ->
                    ImprovementPoint(
                        date = point.timestamp,
                        score = point.score.toInt()
                    )
                }
                
                Result.success(
                    QuizStatistics(
                        totalAttempts = data.total_attempts,
                        averageScore = data.average_score,
                        bestScore = data.best_score,
                        quizWisePerformance = quizPerformance,
                        mostWrongQuestions = wrongQuestions,
                        improvementData = improvementData
                    )
                )
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get user statistics"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}