<?php
/**
 * Admin Authentication Helper
 */

function verifyAdminToken($db) {
    session_start();

    // Check if session has admin token
    if (!isset($_SESSION['admin_token']) || !isset($_SESSION['admin_id'])) {
        return false;
    }

    // Verify admin is still active
    $query = "SELECT id FROM admins WHERE id = :id AND is_active = 1";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':id', $_SESSION['admin_id']);
    $stmt->execute();

    return $stmt->rowCount() > 0;
}

function requireAdminAuth($db) {
    if (!verifyAdminToken($db)) {
        http_response_code(401);
        echo json_encode([
            "success" => false,
            "message" => "Unauthorized. Please login as admin."
        ]);
        exit();
    }
}

function getAdminId() {
    session_start();
    return isset($_SESSION['admin_id']) ? $_SESSION['admin_id'] : null;
}
?>
