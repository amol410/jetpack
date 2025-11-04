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
        // Get all topics or by chapter
        $chapter_id = isset($_GET['chapter_id']) ? $_GET['chapter_id'] : null;

        if ($chapter_id) {
            $query = "SELECT t.*, c.title as chapter_title
                      FROM topics t
                      LEFT JOIN chapters c ON t.chapter_id = c.id
                      WHERE t.chapter_id = :chapter_id
                      ORDER BY t.order_index ASC";
            $stmt = $db->prepare($query);
            $stmt->bindParam(':chapter_id', $chapter_id);
        } else {
            $query = "SELECT t.*, c.title as chapter_title
                      FROM topics t
                      LEFT JOIN chapters c ON t.chapter_id = c.id
                      ORDER BY c.order_index, t.order_index ASC";
            $stmt = $db->prepare($query);
        }

        $stmt->execute();
        $topics = $stmt->fetchAll(PDO::FETCH_ASSOC);

        http_response_code(200);
        echo json_encode([
            "success" => true,
            "data" => $topics
        ]);
        break;

    case 'POST':
        // Create new topic
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->chapter_id) || empty($data->title)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "Chapter ID and title are required"]);
            exit();
        }

        $query = "INSERT INTO topics (chapter_id, title, description, content, order_index)
                  VALUES (:chapter_id, :title, :description, :content, :order_index)";
        $stmt = $db->prepare($query);

        $title = htmlspecialchars(strip_tags($data->title));
        $description = isset($data->description) ? htmlspecialchars(strip_tags($data->description)) : '';
        // Allow HTML in content for rich text formatting, but sanitize dangerous scripts
        $content = isset($data->content) ? strip_tags($data->content, '<p><br><b><i><u><strong><em><h1><h2><h3><h4><h5><h6><ul><ol><li><a><img><blockquote><code><pre><span><div>') : '';
        $order_index = isset($data->order_index) ? $data->order_index : 0;

        $stmt->bindParam(':chapter_id', $data->chapter_id);
        $stmt->bindParam(':title', $title);
        $stmt->bindParam(':description', $description);
        $stmt->bindParam(':content', $content);
        $stmt->bindParam(':order_index', $order_index);

        if ($stmt->execute()) {
            http_response_code(201);
            echo json_encode([
                "success" => true,
                "message" => "Topic created successfully",
                "id" => $db->lastInsertId()
            ]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to create topic"]);
        }
        break;

    case 'PUT':
        // Update topic
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->id) || empty($data->title)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "ID and title are required"]);
            exit();
        }

        $query = "UPDATE topics
                  SET title = :title, description = :description, content = :content,
                      order_index = :order_index, chapter_id = :chapter_id
                  WHERE id = :id";
        $stmt = $db->prepare($query);

        $title = htmlspecialchars(strip_tags($data->title));
        $description = isset($data->description) ? htmlspecialchars(strip_tags($data->description)) : '';
        // Allow HTML in content for rich text formatting, but sanitize dangerous scripts
        $content = isset($data->content) ? strip_tags($data->content, '<p><br><b><i><u><strong><em><h1><h2><h3><h4><h5><h6><ul><ol><li><a><img><blockquote><code><pre><span><div>') : '';

        $stmt->bindParam(':id', $data->id);
        $stmt->bindParam(':chapter_id', $data->chapter_id);
        $stmt->bindParam(':title', $title);
        $stmt->bindParam(':description', $description);
        $stmt->bindParam(':content', $content);
        $stmt->bindParam(':order_index', $data->order_index);

        if ($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["success" => true, "message" => "Topic updated successfully"]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to update topic"]);
        }
        break;

    case 'DELETE':
        // Delete topic
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->id)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "ID is required"]);
            exit();
        }

        $query = "DELETE FROM topics WHERE id = :id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':id', $data->id);

        if ($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["success" => true, "message" => "Topic deleted successfully"]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to delete topic"]);
        }
        break;
}
?>
