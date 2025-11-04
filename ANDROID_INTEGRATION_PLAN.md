# Android App Backend Integration Plan

## 🎯 Overview

Connect your Jetpack Android app to fetch content (chapters, topics, quizzes) from your Hostinger backend instead of hardcoded data.

---

## 📋 Technical Tasks Checklist

### Phase 1: Setup Dependencies & Configuration
- [ ] Add Retrofit dependencies
- [ ] Add networking dependencies (OkHttp, Logging Interceptor)
- [ ] Add Kotlin Serialization (already have it)
- [ ] Create API configuration class
- [ ] Add network security config

### Phase 2: Create Data Layer
- [ ] Create API response models
- [ ] Create API service interfaces (Retrofit)
- [ ] Create repository implementations
- [ ] Add error handling & network states

### Phase 3: Update Domain Layer
- [ ] Update existing models (if needed)
- [ ] Create use cases for fetching data

### Phase 4: Update ViewModels
- [ ] Update QuizViewModel to fetch quizzes from API
- [ ] Create/Update NotesViewModel to fetch chapters/topics
- [ ] Add loading states
- [ ] Handle errors

### Phase 5: Update UI
- [ ] Show loading indicators
- [ ] Handle empty states
- [ ] Display error messages
- [ ] Refresh functionality

### Phase 6: User Sync Integration
- [ ] Sync Firebase Auth user with backend
- [ ] Track user sessions
- [ ] Send user activity data

### Phase 7: Testing & Polish
- [ ] Test with real data
- [ ] Handle offline scenarios
- [ ] Add caching (optional)
- [ ] Error logging

---

## 🔧 Detailed Implementation Steps

### **Phase 1: Dependencies**

#### 1.1 Update `app/build.gradle.kts`

Add these dependencies:

```kotlin
dependencies {
    // Existing dependencies...

    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp for networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Gson for JSON parsing (alternative to kotlinx.serialization)
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines (you likely already have these)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
```

#### 1.2 Update `AndroidManifest.xml`

Internet permission already added ✅

---

### **Phase 2: Create API Configuration**

#### 2.1 Create `data/remote/ApiConfig.kt`

```kotlin
package com.dolphin.jetpack.data.remote

object ApiConfig {
    const val BASE_URL = "https://jetpack.dolphincoder.com/api/"
    const val API_KEY = "a1b2c3d4e5f6g7h8i9j0"
}
```

#### 2.2 Create `data/remote/ApiService.kt`

```kotlin
package com.dolphin.jetpack.data.remote

import retrofit2.Response
import retrofit2.http.*

// Response wrappers
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)

// API Models (matching your backend)
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
    val order_index: Int
)

data class QuizResponse(
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

data class UserRegisterRequest(
    val firebase_uid: String,
    val email: String?,
    val display_name: String?,
    val photo_url: String?
)

data class SessionStartRequest(
    val firebase_uid: String,
    val device_id: String?,
    val device_model: String?,
    val os_version: String?,
    val app_version: String?
)

// API Interface
interface ApiService {
    // Content APIs
    @GET("get_chapters.php")
    suspend fun getChapters(): Response<ApiResponse<List<ChapterResponse>>>

    @GET("get_topic.php")
    suspend fun getTopic(@Query("id") topicId: Int): Response<ApiResponse<TopicResponse>>

    @GET("get_quizzes.php")
    suspend fun getQuizzes(): Response<ApiResponse<List<QuizResponse>>>

    @GET("get_quiz.php")
    suspend fun getQuiz(@Query("id") quizId: Int): Response<ApiResponse<QuizDetailResponse>>

    // User APIs
    @POST("user_register.php")
    suspend fun registerUser(@Body request: UserRegisterRequest): Response<ApiResponse<Any>>

    @POST("session_start.php")
    suspend fun startSession(@Body request: SessionStartRequest): Response<ApiResponse<Any>>

    @POST("session_end.php")
    suspend fun endSession(@Body sessionId: Map<String, Int>): Response<ApiResponse<Any>>
}
```

#### 2.3 Create `data/remote/RetrofitClient.kt`

```kotlin
package com.dolphin.jetpack.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val apiKeyInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("X-API-Key", ApiConfig.API_KEY)
            .build()
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
```

---

### **Phase 3: Create Repository**

#### 3.1 Create `data/repository/ContentRepository.kt`

