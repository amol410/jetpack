<?php
session_start();
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

// Destroy session
session_destroy();

http_response_code(200);
echo json_encode([
    "success" => true,
    "message" => "Logged out successfully"
]);
?>
