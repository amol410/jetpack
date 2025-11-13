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

// Get firebase_uid from query parameter
$firebase_uid = isset($_GET['firebase_uid']) ? $_GET['firebase_uid'] : '';

if (empty($firebase_uid)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "firebase_uid parameter is required"
    ]);
    exit();
}

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

// Get total attempts
$total_attempts_query = "SELECT COUNT(*) as total FROM user_quiz_attempts WHERE user_id = :user_id";
$total_attempts_stmt = $db->prepare($total_attempts_query);
$total_attempts_stmt->bindParam(':user_id', $user->id);
$total_attempts_stmt->execute();
$total_attempts = $total_attempts_stmt->fetch(PDO::FETCH_ASSOC)['total'];

// Get average score
$average_score_query = "SELECT AVG(percentage) as avg_score FROM user_quiz_attempts WHERE user_id = :user_id";
$average_score_stmt = $db->prepare($average_score_query);
$average_score_stmt->bindParam(':user_id', $user->id);
$average_score_stmt->execute();
$average_score = $average_score_stmt->fetch(PDO::FETCH_ASSOC)['avg_score'];
$average_score = $average_score ? (double)$average_score : 0.0;

// Get best score
$best_score_query = "SELECT MAX(percentage) as best_score FROM user_quiz_attempts WHERE user_id = :user_id";
$best_score_stmt = $db->prepare($best_score_query);
$best_score_stmt->bindParam(':user_id', $user->id);
$best_score_stmt->execute();
$best_score = $best_score_stmt->fetch(PDO::FETCH_ASSOC)['best_score'];
$best_score = $best_score ? (int)$best_score : 0;

// Get quiz-wise performance
$quiz_performance_query = "SELECT 
                              quiz_title,
                              COUNT(*) as attempt_count,
                              AVG(percentage) as avg_score
                           FROM user_quiz_attempts 
                           WHERE user_id = :user_id 
                           GROUP BY quiz_title";
$quiz_performance_stmt = $db->prepare($quiz_performance_query);
$quiz_performance_stmt->bindParam(':user_id', $user->id);
$quiz_performance_stmt->execute();
$quiz_performance = $quiz_performance_stmt->fetchAll(PDO::FETCH_ASSOC);

// Get most wrong questions
$most_wrong_query = "SELECT 
                        uqa.question_text,
                        COUNT(*) as wrong_count
                     FROM user_quiz_answers uqa
                     JOIN user_quiz_attempts qua ON uqa.attempt_id = qua.id
                     WHERE qua.user_id = :user_id 
                       AND uqa.is_correct = 0
                     GROUP BY uqa.question_text
                     ORDER BY wrong_count DESC
                     LIMIT 10";
$most_wrong_stmt = $db->prepare($most_wrong_query);
$most_wrong_stmt->bindParam(':user_id', $user->id);
$most_wrong_stmt->execute();
$most_wrong_questions = $most_wrong_stmt->fetchAll(PDO::FETCH_ASSOC);

// Get improvement data (score over time)
$improvement_query = "SELECT 
                         UNIX_TIMESTAMP(date_time) as timestamp,
                         percentage as score
                      FROM user_quiz_attempts 
                      WHERE user_id = :user_id 
                      ORDER BY date_time ASC";
$improvement_stmt = $db->prepare($improvement_query);
$improvement_stmt->bindParam(':user_id', $user->id);
$improvement_stmt->execute();
$improvement_data = $improvement_stmt->fetchAll(PDO::FETCH_ASSOC);

http_response_code(200);
echo json_encode([
    "success" => true,
    "data" => [
        "total_attempts" => (int)$total_attempts,
        "average_score" => $average_score,
        "best_score" => $best_score,
        "quiz_wise_performance" => $quiz_performance,
        "most_wrong_questions" => $most_wrong_questions,
        "improvement_data" => $improvement_data
    ]
]);
?>