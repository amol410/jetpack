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

// Get attempt_id from query parameter
$attempt_id = isset($_GET['attempt_id']) ? $_GET['attempt_id'] : '';

if (empty($attempt_id)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "attempt_id parameter is required"
    ]);
    exit();
}

$attempt_id = (int)$attempt_id;

// Get attempt details
$attempt_query = "SELECT id, user_id, quiz_title, date_time, score, total_questions, time_taken_seconds, percentage, timer_enabled, timer_minutes 
                  FROM user_quiz_attempts 
                  WHERE id = :attempt_id";

$attempt_stmt = $db->prepare($attempt_query);
$attempt_stmt->bindParam(':attempt_id', $attempt_id);
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

// Get user to verify ownership
$user = new User($db);
$user->id = $attempt['user_id'];

if (!$user->getUserByFirebaseUid()) {
    http_response_code(404);
    echo json_encode([
        "success" => false,
        "message" => "User not found"
    ]);
    exit();
}

// Get answers for this attempt
$answers_query = "SELECT question_index, question_text, selected_answer, correct_answer, is_correct 
                  FROM user_quiz_answers 
                  WHERE attempt_id = :attempt_id 
                  ORDER BY question_index";

$answers_stmt = $db->prepare($answers_query);
$answers_stmt->bindParam(':attempt_id', $attempt_id);
$answers_stmt->execute();
$answers = $answers_stmt->fetchAll(PDO::FETCH_ASSOC);

http_response_code(200);
echo json_encode([
    "success" => true,
    "data" => [
        "attempt" => $attempt,
        "answers" => $answers
    ]
]);
?>