# Testing History API Endpoints

## Base URL
```
https://jetpack.dolphincoder.com/api/
```

## API Key (Required in Headers)
```
X-API-Key: a1b2c3d4e5f6g7h8i9j0
```

---

## 1. Save Quiz Attempt API

**Endpoint:** `POST /save_quiz_attempt.php`

**Headers:**
```
Content-Type: application/json
X-API-Key: a1b2c3d4e5f6g7h8i9j0
```

**Request Body:**
```json
{
  "firebase_uid": "YOUR_FIREBASE_UID_HERE",
  "quiz_title": "Android Basics Quiz",
  "score": 8,
  "total_questions": 10,
  "time_taken_seconds": 120,
  "percentage": 80,
  "timer_enabled": true,
  "timer_minutes": 5,
  "question_answers": [
    {
      "question_index": 0,
      "question_text": "What is Android?",
      "selected_answer": "1",
      "correct_answer": "1",
      "is_correct": true
    },
    {
      "question_index": 1,
      "question_text": "What is Jetpack Compose?",
      "selected_answer": "2",
      "correct_answer": "1",
      "is_correct": false
    }
  ]
}
```

**Expected Success Response:**
```json
{
  "success": true,
  "message": "Quiz attempt saved successfully",
  "data": {
    "attempt_id": 123
  }
}
```

**Possible Error Responses:**
- **401 Unauthorized:** Invalid API key
- **400 Bad Request:** Missing required fields
- **404 Not Found:** User not found with firebase_uid
- **500 Internal Server Error:** Database error

---

## 2. Get User Quiz Attempts API

**Endpoint:** `GET /get_user_quiz_attempts.php`

**Headers:**
```
X-API-Key: a1b2c3d4e5f6g7h8i9j0
```

**Query Parameters:**
- `firebase_uid` (required): User's Firebase UID
- `quiz_title` (optional): Filter by specific quiz
- `limit` (optional): Number of results (default: 50, max: 100)
- `offset` (optional): Pagination offset (default: 0)

**Example Request:**
```
GET /get_user_quiz_attempts.php?firebase_uid=YOUR_FIREBASE_UID_HERE&limit=20
```

**Expected Success Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 123,
      "quiz_title": "Android Basics Quiz",
      "date_time": "2025-11-23 17:30:45",
      "score": 8,
      "total_questions": 10,
      "time_taken_seconds": 120,
      "percentage": 80,
      "timer_enabled": 1,
      "timer_minutes": 5
    },
    {
      "id": 122,
      "quiz_title": "Kotlin Fundamentals",
      "date_time": "2025-11-22 14:20:30",
      "score": 9,
      "total_questions": 10,
      "time_taken_seconds": 95,
      "percentage": 90,
      "timer_enabled": 0,
      "timer_minutes": 0
    }
  ],
  "count": 2
}
```

**Possible Error Responses:**
- **401 Unauthorized:** Invalid API key
- **400 Bad Request:** Missing firebase_uid parameter
- **404 Not Found:** User not found with firebase_uid

---

## 3. Get Quiz Attempt Detail API

**Endpoint:** `GET /get_quiz_attempt_detail.php`

**Headers:**
```
X-API-Key: a1b2c3d4e5f6g7h8i9j0
```

**Query Parameters:**
- `attempt_id` (required): ID of the quiz attempt

**Example Request:**
```
GET /get_quiz_attempt_detail.php?attempt_id=123
```

**Expected Success Response:**
```json
{
  "success": true,
  "data": {
    "attempt": {
      "id": 123,
      "quiz_title": "Android Basics Quiz",
      "date_time": "2025-11-23 17:30:45",
      "score": 8,
      "total_questions": 10,
      "time_taken_seconds": 120,
      "percentage": 80,
      "timer_enabled": 1,
      "timer_minutes": 5
    },
    "question_answers": [
      {
        "question_index": 0,
        "question_text": "What is Android?",
        "selected_answer": "1",
        "correct_answer": "1",
        "is_correct": 1
      },
      {
        "question_index": 1,
        "question_text": "What is Jetpack Compose?",
        "selected_answer": "2",
        "correct_answer": "1",
        "is_correct": 0
      }
    ]
  }
}
```

---

## How to Test the APIs

### Method 1: Using cURL (Command Line)

**Test Save Quiz Attempt:**
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

**Test Get Quiz Attempts:**
```bash
curl -X GET "https://jetpack.dolphincoder.com/api/get_user_quiz_attempts.php?firebase_uid=YOUR_FIREBASE_UID" \
  -H "X-API-Key: a1b2c3d4e5f6g7h8i9j0"
