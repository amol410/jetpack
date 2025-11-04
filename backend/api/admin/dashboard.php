<?php
session_start();
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

include_once '../../config/database.php';
include_once '../../config/admin_auth.php';

$database = new Database();
$db = $database->getConnection();

requireAdminAuth($db);

// Get statistics
$stats = [];

// Total users
$query = "SELECT COUNT(*) as total FROM users";
$stmt = $db->prepare($query);
$stmt->execute();
$stats['total_users'] = $stmt->fetch(PDO::FETCH_ASSOC)['total'];

// Total sessions
$query = "SELECT COUNT(*) as total FROM user_sessions";
$stmt = $db->prepare($query);
$stmt->execute();
$stats['total_sessions'] = $stmt->fetch(PDO::FETCH_ASSOC)['total'];

// Total chapters
$query = "SELECT COUNT(*) as total FROM chapters";
$stmt = $db->prepare($query);
$stmt->execute();
$stats['total_chapters'] = $stmt->fetch(PDO::FETCH_ASSOC)['total'];

// Total topics
$query = "SELECT COUNT(*) as total FROM topics";
$stmt = $db->prepare($query);
$stmt->execute();
$stats['total_topics'] = $stmt->fetch(PDO::FETCH_ASSOC)['total'];

// Total quizzes
$query = "SELECT COUNT(*) as total FROM quizzes";
$stmt = $db->prepare($query);
$stmt->execute();
$stats['total_quizzes'] = $stmt->fetch(PDO::FETCH_ASSOC)['total'];

// Total questions
$query = "SELECT COUNT(*) as total FROM questions";
$stmt = $db->prepare($query);
$stmt->execute();
$stats['total_questions'] = $stmt->fetch(PDO::FETCH_ASSOC)['total'];

// Recent users
$query = "SELECT id, email, display_name, created_at FROM users ORDER BY created_at DESC LIMIT 5";
$stmt = $db->prepare($query);
$stmt->execute();
$stats['recent_users'] = $stmt->fetchAll(PDO::FETCH_ASSOC);

http_response_code(200);
echo json_encode([
    "success" => true,
    "data" => $stats
]);
?>
