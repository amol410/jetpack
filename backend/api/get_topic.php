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

// Get topic ID from query parameter
$topic_id = isset($_GET['id']) ? $_GET['id'] : '';

if (empty($topic_id)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Topic ID is required"
    ]);
    exit();
}

// Get topic with full content
$query = "SELECT t.*, c.title as chapter_title
          FROM topics t
          LEFT JOIN chapters c ON t.chapter_id = c.id
          WHERE t.id = :topic_id";
$stmt = $db->prepare($query);
$stmt->bindParam(':topic_id', $topic_id);
$stmt->execute();

if ($stmt->rowCount() > 0) {
    $topic = $stmt->fetch(PDO::FETCH_ASSOC);
    http_response_code(200);
    echo json_encode([
        "success" => true,
        "data" => $topic
    ]);
} else {
    http_response_code(404);
    echo json_encode([
        "success" => false,
        "message" => "Topic not found"
    ]);
}
?>
