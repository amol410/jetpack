<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
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

// Get POST data
$data = json_decode(file_get_contents("php://input"), true);

if (
    !isset($data['firebase_uid']) ||
    !isset($data['quiz_title']) ||
    !isset($data['score']) ||
    !isset($data['total_questions']) ||
    !isset($data['time_taken_seconds']) ||
    !isset($data['percentage'])
) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Missing required fields"
    ]);
    exit();
}

$firebase_uid = $data['firebase_uid'];
$quiz_title = $data['quiz_title'];
$score = $data['score'];
$total_questions = $data['total_questions'];
$time_taken_seconds = $data['time_taken_seconds'];
$percentage = $data['percentage'];
$timer_enabled = isset($data['timer_enabled']) ? (bool)$data['timer_enabled'] : false;
$timer_minutes = isset($data['timer_minutes']) ? $data['timer_minutes'] : 0;
$question_answers = isset($data['question_answers']) ? $data['question_answers'] : [];

// Get user from Firebase UID
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

// Start transaction
$db->beginTransaction();

try {
    // Insert quiz attempt
    $query = "INSERT INTO user_quiz_attempts 
              (user_id, firebase_uid, quiz_title, score, total_questions, time_taken_seconds, percentage, timer_enabled, timer_minutes) 
              VALUES 
              (:user_id, :firebase_uid, :quiz_title, :score, :total_questions, :time_taken_seconds, :percentage, :timer_enabled, :timer_minutes)";

    $stmt = $db->prepare($query);
    $stmt->bindParam(':user_id', $user->id);
    $stmt->bindParam(':firebase_uid', $firebase_uid);
    $stmt->bindParam(':quiz_title', $quiz_title);
    $stmt->bindParam(':score', $score);
    $stmt->bindParam(':total_questions', $total_questions);
    $stmt->bindParam(':time_taken_seconds', $time_taken_seconds);
    $stmt->bindParam(':percentage', $percentage);
    $stmt->bindParam(':timer_enabled', $timer_enabled, PDO::PARAM_BOOL);
    $stmt->bindParam(':timer_minutes', $timer_minutes);

    if (!$stmt->execute()) {
        throw new Exception("Failed to save quiz attempt");
    }

    $attempt_id = $db->lastInsertId();

    // If question answers are provided, save them
    if (!empty($question_answers)) {
        $answer_query = "INSERT INTO user_quiz_answers 
                         (attempt_id, user_id, firebase_uid, question_index, question_text, selected_answer, correct_answer, is_correct) 
                         VALUES 
                         (:attempt_id, :user_id, :firebase_uid, :question_index, :question_text, :selected_answer, :correct_answer, :is_correct)";

        $answer_stmt = $db->prepare($answer_query);

        foreach ($question_answers as $answer) {
            $answer_stmt->bindParam(':attempt_id', $attempt_id);
            $answer_stmt->bindParam(':user_id', $user->id);
            $answer_stmt->bindParam(':firebase_uid', $firebase_uid);
            $answer_stmt->bindParam(':question_index', $answer['question_index']);
            $answer_stmt->bindParam(':question_text', $answer['question_text']);
            $answer_stmt->bindParam(':selected_answer', $answer['selected_answer']);
            $answer_stmt->bindParam(':correct_answer', $answer['correct_answer']);
            $answer_stmt->bindParam(':is_correct', $answer['is_correct'], PDO::PARAM_BOOL);
            
            if (!$answer_stmt->execute()) {
                throw new Exception("Failed to save question answer");
            }
        }
    }

    // Commit transaction
    $db->commit();

    http_response_code(200);
    echo json_encode([
        "success" => true,
        "message" => "Quiz attempt saved successfully",
        "data" => [
            "attempt_id" => $attempt_id
        ]
    ]);

} catch (Exception $e) {
    // Rollback transaction
    $db->rollback();
    
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Error saving quiz attempt: " . $e->getMessage()
    ]);
}
?>