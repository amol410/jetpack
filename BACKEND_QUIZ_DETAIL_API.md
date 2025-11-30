# Backend API: Quiz Attempt Detail

## ⚠️ **IMPORTANT - MANUAL IMPLEMENTATION REQUIRED**

This document describes the backend API endpoint that **MUST BE IMPLEMENTED MANUALLY** for the quiz details feature to work properly.

---

## **API Endpoint**

### **File**: `backend/get_quiz_attempt_detail.php`
### **URL**: `https://jetpack.dolphincoder.com/get_quiz_attempt_detail.php`
### **Method**: `GET`

---

## **Request Parameters**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `attempt_id` | Long/Integer | Yes | The ID of the quiz attempt to retrieve |

### **Example Request**
```
GET https://jetpack.dolphincoder.com/get_quiz_attempt_detail.php?attempt_id=1
```

---

## **Response Format**

### **Success Response** (HTTP 200)
```json
{
  "success": true,
  "message": "Quiz attempt details retrieved successfully",
  "data": {
    "attempt": {
      "id": 1,
      "firebase_uid": "user123abc",
      "quiz_title": "Java Basics Quiz",
      "date_time": "2025-11-30 14:30:00",
      "score": 8,
      "total_questions": 10,
      "time_taken_seconds": 450,
      "percentage": 80,
      "timer_enabled": 1,
      "timer_minutes": 10
    },
    "answers": [
      {
        "question_index": 0,
        "question_text": "What is the size of int in Java?",
        "selected_answer": "2",
        "correct_answer": "2",
        "is_correct": 1
      },
      {
        "question_index": 1,
        "question_text": "Which keyword is used for inheritance?",
        "selected_answer": "1",
        "correct_answer": "0",
        "is_correct": 0
      }
      // ... more answers
    ]
  }
}
```

### **Error Response** (HTTP 200 with success: false)
```json
{
  "success": false,
  "message": "Attempt not found",
  "data": null
}
```

### **Server Error Response** (HTTP 500)
```json
{
  "success": false,
  "message": "Database error: Connection failed",
  "data": null
}
```

---

## **Database Schema Requirements**

### **Table: `quiz_attempts`**
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- firebase_uid (VARCHAR)
- quiz_title (VARCHAR)
- date_time (DATETIME)
- score (INT)
- total_questions (INT)
- time_taken_seconds (BIGINT)
- percentage (INT)
- timer_enabled (TINYINT) -- 0 or 1
- timer_minutes (INT)
```

### **Table: `question_answers`**
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- attempt_id (INT, FOREIGN KEY references quiz_attempts.id)
- question_index (INT)
- question_text (TEXT)
- selected_answer (VARCHAR) -- String representation of answer index
- correct_answer (VARCHAR) -- String representation of correct answer index
- is_correct (TINYINT) -- 0 or 1
```

---

## **PHP Implementation Example**

```php
<?php
header('Content-Type: application/json');
require_once 'db_connection.php'; // Your database connection file

try {
    // Get attempt_id from query parameter
    if (!isset($_GET['attempt_id'])) {
        echo json_encode([
            'success' => false,
            'message' => 'Missing attempt_id parameter',
            'data' => null
        ]);
        exit;
    }

    $attemptId = intval($_GET['attempt_id']);

    // Fetch attempt details
    $attemptQuery = "SELECT * FROM quiz_attempts WHERE id = ?";
    $stmt = $pdo->prepare($attemptQuery);
    $stmt->execute([$attemptId]);
    $attempt = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$attempt) {
        echo json_encode([
            'success' => false,
            'message' => 'Attempt not found',
            'data' => null
        ]);
        exit;
    }

    // Fetch question answers
    $answersQuery = "SELECT * FROM question_answers WHERE attempt_id = ? ORDER BY question_index ASC";
    $stmt = $pdo->prepare($answersQuery);
    $stmt->execute([$attemptId]);
    $answers = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Convert database values to match expected format
    foreach ($answers as &$answer) {
        $answer['is_correct'] = (int)$answer['is_correct'];
    }

    // Return success response
    echo json_encode([
        'success' => true,
        'message' => 'Quiz attempt details retrieved successfully',
        'data' => [
            'attempt' => [
                'id' => (int)$attempt['id'],
                'firebase_uid' => $attempt['firebase_uid'],
                'quiz_title' => $attempt['quiz_title'],
                'date_time' => $attempt['date_time'],
                'score' => (int)$attempt['score'],
                'total_questions' => (int)$attempt['total_questions'],
                'time_taken_seconds' => (int)$attempt['time_taken_seconds'],
                'percentage' => (int)$attempt['percentage'],
                'timer_enabled' => (int)$attempt['timer_enabled'],
                'timer_minutes' => (int)$attempt['timer_minutes']
            ],
            'answers' => $answers
        ]
    ]);

} catch (PDOException $e) {
    error_log("Database error: " . $e->getMessage());
    echo json_encode([
        'success' => false,
        'message' => 'Database error occurred',
        'data' => null
    ]);
}
?>
```

---

## **Testing the API**

### **Using Browser**
```
https://jetpack.dolphincoder.com/get_quiz_attempt_detail.php?attempt_id=1
```

### **Using cURL**
```bash
curl "https://jetpack.dolphincoder.com/get_quiz_attempt_detail.php?attempt_id=1"
```

### **Using Postman**
1. Method: GET
2. URL: `https://jetpack.dolphincoder.com/get_quiz_attempt_detail.php`
3. Query Params: `attempt_id = 1`

---

## **Android App Integration**

The Android app is already configured to use this API:

**File**: `QuizRemoteRepositoryImpl.kt:181-229`
- Automatically calls this API when local database doesn't have the attempt
- Parses the response and converts to `QuizAttempt` model
- Handles errors gracefully with fallback mechanisms

**File**: `HistoryViewModel.kt:180-224`
- First tries local database
- Falls back to backend API if not found locally
- Provides detailed logging for debugging

---

## **Current Status**

✅ **Android Frontend**: Fully implemented and ready
❓ **Backend API**: **NEEDS MANUAL IMPLEMENTATION**

**Action Required**: Implement the `get_quiz_attempt_detail.php` file on the backend server following the specifications above.

---

## **Verification Steps**

After implementing the backend API:

1. **Test API directly** via browser/Postman
2. **Check response format** matches specification
3. **Verify with Android app**:
   - Solve a quiz
   - Go to History
   - Click on any quiz attempt
   - Details should load (showing questions breakdown)
4. **Check Android logs** with filter: `HistoryViewModel`
   - Should show "✅ Successfully fetched from backend"

---

## **Error Scenarios to Handle**

1. **Invalid attempt_id**: Return `success: false` with appropriate message
2. **Attempt not found**: Return `success: false` with "Attempt not found"
3. **Database connection error**: Return error with generic message (don't expose DB details)
4. **Missing parameters**: Validate and return error
5. **Unauthorized access**: (Optional) Add firebase_uid check for security

---

**Last Updated**: 2025-11-30
**Android App Version**: 6.5.5
