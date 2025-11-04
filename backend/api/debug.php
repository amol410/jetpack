<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, GET");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With, X-API-Key");

// Handle preflight
if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    http_response_code(200);
    exit();
}

include_once '../config/database.php';

$database = new Database();
$db = $database->getConnection();

// Get all headers (try multiple methods)
$headers1 = getallheaders();
$headers2 = [];
foreach ($_SERVER as $name => $value) {
    if (substr($name, 0, 5) == 'HTTP_') {
        $headers2[str_replace(' ', '-', ucwords(strtolower(str_replace('_', ' ', substr($name, 5)))))] = $value;
    }
}

// Try to get API key from different sources
$api_key_1 = isset($headers1['X-API-Key']) ? $headers1['X-API-Key'] : 'not found in headers1';
$api_key_2 = isset($headers2['X-Api-Key']) ? $headers2['X-Api-Key'] : 'not found in headers2';
$api_key_3 = isset($_SERVER['HTTP_X_API_KEY']) ? $_SERVER['HTTP_X_API_KEY'] : 'not found in SERVER';

// Check what's in database
$query = "SELECT * FROM api_keys WHERE is_active = 1";
$stmt = $db->prepare($query);
$stmt->execute();
$db_keys = [];
while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
    $db_keys[] = [
        'id' => $row['id'],
        'api_key' => $row['api_key'],
        'app_name' => $row['app_name'],
        'is_active' => $row['is_active']
    ];
}

echo json_encode([
    "message" => "Debug Information",
    "headers_method_1" => $headers1,
    "headers_method_2" => $headers2,
    "api_key_from_headers1" => $api_key_1,
    "api_key_from_headers2" => $api_key_2,
    "api_key_from_server" => $api_key_3,
    "database_api_keys" => $db_keys,
    "request_method" => $_SERVER['REQUEST_METHOD'],
    "server_software" => $_SERVER['SERVER_SOFTWARE']
], JSON_PRETTY_PRINT);
?>
