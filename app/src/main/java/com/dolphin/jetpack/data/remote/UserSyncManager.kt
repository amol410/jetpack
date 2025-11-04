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
            Log.w(TAG, "No Firebase user to sync")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Syncing user: ${currentUser.uid}")
                val request = UserRegisterRequest(
                    firebase_uid = currentUser.uid,
                    email = currentUser.email,
                    display_name = currentUser.displayName,
                    photo_url = currentUser.photoUrl?.toString()
                )

                val response = api.registerUser(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val userData = response.body()?.data
                    Log.d(TAG, "User synced successfully: ${userData?.user_id}")
                } else {
                    Log.e(TAG, "Failed to sync user: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing user: ${e.message}", e)
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

        CoroutineScope(Dispatchers.IO).launch {
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

        CoroutineScope(Dispatchers.IO).launch {
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
