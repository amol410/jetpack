<?php
class User {
    private $conn;
    private $table_name = "users";

    public $id;
    public $firebase_uid;
    public $email;
    public $display_name;
    public $photo_url;
    public $created_at;
    public $last_login;

    public function __construct($db) {
        $this->conn = $db;
    }

    // Create or update user
    public function createOrUpdate() {
        $query = "INSERT INTO " . $this->table_name . "
                  (firebase_uid, email, display_name, photo_url, last_login)
                  VALUES (:firebase_uid, :email, :display_name, :photo_url, NOW())
                  ON DUPLICATE KEY UPDATE
                  email = :email,
                  display_name = :display_name,
                  photo_url = :photo_url,
                  last_login = NOW()";

        $stmt = $this->conn->prepare($query);

        // Sanitize
        $this->firebase_uid = htmlspecialchars(strip_tags($this->firebase_uid));
        $this->email = htmlspecialchars(strip_tags($this->email));
        $this->display_name = htmlspecialchars(strip_tags($this->display_name));
        $this->photo_url = htmlspecialchars(strip_tags($this->photo_url));

        // Bind
        $stmt->bindParam(":firebase_uid", $this->firebase_uid);
        $stmt->bindParam(":email", $this->email);
        $stmt->bindParam(":display_name", $this->display_name);
        $stmt->bindParam(":photo_url", $this->photo_url);

        if($stmt->execute()) {
            // Get the user ID
            $this->id = $this->conn->lastInsertId();
            if($this->id == 0) {
                // User was updated, fetch the ID
                $this->getUserByFirebaseUid();
            }
            return true;
        }

        return false;
    }

    // Get user by Firebase UID
    public function getUserByFirebaseUid() {
        $query = "SELECT id, firebase_uid, email, display_name, photo_url, created_at, last_login
                  FROM " . $this->table_name . "
                  WHERE firebase_uid = :firebase_uid
                  LIMIT 1";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(":firebase_uid", $this->firebase_uid);
        $stmt->execute();

        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        if($row) {
            $this->id = $row['id'];
            $this->firebase_uid = $row['firebase_uid'];
            $this->email = $row['email'];
            $this->display_name = $row['display_name'];
            $this->photo_url = $row['photo_url'];
            $this->created_at = $row['created_at'];
            $this->last_login = $row['last_login'];
            return true;
        }

        return false;
    }

    // Get total user count
    public function getTotalUsers() {
        $query = "SELECT COUNT(*) as total FROM " . $this->table_name;
        $stmt = $this->conn->prepare($query);
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        return $row['total'];
    }

    // Get user statistics
    public function getUserStats() {
        $query = "SELECT
                    u.id,
                    u.firebase_uid,
                    u.email,
                    u.display_name,
                    u.created_at,
                    u.last_login,
                    COUNT(DISTINCT s.id) as total_sessions,
                    SUM(s.session_duration) as total_session_time,
                    COUNT(DISTINCT a.id) as total_activities
                  FROM " . $this->table_name . " u
                  LEFT JOIN user_sessions s ON u.id = s.user_id
                  LEFT JOIN user_activity a ON u.id = a.user_id
                  WHERE u.firebase_uid = :firebase_uid
                  GROUP BY u.id";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(":firebase_uid", $this->firebase_uid);
        $stmt->execute();

        return $stmt->fetch(PDO::FETCH_ASSOC);
    }
}
?>
