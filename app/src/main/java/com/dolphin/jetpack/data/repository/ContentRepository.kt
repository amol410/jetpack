package com.dolphin.jetpack.data.repository

import android.text.Html
import android.util.Log
import com.dolphin.jetpack.data.local.dao.ChapterDao
import com.dolphin.jetpack.data.local.dao.NoteDao
import com.dolphin.jetpack.data.local.dao.QuizDao
import com.dolphin.jetpack.data.local.entity.ChapterEntity
import com.dolphin.jetpack.data.local.entity.NoteEntity
import com.dolphin.jetpack.data.local.entity.QuizEntity
import com.dolphin.jetpack.data.remote.NetworkResult
import com.dolphin.jetpack.data.remote.RetrofitClient
import com.dolphin.jetpack.domain.model.Chapter
import com.dolphin.jetpack.domain.model.Topic
import com.dolphin.jetpack.domain.model.Note
import com.dolphin.jetpack.domain.model.Question
import com.dolphin.jetpack.domain.model.Quiz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentRepository(
    private val quizDao: QuizDao? = null,
    private val chapterDao: ChapterDao? = null,
    private val noteDao: NoteDao? = null
) {
    private val api = RetrofitClient.apiService
    private val TAG = "ContentRepository"

    // Helper function to decode HTML entities
    private fun decodeHtml(text: String): String {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
    }

    suspend fun getChapters(): NetworkResult<List<Chapter>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching chapters from API...")
            val response = api.getChapters()

            if (response.isSuccessful && response.body()?.success == true) {
                val chapters = response.body()?.data?.map { chapterResponse ->
                    Chapter(
                        id = chapterResponse.id,
                        title = decodeHtml(chapterResponse.title),
                        description = decodeHtml(chapterResponse.description ?: ""),
                        topics = chapterResponse.topics.map { topicResponse ->
                            Topic(
                                id = topicResponse.id,
                                chapterId = topicResponse.chapter_id,
                                title = decodeHtml(topicResponse.title),
                                description = decodeHtml(topicResponse.description ?: ""),
                                isCompleted = false,
                                content = decodeHtml(topicResponse.content ?: "")
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

                // Try to load from local database if API fails
                if (chapterDao != null) {
                    val cachedEntities = chapterDao.getAllOfflineChaptersList()
                    if (cachedEntities.isNotEmpty()) {
                        val chapters = cachedEntities.map { entity ->
                            Chapter(
                                id = entity.id,
                                title = entity.title,
                                description = entity.description ?: "",
                                topics = entity.topics,
                                completionPercentage = 0
                            )
                        }
                        Log.d(TAG, "Loaded ${chapters.size} chapters from local cache")
                        return@withContext NetworkResult.Success(chapters)
                    }
                }

                NetworkResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)

            // Try to load from local database if network error occurs
            if (chapterDao != null) {
                try {
                    val cachedEntities = chapterDao.getAllOfflineChaptersList()
                    if (cachedEntities.isNotEmpty()) {
                        val chapters = cachedEntities.map { entity ->
                            Chapter(
                                id = entity.id,
                                title = entity.title,
                                description = entity.description ?: "",
                                topics = entity.topics,
                                completionPercentage = 0
                            )
                        }
                        Log.d(TAG, "Loaded ${chapters.size} chapters from local cache (offline)")
                        return@withContext NetworkResult.Success(chapters)
                    }
                } catch (dbException: Exception) {
                    Log.e(TAG, "Failed to load from cache: ${dbException.message}")
                }
            }

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
                        title = decodeHtml(topicResponse.title),
                        description = decodeHtml(topicResponse.description ?: ""),
                        isCompleted = false,
                        content = decodeHtml(topicResponse.content ?: "")
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
                        title = decodeHtml(noteResponse.title),
                        content = noteResponse.content ?: "", // Keep HTML formatting for proper rendering
                        orderIndex = noteResponse.order_index,
                        topicTitle = noteResponse.topic_title?.let { decodeHtml(it) } ?: "",
                        chapterId = noteResponse.chapter_id,
                        chapterTitle = noteResponse.chapter_title?.let { decodeHtml(it) } ?: ""
                    )
                } ?: emptyList()

                Log.d(TAG, "Successfully fetched ${notes.size} notes")
                NetworkResult.Success(notes)
            } else {
                val errorMsg = response.body()?.message ?: "Failed to fetch notes"
                Log.e(TAG, "Error: $errorMsg")

                // Try to load from local database if API fails
                if (noteDao != null) {
                    val cachedNotes = noteDao.getNotesByTopicId(topicId)
                    if (cachedNotes.isNotEmpty()) {
                        Log.d(TAG, "Loaded ${cachedNotes.size} notes from local cache")
                        val notes = cachedNotes.map { entity ->
                            Note(
                                id = entity.id,
                                topicId = entity.topicId,
                                title = entity.title,
                                content = entity.content,
                                orderIndex = entity.orderIndex,
                                topicTitle = entity.topicTitle,
                                chapterId = entity.chapterId,
                                chapterTitle = entity.chapterTitle
                            )
                        }
                        return@withContext NetworkResult.Success(notes)
                    }
                }

                NetworkResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)

            // Try to load from local database if network error occurs
            if (noteDao != null) {
                try {
                    val cachedNotes = noteDao.getNotesByTopicId(topicId)
                    if (cachedNotes.isNotEmpty()) {
                        Log.d(TAG, "Loaded ${cachedNotes.size} notes from local cache (offline)")
                        val notes = cachedNotes.map { entity ->
                            Note(
                                id = entity.id,
                                topicId = entity.topicId,
                                title = entity.title,
                                content = entity.content,
                                orderIndex = entity.orderIndex,
                                topicTitle = entity.topicTitle,
                                chapterId = entity.chapterId,
                                chapterTitle = entity.chapterTitle
                            )
                        }
                        return@withContext NetworkResult.Success(notes)
                    }
                } catch (dbException: Exception) {
                    Log.e(TAG, "Failed to load from cache: ${dbException.message}")
                }
            }

            NetworkResult.Error(e.message ?: "Network error occurred")
        }
    }

    suspend fun getQuizzes(): NetworkResult<List<Quiz>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching quizzes list from API...")
            val response = api.getQuizzes()

            if (response.isSuccessful && response.body()?.success == true) {
                val quizList = response.body()?.data ?: emptyList()

                // Return metadata-only quizzes (no questions loaded yet)
                val quizzes = quizList.map { quizResponse ->
                    Quiz(
                        id = quizResponse.id,
                        title = decodeHtml(quizResponse.title),
                        questions = emptyList(), // Don't load questions yet
                        questionCount = quizResponse.question_count ?: 0
                    )
                }

                Log.d(TAG, "Successfully loaded ${quizzes.size} quiz metadata (questions not loaded)")
                NetworkResult.Success(quizzes)
            } else {
                val errorMsg = response.body()?.message ?: "Failed to fetch quizzes"
                Log.e(TAG, "Error: $errorMsg")

                // Try to load from local database if API fails
                if (quizDao != null) {
                    val cachedEntities = quizDao.getAllOfflineQuizzesList()
                    if (cachedEntities.isNotEmpty()) {
                        val quizzes = cachedEntities.map { entity ->
                            Quiz(
                                id = entity.id,
                                title = entity.title,
                                questions = entity.questions,
                                questionCount = entity.questions.size
                            )
                        }
                        Log.d(TAG, "Loaded ${quizzes.size} quizzes from local cache")
                        return@withContext NetworkResult.Success(quizzes)
                    }
                }

                NetworkResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)

            // Try to load from local database if network error occurs
            if (quizDao != null) {
                try {
                    val cachedEntities = quizDao.getAllOfflineQuizzesList()
                    if (cachedEntities.isNotEmpty()) {
                        val quizzes = cachedEntities.map { entity ->
                            Quiz(
                                id = entity.id,
                                title = entity.title,
                                questions = entity.questions,
                                questionCount = entity.questions.size
                            )
                        }
                        Log.d(TAG, "Loaded ${quizzes.size} quizzes from local cache (offline)")
                        return@withContext NetworkResult.Success(quizzes)
                    }
                } catch (dbException: Exception) {
                    Log.e(TAG, "Failed to load quizzes from cache: ${dbException.message}")
                }
            }

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
                    val questions = quizResponse.questions.map { questionResponse ->
                        Question(
                            text = decodeHtml(questionResponse.text),
                            options = questionResponse.options.map { option -> decodeHtml(option) },
                            correctAnswerIndex = questionResponse.correctAnswerIndex
                        )
                    }
                    val quiz = Quiz(
                        id = quizResponse.id,
                        title = decodeHtml(quizResponse.title),
                        questions = questions,
                        questionCount = questions.size
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

                // Try to load from local database if API fails
                if (quizDao != null) {
                    val cachedQuiz = quizDao.getQuizById(quizId)
                    if (cachedQuiz != null) {
                        Log.d(TAG, "Loaded quiz from local cache")
                        val quiz = Quiz(
                            id = cachedQuiz.id,
                            title = cachedQuiz.title,
                            questions = cachedQuiz.questions,
                            questionCount = cachedQuiz.questions.size
                        )
                        return@withContext NetworkResult.Success(quiz)
                    }
                }

                NetworkResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)

            // Try to load from local database if network error occurs
            if (quizDao != null) {
                try {
                    val cachedQuiz = quizDao.getQuizById(quizId)
                    if (cachedQuiz != null) {
                        Log.d(TAG, "Loaded quiz from local cache (offline)")
                        val quiz = Quiz(
                            id = cachedQuiz.id,
                            title = cachedQuiz.title,
                            questions = cachedQuiz.questions,
                            questionCount = cachedQuiz.questions.size
                        )
                        return@withContext NetworkResult.Success(quiz)
                    }
                } catch (dbException: Exception) {
                    Log.e(TAG, "Failed to load quiz from cache: ${dbException.message}")
                }
            }

            NetworkResult.Error(e.message ?: "Network error occurred")
        }
    }

    // Offline Management Methods
    suspend fun toggleQuizOffline(quizId: Int, quizTitle: String, makeOffline: Boolean) = withContext(Dispatchers.IO) {
        if (quizDao == null) {
            Log.w(TAG, "QuizDao is null, cannot toggle offline status")
            return@withContext
        }

        if (makeOffline) {
            // Fetch quiz and save to local database
            when (val result = getQuiz(quizId)) {
                is NetworkResult.Success -> {
                    val quiz = result.data
                    val entity = QuizEntity(
                        id = quizId,
                        title = quiz.title,
                        description = null,
                        questions = quiz.questions,
                        isOffline = true,
                        manuallyDownloaded = true // User manually downloaded this
                    )
                    quizDao.insertQuiz(entity)
                    Log.d(TAG, "Quiz ${quiz.title} manually downloaded for offline use")
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Failed to fetch quiz for offline: ${result.message}")
                }
                else -> {}
            }
        } else {
            // Remove from offline storage
            quizDao.deleteQuizById(quizId)
            Log.d(TAG, "Quiz $quizTitle removed from offline storage")
        }
    }

    suspend fun isQuizOffline(quizId: Int): Boolean {
        return quizDao?.isQuizOffline(quizId) ?: false
    }

    suspend fun isQuizManuallyDownloaded(quizId: Int): Boolean {
        return quizDao?.isQuizManuallyDownloaded(quizId) ?: false
    }

    suspend fun toggleChapterOffline(chapterId: Int, chapterTitle: String, makeOffline: Boolean) = withContext(Dispatchers.IO) {
        if (chapterDao == null) {
            Log.w(TAG, "ChapterDao is null, cannot toggle offline status")
            return@withContext
        }

        if (makeOffline) {
            // Fetch chapters and find the specific one to save
            when (val result = getChapters()) {
                is NetworkResult.Success -> {
                    val chapter = result.data.find { it.id == chapterId }
                    if (chapter != null) {
                        val entity = ChapterEntity(
                            id = chapter.id,
                            title = chapter.title,
                            description = chapter.description,
                            orderIndex = chapter.id,
                            topicCount = chapter.topics.size,
                            topics = chapter.topics,
                            isOffline = true,
                            manuallyDownloaded = true // User manually downloaded this
                        )
                        chapterDao.insertChapter(entity)
                        Log.d(TAG, "Chapter ${chapter.title} manually downloaded for offline use")

                        // Also fetch and cache all notes for all topics in this chapter
                        if (noteDao != null) {
                            chapter.topics.forEach { topic ->
                                try {
                                    when (val notesResult = getNotes(topic.id)) {
                                        is NetworkResult.Success -> {
                                            Log.d(TAG, "Cached ${notesResult.data.size} notes for topic: ${topic.title}")
                                        }
                                        is NetworkResult.Error -> {
                                            Log.w(TAG, "Failed to cache notes for topic ${topic.title}: ${notesResult.message}")
                                        }
                                        else -> {}
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error caching notes for topic ${topic.title}: ${e.message}")
                                }
                            }
                            Log.d(TAG, "Finished caching notes for chapter: ${chapter.title}")
                        }
                    }
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Failed to fetch chapter for offline: ${result.message}")
                }
                else -> {}
            }
        } else {
            // Remove from offline storage (chapter and its notes)
            chapterDao.deleteChapterById(chapterId)
            if (noteDao != null) {
                noteDao.deleteNotesByChapterId(chapterId)
            }
            Log.d(TAG, "Chapter $chapterTitle and its notes removed from offline storage")
        }
    }

    suspend fun isChapterOffline(chapterId: Int): Boolean {
        return chapterDao?.isChapterOffline(chapterId) ?: false
    }

    suspend fun isChapterManuallyDownloaded(chapterId: Int): Boolean {
        return chapterDao?.isChapterManuallyDownloaded(chapterId) ?: false
    }

    // Auto-cache quiz when user opens it
    suspend fun cacheQuizOnView(quiz: Quiz) = withContext(Dispatchers.IO) {
        if (quizDao == null) return@withContext

        try {
            val entity = QuizEntity(
                id = quiz.id,
                title = quiz.title,
                description = null,
                questions = quiz.questions,
                isOffline = true,
                lastUpdated = System.currentTimeMillis()
            )
            quizDao.insertQuiz(entity)
            Log.d(TAG, "Cached quiz on view: ${quiz.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache quiz: ${e.message}", e)
            // Don't throw exception, just log it - caching is not critical
        }
    }

    // Auto-cache chapter when user opens it
    suspend fun cacheChapterOnView(chapter: Chapter) = withContext(Dispatchers.IO) {
        if (chapterDao == null) return@withContext

        try {
            val entity = ChapterEntity(
                id = chapter.id,
                title = chapter.title,
                description = chapter.description,
                orderIndex = chapter.id,
                topicCount = chapter.topics.size,
                topics = chapter.topics,
                isOffline = true,
                lastUpdated = System.currentTimeMillis()
            )
            chapterDao.insertChapter(entity)
            Log.d(TAG, "Cached chapter on view: ${chapter.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache chapter: ${e.message}", e)
            // Don't throw exception, just log it - caching is not critical
        }
    }
}
