# Release Build Guide - Preventing NoSuchMethodError Crashes

## Critical Information

**Never use incremental builds for production releases.** The NoSuchMethodError crash in v6.2.0 was caused by stale build artifacts where the `cacheNotesOnView()` method was missing at runtime despite being present in source code.

## Crash Reference

- **Version Affected**: v6.2.0
- **Error**: `NoSuchMethodError: No virtual method cacheNotesOnView`
- **Location**: `ContentRepository.kt:561` called from `NotesViewModel.kt:181`
- **Root Cause**: Stale/incomplete build artifacts in multidex compilation

## Safe Release Build Process

### Option 1: Using Custom Gradle Task (Recommended)

```bash
# For Google Play Store (AAB)
./gradlew cleanReleaseBundle

# For Direct APK Distribution
./gradlew cleanReleaseApk
```

These custom tasks automatically:
- Clean all build artifacts
- Verify ProGuard rules are comprehensive
- Generate a fresh release build
- Prevent NoSuchMethodError crashes

### Option 2: Manual Clean Build

```bash
# Clean all artifacts
./gradlew clean

# Build release AAB
./gradlew bundleRelease

# Or build release APK
./gradlew assembleRelease
```

## What Changed to Prevent This

### 1. Comprehensive ProGuard Rules (proguard-rules.pro)

The following critical rules were added:

```proguard
# Keep all repository classes and their methods
-keep class com.dolphin.jetpack.data.repository.** { *; }
-keep class com.dolphin.jetpack.domain.repository.** { *; }

# Keep all suspend functions (critical for coroutines)
-keepclassmembers class * {
    suspend ** *(...);
}

# Keep all ViewModel classes
-keep class com.dolphin.jetpack.presentation.viewmodel.** { *; }
```

### 2. R8 Minification Enabled

- **isMinifyEnabled = true** - Now safe with comprehensive keep rules
- **isShrinkResources = true** - Removes unused resources
- Reduces APK size by ~30-40%
- Better performance and security

### 3. Automatic Verification

Every release build now automatically verifies that critical ProGuard rules exist before building.

## Build Artifacts

### Release AAB Location
```
app/release/app-release.aab
```

### ProGuard Mapping File
```
app/build/outputs/mapping/release/mapping.txt
```

**IMPORTANT**: Upload mapping.txt to Firebase Crashlytics after each release for readable stack traces.

## Testing ProGuard Rules Locally

To test minification in debug builds without affecting development speed:

1. Edit `app/build.gradle.kts`
2. Uncomment the debug minification lines:

```kotlin
debug {
    isMinifyEnabled = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

3. Build and test thoroughly
4. Comment out again for normal development

## Common Issues and Solutions

### Issue: "Duplicate class" errors during build
**Solution**: Clean the project first
```bash
./gradlew clean
```

### Issue: App crashes immediately after install
**Likely Cause**: Missing ProGuard keep rule
**Solution**:
1. Check the stack trace
2. Add keep rule for the affected class
3. Rebuild

### Issue: Methods are stripped despite keep rules
**Solution**: Verify rules are being applied
```bash
./gradlew verifyProguardRules
```

## Pre-Release Checklist

- [ ] Increment versionCode in build.gradle.kts
- [ ] Update versionName in build.gradle.kts
- [ ] Run clean build using `./gradlew cleanReleaseBundle`
- [ ] Test the release build on a physical device
- [ ] Verify all features work (especially caching and offline mode)
- [ ] Check ProGuard mapping file is generated
- [ ] Upload mapping.txt to Firebase Crashlytics
- [ ] Create git tag for the release
- [ ] Commit and push changes

## Upload to Google Play

```bash
# AAB file location after successful build
app/release/app-release.aab

# ProGuard mapping for Crashlytics
app/build/outputs/mapping/release/mapping.txt
```

1. Upload AAB to Google Play Console
2. Upload mapping.txt to Firebase Crashlytics (Console > Crashlytics > Mappings)
3. Monitor crash reports for any new issues

## Emergency Rollback

If a crash is detected after release:

1. Check Firebase Crashlytics for stack traces
2. Use the mapping file to deobfuscate crashes
3. If critical, rollback to previous version in Play Console
4. Fix the issue and release a hotfix

## Files Modified for Crash Prevention

1. `app/proguard-rules.pro` - Comprehensive keep rules
2. `app/build.gradle.kts` - R8 enabled + custom tasks
3. This document - Build process guidelines

## Support

For build issues or crashes:
1. Check Firebase Crashlytics dashboard
2. Review recent commits affecting repository/viewmodel classes
3. Verify ProGuard rules cover all affected classes
4. Test with debug minification enabled

---

**Last Updated**: After fixing v6.2.0 NoSuchMethodError crash
**Next Review**: Before enabling any new Android libraries
