package com.dolphin.jetpack.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsHelper {
    private var analytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context)
    }

    // Authentication Events
    fun logLoginEvent(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        analytics?.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    fun logSignUpEvent(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        analytics?.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
    }

    // Quiz Events
    fun logQuizStarted(quizTitle: String, withTimer: Boolean) {
        val bundle = Bundle().apply {
            putString("quiz_title", quizTitle)
            putString("with_timer", withTimer.toString())
        }
        analytics?.logEvent("quiz_started", bundle)
    }

    fun logQuizCompleted(quizTitle: String, score: Int, totalQuestions: Int, timeTaken: Long) {
        val bundle = Bundle().apply {
            putString("quiz_title", quizTitle)
            putLong("score", score.toLong())
            putLong("total_questions", totalQuestions.toLong())
            putLong("time_taken_seconds", timeTaken)
            putLong("percentage", ((score.toDouble() / totalQuestions) * 100).toLong())
        }
        analytics?.logEvent("quiz_completed", bundle)
    }

    fun logQuizAbandoned(quizTitle: String, questionsAnswered: Int, totalQuestions: Int) {
        val bundle = Bundle().apply {
            putString("quiz_title", quizTitle)
            putLong("questions_answered", questionsAnswered.toLong())
            putLong("total_questions", totalQuestions.toLong())
        }
        analytics?.logEvent("quiz_abandoned", bundle)
    }

    // Screen View Events
    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    // History Events
    fun logHistoryViewed() {
        analytics?.logEvent("history_viewed", null)
    }

    fun logHistoryDetailViewed(attemptId: Long) {
        val bundle = Bundle().apply {
            putLong("attempt_id", attemptId)
        }
        analytics?.logEvent("history_detail_viewed", bundle)
    }

    fun logHistoryExported(format: String) {
        val bundle = Bundle().apply {
            putString("format", format)
        }
        analytics?.logEvent("history_exported", bundle)
    }

    // Statistics Events
    fun logStatisticsViewed() {
        analytics?.logEvent("statistics_viewed", null)
    }

    // FCM Events
    fun logNotificationReceived(type: String) {
        val bundle = Bundle().apply {
            putString("type", type)
        }
        analytics?.logEvent("notification_received", bundle)
    }

    fun logNotificationOpened(type: String) {
        val bundle = Bundle().apply {
            putString("type", type)
        }
        analytics?.logEvent("notification_opened", bundle)
    }

    // Crashlytics Test
    fun logCrashlyticsTest() {
        analytics?.logEvent("crashlytics_test_triggered", null)
    }

    // User Properties
    fun setUserProperties(userId: String, email: String?) {
        analytics?.setUserId(userId)
        email?.let {
            analytics?.setUserProperty("user_email", it)
        }
    }
}
