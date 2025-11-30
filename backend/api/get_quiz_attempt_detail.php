<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With, X-API-Key");

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    http_response_code(200);
    exit();
}

include_once '../config/database.php';
include_once '../config/helpers.php';
include_once '../models/User.php';

$database = new Database();
$db = $database->getConnection();

// Verify API key
if (!verifyApiKey($db)) {
    http_response_code(401);
    echo json_encode([
        "success" => false,
        "message" => "Invalid API key"
    ]);
    exit();
}

// Get attempt_id and firebase_uid from query parameters
$attempt_id = isset($_GET['attempt_id']) ? $_GET['attempt_id'] : '';
$firebase_uid = isset($_GET['firebase_uid']) ? $_GET['firebase_uid'] : '';

if (empty($attempt_id)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "attempt_id parameter is required"
    ]);
    exit();
}

if (empty($firebase_uid)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "firebase_uid parameter is required"
    ]);
    exit();
}

$attempt_id = (int)$attempt_id;

// Ensure user exists
$user = new User($db);
$user->firebase_uid = $firebase_uid;
if (!$user->getUserByFirebaseUid()) {
    http_response_code(404);
    echo json_encode([
        "success" => false,
        "message" => "User not found"
    ]);
    exit();
}

// Get attempt details for this user
$attempt_query = "SELECT
                    ua.id,
                    u.firebase_uid,
                    ua.quiz_title,
                    ua.date_time,
                    ua.score,
                    ua.total_questions,
                    ua.time_taken_seconds,
                    ua.percentage,
                    ua.timer_enabled,
                    ua.timer_minutes
                  FROM user_quiz_attempts ua
                  LEFT JOIN users u ON ua.user_id = u.id
                  WHERE ua.id = :attempt_id AND ua.user_id = :user_id";

$attempt_stmt = $db->prepare($attempt_query);
$attempt_stmt->bindParam(':attempt_id', $attempt_id, PDO::PARAM_INT);
$attempt_stmt->bindParam(':user_id', $user->id, PDO::PARAM_INT);
$attempt_stmt->execute();
$attempt = $attempt_stmt->fetch(PDO::FETCH_ASSOC);

if (!$attempt) {
    http_response_code(404);
    echo json_encode([
        "success" => false,
        "message" => "Quiz attempt not found"
    ]);
    exit();
}

// Verify that firebase_uid exists (user should exist since we joined)
if (empty($attempt['firebase_uid'])) {
    http_response_code(404);
    echo json_encode([
        "success" => false,
        "message" => "User not found for this attempt"
    ]);
    exit();
}

// Get answers for this attempt
$answers_query = "SELECT question_index, question_text, selected_answer, correct_answer, is_correct
                  FROM user_quiz_answers
                  WHERE attempt_id = :attempt_id AND user_id = :user_id
                  ORDER BY question_index";

$answers_stmt = $db->prepare($answers_query);
$answers_stmt->bindParam(':attempt_id', $attempt_id, PDO::PARAM_INT);
$answers_stmt->bindParam(':user_id', $user->id, PDO::PARAM_INT);
$answers_stmt->execute();
$answers = $answers_stmt->fetchAll(PDO::FETCH_ASSOC);

// Format the response to match Android app expectations
http_response_code(200);
echo json_encode([
    "success" => true,
    "message" => "Quiz attempt details retrieved successfully",
    "data" => [
        "attempt" => [
            "id" => (int)$attempt['id'],
            "firebase_uid" => $attempt['firebase_uid'],
            "quiz_title" => $attempt['quiz_title'],
            "date_time" => $attempt['date_time'],
            "score" => (int)$attempt['score'],
            "total_questions" => (int)$attempt['total_questions'],
            "time_taken_seconds" => (int)$attempt['time_taken_seconds'],
            "percentage" => (int)$attempt['percentage'],
            "timer_enabled" => (int)$attempt['timer_enabled'],
            "timer_minutes" => (int)$attempt['timer_minutes']
        ],
        "answers" => array_map(function($answer) {
            return [
                "question_index" => (int)$answer['question_index'],
                "question_text" => $answer['question_text'],
                "selected_answer" => (string)$answer['selected_answer'],
                "correct_answer" => (string)$answer['correct_answer'],
                "is_correct" => (int)$answer['is_correct']
            ];
        }, $answers)
    ]
]);
?>
