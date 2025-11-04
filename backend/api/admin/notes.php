<?php
session_start();
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE");
header("Access-Control-Allow-Headers: Content-Type");

include_once '../../config/database.php';
include_once '../../config/admin_auth.php';

$database = new Database();
$db = $database->getConnection();

requireAdminAuth($db);

$method = $_SERVER['REQUEST_METHOD'];

switch($method) {
    case 'GET':
        // Get all notes with topic and chapter info
        $query = "SELECT n.*,
                  t.title as topic_title,
                  t.chapter_id,
                  c.title as chapter_title
                  FROM notes n
                  LEFT JOIN topics t ON n.topic_id = t.id
                  LEFT JOIN chapters c ON t.chapter_id = c.id
                  ORDER BY n.order_index ASC";
        $stmt = $db->prepare($query);
        $stmt->execute();
        $notes = $stmt->fetchAll(PDO::FETCH_ASSOC);

        http_response_code(200);
        echo json_encode([
            "success" => true,
            "data" => $notes
        ]);
        break;

    case 'POST':
        // Create new note
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->topic_id) || empty($data->title)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "Topic ID and title are required"]);
            exit();
        }

        // Check if note already exists for this topic
        $checkQuery = "SELECT id FROM notes WHERE topic_id = :topic_id";
        $checkStmt = $db->prepare($checkQuery);
        $checkStmt->bindParam(':topic_id', $data->topic_id);
        $checkStmt->execute();

        if ($checkStmt->rowCount() > 0) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "A note already exists for this topic. Please edit the existing note or choose a different topic."]);
            exit();
        }

        $query = "INSERT INTO notes (topic_id, title, content, order_index)
                  VALUES (:topic_id, :title, :content, :order_index)";
        $stmt = $db->prepare($query);

        $title = htmlspecialchars(strip_tags($data->title));
        // Allow HTML in content for rich text formatting, but sanitize dangerous scripts
        $content = isset($data->content) ? strip_tags($data->content, '<p><br><b><i><u><strong><em><h1><h2><h3><h4><h5><h6><ul><ol><li><a><img><blockquote><code><pre><span><div>') : '';
        $order_index = isset($data->order_index) ? $data->order_index : 0;

        $stmt->bindParam(':topic_id', $data->topic_id);
        $stmt->bindParam(':title', $title);
        $stmt->bindParam(':content', $content);
        $stmt->bindParam(':order_index', $order_index);

        if ($stmt->execute()) {
            http_response_code(201);
            echo json_encode([
                "success" => true,
                "message" => "Note created successfully",
                "id" => $db->lastInsertId()
            ]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to create note"]);
        }
        break;

    case 'PUT':
        // Update note
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->id) || empty($data->title)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "ID and title are required"]);
            exit();
        }

        $query = "UPDATE notes
                  SET title = :title, content = :content, order_index = :order_index
                  WHERE id = :id";
        $stmt = $db->prepare($query);

        $title = htmlspecialchars(strip_tags($data->title));
        // Allow HTML in content for rich text formatting, but sanitize dangerous scripts
        $content = isset($data->content) ? strip_tags($data->content, '<p><br><b><i><u><strong><em><h1><h2><h3><h4><h5><h6><ul><ol><li><a><img><blockquote><code><pre><span><div>') : '';

        $stmt->bindParam(':id', $data->id);
        $stmt->bindParam(':title', $title);
        $stmt->bindParam(':content', $content);
        $stmt->bindParam(':order_index', $data->order_index);

        if ($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["success" => true, "message" => "Note updated successfully"]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to update note"]);
        }
        break;

    case 'DELETE':
        // Delete note
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->id)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "ID is required"]);
            exit();
        }

        $query = "DELETE FROM notes WHERE id = :id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':id', $data->id);

        if ($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["success" => true, "message" => "Note deleted successfully"]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to delete note"]);
        }
        break;
}
?>
