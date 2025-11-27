package com.dolphin.jetpack.data.remote

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserSyncManager(private val context: Context) {
    private val api = RetrofitClient.apiService
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "UserSyncManager"

    private var currentSessionId: Int? = null

    /**
     * Sync current Firebase user with backend
     */
    fun syncUser() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "════════════════════════════════════════")
            Log.e(TAG, "❌ Cannot sync user - No Firebase user")
            Log.e(TAG, "════════════════════════════════════════")
            return
        }

        CoroutineScope(Dispatchers.IO + kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "❌ Uncaught exception in user sync coroutine: ${throwable.message}", throwable)
        }).launch {
            try {
                Log.d(TAG, "════════════════════════════════════════")
                Log.d(TAG, "🌐 USER REGISTRATION API CALL")
                Log.d(TAG, "════════════════════════════════════════")
                Log.d(TAG, "📋 User Info:")
                Log.d(TAG, "   - Firebase UID: ${currentUser.uid}")
                Log.d(TAG, "   - Email: ${currentUser.email}")
                Log.d(TAG, "   - Display Name: ${currentUser.displayName}")
                Log.d(TAG, "   - Photo URL: ${currentUser.photoUrl}")

                val request = UserRegisterRequest(
                    firebase_uid = currentUser.uid,
                    email = currentUser.email,
                    display_name = currentUser.displayName,
                    photo_url = currentUser.photoUrl?.toString()
                )

                Log.d(TAG, "────────────────────────────────────────")
                Log.d(TAG, "📡 Sending POST to:")
                Log.d(TAG, "   URL: https://jetpack.dolphincoder.com/api/user_register.php")

                val startTime = System.currentTimeMillis()
                val response = api.registerUser(request)
                val duration = System.currentTimeMillis() - startTime

                Log.d(TAG, "────────────────────────────────────────")
                Log.d(TAG, "📥 Response (${duration}ms):")
                Log.d(TAG, "   - HTTP Status: ${response.code()}")
                Log.d(TAG, "   - Is Successful: ${response.isSuccessful}")
                Log.d(TAG, "   - Success Flag: ${response.body()?.success}")
                Log.d(TAG, "   - Message: ${response.body()?.message}")

                if (response.isSuccessful && response.body()?.success == true) {
                    val userData = response.body()?.data
                    Log.d(TAG, "════════════════════════════════════════")
                    Log.d(TAG, "✅ SUCCESS: User registered/updated!")
                    Log.d(TAG, "   - User ID: ${userData?.user_id}")
                    Log.d(TAG, "   - Firebase UID: ${userData?.firebase_uid}")
                    Log.d(TAG, "════════════════════════════════════════")
                } else {
                    val errorBody = try {
                        response.errorBody()?.string() ?: "No error body"
                    } catch (e: Exception) {
                        "Error reading error body"
                    }
                    Log.e(TAG, "════════════════════════════════════════")
                    Log.e(TAG, "❌ FAILED: User registration failed!")
                    Log.e(TAG, "   - HTTP Status: ${response.code()}")
                    Log.e(TAG, "   - Message: ${response.body()?.message}")
                    Log.e(TAG, "   - Error Body: $errorBody")
                    Log.e(TAG, "════════════════════════════════════════")
                }
            } catch (e: Exception) {
                Log.e(TAG, "════════════════════════════════════════")
                Log.e(TAG, "💥 EXCEPTION in syncUser!")
                Log.e(TAG, "   - Exception Type: ${e.javaClass.simpleName}")
                Log.e(TAG, "   - Message: ${e.message}")
                Log.e(TAG, "   - Cause: ${e.cause?.message ?: "None"}")

                when (e) {
                    is java.net.UnknownHostException -> {
                        Log.e(TAG, "   - Type: NETWORK ERROR - Cannot resolve hostname")
                    }
                    is java.net.SocketTimeoutException -> {
                        Log.e(TAG, "   - Type: TIMEOUT ERROR")
                    }
                    is java.net.ConnectException -> {
                        Log.e(TAG, "   - Type: CONNECTION ERROR")
                    }
                }

                Log.e(TAG, "   - Stack Trace:", e)
                Log.e(TAG, "════════════════════════════════════════")
            }
        }
    }

    /**
     * Start a new session for current user
     */
    fun startSession() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "No Firebase user for session")
            return
        }

        CoroutineScope(Dispatchers.IO + kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "❌ Uncaught exception in user sync coroutine: ${throwable.message}", throwable)
        }).launch {
            try {
                Log.d(TAG, "Starting session for user: ${currentUser.uid}")
                val request = SessionStartRequest(
                    firebase_uid = currentUser.uid,
                    device_id = getDeviceId(),
                    device_model = Build.MODEL,
                    os_version = "Android ${Build.VERSION.RELEASE}",
                    app_version = "2.0"  // Using hard-coded version instead of BuildConfig
                )

                val response = api.startSession(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val sessionData = response.body()?.data
                    currentSessionId = sessionData?.session_id
                    Log.d(TAG, "Session started successfully: $currentSessionId")
                } else {
                    Log.e(TAG, "Failed to start session: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting session: ${e.message}", e)
            }
        }
    }

    /**
     * End current session
     */
    fun endSession() {
        val sessionId = currentSessionId
        if (sessionId == null) {
            Log.w(TAG, "No active session to end")
            return
        }

        CoroutineScope(Dispatchers.IO + kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "❌ Uncaught exception in user sync coroutine: ${throwable.message}", throwable)
        }).launch {
            try {
                Log.d(TAG, "Ending session: $sessionId")
                val request = SessionEndRequest(session_id = sessionId)

                val response = api.endSession(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "Session ended successfully")
                    currentSessionId = null
                } else {
                    Log.e(TAG, "Failed to end session: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error ending session: ${e.message}", e)
            }
        }
    }

    /**
     * Get unique device ID
     */
    private fun getDeviceId(): String {
        return try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device ID: ${e.message}")
            "unknown"
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserSyncManager? = null

        fun getInstance(context: Context): UserSyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserSyncManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
