# 🐛 Backend Sync Debugging Guide

## Problem Found
Your backend API is working fine, but **NO DATA** is being saved to the database! 😄

This means the Android app is either:
1. ❌ Not calling the sync function
2. ❌ Sync is failing silently
3. ❌ User is not authenticated when sync happens

## What I Fixed

I added **detailed logging** to help you debug why data isn't syncing to the backend.

### Files Changed:

1. **QuizViewModel.kt** (Lines 206-218)
   - Added success/failure logging for sync attempts
   - Added warning if user is not authenticated

2. **QuizRemoteRepositoryImpl.kt** (Lines 11-62)
   - Added detailed logging for every step of the sync process
   - Shows Firebase UID, quiz title, score
   - Shows HTTP response codes and error messages

---

## How to Debug

### Step 1: Build and Install the New APK

```bash
cd C:\Users\Admin\AndroidStudioProjects\Jetpack
gradlew.bat assembleDebug
```

Then install the APK on your device.

### Step 2: Open Android Studio Logcat

1. Connect your device/emulator
2. Open Android Studio
3. Go to **Logcat** (bottom panel)
4. Set filter to show only relevant logs

### Step 3: Complete a Quiz

1. Open the app
2. Sign in with Firebase (Google/Email)
3. Complete a quiz
4. Finish the quiz

### Step 4: Check the Logs

Filter Logcat by these tags:

```
QuizViewModel
QuizRemoteRepo
```

You should see logs like these:

#### ✅ **If Sync is Working:**

```
QuizViewModel: ✅ Quiz attempt synced to backend with ID: 123
QuizRemoteRepo: 📤 Attempting to sync quiz attempt to backend...
QuizRemoteRepo:    Firebase UID: abc123xyz
QuizRemoteRepo:    Quiz: Android Basics Quiz
QuizRemoteRepo:    Score: 8/10
QuizRemoteRepo: 📡 Sending POST request to backend...
QuizRemoteRepo: 📥 Response received: code=200, success=true
QuizRemoteRepo: ✅ Backend sync SUCCESS! Attempt ID: 123
```

#### ❌ **If User is Not Authenticated:**

```
QuizViewModel: ⚠️ User not authenticated - quiz attempt NOT synced to backend
```

**Solution:** Make sure you're signed in with Firebase (Google Sign-In or Email)

#### ❌ **If Sync Fails - Network Error:**

```
QuizRemoteRepo: 📤 Attempting to sync quiz attempt to backend...
QuizRemoteRepo: 📡 Sending POST request to backend...
QuizRemoteRepo: ❌ Exception during backend sync: Unable to resolve host
```

**Solution:** Check internet connection

#### ❌ **If Sync Fails - API Key Error (401):**

```
QuizRemoteRepo: 📥 Response received: code=401, success=false
QuizRemoteRepo: ❌ Backend sync FAILED: Invalid API key
```

**Solution:** Check `ApiConfig.kt` - API key should be `a1b2c3d4e5f6g7h8i9j0`

#### ❌ **If Sync Fails - User Not Found (404):**

```
QuizRemoteRepo: 📥 Response received: code=404, success=false
QuizRemoteRepo: ❌ Backend sync FAILED: User not found
```

**Solution:**
1. Check if user exists in `users` table in database
2. Make sure `user_register.php` was called when user signed in
3. Check if Firebase UID matches between app and database

---

## Common Issues and Solutions

### Issue 1: User Not Authenticated

**Symptom:** Log shows "User not authenticated"

**Check:**
```kotlin
// In your app, add this log
val currentUser = FirebaseAuth.getInstance().currentUser
Log.d("DEBUG", "Current User: ${currentUser?.uid}")
```

**Solution:**
- Make sure user is signed in with Firebase
- Check Firebase Authentication in Firebase Console

### Issue 2: User Not Registered in Backend

**Symptom:** Log shows "User not found" (404 error)

**Check Database:**
```sql
SELECT * FROM users WHERE firebase_uid = 'YOUR_FIREBASE_UID';
```

**Solution:**
- User should be auto-registered when first signing in
- Check if `user_register.php` endpoint is being called
- Manually register user in database if needed

### Issue 3: API Key Invalid

**Symptom:** Log shows "Invalid API key" (401 error)

**Check:**
1. `app/src/main/java/com/dolphin/jetpack/data/remote/ApiConfig.kt`
   ```kotlin
   const val API_KEY = "a1b2c3d4e5f6g7h8i9j0"
   ```

2. Database `api_keys` table:
   ```sql
   SELECT * FROM api_keys WHERE api_key = 'a1b2c3d4e5f6g7h8i9j0';
   ```

**Solution:** Make sure API key matches in both places

### Issue 4: Network/SSL Error

**Symptom:** Logs show "Unable to resolve host" or SSL certificate error

**Check:**
1. Internet connection
2. Backend server is running
3. SSL certificate is valid (https://jetpack.dolphincoder.com)

**Solution:**
- Test backend URL in browser
- Check if backend server is online
- Verify SSL certificate

---

## Manual Test: Check if API Works

### Test 1: Check if backend is reachable

Open browser and go to:
```
https://jetpack.dolphincoder.com/api/get_chapters.php
```

Should return JSON data.

### Test 2: Test save endpoint with cURL

```bash
curl -X POST https://jetpack.dolphincoder.com/api/save_quiz_attempt.php \
  -H "Content-Type: application/json" \
  -H "X-API-Key: a1b2c3d4e5f6g7h8i9j0" \
  -d '{
    "firebase_uid": "YOUR_FIREBASE_UID",
    "quiz_title": "Test Quiz",
    "score": 5,
    "total_questions": 10,
    "time_taken_seconds": 60,
    "percentage": 50,
    "timer_enabled": false,
    "timer_minutes": 0,
    "question_answers": []
  }'
```

Expected response:
```json
{
  "success": true,
  "message": "Quiz attempt saved successfully",
  "data": {
    "attempt_id": 1
  }
}
```

---

## Quick Checklist

After installing the new APK with logging, complete these steps:

- [ ] Open Logcat in Android Studio
- [ ] Filter by `QuizViewModel` and `QuizRemoteRepo`
- [ ] Sign in to the app
- [ ] Complete a quiz
- [ ] Check logs for sync messages
- [ ] Note the Firebase UID from logs
- [ ] Check database for that UID in `users` table
- [ ] Check database for quiz attempt in `user_quiz_attempts` table

---

## Expected Database Structure

After successful sync, you should see data in these tables:

### `users` table:
```
id | firebase_uid | email | created_at
1  | abc123xyz    | test@example.com | 2025-11-23 17:00:00
```

### `user_quiz_attempts` table:
```
id | user_id | firebase_uid | quiz_title | score | total_questions | percentage | date_time
1  | 1       | abc123xyz    | Android 101| 8     | 10              | 80         | 2025-11-23 17:30:00
```

### `user_quiz_answers` table:
```
id | attempt_id | user_id | firebase_uid | question_index | is_correct
1  | 1          | 1       | abc123xyz    | 0              | 1
2  | 1          | 1       | abc123xyz    | 1              | 0
```

---

## Need More Help?

If you're still not seeing data in the backend:

1. **Copy ALL logs from Logcat** and share them
2. **Check your Firebase UID** in the logs
3. **Run this SQL query:**
   ```sql
   SELECT * FROM users WHERE firebase_uid = 'YOUR_UID_FROM_LOGS';
   ```
4. **Check if `user_register.php` was called** - look for registration logs

The detailed logs will tell us exactly where the sync is failing! 🔍
