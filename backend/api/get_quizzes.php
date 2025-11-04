<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type, X-API-Key");

include_once '../config/database.php';
include_once '../config/helpers.php';

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

// Get all quizzes with question count (without questions details)
$query = "SELECT q.id, q.title, q.description, q.order_index,
          (SELECT COUNT(*) FROM questions WHERE quiz_id = q.id) as question_count
          FROM quizzes q
          ORDER BY q.order_index ASC";
$stmt = $db->prepare($query);
$stmt->execute();
$quizzes = $stmt->fetchAll(PDO::FETCH_ASSOC);

http_response_code(200);
echo json_encode([
    "success" => true,
    "data" => $quizzes
]);
?>
