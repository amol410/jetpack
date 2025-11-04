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
        // Get all chapters with topic count
        $query = "SELECT c.*, COUNT(t.id) as topic_count
                  FROM chapters c
                  LEFT JOIN topics t ON c.id = t.chapter_id
                  GROUP BY c.id
                  ORDER BY c.order_index ASC";
        $stmt = $db->prepare($query);
        $stmt->execute();
        $chapters = $stmt->fetchAll(PDO::FETCH_ASSOC);

        http_response_code(200);
        echo json_encode([
            "success" => true,
            "data" => $chapters
        ]);
        break;

    case 'POST':
        // Create new chapter
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->title)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "Title is required"]);
            exit();
        }

        $query = "INSERT INTO chapters (title, description, order_index)
                  VALUES (:title, :description, :order_index)";
        $stmt = $db->prepare($query);

        $title = htmlspecialchars(strip_tags($data->title));
        $description = isset($data->description) ? htmlspecialchars(strip_tags($data->description)) : '';
        $order_index = isset($data->order_index) ? $data->order_index : 0;

        $stmt->bindParam(':title', $title);
        $stmt->bindParam(':description', $description);
        $stmt->bindParam(':order_index', $order_index);

        if ($stmt->execute()) {
            http_response_code(201);
            echo json_encode([
                "success" => true,
                "message" => "Chapter created successfully",
                "id" => $db->lastInsertId()
            ]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to create chapter"]);
        }
        break;

    case 'PUT':
        // Update chapter
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->id) || empty($data->title)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "ID and title are required"]);
            exit();
        }

        $query = "UPDATE chapters
                  SET title = :title, description = :description, order_index = :order_index
                  WHERE id = :id";
        $stmt = $db->prepare($query);

        $stmt->bindParam(':id', $data->id);
        $stmt->bindParam(':title', $data->title);
        $stmt->bindParam(':description', $data->description);
        $stmt->bindParam(':order_index', $data->order_index);

        if ($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["success" => true, "message" => "Chapter updated successfully"]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to update chapter"]);
        }
        break;

    case 'DELETE':
        // Delete chapter
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->id)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "ID is required"]);
            exit();
        }

        $query = "DELETE FROM chapters WHERE id = :id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':id', $data->id);

        if ($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["success" => true, "message" => "Chapter deleted successfully"]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to delete chapter"]);
        }
        break;
}
?>
