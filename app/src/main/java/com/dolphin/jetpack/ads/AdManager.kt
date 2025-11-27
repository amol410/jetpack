package com.dolphin.jetpack.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Manages Google AdMob interstitial ads for the app
 * Shows ads after user views a specified number of components
 */
class AdManager(private val context: Context) {

    companion object {
        private const val TAG = "AdManager"

        // Test Ad Unit ID - REPLACE with your real ad unit ID for production
        private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

        // Production Ad Unit ID - From your AdMob console
        private const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6038618318911032/2211241697"

        // Use test ads during development, production ads for release
        private const val USE_TEST_ADS = true // Set to false for production

        private val INTERSTITIAL_AD_UNIT_ID = if (USE_TEST_ADS) {
            TEST_INTERSTITIAL_AD_UNIT_ID
        } else {
            PROD_INTERSTITIAL_AD_UNIT_ID
        }

        // Show ad after viewing this many components
        private const val COMPONENT_VIEW_THRESHOLD = 3
    }

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingAd = false
    private var componentViewCount = 0

    init {
        // Initialize Mobile Ads SDK
        MobileAds.initialize(context) {
            Log.d(TAG, "AdMob SDK initialized")
            // Preload first ad
            loadInterstitialAd()
        }
    }

    /**
     * Load an interstitial ad in the background
     */
    private fun loadInterstitialAd() {
        if (isLoadingAd || interstitialAd != null) {
            Log.d(TAG, "Ad already loaded or loading")
            return
        }

        isLoadingAd = true
        val adRequest = AdRequest.Builder().build()

        Log.d(TAG, "Loading interstitial ad...")
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isLoadingAd = false
                    setupAdCallbacks(ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Failed to load interstitial ad: ${error.message}")
                    interstitialAd = null
                    isLoadingAd = false

                    // Retry loading after 30 seconds
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        loadInterstitialAd()
                    }, 30_000)
                }
            }
        )
    }

    /**
     * Setup callbacks for when ad is shown/dismissed
     */
    private fun setupAdCallbacks(ad: InterstitialAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad was dismissed")
                interstitialAd = null

                // Load next ad immediately for infinite loop
                loadInterstitialAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Failed to show interstitial ad: ${error.message}")
                interstitialAd = null

                // Try loading again
                loadInterstitialAd()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad shown successfully")
            }
        }
    }

    /**
     * Call this method whenever user views a component
     * Automatically shows ad after threshold is reached
     */
    fun onComponentViewed() {
        componentViewCount++
        Log.d(TAG, "Component viewed. Count: $componentViewCount / $COMPONENT_VIEW_THRESHOLD")

        if (componentViewCount >= COMPONENT_VIEW_THRESHOLD) {
            showInterstitialAdIfReady()
        }
    }

    /**
     * Show interstitial ad immediately if ready (no frequency cap - infinite loop)
     */
    private fun showInterstitialAdIfReady() {
        // Check if ad is ready
        if (interstitialAd != null) {
            val activity = context as? Activity
            if (activity != null) {
                Log.d(TAG, "Showing interstitial ad (infinite loop mode)")
                interstitialAd?.show(activity)
                componentViewCount = 0 // Reset counter after showing ad
            } else {
                Log.e(TAG, "Context is not an Activity, cannot show ad")
                componentViewCount = 0
            }
        } else {
            Log.d(TAG, "Interstitial ad not ready yet, loading...")
            componentViewCount = 0 // Reset counter
            loadInterstitialAd()
        }
    }

    /**
     * Manually show ad (for testing or special cases)
     */
    fun showAdNow() {
        val activity = context as? Activity
        if (activity != null && interstitialAd != null) {
            interstitialAd?.show(activity)
            componentViewCount = 0
        } else {
            Log.w(TAG, "Cannot show ad: Activity null or ad not loaded")
            loadInterstitialAd()
        }
    }

    /**
     * Reset component view counter
     */
    fun resetCounter() {
        componentViewCount = 0
        Log.d(TAG, "Component view counter reset")
    }

    /**
     * Get current component view count (useful for debugging)
     */
    fun getComponentViewCount(): Int = componentViewCount

    /**
     * Check if ad is ready to show
     */
    fun isAdReady(): Boolean = interstitialAd != null
}