```

### Method 2: Using Postman

1. **Download Postman:** https://www.postman.com/downloads/

2. **For POST request (Save Quiz Attempt):**
   - Method: POST
   - URL: `https://jetpack.dolphincoder.com/api/save_quiz_attempt.php`
   - Headers:
     - `Content-Type`: `application/json`
     - `X-API-Key`: `a1b2c3d4e5f6g7h8i9j0`
   - Body (raw JSON): Copy the JSON from example above
   - Click "Send"

3. **For GET request (Get Quiz Attempts):**
   - Method: GET
   - URL: `https://jetpack.dolphincoder.com/api/get_user_quiz_attempts.php?firebase_uid=YOUR_FIREBASE_UID`
   - Headers:
     - `X-API-Key`: `a1b2c3d4e5f6g7h8i9j0`
   - Click "Send"

### Method 3: Using Browser (for GET requests only)

Simply paste this in your browser (replace YOUR_FIREBASE_UID with actual UID):
```
https://jetpack.dolphincoder.com/api/get_user_quiz_attempts.php?firebase_uid=YOUR_FIREBASE_UID
```

**Note:** You'll need a browser extension like "ModHeader" to add the `X-API-Key` header.

### Method 4: Check in the App Logs

1. Connect your Android device or emulator
2. Open Android Studio
3. Go to Logcat
4. Complete a quiz in the app
5. Filter logs by "Retrofit" or "OkHttp" to see API requests/responses
6. Look for:
   - Request URL
   - Response code (200 = success, 401 = auth error, etc.)
   - Response body

---

## Database Tables

The backend uses these tables:

**user_quiz_attempts:**
- id
- user_id
- firebase_uid
- quiz_title
- date_time (TIMESTAMP, auto-generated)
- score
- total_questions
- time_taken_seconds
- percentage
- timer_enabled
- timer_minutes

**user_quiz_answers:**
- id
- attempt_id (foreign key to user_quiz_attempts.id)
- user_id
- firebase_uid
- question_index
- question_text
- selected_answer
- correct_answer
- is_correct

---

## Troubleshooting

### If you get "User not found" error:
- Make sure the user exists in the `users` table
- The user should be created automatically when first logging in via Firebase
- Check if `user_register.php` is working

### If you get "Invalid API key" error:
- Check that `X-API-Key` header is set to: `a1b2c3d4e5f6g7h8i9j0`
- Check the `api_keys` table in your database

### If you get database connection errors:
- Check `/backend/config/database.php` configuration
- Verify database credentials
- Make sure MySQL/MariaDB is running

### To check actual Firebase UID:
1. Open the app and sign in
2. Add this log in your app code:
```kotlin
val currentUser = FirebaseAuth.getInstance().currentUser
Log.d("TEST", "Firebase UID: ${currentUser?.uid}")
```
3. Use this UID in your API tests

---

## Quick Test Checklist

- [ ] Test save_quiz_attempt.php with valid data → Should return attempt_id
- [ ] Test get_user_quiz_attempts.php → Should return list of attempts
- [ ] Test get_quiz_attempt_detail.php → Should return attempt with answers
- [ ] Verify data appears in Android app History screen
- [ ] Complete a quiz in app → Check if it appears in backend database
- [ ] Check for duplicate entries in history screen
