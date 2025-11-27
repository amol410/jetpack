# AdMob Integration Guide

## Overview
This guide explains the AdMob interstitial ad integration in your Jetpack app, specifically for the Material3 Showcase feature.

## Implementation Details

### 1. Ad Configuration
- **App ID**: `ca-app-pub-6038618318911032~5311453883` (configured in AndroidManifest.xml)
- **Ad Unit ID**: `ca-app-pub-6038618318911032/2211241697` (interstitial ads)
- **Test Mode**: Currently enabled (`USE_TEST_ADS = true`)

### 2. How It Works

#### Component View Tracking
- Every time a user views a Material3 component category, the view count increments
- After viewing **3 components**, an interstitial ad is automatically shown
- The counter resets after showing the ad

#### Infinite Loop Mode
- **NO frequency cap** - ads show immediately every 3 components
- User sees ad after EVERY 3 component views throughout the session
- Maximizes ad impressions and revenue

### 3. Files Modified/Created

#### New File: `AdManager.kt`
Location: `app/src/main/java/com/dolphin/jetpack/ads/AdManager.kt`

Key features:
- Initializes AdMob SDK
- Preloads interstitial ads in background
- Tracks component views
- Shows ads automatically after threshold
- Implements frequency capping (5 minutes minimum)

#### Modified: `Material3ShowcaseScreen.kt`
- Integrated AdManager initialization
- Tracks each component category view
- Automatically triggers ads after 3 component views

### 4. User Flow

```
User opens Material3 Showcase
  ↓
User views Component 1 → Count: 1/3
  ↓
User views Component 2 → Count: 2/3
  ↓
User views Component 3 → Count: 3/3
  ↓
✓ INTERSTITIAL AD SHOWS IMMEDIATELY
  ↓
Counter resets to 0
  ↓
User views 3 MORE components → Count: 3/3
  ↓
✓ INTERSTITIAL AD SHOWS IMMEDIATELY
  ↓
Counter resets to 0
  ↓
🔁 INFINITE LOOP - Repeats forever in same session
```

### 5. Testing

#### Test Mode (Current Setup)
The app is currently using **test ads** (safe for development):
- Test Ad Unit ID: `ca-app-pub-3940256099942544/1033173712`
- Shows test ads with "Test Ad" watermark
- No risk of account suspension
- **DO NOT CLICK PRODUCTION ADS DURING TESTING**

#### Switching to Production Ads
When ready to go live:

1. Open `AdManager.kt` (line 30)
2. Change: `private const val USE_TEST_ADS = true`
3. To: `private const val USE_TEST_ADS = false`
4. Rebuild the app

### 6. Configuration Options

#### Change Component Threshold
In `AdManager.kt` (line 37):
```kotlin
private const val COMPONENT_VIEW_THRESHOLD = 3 // Change this number
```

#### Frequency Cap (Currently DISABLED)
The app uses **infinite loop mode** - no time restrictions between ads.
To add a frequency cap (e.g., minimum 5 minutes between ads):

1. Add this line after `componentViewCount` in `AdManager.kt`:
```kotlin
private var lastAdShownTime = 0L
```

2. In `setupAdCallbacks`, add after `interstitialAd = null`:
```kotlin
lastAdShownTime = System.currentTimeMillis()
```

3. In `showInterstitialAdIfReady`, add time check before showing ad:
```kotlin
val currentTime = System.currentTimeMillis()
val timeSinceLastAd = currentTime - lastAdShownTime
val MIN_AD_INTERVAL_MS = 5 * 60 * 1000L

if (timeSinceLastAd < MIN_AD_INTERVAL_MS) {
    componentViewCount = 0
    return
}
```

### 7. Monitoring

#### Debug Logs
The AdManager logs all activity with tag `"AdManager"`:
- Component view counts
- Ad loading status
- Ad show attempts
- Frequency cap checks

#### Example Log Output:
```
D/AdManager: AdMob SDK initialized
D/AdManager: Loading interstitial ad...
D/AdManager: Interstitial ad loaded successfully
D/AdManager: Component viewed. Count: 1 / 3
D/AdManager: Component viewed. Count: 2 / 3
D/AdManager: Component viewed. Count: 3 / 3
D/AdManager: Showing interstitial ad (infinite loop mode)
D/AdManager: Interstitial ad shown successfully
D/AdManager: Interstitial ad was dismissed
D/AdManager: Loading interstitial ad...
D/AdManager: Component viewed. Count: 1 / 3
D/AdManager: Component viewed. Count: 2 / 3
D/AdManager: Component viewed. Count: 3 / 3
D/AdManager: Showing interstitial ad (infinite loop mode)
... (continues infinitely)
```

### 8. Best Practices

#### ✓ DO:
- Keep test mode enabled during development
- Test on multiple devices
- Monitor analytics in AdMob console
- Watch for user drop-off (too many ads = users quit)
- Use appropriate ad units for different screens
- Consider adding frequency cap if users complain

#### ✗ DON'T:
- Click your own production ads (account ban risk)
- Ignore user retention metrics
- Force ads on critical user flows
- Ignore AdMob policy guidelines
- Show ads during sensitive operations

### 9. AdMob Dashboard

Monitor your ads at: https://apps.admob.com/

Key metrics to watch:
- **Impressions**: How many times ads are shown
- **Click-through rate (CTR)**: Percentage of ad clicks
- **eCPM**: Effective cost per thousand impressions
- **Fill rate**: Percentage of successful ad loads

### 10. Troubleshooting

#### Ads Not Showing?
1. Check internet connection
2. Verify ad unit ID is correct
3. Check logcat for error messages
4. Ensure `USE_TEST_ADS = true` during testing
5. Wait for ad to preload (check logs)

#### "Ad failed to load" errors?
- Normal during testing
- May take 10-30 seconds to load first ad
- AdManager auto-retries after 30 seconds
- Check AdMob account status

#### App crashes?
- Clean and rebuild: `./gradlew.bat clean assembleDebug`
- Check for dependency conflicts
- Verify AdMob SDK version compatibility

### 11. Future Enhancements

Consider adding:
- **Banner ads** at the bottom of component screens
- **Native ads** blended with component list
- **Rewarded ads** for unlocking premium themes
- **Analytics integration** for detailed tracking
- **A/B testing** for optimal ad placement

### 12. Revenue Optimization Tips

1. **Ad Placement**: Show ads at natural break points
2. **Frequency**: Currently INFINITE - monitor user retention closely!
3. **Fill Rate**: Use AdMob mediation for higher fill rates
4. **User Segments**: Consider showing fewer ads to premium users
5. **Timing**: Ads show immediately every 3 components (aggressive monetization)

**⚠️ WARNING**: Infinite ad loop maximizes revenue but may hurt user retention. Monitor these metrics:
- Session duration (are users leaving quickly?)
- User retention rate (do they come back?)
- App store reviews (complaints about ads?)

Consider adding a frequency cap if retention drops.

---

## Support

For AdMob support:
- **AdMob Help Center**: https://support.google.com/admob
- **Community Forum**: https://groups.google.com/g/google-admob-ads-sdk

For code issues:
- Check `AdManager.kt` implementation
- Review logcat output with tag `AdManager`
- Verify integration follows the pattern in this guide
