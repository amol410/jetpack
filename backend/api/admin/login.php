<?php
session_start();
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

// Handle preflight
if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    http_response_code(200);
    exit();
}

include_once '../../config/database.php';

$database = new Database();
$db = $database->getConnection();

// Get posted data
$data = json_decode(file_get_contents("php://input"));

if (empty($data->username) || empty($data->password)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Username and password are required"
    ]);
    exit();
}

$username = htmlspecialchars(strip_tags($data->username));
$password = $data->password;

// Get admin from database
$query = "SELECT * FROM admins WHERE username = :username AND is_active = 1";
$stmt = $db->prepare($query);
$stmt->bindParam(':username', $username);
$stmt->execute();

if ($stmt->rowCount() > 0) {
    $admin = $stmt->fetch(PDO::FETCH_ASSOC);

    // Verify password
    if (password_verify($password, $admin['password_hash'])) {
        // Generate session token
        $token = bin2hex(random_bytes(32));
        $_SESSION['admin_token'] = $token;
        $_SESSION['admin_id'] = $admin['id'];
        $_SESSION['admin_username'] = $admin['username'];

        // Update last login
        $updateQuery = "UPDATE admins SET last_login = NOW() WHERE id = :id";
        $updateStmt = $db->prepare($updateQuery);
        $updateStmt->bindParam(':id', $admin['id']);
        $updateStmt->execute();

        http_response_code(200);
        echo json_encode([
            "success" => true,
            "message" => "Login successful",
            "data" => [
                "token" => $token,
                "admin" => [
                    "id" => $admin['id'],
                    "username" => $admin['username'],
                    "email" => $admin['email'],
                    "full_name" => $admin['full_name']
                ]
            ]
        ]);
    } else {
        http_response_code(401);
        echo json_encode([
            "success" => false,
            "message" => "Invalid username or password"
        ]);
    }
} else {
    http_response_code(401);
    echo json_encode([
        "success" => false,
        "message" => "Invalid username or password"
    ]);
}
?>
