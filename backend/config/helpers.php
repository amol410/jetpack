<?php
/**
 * Helper Functions
 */

// Get API key from headers (case-insensitive)
function getApiKey() {
    // Method 1: Try getallheaders()
    if (function_exists('getallheaders')) {
        $headers = getallheaders();
        // Make all keys lowercase for comparison
        $headers = array_change_key_case($headers, CASE_LOWER);
        if (isset($headers['x-api-key'])) {
            return $headers['x-api-key'];
        }
    }

    // Method 2: Try $_SERVER
    if (isset($_SERVER['HTTP_X_API_KEY'])) {
        return $_SERVER['HTTP_X_API_KEY'];
    }

    // Method 3: Apache specific
    $headerName = 'X-Api-Key';
    foreach ($_SERVER as $name => $value) {
        if (strcasecmp($name, 'HTTP_X_API_KEY') === 0) {
            return $value;
        }
    }

    return null;
}

// Verify API key
function verifyApiKey($db) {
    $api_key = getApiKey();

    if (!$api_key) {
        return false;
    }

    $query = "SELECT * FROM api_keys WHERE api_key = :api_key AND is_active = 1";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':api_key', $api_key);
    $stmt->execute();

    return $stmt->rowCount() > 0;
}
?>
