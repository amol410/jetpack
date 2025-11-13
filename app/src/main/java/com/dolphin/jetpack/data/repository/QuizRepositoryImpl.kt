// File: QuizRepositoryImpl.kt
package com.dolphin.jetpack.data.repository

import com.dolphin.jetpack.data.local.dao.QuestionAnswerDao
import com.dolphin.jetpack.data.local.dao.QuizAttemptDao
import com.dolphin.jetpack.data.local.dao.QuizStateDao
import com.dolphin.jetpack.data.local.entity.QuestionAnswerEntity
import com.dolphin.jetpack.data.local.entity.QuizAttemptEntity
import com.dolphin.jetpack.data.local.entity.QuizStateEntity
import com.dolphin.jetpack.domain.model.*
import com.dolphin.jetpack.domain.repository.QuizRemoteRepository
import com.dolphin.jetpack.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString 
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

class QuizRepositoryImpl(
    private val quizAttemptDao: QuizAttemptDao,
    private val questionAnswerDao: QuestionAnswerDao,
    private val quizStateDao: QuizStateDao,
    private val quizRemoteRepository: QuizRemoteRepository
) : QuizRepository {

    // Quiz Attempts
    override suspend fun saveQuizAttempt(attempt: QuizAttempt): Long {
        // Get the user ID from the authentication system
        val userId = getCurrentUserId() ?: "unknown_user"
        
        val entity = QuizAttemptEntity(
            id = attempt.id,
            userId = userId,
            quizTitle = attempt.quizTitle,
            dateTime = attempt.dateTime,
            score = attempt.score,
            totalQuestions = attempt.totalQuestions,
            timeTakenSeconds = attempt.timeTakenSeconds,
            percentage = attempt.percentage,
            timerEnabled = attempt.timerEnabled,
            timerMinutes = attempt.timerMinutes
        )
        return quizAttemptDao.insertAttempt(entity)
    }

    override fun getAllAttempts(): Flow<List<QuizAttempt>> {
        val userId = getCurrentUserId() ?: "unknown_user"
        return quizAttemptDao.getAllAttemptsByUserId(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAttemptById(id: Long): QuizAttempt? {
        val entity = quizAttemptDao.getAttemptById(id) ?: return null
        val answers = questionAnswerDao.getQuestionAnswersByAttemptSync(id)
        return entity.toDomain(answers.map { it.toDomain() })
    }

    override fun getAttemptsByQuiz(quizTitle: String): Flow<List<QuizAttempt>> {
        val userId = getCurrentUserId() ?: "unknown_user"
        return quizAttemptDao.getAttemptsByQuizForUser(userId, quizTitle).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFilteredAttempts(filter: FilterOptions): Flow<List<QuizAttempt>> {
        val userId = getCurrentUserId() ?: "unknown_user"
        return when {
            filter.startDate != null && filter.endDate != null -> {
                quizAttemptDao.getAttemptsByDateRangeForUser(userId, filter.startDate, filter.endDate)
            }
            filter.minScore != null && filter.maxScore != null -> {
                quizAttemptDao.getAttemptsByScoreRangeForUser(userId, filter.minScore, filter.maxScore)
            }
            filter.quizTitle != null -> {
                quizAttemptDao.getAttemptsByQuizForUser(userId, filter.quizTitle!!)
            }
            else -> {
                quizAttemptDao.getAllAttemptsByUserId(userId)
            }
        }.map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun deleteAttempt(id: Long) {
        quizAttemptDao.deleteAttemptById(id)
    }

    override suspend fun deleteAllAttempts() {
        val userId = getCurrentUserId() ?: "unknown_user"
        quizAttemptDao.deleteAllAttemptsForUser(userId)
        questionAnswerDao.deleteAllQuestionAnswersForUser(userId)
    }

    // Question Answers
    override suspend fun saveQuestionAnswers(answers: List<QuestionAnswer>) {
        val entities = answers.map { it.toEntity() }
        questionAnswerDao.insertQuestionAnswers(entities)
    }

    override fun getQuestionAnswersByAttempt(attemptId: Long): Flow<List<QuestionAnswer>> {
        return questionAnswerDao.getQuestionAnswersByAttempt(attemptId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getQuestionAnswersByAttemptSync(attemptId: Long): List<QuestionAnswer> {
        return questionAnswerDao.getQuestionAnswersByAttemptSync(attemptId).map { it.toDomain() }
    }

    // Quiz State
    override suspend fun saveQuizState(state: QuizState) {
        val entity = QuizStateEntity(
            quizTitle = state.quizTitle,
            currentQuestionIndex = state.currentQuestionIndex,
            answersJson = Json.encodeToString(state.answers),
            startTime = state.startTime,
            timerEnabled = state.timerEnabled,
            timerMinutes = state.timerMinutes,
            timeRemaining = state.timeRemaining
        )
        quizStateDao.insertQuizState(entity)
    }

    override suspend fun getQuizState(quizTitle: String): QuizState? {
        val entity = quizStateDao.getQuizState(quizTitle) ?: return null
        return QuizState(
            quizTitle = entity.quizTitle,
            currentQuestionIndex = entity.currentQuestionIndex,
            answers = try {
                Json.decodeFromString(entity.answersJson)
            } catch (e: Exception) {
                emptyMap()
            },
            startTime = entity.startTime,
            timerEnabled = entity.timerEnabled,
            timerMinutes = entity.timerMinutes,
            timeRemaining = entity.timeRemaining
        )
    }

    override suspend fun hasQuizState(quizTitle: String): Boolean {
        return quizStateDao.hasQuizState(quizTitle)
    }

    override suspend fun deleteQuizState(quizTitle: String) {
        quizStateDao.deleteQuizStateByTitle(quizTitle)
    }

    override suspend fun deleteAllQuizStates() {
        quizStateDao.deleteAllQuizStates()
    }

    // Statistics
    override suspend fun getStatistics(): QuizStatistics {
        return QuizStatistics(
            totalAttempts = getTotalAttempts(),
            averageScore = getAverageScore(),
            bestScore = getBestScore(),
            quizWisePerformance = getQuizWisePerformance(),
            mostWrongQuestions = getMostWrongQuestions(),
            improvementData = getImprovementData()
        )
    }

    override suspend fun getTotalAttempts(): Int {
        val userId = getCurrentUserId() ?: "unknown_user"
        return quizAttemptDao.getTotalAttemptsForUser(userId)
    }

    override suspend fun getAverageScore(): Double {
        val userId = getCurrentUserId() ?: "unknown_user"
        return quizAttemptDao.getAverageScoreForUser(userId) ?: 0.0
    }

    override suspend fun getBestScore(): Int {
        val userId = getCurrentUserId() ?: "unknown_user"
        return quizAttemptDao.getBestScoreForUser(userId) ?: 0
    }

    override suspend fun getQuizWisePerformance(): List<QuizPerformance> {
        val userId = getCurrentUserId() ?: "unknown_user"
        val counts = quizAttemptDao.getQuizAttemptCountsForUser(userId)
        val avgScores = quizAttemptDao.getAverageScoreByQuizForUser(userId)

        return counts.map { count ->
            val avgScore = avgScores.find { it.quizTitle == count.quizTitle }?.avgScore ?: 0.0
            QuizPerformance(
                quizTitle = count.quizTitle,
                attemptCount = count.count,
                averageScore = avgScore
            )
        }
    }

    override suspend fun getMostWrongQuestions(limit: Int): List<WrongQuestion> {
        val userId = getCurrentUserId() ?: "unknown_user"
        return questionAnswerDao.getMostWrongQuestionsForUser(userId, limit).map {
            WrongQuestion(it.questionText, it.wrongCount)
        }
    }

    override suspend fun getImprovementData(): List<ImprovementPoint> {
        val userId = getCurrentUserId() ?: "unknown_user"
        val attempts = quizAttemptDao.getAllAttemptsSyncByUserId(userId)
        return attempts.sortedBy { it.dateTime }.map {
            ImprovementPoint(it.dateTime, it.percentage)
        }
    }

    // Export
    override suspend fun exportToCSV(): String {
        val userId = getCurrentUserId() ?: "unknown_user"
        val attempts = quizAttemptDao.getAllAttemptsSyncByUserId(userId)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val csv = StringBuilder()
        csv.append("Quiz Title,Date,Score,Total Questions,Percentage,Time Taken (seconds),Timer Enabled,Timer Minutes\n")

        attempts.forEach { attempt ->
            csv.append("${attempt.quizTitle},")
            csv.append("${dateFormat.format(Date(attempt.dateTime))},")
            csv.append("${attempt.score},")
            csv.append("${attempt.totalQuestions},")
            csv.append("${attempt.percentage}%,")
            csv.append("${attempt.timeTakenSeconds},")
            csv.append("${attempt.timerEnabled},")
            csv.append("${attempt.timerMinutes}\n")
        }

        return csv.toString()
    }

    // Current user ID - needs to be set from the authentication system
    private var currentUserId: String? = null
    
    fun setCurrentUserId(userId: String?) {
        currentUserId = userId
    }
    
    private fun getCurrentUserId(): String? {
        return currentUserId
    }

    // Extension functions
    private fun QuizAttemptEntity.toDomain(answers: List<QuestionAnswer> = emptyList()): QuizAttempt {
        return QuizAttempt(
            id = id,
            quizTitle = quizTitle,
            dateTime = dateTime,
            score = score,
            totalQuestions = totalQuestions,
            timeTakenSeconds = timeTakenSeconds,
            percentage = percentage,
            timerEnabled = timerEnabled,
            timerMinutes = timerMinutes,
            questionAnswers = answers
        )
    }

    private fun QuestionAnswerEntity.toDomain(): QuestionAnswer {
        return QuestionAnswer(
            id = id,
            attemptId = attemptId,
            questionIndex = questionIndex,
            questionText = questionText,
            selectedAnswer = selectedAnswer,
            correctAnswer = correctAnswer,
            isCorrect = isCorrect
        )
    }

    private fun QuestionAnswer.toEntity(): QuestionAnswerEntity {
        // Use the current user ID for the question answer
        val userId = getCurrentUserId() ?: "unknown_user"
        
        return QuestionAnswerEntity(
            id = id,
            attemptId = attemptId,
            userId = userId,
            questionIndex = questionIndex,
            questionText = questionText,
            selectedAnswer = selectedAnswer,
            correctAnswer = correctAnswer,
            isCorrect = isCorrect
        )
    }

    // Remote sync operations
    override suspend fun syncQuizAttempt(firebaseUid: String, attempt: QuizAttempt): Result<Long> {
        // Attempt is already saved locally by the ViewModel
        // Just sync to remote backend
        return try {
            quizRemoteRepository.saveQuizAttempt(firebaseUid, attempt)
        } catch (e: Exception) {
            // Return failure if remote sync fails
            Result.failure(e)
        }
    }

    override suspend fun syncAllQuizAttempts(firebaseUid: String): Result<List<QuizAttempt>> {
        return try {
            // Get from remote backend
            val remoteResult = quizRemoteRepository.getUserQuizAttempts(firebaseUid)
            remoteResult.onSuccess { remoteAttempts ->
                // Optionally sync these to local database as well
                // For now, just return the remote attempts
                return@onSuccess
            }
            remoteResult
        } catch (e: Exception) {
            // Fallback to local data if remote sync fails
            Result.success(quizAttemptDao.getAllAttemptsSync().map { it.toDomain() })
        }
    }

    override suspend fun syncUserStatistics(firebaseUid: String): Result<QuizStatistics> {
        return try {
            quizRemoteRepository.getUserStatistics(firebaseUid)
        } catch (e: Exception) {
            // Fallback to local statistics if remote sync fails
            Result.success(getStatistics())
        }
    }
}
