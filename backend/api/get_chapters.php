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

// Get all chapters with topics
$query = "SELECT c.id, c.title, c.description, c.order_index,
          (SELECT COUNT(*) FROM topics WHERE chapter_id = c.id) as topic_count
          FROM chapters c
          ORDER BY c.order_index ASC";
$stmt = $db->prepare($query);
$stmt->execute();
$chapters = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Get topics for each chapter
foreach ($chapters as &$chapter) {
    $topicQuery = "SELECT id, title, description, order_index
                   FROM topics
                   WHERE chapter_id = :chapter_id
                   ORDER BY order_index ASC";
    $topicStmt = $db->prepare($topicQuery);
    $topicStmt->bindParam(':chapter_id', $chapter['id']);
    $topicStmt->execute();
    $chapter['topics'] = $topicStmt->fetchAll(PDO::FETCH_ASSOC);
}

http_response_code(200);
echo json_encode([
    "success" => true,
    "data" => $chapters
]);
?>
