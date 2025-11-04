<?php
// Admin Panel Debug Script
echo "<h1>Admin Panel Debug</h1>";
echo "<pre>";

// Test 1: Check PHP version
echo "1. PHP Version: " . phpversion() . "\n\n";

// Test 2: Check if config files exist
echo "2. Checking file paths:\n";
$configPath = '../config/database.php';
echo "   database.php exists: " . (file_exists($configPath) ? 'YES' : 'NO') . "\n";
echo "   Absolute path: " . realpath($configPath) . "\n\n";

$helpersPath = '../config/helpers.php';
echo "   helpers.php exists: " . (file_exists($helpersPath) ? 'YES' : 'NO') . "\n";
echo "   Absolute path: " . realpath($helpersPath) . "\n\n";

$adminAuthPath = '../config/admin_auth.php';
echo "   admin_auth.php exists: " . (file_exists($adminAuthPath) ? 'YES' : 'NO') . "\n";
echo "   Absolute path: " . realpath($adminAuthPath) . "\n\n";

// Test 3: Try to include database config
echo "3. Testing database connection:\n";
try {
    include_once $configPath;
    echo "   ✓ database.php included successfully\n";

    $database = new Database();
    $db = $database->getConnection();

    if ($db) {
        echo "   ✓ Database connection successful!\n\n";

        // Test 4: Check tables
        echo "4. Checking tables:\n";
        $tables = ['admins', 'subjects', 'chapters', 'topics', 'quizzes', 'questions'];
        foreach ($tables as $table) {
            $query = "SHOW TABLES LIKE '$table'";
            $stmt = $db->prepare($query);
            $stmt->execute();
            $exists = $stmt->rowCount() > 0 ? 'EXISTS' : 'MISSING';
            echo "   $table: $exists\n";
        }
    } else {
        echo "   ✗ Database connection failed\n";
    }
} catch (Exception $e) {
    echo "   ✗ Error: " . $e->getMessage() . "\n";
}

echo "\n5. Current directory: " . getcwd();
echo "\n6. Script location: " . __FILE__;
echo "\n\n";

echo "</pre>";
?>
