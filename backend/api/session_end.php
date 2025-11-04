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

$session = new Session($db);

// Get posted data
$data = json_decode(file_get_contents("php://input"));

// Validate required fields
if (empty($data->session_id)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "session_id is required"
    ]);
    exit();
}

$session->id = $data->session_id;

if ($session->endSession()) {
    http_response_code(200);
    echo json_encode([
        "success" => true,
        "message" => "Session ended successfully"
    ]);
} else {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Unable to end session"
    ]);
}
?>
