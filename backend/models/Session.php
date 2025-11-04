<?php
class Session {
    private $conn;
    private $table_name = "user_sessions";

    public $id;
    public $user_id;
    public $firebase_uid;
    public $device_id;
    public $device_model;
    public $os_version;
    public $app_version;
    public $session_start;
    public $session_end;
    public $session_duration;

    public function __construct($db) {
        $this->conn = $db;
    }

    // Start a new session
    public function startSession() {
        $query = "INSERT INTO " . $this->table_name . "
                  (user_id, firebase_uid, device_id, device_model, os_version, app_version)
                  VALUES (:user_id, :firebase_uid, :device_id, :device_model, :os_version, :app_version)";

        $stmt = $this->conn->prepare($query);

        // Sanitize
        $this->user_id = htmlspecialchars(strip_tags($this->user_id));
        $this->firebase_uid = htmlspecialchars(strip_tags($this->firebase_uid));
        $this->device_id = htmlspecialchars(strip_tags($this->device_id));
        $this->device_model = htmlspecialchars(strip_tags($this->device_model));
        $this->os_version = htmlspecialchars(strip_tags($this->os_version));
        $this->app_version = htmlspecialchars(strip_tags($this->app_version));

        // Bind
        $stmt->bindParam(":user_id", $this->user_id);
        $stmt->bindParam(":firebase_uid", $this->firebase_uid);
        $stmt->bindParam(":device_id", $this->device_id);
        $stmt->bindParam(":device_model", $this->device_model);
        $stmt->bindParam(":os_version", $this->os_version);
        $stmt->bindParam(":app_version", $this->app_version);

        if($stmt->execute()) {
            $this->id = $this->conn->lastInsertId();
            return true;
        }

        return false;
    }

    // End a session
    public function endSession() {
        $query = "UPDATE " . $this->table_name . "
                  SET session_end = NOW(),
                      session_duration = TIMESTAMPDIFF(SECOND, session_start, NOW())
                  WHERE id = :session_id";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(":session_id", $this->id);

        if($stmt->execute()) {
            return true;
        }

        return false;
    }

    // Get active session for user
    public function getActiveSession() {
        $query = "SELECT *
                  FROM " . $this->table_name . "
                  WHERE firebase_uid = :firebase_uid
                  AND session_end IS NULL
                  ORDER BY session_start DESC
                  LIMIT 1";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(":firebase_uid", $this->firebase_uid);
        $stmt->execute();

        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        if($row) {
            $this->id = $row['id'];
            $this->user_id = $row['user_id'];
            $this->firebase_uid = $row['firebase_uid'];
            $this->device_id = $row['device_id'];
            $this->device_model = $row['device_model'];
            $this->os_version = $row['os_version'];
            $this->app_version = $row['app_version'];
            $this->session_start = $row['session_start'];
            return true;
        }

        return false;
    }

    // Get user's session history
    public function getUserSessions() {
        $query = "SELECT *
                  FROM " . $this->table_name . "
                  WHERE firebase_uid = :firebase_uid
                  ORDER BY session_start DESC
                  LIMIT 50";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(":firebase_uid", $this->firebase_uid);
        $stmt->execute();

        return $stmt;
    }
}
?>
