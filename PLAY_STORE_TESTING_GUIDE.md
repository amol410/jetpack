# Play Store Testing Guide - Critical

## The Problem You're Experiencing

**USB Debug Build Works ✓** → This is because you're testing the **DEBUG** build which has `isMinifyEnabled = false` (no ProGuard)

**Play Store Build Fails ✗** → Play Store uses the **RELEASE** build which has `isMinifyEnabled = true` (ProGuard enabled)

## Why This Happens

1. When you connect via USB in Android Studio, you're installing the **debug** build
2. Debug builds don't use ProGuard minification
3. Play Store requires **release** builds with minification enabled
4. ProGuard obfuscates code, which can break JSON deserialization if not configured properly

## How to Test the ACTUAL Release Build

### Option 1: Install Release APK Locally (RECOMMENDED)

Build and install the actual release APK that simulates Play Store conditions:

```bash
# Build release APK
./gradlew.bat assembleRelease

# Install on connected device
adb install app/build/outputs/apk/release/app-release-unsigned.apk
```

**Note:** The release APK is unsigned, so you may need to uninstall the previous version first:
```bash
adb uninstall com.dolphin.jetpack
adb install app/build/outputs/apk/release/app-release-unsigned.apk
```

### Option 2: Use Internal Testing Track

1. Build AAB: `./gradlew.bat bundleRelease`
2. Upload `app/build/outputs/bundle/release/app-release.aab` to Play Console
3. Publish to **Internal Testing** track
4. Join as tester and download from Play Store
5. This is the EXACT build users will get

### Option 3: Enable Minification in Debug Build

Temporarily test with minification in debug mode:

Edit `app/build.gradle.kts`:
```kotlin
debug {
    isMinifyEnabled = true  // Temporarily enable
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

Then run from Android Studio normally.

**Remember to revert this after testing!**

## What Was Fixed

### Enhanced ProGuard Rules Added

```proguard
# Keep all API response models for Gson/Retrofit deserialization
-keep class com.dolphin.jetpack.data.remote.** { *; }

# Keep all local database entities
-keep class com.dolphin.jetpack.data.local.entity.** { *; }

# Keep generic signatures for Gson reflection
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Prevent obfuscation of classes with @SerializedName
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
```

## Testing Checklist

After installing the RELEASE build:

- [ ] Open app and navigate to **Notes** tab
- [ ] Verify chapters load (should see list of chapters)
- [ ] Tap on a chapter
- [ ] Verify topics load inside the chapter
- [ ] Tap on a topic
- [ ] Verify notes content displays properly
- [ ] Navigate to **Quizzes** tab
- [ ] Verify quiz list loads (should see available quizzes)
- [ ] Tap on a quiz
- [ ] Verify quiz questions display
- [ ] Complete the quiz
- [ ] Verify results screen shows correctly
- [ ] Check **History** tab shows the completed quiz
- [ ] Check **Statistics** tab shows updated stats

## Common Mistakes

### ❌ WRONG: Testing debug build via USB
```bash
# This installs DEBUG build (no minification)
Run > Run 'app' in Android Studio
```

### ✅ CORRECT: Testing release build via USB
```bash
# This installs RELEASE build (with minification)
./gradlew.bat assembleRelease
adb install app/build/outputs/apk/release/app-release-unsigned.apk
```

### ❌ WRONG: Assuming USB testing = Play Store behavior
Debug builds behave differently than release builds!

### ✅ CORRECT: Always test release builds before Play Store upload
Use Internal Testing track or install release APK directly

## Troubleshooting

### If Notes/Quizzes Still Don't Load in Release Build:

1. **Check Logcat for errors:**
   ```bash
   adb logcat | grep -i "ContentRepository\|Gson\|ApiService"
   ```

2. **Look for JSON parsing errors:**
   - `Expected BEGIN_OBJECT but was STRING`
   - `Unable to create instance of class`
   - `NoSuchFieldException`

3. **Verify ProGuard mapping:**
   - Check `app/build/outputs/mapping/release/mapping.txt`
   - Search for your API model classes (should be preserved)

4. **Enable R8 full mode debugging:**
   Add to `gradle.properties`:
   ```properties
   android.enableR8.fullMode=false
   ```

### If Build Fails:

1. Clean project:
   ```bash
   ./gradlew.bat clean
   ```

2. Invalidate caches in Android Studio:
   - File > Invalidate Caches > Invalidate and Restart

3. Delete build folders manually:
   ```bash
   rm -rf app/build .gradle
   ```

## Upload to Play Store

Once you've verified the release build works locally:

1. **Build AAB:**
   ```bash
   ./gradlew.bat bundleRelease
   ```

2. **AAB Location:**
   ```
   app/build/outputs/bundle/release/app-release.aab
   ```

   Also copied to:
   ```
   app/release/app-release.aab
   ```

3. **Upload to Play Console:**
   - Go to Play Console
   - Select your app
   - Production > Create new release
   - Upload the AAB file
   - Roll out to production or testing track

4. **Download ProGuard mapping:**
   - Location: `app/build/outputs/mapping/release/mapping.txt`
   - Upload this to Play Console for crash deobfuscation
   - Go to: App Bundle Explorer > Download > Mapping file

## Important Notes

- **Always test the release build before uploading to Play Store**
- **Never assume debug build behavior = release build behavior**
- **ProGuard can break things that work in debug mode**
- **Use Internal Testing track for final validation**
- **Keep mapping.txt files for crash analysis**

## Version Info
- Version: 6.5.4 (build 15)
- Date: 2025-11-29
- Issue: Quiz and Notes not loading in Play Store builds
- Fix: Enhanced ProGuard rules for Gson deserialization
