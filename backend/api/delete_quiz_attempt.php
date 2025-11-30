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

// Parse JSON body
$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['firebase_uid']) || !isset($data['attempt_id'])) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "firebase_uid and attempt_id are required"
    ]);
    exit();
}

$firebase_uid = $data['firebase_uid'];
$attempt_id = (int)$data['attempt_id'];

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

try {
    // Ensure the attempt belongs to this user
    $checkQuery = "SELECT id FROM user_quiz_attempts WHERE id = :attempt_id AND user_id = :user_id LIMIT 1";
    $checkStmt = $db->prepare($checkQuery);
    $checkStmt->bindParam(':attempt_id', $attempt_id, PDO::PARAM_INT);
    $checkStmt->bindParam(':user_id', $user->id, PDO::PARAM_INT);
    $checkStmt->execute();

    if ($checkStmt->rowCount() === 0) {
        http_response_code(404);
        echo json_encode([
            "success" => false,
            "message" => "Attempt not found"
        ]);
        exit();
    }

    // Delete attempt (answers cascade via FK)
    $deleteQuery = "DELETE FROM user_quiz_attempts WHERE id = :attempt_id AND user_id = :user_id";
    $deleteStmt = $db->prepare($deleteQuery);
    $deleteStmt->bindParam(':attempt_id', $attempt_id, PDO::PARAM_INT);
    $deleteStmt->bindParam(':user_id', $user->id, PDO::PARAM_INT);

    if (!$deleteStmt->execute()) {
        throw new Exception("Failed to delete attempt");
    }

    http_response_code(200);
    echo json_encode([
        "success" => true,
        "message" => "Quiz attempt deleted"
    ]);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Error deleting quiz attempt: " . $e->getMessage()
    ]);
}
?>
