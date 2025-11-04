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
        // Get all questions or by quiz
        $quiz_id = isset($_GET['quiz_id']) ? $_GET['quiz_id'] : null;

        if ($quiz_id) {
            $query = "SELECT q.*, qz.title as quiz_title
                      FROM questions q
                      LEFT JOIN quizzes qz ON q.quiz_id = qz.id
                      WHERE q.quiz_id = :quiz_id
                      ORDER BY q.order_index ASC";
            $stmt = $db->prepare($query);
            $stmt->bindParam(':quiz_id', $quiz_id);
        } else {
            $query = "SELECT q.*, qz.title as quiz_title
                      FROM questions q
                      LEFT JOIN quizzes qz ON q.quiz_id = qz.id
                      ORDER BY qz.order_index, q.order_index ASC";
            $stmt = $db->prepare($query);
        }

        $stmt->execute();
        $questions = $stmt->fetchAll(PDO::FETCH_ASSOC);

        http_response_code(200);
        echo json_encode([
            "success" => true,
            "data" => $questions
        ]);
        break;

    case 'POST':
        // Create new question
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->quiz_id) || empty($data->question_text) ||
            empty($data->option_a) || empty($data->option_b) ||
            empty($data->option_c) || empty($data->option_d) ||
            !isset($data->correct_answer_index)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "All fields are required"]);
            exit();
        }

        if ($data->correct_answer_index < 0 || $data->correct_answer_index > 3) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "Correct answer index must be 0-3"]);
            exit();
        }

        $query = "INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d,
                                        correct_answer_index, explanation, order_index)
                  VALUES (:quiz_id, :question_text, :option_a, :option_b, :option_c, :option_d,
                          :correct_answer_index, :explanation, :order_index)";
        $stmt = $db->prepare($query);

        $question_text = htmlspecialchars(strip_tags($data->question_text));
        $option_a = htmlspecialchars(strip_tags($data->option_a));
        $option_b = htmlspecialchars(strip_tags($data->option_b));
        $option_c = htmlspecialchars(strip_tags($data->option_c));
        $option_d = htmlspecialchars(strip_tags($data->option_d));
        $explanation = isset($data->explanation) ? htmlspecialchars(strip_tags($data->explanation)) : '';
        $order_index = isset($data->order_index) ? $data->order_index : 0;

        $stmt->bindParam(':quiz_id', $data->quiz_id);
        $stmt->bindParam(':question_text', $question_text);
        $stmt->bindParam(':option_a', $option_a);
        $stmt->bindParam(':option_b', $option_b);
        $stmt->bindParam(':option_c', $option_c);
        $stmt->bindParam(':option_d', $option_d);
        $stmt->bindParam(':correct_answer_index', $data->correct_answer_index);
        $stmt->bindParam(':explanation', $explanation);
        $stmt->bindParam(':order_index', $order_index);

        if ($stmt->execute()) {
            http_response_code(201);
            echo json_encode([
                "success" => true,
                "message" => "Question created successfully",
                "id" => $db->lastInsertId()
            ]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to create question"]);
        }
        break;

    case 'PUT':
        // Update question
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->id) || empty($data->question_text)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "ID and question text are required"]);
            exit();
        }

        $query = "UPDATE questions
                  SET quiz_id = :quiz_id, question_text = :question_text,
                      option_a = :option_a, option_b = :option_b,
                      option_c = :option_c, option_d = :option_d,
                      correct_answer_index = :correct_answer_index,
                      explanation = :explanation, order_index = :order_index
                  WHERE id = :id";
        $stmt = $db->prepare($query);

        $stmt->bindParam(':id', $data->id);
        $stmt->bindParam(':quiz_id', $data->quiz_id);
        $stmt->bindParam(':question_text', $data->question_text);
        $stmt->bindParam(':option_a', $data->option_a);
        $stmt->bindParam(':option_b', $data->option_b);
        $stmt->bindParam(':option_c', $data->option_c);
        $stmt->bindParam(':option_d', $data->option_d);
        $stmt->bindParam(':correct_answer_index', $data->correct_answer_index);
        $stmt->bindParam(':explanation', $data->explanation);
        $stmt->bindParam(':order_index', $data->order_index);

        if ($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["success" => true, "message" => "Question updated successfully"]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to update question"]);
        }
        break;

    case 'DELETE':
        // Delete question
        $data = json_decode(file_get_contents("php://input"));

        if (empty($data->id)) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "ID is required"]);
            exit();
        }

        $query = "DELETE FROM questions WHERE id = :id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':id', $data->id);

        if ($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["success" => true, "message" => "Question deleted successfully"]);
        } else {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "Failed to delete question"]);
        }
        break;
}
?>
