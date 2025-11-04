<?php
/**
 * Database Configuration
 * Update these values with your Hostinger MySQL credentials
 */

class Database {
    // TODO: Update these with your Hostinger database credentials
    private $host = "localhost"; // Usually localhost for Hostinger
    private $db_name = "your_database_name"; // Your database name
    private $username = "your_database_user"; // Your database username
    private $password = "your_database_password"; // Your database password
    public $conn;

    // Get database connection
    public function getConnection() {
        $this->conn = null;

        try {
            $this->conn = new PDO(
                "mysql:host=" . $this->host . ";dbname=" . $this->db_name,
                $this->username,
                $this->password
            );
            $this->conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            $this->conn->exec("set names utf8mb4");
        } catch(PDOException $exception) {
            error_log("Connection error: " . $exception->getMessage());
            echo json_encode([
                "success" => false,
                "message" => "Database connection failed"
            ]);
            die();
        }

        return $this->conn;
    }
}
?>
