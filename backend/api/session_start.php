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
include_once '../models/Session.php';

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

$user = new User($db);
$session = new Session($db);

// Get posted data
$data = json_decode(file_get_contents("php://input"));

// Validate required fields
if (empty($data->firebase_uid)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "firebase_uid is required"
    ]);
    exit();
}

// Get user ID
$user->firebase_uid = $data->firebase_uid;
if (!$user->getUserByFirebaseUid()) {
    http_response_code(404);
    echo json_encode([
        "success" => false,
        "message" => "User not found. Please register first."
    ]);
    exit();
}

// Check if there's an active session
$session->firebase_uid = $data->firebase_uid;
if ($session->getActiveSession()) {
    // End the previous active session
    $session->endSession();
}

// Create new session
$session->user_id = $user->id;
$session->firebase_uid = $data->firebase_uid;
$session->device_id = isset($data->device_id) ? $data->device_id : null;
$session->device_model = isset($data->device_model) ? $data->device_model : null;
$session->os_version = isset($data->os_version) ? $data->os_version : null;
$session->app_version = isset($data->app_version) ? $data->app_version : null;

if ($session->startSession()) {
    http_response_code(200);
    echo json_encode([
        "success" => true,
        "message" => "Session started successfully",
        "data" => [
            "session_id" => $session->id,
            "user_id" => $user->id
        ]
    ]);
} else {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Unable to start session"
    ]);
}
?>
