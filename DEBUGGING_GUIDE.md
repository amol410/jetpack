# Complete Debugging Guide - Why Quiz Attempts Aren't Saving

## Current Status

✅ **Users ARE being saved** - Your user registration works!
❌ **Quiz attempts are NOT being saved** - Need to find out why

---

## Step-by-Step Debugging Process

### Step 1: Install the Updated APK

```bash
# If you haven't built yet
./gradlew.bat assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk

# Or reinstall (clear app data)
adb uninstall com.dolphin.jetpack
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

### Step 2: Capture Logs While Testing

#### Option A: Use the Batch Script (Easiest)

1. Open Command Prompt
2. Navigate to the project folder:
   ```cmd
   cd C:\Users\Admin\AndroidStudioProjects\Jetpack
   ```
3. Run the log capture script:
   ```cmd
   capture_quiz_logs.bat
   ```
4. Follow the on-screen instructions
5. Complete a quiz in the app
6. Press Ctrl+C to stop
7. Copy all the logs and save them

#### Option B: Manual logcat Command

```bash
# Clear old logs first
adb logcat -c

# Start capturing
adb logcat -s QuizViewModel:* QuizRemoteRepo:* AuthViewModel:* UserSyncManager:* OkHttp:* > quiz_logs.txt

# Complete a quiz in the app, then press Ctrl+C
```

---

### Step 3: What to Look For in the Logs

When you complete a quiz, you should see this sequence:

#### 🎯 **1. Quiz Save Initiated**
```
QuizViewModel: ========================================
QuizViewModel: 🎯 SAVING QUIZ ATTEMPT
QuizViewModel: ========================================
QuizViewModel: Quiz Title: Java Basics
QuizViewModel: Score: 8/10 (80%)
QuizViewModel: 💾 Saving to local database...
QuizViewModel: ✅ Saved locally with ID: 42
```

#### 🔐 **2. Authentication Check**
```
QuizViewModel: 🔐 Checking authentication status...
QuizViewModel: ✅ User IS authenticated:
QuizViewModel:    - Firebase UID: abc123xyz...
QuizViewModel:    - Email: user@example.com
QuizViewModel: 📤 Starting backend sync...
```

**⚠️ If you see this instead:**
```
QuizViewModel: ❌ CRITICAL: User NOT authenticated!
QuizViewModel:    - FirebaseAuth.currentUser is NULL
```
**Problem:** User is not signed in. Sign in again.

#### 🌐 **3. API Request Sent**
```
QuizRemoteRepo: ════════════════════════════════════════
QuizRemoteRepo: 🌐 BACKEND API CALL: saveQuizAttempt
QuizRemoteRepo: ════════════════════════════════════════
QuizRemoteRepo: 📡 Sending POST request to:
QuizRemoteRepo:    URL: https://jetpack.dolphincoder.com/api/save_quiz_attempt.php
```

**⚠️ If you DON'T see these logs at all:**
**Problem:** The API call is not being made. User might not be authenticated.

#### 📥 **4. API Response Received**
```
QuizRemoteRepo: 📥 Response Received (350ms):
QuizRemoteRepo:    - HTTP Status: 200
QuizRemoteRepo:    - Is Successful: true
QuizRemoteRepo:    - Response Body Success: true
QuizRemoteRepo: ✅ SUCCESS: Backend sync completed!
QuizRemoteRepo:    - Attempt ID: 123
```

**⚠️ If you see HTTP Status 404:**
```
QuizRemoteRepo: ❌ FAILED: Backend sync failed!
QuizRemoteRepo:    - HTTP Status: 404
QuizRemoteRepo:    - Error Message: User not found
```
**Problem:** The user exists in the `users` table but the Firebase UID doesn't match. Check database!

**⚠️ If you see HTTP Status 500:**
```
QuizRemoteRepo: ❌ FAILED: Backend sync failed!
QuizRemoteRepo:    - HTTP Status: 500
QuizRemoteRepo:    - Error Message: Database error
```
**Problem:** Backend database issue. Check PHP error logs.

**⚠️ If you see network error:**
```
QuizRemoteRepo: 💥 EXCEPTION during backend sync!
QuizRemoteRepo:    - Exception Type: UnknownHostException
QuizRemoteRepo:    - Type: NETWORK ERROR - Cannot resolve hostname
```
**Problem:** No internet connection or server is down.

---

### Step 4: Test API Manually

I created a test HTML file to manually test the API:

1. Open `TEST_API_MANUALLY.html` in your web browser
2. Go to your database and copy a `firebase_uid` from the `users` table
3. Paste it in the test page
4. Click "Send Test Quiz Attempt"
5. Check if it works

**If the manual test WORKS but the app doesn't:**
- The API is fine, the problem is in the app (authentication, network, etc.)

**If the manual test FAILS:**
- The API has an issue (check PHP error logs, database connection, etc.)

---

### Step 5: Check Your Database

#### Check if Users Table Has Data
```sql
SELECT
    user_id,
    firebase_uid,
    email,
    display_name,
    created_at,
    last_login