```kotlin
package com.dolphin.jetpack.data.repository

import com.dolphin.jetpack.data.remote.RetrofitClient
import com.dolphin.jetpack.domain.model.Quiz
import com.dolphin.jetpack.domain.model.Question
import com.dolphin.jetpack.domain.model.Chapter
import com.dolphin.jetpack.domain.model.Topic

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

class ContentRepository {
    private val api = RetrofitClient.apiService

    suspend fun getChapters(): NetworkResult<List<Chapter>> {
        return try {
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
                                content = topicResponse.content ?: ""
                            )
                        }
                    )
                } ?: emptyList()
                NetworkResult.Success(chapters)
            } else {
                NetworkResult.Error("Failed to fetch chapters")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getTopic(topicId: Int): NetworkResult<Topic> {
        return try {
            val response = api.getTopic(topicId)
            if (response.isSuccessful && response.body()?.success == true) {
                val topicResponse = response.body()?.data
                if (topicResponse != null) {
                    val topic = Topic(
                        id = topicResponse.id,
                        chapterId = topicResponse.chapter_id,
                        title = topicResponse.title,
                        description = topicResponse.description ?: "",
                        content = topicResponse.content ?: ""
                    )
                    NetworkResult.Success(topic)
                } else {
                    NetworkResult.Error("Topic not found")
                }
            } else {
                NetworkResult.Error("Failed to fetch topic")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getQuizzes(): NetworkResult<List<Quiz>> {
        return try {
            val response = api.getQuizzes()
            if (response.isSuccessful && response.body()?.success == true) {
                val quizzes = response.body()?.data?.map { quizResponse ->
                    // First, get the full quiz with questions
                    val quizDetailResult = getQuiz(quizResponse.id)
                    if (quizDetailResult is NetworkResult.Success) {
                        quizDetailResult.data
                    } else {
                        // Return empty quiz if details fetch fails
                        Quiz(
                            title = quizResponse.title,
                            questions = emptyList()
                        )
                    }
                } ?: emptyList()
                NetworkResult.Success(quizzes)
            } else {
                NetworkResult.Error("Failed to fetch quizzes")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getQuiz(quizId: Int): NetworkResult<Quiz> {
        return try {
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
                                correctAnswerIndex = questionResponse.correctAnswerIndex,
                                explanation = questionResponse.explanation ?: ""
                            )
                        }
                    )
                    NetworkResult.Success(quiz)
                } else {
                    NetworkResult.Error("Quiz not found")
                }
            } else {
                NetworkResult.Error("Failed to fetch quiz")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }
}
```

---

### **Phase 4: Update ViewModels**

#### 4.1 Update `QuizViewModel` to fetch from server

Add repository and loading states:

```kotlin
// In QuizViewModel.kt
class QuizViewModel : ViewModel() {
    private val repository = ContentRepository()

    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadQuizzes()
    }

    fun loadQuizzes() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getQuizzes()) {
                is NetworkResult.Success -> {
                    _quizzes.value = result.data
                    _error.value = null
                }
                is NetworkResult.Error -> {
                    _error.value = result.message
                }
                is NetworkResult.Loading -> {
                    // Already handled
                }
            }
            _isLoading.value = false
        }
    }

    // Rest of your existing code...
}
```

---

### **Phase 5: Update UI**

#### 5.1 Update `QuizSelectionScreen` to show loading/error states

```kotlin
@Composable
fun QuizSelectionScreen(navController: NavController, viewModel: QuizViewModel) {
    val quizzes by viewModel.quizzes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column {
        // Show loading indicator
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Show error message
        error?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = { viewModel.loadQuizzes() }) {
                Text("Retry")
            }
        }

        // Show quizzes list
        if (!isLoading && error == null) {
            LazyColumn {
                items(quizzes) { quiz ->
                    // Your existing quiz item UI
                }
            }
        }
    }
}
```

---

### **Phase 6: User Sync Integration**

#### 6.1 Create `UserSyncManager.kt`

```kotlin
package com.dolphin.jetpack.data.remote

import android.content.Context
import android.os.Build
import com.dolphin.jetpack.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserSyncManager(private val context: Context) {
    private val api = RetrofitClient.apiService
    private val auth = FirebaseAuth.getInstance()

    fun syncUser() {
        val currentUser = auth.currentUser ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = UserRegisterRequest(
                    firebase_uid = currentUser.uid,
                    email = currentUser.email,
                    display_name = currentUser.displayName,
                    photo_url = currentUser.photoUrl?.toString()
                )

                val response = api.registerUser(request)
                // Handle response if needed
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun startSession() {
        val currentUser = auth.currentUser ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = SessionStartRequest(
                    firebase_uid = currentUser.uid,
                    device_id = getDeviceId(),
                    device_model = Build.MODEL,
                    os_version = "Android ${Build.VERSION.RELEASE}",
                    app_version = BuildConfig.VERSION_NAME
                )

                val response = api.startSession(request)
                // Store session ID if needed
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    private fun getDeviceId(): String {
        // Return a unique device identifier
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
    }
}
```

#### 6.2 Call in MainActivity

```kotlin
// In MainActivity.onCreate()
class MainActivity : ComponentActivity() {
    private lateinit var userSyncManager: UserSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userSyncManager = UserSyncManager(this)

        // Sync user when Firebase auth is ready
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                userSyncManager.syncUser()
                userSyncManager.startSession()
            }
        }

        // Rest of your code...
    }
}
```

---

## 📝 Summary of Files to Create/Modify

### New Files to Create:
1. ✅ `data/remote/ApiConfig.kt`
2. ✅ `data/remote/ApiService.kt`
3. ✅ `data/remote/RetrofitClient.kt`
4. ✅ `data/repository/ContentRepository.kt`
5. ✅ `data/remote/UserSyncManager.kt`

### Files to Modify:
1. ✅ `app/build.gradle.kts` - Add dependencies
2. ✅ `presentation/viewmodel/QuizViewModel.kt` - Fetch from API
3. ✅ `presentation/screens/QuizSelectionScreen.kt` - Add loading states
4. ✅ `MainActivity.kt` - Add user sync
5. ✅ Create similar for Notes (Chapters/Topics)

---

## 🧪 Testing Steps

1. Add test content in admin panel (1 chapter, 1 topic, 1 quiz)
2. Run app
3. Verify quizzes load from server
4. Check Logcat for API calls
5. Test error handling (turn off internet)

---

## ⚡ Quick Start Order

1. **First:** Add dependencies & sync Gradle
2. **Second:** Create API config files
3. **Third:** Create repository
4. **Fourth:** Update one ViewModel (QuizViewModel)
5. **Fifth:** Test with real data
6. **Then:** Repeat for Notes/Chapters

---

This is a complete technical roadmap! Want me to start creating these files one by one?
