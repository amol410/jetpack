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
$topic_id = isset($_GET['topic_id']) ? $_GET['topic_id'] : '';

if (empty($topic_id)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Topic ID is required"
    ]);
    exit();
}

// Get notes for the specified topic
$query = "SELECT n.*,
          t.title as topic_title,
          t.chapter_id,
          c.title as chapter_title
          FROM notes n
          LEFT JOIN topics t ON n.topic_id = t.id
          LEFT JOIN chapters c ON t.chapter_id = c.id
          WHERE n.topic_id = :topic_id
          ORDER BY n.order_index ASC";
$stmt = $db->prepare($query);
$stmt->bindParam(':topic_id', $topic_id);
$stmt->execute();

$notes = $stmt->fetchAll(PDO::FETCH_ASSOC);

http_response_code(200);
echo json_encode([
    "success" => true,
    "data" => $notes
]);
?>
