<?php
// Password Hash Generator

$password = 'amol123';
$hash = password_hash($password, PASSWORD_BCRYPT);

echo "<h2>Password Hash Generator</h2>";
echo "<pre>";
echo "Password: <strong>$password</strong>\n\n";
echo "Generated Hash:\n<strong>$hash</strong>\n\n";
echo "==================================\n\n";
echo "SQL Command to Update Admin Password:\n\n";
echo "UPDATE admins \nSET password_hash = '$hash'\nWHERE username = 'admin';\n\n";
echo "==================================\n\n";
echo "Or use this INSERT command if admin doesn't exist:\n\n";
echo "INSERT INTO admins (username, password_hash, email, full_name)\nVALUES (\n";
echo "    'admin',\n";
echo "    '$hash',\n";
echo "    'admin@jetpack.com',\n";
echo "    'Administrator'\n";
echo ");\n";
echo "</pre>";
?>
