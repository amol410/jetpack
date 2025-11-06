# Changelog

All notable changes to the Jetpack Compose Quiz App will be documented in this file.

## [5.0.0] - 2025-01-07

### Major Features Added

#### AdMob Integration
- **Interstitial Ads**: Integrated Google AdMob for monetization
  - Ads display after quiz completion, before showing results
  - Automatic ad preloading for smooth user experience
  - Graceful fallback when ads are not available
  - Test ads configured for development
  - Production ad units ready for deployment
- **AdManager Class**: Created `InterstitialAdManager` for centralized ad management
- **Dependencies**: Added Google Play Services Ads SDK v23.6.0

#### Dark Mode Support
- **System-wide Dark Theme**: Complete dark mode implementation
  - Automatic theme switching based on user preference
  - Persistent theme selection using DataStore
  - Beautiful Material 3 dark color scheme
  - Smooth theme transitions without app restart
- **Theme Toggle**: Added dark mode switch in Settings screen
  - Easy to access and use
  - Immediate visual feedback
  - Preference saved across app sessions
- **UI Fixes**: Fixed all hardcoded colors for proper dark mode support
  - Updated NotesScreen topic cards
  - Fixed HistoryDetailScreen colors
  - Corrected QuizResultScreen text colors
  - All screens now properly adapt to light/dark themes

### Technical Improvements

#### AdMob Implementation Details
- Added AdMob App ID configuration in AndroidManifest
- Created `InterstitialAdManager.kt` with lifecycle-aware ad loading
- Integrated ad loading in `QuizViewModel`
- Added ad display logic in quiz completion flow
- Fixed manifest merger conflicts for AD_SERVICES_CONFIG
- Configured proper ad callbacks and error handling

#### Theme System
- Implemented `ThemePreferences.kt` using DataStore for preferences
- Updated `JetpackTheme` to support dynamic theme switching
- Modified `MainActivity` to observe and apply theme changes
- Added DataStore Preferences dependency v1.1.1

#### Color System Updates
- Replaced all hardcoded `Color.White`, `Color.Gray`, `Color.Black` with theme colors
- Replaced hardcoded hex colors with Material Theme color scheme
- Updated score colors to use MaterialTheme color scheme
- Fixed text contrast issues in dark mode
- Improved accessibility with proper color ratios

### Files Modified
- `app/build.gradle.kts` - Added AdMob and DataStore dependencies, updated version
- `app/src/main/AndroidManifest.xml` - Added AdMob configuration
- `MainActivity.kt` - Integrated theme preferences and AdMob initialization
- `QuizViewModel.kt` - Added AdMob ad management
- `SettingsScreen.kt` - Added dark mode toggle
- `NotesScreen.kt` - Fixed hardcoded colors
- `HistoryDetailScreen.kt` - Fixed all hardcoded colors
- `QuizResultScreen.kt` - Fixed hardcoded colors

### New Files Added
- `app/src/main/java/com/dolphin/jetpack/ads/InterstitialAdManager.kt` - AdMob manager
- `app/src/main/java/com/dolphin/jetpack/util/ThemePreferences.kt` - Theme preferences manager
- `CHANGELOG.md` - This file

### Dependencies Added
```gradle
// AdMob
implementation("com.google.android.gms:play-services-ads:23.6.0")

// DataStore for preferences
implementation("androidx.datastore:datastore-preferences:1.1.1")
```

### Configuration Required
1. **AdMob Setup**:
   - Replace `YOUR_ADMOB_APP_ID_HERE` in AndroidManifest.xml with actual App ID
   - Replace `YOUR_AD_UNIT_ID_HERE` in QuizViewModel.kt with actual Ad Unit ID
   - Current configuration uses Google's test IDs for development

2. **Theme**:
   - No additional configuration required
   - Works out of the box

### Known Issues
- AdMob ads require tax form completion in AdSense account for production
- First ad load may take a few seconds on slow connections

### Testing
- Build successful on all configurations
- Dark mode tested on multiple screens
- Ad loading and display tested with Google test ads
- Theme persistence verified across app restarts

---

## [4.0] - Previous Release
- Swipe navigation between tabs
- Floating back button with gestures
- UI/UX improvements

## [3.0.0] - Previous Release
- Initial stable release with core features
- Quiz functionality
- History tracking
- Statistics

---

## Version Format
This project follows [Semantic Versioning](https://semver.org/):
- **MAJOR** version for incompatible API changes
- **MINOR** version for new functionality in a backward compatible manner
- **PATCH** version for backward compatible bug fixes

Current Version: **5.0.0**
- versionCode: 5
- versionName: "5.0.0"
