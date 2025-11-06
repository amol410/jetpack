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

class InterstitialAdManager(
    private val context: Context,
    private val adUnitId: String
) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    companion object {
        private const val TAG = "InterstitialAdManager"

        // Initialize Mobile Ads SDK
        fun initialize(context: Context, onComplete: () -> Unit = {}) {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob SDK initialized: ${initializationStatus.adapterStatusMap}")
                onComplete()
            }
        }
    }

    /**
     * Load an interstitial ad
     */
    fun loadAd(onAdLoaded: (() -> Unit)? = null, onAdFailedToLoad: ((LoadAdError) -> Unit)? = null) {
        if (isLoading) {
            Log.d(TAG, "Ad is already loading")
            return
        }

        if (interstitialAd != null) {
            Log.d(TAG, "Ad is already loaded")
            onAdLoaded?.invoke()
            return
        }

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Ad failed to load: ${adError.message}")
                    interstitialAd = null
                    isLoading = false
                    onAdFailedToLoad?.invoke(adError)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Ad loaded successfully")
                    interstitialAd = ad
                    isLoading = false
                    onAdLoaded?.invoke()
                }
            }
        )
    }

    /**
     * Show the interstitial ad if available
     * @param activity The activity context
     * @param onAdDismissed Callback invoked when ad is dismissed or failed to show
     */
    fun showAd(
        activity: Activity,
        onAdDismissed: () -> Unit
    ) {
        val ad = interstitialAd

        if (ad == null) {
            Log.w(TAG, "Interstitial ad not loaded yet")
            onAdDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed")
                interstitialAd = null
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show: ${adError.message}")
                interstitialAd = null
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content")
            }
        }

        ad.show(activity)
    }

    /**
     * Check if ad is ready to show
     */
    fun isAdReady(): Boolean {
        return interstitialAd != null
    }

    /**
     * Clean up the ad
     */
    fun destroy() {
        interstitialAd = null
        isLoading = false
    }
}
