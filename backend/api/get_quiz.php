<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type, X-API-Key");

include_once '../config/database.php';
include_once '../config/helpers.php';

$database = new Database();
$db = $database->getConnection();

// Verify API key
if (!verifyApiKey($db)) {
    http_response_code(401);
    echo json_encode([
        "success" => false,
        "message" => "Invalid API key"
    ]);
    exit();
}

// Get quiz ID from query parameter
$quiz_id = isset($_GET['id']) ? $_GET['id'] : '';

if (empty($quiz_id)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Quiz ID is required"
    ]);
    exit();
}

// Get quiz details
$query = "SELECT id, title, description FROM quizzes WHERE id = :quiz_id";
$stmt = $db->prepare($query);
$stmt->bindParam(':quiz_id', $quiz_id);
$stmt->execute();

if ($stmt->rowCount() > 0) {
    $quiz = $stmt->fetch(PDO::FETCH_ASSOC);

    // Get all questions for this quiz
    $questionQuery = "SELECT id, question_text as text,
                      option_a, option_b, option_c, option_d,
                      correct_answer_index as correctAnswerIndex,
                      explanation, order_index
                      FROM questions
                      WHERE quiz_id = :quiz_id
                      ORDER BY order_index ASC";
    $questionStmt = $db->prepare($questionQuery);
    $questionStmt->bindParam(':quiz_id', $quiz_id);
    $questionStmt->execute();
    $questions = $questionStmt->fetchAll(PDO::FETCH_ASSOC);

    // Format questions to match Android app structure
    $formattedQuestions = [];
    foreach ($questions as $q) {
        $formattedQuestions[] = [
            'text' => $q['text'],
            'options' => [
                $q['option_a'],
                $q['option_b'],
                $q['option_c'],
                $q['option_d']
            ],
            'correctAnswerIndex' => (int)$q['correctAnswerIndex'],
            'explanation' => $q['explanation']
        ];
    }

    $quiz['questions'] = $formattedQuestions;

    http_response_code(200);
    echo json_encode([
        "success" => true,
        "data" => $quiz
    ]);
} else {
    http_response_code(404);
    echo json_encode([
        "success" => false,
        "message" => "Quiz not found"
    ]);
}
?>