FROM users
ORDER BY created_at DESC
LIMIT 10;
```

**Expected:** You should see users with their Firebase UIDs

#### Check if Quiz Attempts Table is Empty
```sql
SELECT
    id,
    user_id,
    firebase_uid,
    quiz_title,
    score,
    total_questions,
    percentage,
    date_time
FROM user_quiz_attempts
ORDER BY date_time DESC
LIMIT 10;
```

**If this is empty:** Quiz attempts are not reaching the backend

#### Check if a Specific User Has Attempts
```sql
-- Replace 'YOUR_FIREBASE_UID' with actual UID from users table
SELECT * FROM user_quiz_attempts
WHERE firebase_uid = 'YOUR_FIREBASE_UID'
ORDER BY date_time DESC;
```

---

## Common Issues and Solutions

### Issue 1: User Not Authenticated

**Symptoms:**
- Log shows "❌ CRITICAL: User NOT authenticated!"
- No API calls are made

**Solution:**
1. Sign out of the app
2. Close the app completely
3. Reopen and sign in again
4. Try completing a quiz

### Issue 2: User Not Found (HTTP 404)

**Symptoms:**
- Log shows "HTTP Status: 404"
- Error message: "User not found"

**Solution:**
1. Check if the Firebase UID in the logs matches the UID in your `users` table
2. If not, the user registration might have used a different UID
3. Sign out and sign in again to trigger re-registration

### Issue 3: Network/Timeout Error

**Symptoms:**
- Log shows "UnknownHostException" or "SocketTimeoutException"
- API call is made but times out

**Solution:**
1. Check internet connection on the device
2. Verify the server is accessible: `curl https://jetpack.dolphincoder.com/api/save_quiz_attempt.php`
3. Check firewall settings

### Issue 4: API Key Invalid

**Symptoms:**
- HTTP Status: 401
- Error: "Invalid API key"

**Solution:**
1. Check that `ApiConfig.kt` has: `const val API_KEY = "a1b2c3d4e5f6g7h8i9j0"`
2. Rebuild the app if you changed this

### Issue 5: Database Transaction Fails

**Symptoms:**
- HTTP Status: 500
- Error message mentions "Failed to save" or "transaction"

**Solution:**
1. Check PHP error logs on your server
2. Verify database connection in `config/database.php`
3. Make sure the `user_quiz_attempts` and `user_quiz_answers` tables exist

---

## What to Send Me for Help

If you're still stuck, send me:

1. **The complete logcat output** after completing a quiz (use `capture_quiz_logs.bat`)

2. **A screenshot** of your `users` table showing:
   - At least one user with their `firebase_uid`
   - Whether `last_login` is recent

3. **The result** of the manual API test (`TEST_API_MANUALLY.html`)

4. **Database check results:**
   ```sql
   -- Run these and share the results
   SELECT COUNT(*) as user_count FROM users;
   SELECT COUNT(*) as attempt_count FROM user_quiz_attempts;
   SELECT COUNT(*) as answer_count FROM user_quiz_answers;
   ```

With this information, I can pinpoint exactly where the failure is happening!

---

## Expected Behavior (When Everything Works)

1. ✅ User signs in → `UserSyncManager` registers user in backend
2. ✅ User completes quiz → Quiz saved locally
3. ✅ App checks authentication → User is authenticated
4. ✅ App sends API request → `save_quiz_attempt.php` receives it
5. ✅ Backend validates user → User found in database
6. ✅ Backend saves attempt → `user_quiz_attempts` table updated
7. ✅ Backend saves answers → `user_quiz_answers` table updated
8. ✅ Backend returns success → App logs "✅ SUCCESS"
9. ✅ Database shows new data

---

## Quick Checklist

- [ ] Updated APK installed
- [ ] App data cleared (or reinstalled)
- [ ] Signed in with Google or Email
- [ ] Completed a quiz (all questions answered)
- [ ] Waited 3+ minutes after quiz completion
- [ ] Captured logcat output
- [ ] Checked `users` table (has data)
- [ ] Checked `user_quiz_attempts` table (empty?)
- [ ] Ran manual API test
- [ ] Verified internet connection works

---

**Next Steps:**

1. Run through this debugging guide step by step
2. Capture the logs when you complete a quiz
3. Share the logs with me
4. We'll fix it together!
