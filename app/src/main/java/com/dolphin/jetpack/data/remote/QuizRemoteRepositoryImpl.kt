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
            
            val response = apiService.saveQuizAttempt(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val attemptId = response.body()?.data?.attempt_id ?: -1
                Result.success(attemptId)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to save quiz attempt"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserQuizAttempts(firebaseUid: String): Result<List<QuizAttempt>> {
        return try {
            val response = apiService.getUserQuizAttempts(firebaseUid)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val backendAttempts = response.body()?.data ?: emptyList()
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
                        timerEnabled = backend.timer_enabled,
                        timerMinutes = backend.timer_minutes,
                        questionAnswers = emptyList() // We'll fetch this separately if needed
                    )
                }
                Result.success(attempts)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get quiz attempts"))
            }
        } catch (e: Exception) {
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
                        isCorrect = answer.is_correct
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
                        timerEnabled = backendAttempt.timer_enabled,
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