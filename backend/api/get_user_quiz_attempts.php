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

// Get optional parameters
$quiz_title = isset($_GET['quiz_title']) ? $_GET['quiz_title'] : null;
$limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 50; // Default to 50
$offset = isset($_GET['offset']) ? (int)$_GET['offset'] : 0; // Default to 0

// Validate limit
if ($limit > 100) $limit = 100; // Maximum 100 per request

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

// Build query with optional filters
$query = "SELECT id, quiz_title, date_time, score, total_questions, time_taken_seconds, percentage, timer_enabled, timer_minutes 
          FROM user_quiz_attempts 
          WHERE user_id = :user_id";

$params = [':user_id' => $user->id];

if ($quiz_title) {
    $query .= " AND quiz_title = :quiz_title";
    $params[':quiz_title'] = $quiz_title;
}

$query .= " ORDER BY date_time DESC LIMIT :limit OFFSET :offset";

$stmt = $db->prepare($query);

// Bind limit and offset as integers
$stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
$stmt->bindValue(':offset', $offset, PDO::PARAM_INT);

// Bind other parameters
foreach ($params as $key => $value) {
    $stmt->bindValue($key, $value);
}

$stmt->execute();
$attempts = $stmt->fetchAll(PDO::FETCH_ASSOC);

http_response_code(200);
echo json_encode([
    "success" => true,
    "data" => $attempts,
    "count" => count($attempts)
]);
?>