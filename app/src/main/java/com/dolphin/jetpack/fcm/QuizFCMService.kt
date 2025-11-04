package com.dolphin.jetpack.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dolphin.jetpack.MainActivity
import com.dolphin.jetpack.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class QuizFCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "QuizFCMService"
        private const val CHANNEL_ID = "quiz_notifications"
        private const val CHANNEL_NAME = "Quiz Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for quiz updates and reminders"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        // Send token to your server if needed
        // For now, just log it
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        // Check if message contains a notification payload
        message.notification?.let {
            Log.d(TAG, "Notification title: ${it.title}")
            Log.d(TAG, "Notification body: ${it.body}")

            showNotification(
                title = it.title ?: "Quiz App",
                body = it.body ?: "You have a new notification",
                data = message.data
            )
        }

        // Check if message contains a data payload
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            handleDataPayload(message.data)
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open app when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add any extra data from the notification
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        // Show notification
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun handleDataPayload(data: Map<String, String>) {
        // Handle custom data payload
        // Example: trigger specific actions based on data
        when (data["type"]) {
            "new_quiz" -> {
                Log.d(TAG, "New quiz available: ${data["quiz_title"]}")
                // You can show a custom notification or update local data
            }
            "quiz_reminder" -> {
                Log.d(TAG, "Quiz reminder for: ${data["quiz_title"]}")
            }
            else -> {
                Log.d(TAG, "Unknown notification type")
            }
        }
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Send token to your backend server
        // This is where you would make an API call to store the FCM token
        // associated with the current user
        Log.d(TAG, "Token should be sent to server: $token")
    }
}
