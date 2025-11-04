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
        // Get all quizzes with question count, chapter and topic info
        $query = "SELECT q.*,
                  COUNT(qu.id) as question_count,
                  c.title as chapter_title,
                  t.title as topic_title
                  FROM quizzes q
                  LEFT JOIN questions qu ON q.id = qu.quiz_id
                  LEFT JOIN chapters c ON q.chapter_id = c.id
                  LEFT JOIN topics t ON q.topic_id = t.id
                  GROUP BY q.id
                  ORDER BY q.order_index ASC";
        $stmt = $db->prepare($query);
        $stmt->execute();
        $quizzes = $stmt->fetchAll(PDO::FETCH_ASSOC);

        http_response_code(200);
        echo json_encode([
            "success" => true,
            "data" => $quizzes
        ]);
        break;

    case 'POST':
        // Create new quiz
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->title)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "Title is required"]);
            exit();
        }

        $query = "INSERT INTO quizzes (title, description, chapter_id, topic_id, order_index)
                  VALUES (:title, :description, :chapter_id, :topic_id, :order_index)";
        $stmt = $db->prepare($query);

        $title = htmlspecialchars(strip_tags($data->title));
        $description = isset($data->description) ? htmlspecialchars(strip_tags($data->description)) : '';
        $chapter_id = isset($data->chapter_id) && $data->chapter_id > 0 ? $data->chapter_id : null;
        $topic_id = isset($data->topic_id) && $data->topic_id > 0 ? $data->topic_id : null;
        $order_index = isset($data->order_index) ? $data->order_index : 0;

        $stmt->bindParam(':title', $title);
        $stmt->bindParam(':description', $description);
        $stmt->bindParam(':chapter_id', $chapter_id);
        $stmt->bindParam(':topic_id', $topic_id);
        $stmt->bindParam(':order_index', $order_index);

        if ($stmt->execute()) {
            http_response_code(201);
            echo json_encode([
                "success" => true,
                "message" => "Quiz created successfully",
                "id" => $db->lastInsertId()
            ]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to create quiz"]);
        }
        break;

    case 'PUT':
        // Update quiz
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->id) || empty($data->title)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "ID and title are required"]);
            exit();
        }

        $query = "UPDATE quizzes
                  SET title = :title, description = :description,
                      chapter_id = :chapter_id, topic_id = :topic_id, order_index = :order_index
                  WHERE id = :id";
        $stmt = $db->prepare($query);

        $chapter_id = isset($data->chapter_id) && $data->chapter_id > 0 ? $data->chapter_id : null;
        $topic_id = isset($data->topic_id) && $data->topic_id > 0 ? $data->topic_id : null;

        $stmt->bindParam(':id', $data->id);
        $stmt->bindParam(':title', $data->title);
        $stmt->bindParam(':description', $data->description);
        $stmt->bindParam(':chapter_id', $chapter_id);
        $stmt->bindParam(':topic_id', $topic_id);
        $stmt->bindParam(':order_index', $data->order_index);

        if ($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["success" => true, "message" => "Quiz updated successfully"]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to update quiz"]);
        }
        break;

    case 'DELETE':
        // Delete quiz
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->id)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "ID is required"]);
            exit();
        }

        $query = "DELETE FROM quizzes WHERE id = :id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':id', $data->id);

        if ($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["success" => true, "message" => "Quiz deleted successfully"]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to delete quiz"]);
        }
        break;
}
?>
