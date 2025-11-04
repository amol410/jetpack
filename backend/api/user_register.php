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

$user = new User($db);

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

// Set user properties
$user->firebase_uid = $data->firebase_uid;
$user->email = isset($data->email) ? $data->email : null;
$user->display_name = isset($data->display_name) ? $data->display_name : null;
$user->photo_url = isset($data->photo_url) ? $data->photo_url : null;

// Create or update user
if ($user->createOrUpdate()) {
    http_response_code(200);
    echo json_encode([
        "success" => true,
        "message" => "User registered successfully",
        "data" => [
            "user_id" => $user->id,
            "firebase_uid" => $user->firebase_uid,
            "email" => $user->email,
            "display_name" => $user->display_name
        ]
    ]);
} else {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Unable to register user"
    ]);
}
?>
