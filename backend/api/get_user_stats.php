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

$user = new User($db);

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

$user->firebase_uid = $firebase_uid;
$stats = $user->getUserStats();

if ($stats) {
    http_response_code(200);
    echo json_encode([
        "success" => true,
        "data" => $stats
    ]);
} else {
    http_response_code(404);
    echo json_encode([
        "success" => false,
        "message" => "User not found"
    ]);
}
?>
