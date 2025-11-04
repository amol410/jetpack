# Jetpack Backend Deployment Guide (Hostinger)

## 📋 Overview
This guide will help you deploy the Jetpack API backend to your Hostinger subdomain.

---

## 🚀 Step 1: Upload Files to Hostinger

### File Structure on Hostinger
Upload the backend folder contents to `public_html/jetpack/` with this structure:

```
public_html/jetpack/
├── api/
│   ├── user_register.php
│   ├── session_start.php
│   ├── session_end.php
│   └── get_user_stats.php
├── config/
│   └── database.php
├── models/
│   ├── User.php
│   └── Session.php
├── test_api.html
└── database_setup.sql
```

### How to Upload:
1. **Via File Manager:**
   - Log in to Hostinger control panel
   - Go to "File Manager"
   - Navigate to `public_html/jetpack/`
   - Upload all folders and files

2. **Via FTP:**
   - Use FileZilla or any FTP client
   - Connect to your Hostinger FTP
   - Upload to `public_html/jetpack/`

---

## 🗄️ Step 2: Create MySQL Database

### In Hostinger Control Panel:

1. Go to **Databases** → **MySQL Databases**
2. Click **Create New Database**
3. Name it (e.g., `jetpack_db`)
4. Create a database user (e.g., `jetpack_user`)
5. Set a strong password
6. Assign the user to the database with all privileges
7. **Note down:**
   - Database name
   - Database username
   - Database password
   - Host (usually `localhost`)

---

## 🔧 Step 3: Configure Database Connection

1. Open `config/database.php` in the File Manager or via FTP
2. Update the following values:

```php
private $host = "localhost";
private $db_name = "YOUR_DATABASE_NAME";     // Replace with your database name
private $username = "YOUR_DATABASE_USER";     // Replace with your database username
private $password = "YOUR_DATABASE_PASSWORD"; // Replace with your database password
```

---

## 📊 Step 4: Run Database Setup

### Option 1: Using phpMyAdmin
1. Go to **Databases** → **phpMyAdmin**
2. Select your database from the left sidebar
3. Click **SQL** tab
4. Copy the entire content of `database_setup.sql`
5. Paste it into the SQL editor
6. Click **Go** to execute

### Option 2: Import SQL File
1. Go to **phpMyAdmin**
2. Select your database
3. Click **Import** tab
4. Choose `database_setup.sql` file
5. Click **Go**

**Verify:** You should see 4 tables created:
- `users`
- `user_sessions`
- `user_activity`
- `api_keys`

---

## 🔐 Step 5: Update API Key

### Generate a Secure API Key:
1. Go to phpMyAdmin
2. Select `api_keys` table
3. Click **Edit** on the default row
4. Generate a new secure API key (32-64 characters, random)
   - Online tool: https://www.uuidgenerator.net/
   - Or use: `jetpack_prod_` + random string
5. Update the `api_key` field
6. Save

**Example:**
```
jetpack_prod_a7f3c9e4b2d1f8a6c3e7b9d2f4a8c6e1
```

**Important:** Save this API key - you'll need it in your Android app!

---

## 🧪 Step 6: Test Your API

### Using the Test Page:

1. Open in browser: `https://your-subdomain.com/jetpack/test_api.html`
2. Update the Base URL: `https://your-subdomain.com/jetpack/api`
3. Update the API Key (from Step 5)
4. Test each endpoint:
   - Register User
   - Start Session
   - End Session
   - Get User Stats

### Expected Results:
- **Register User:** Should return success with user_id
- **Start Session:** Should return session_id
- **End Session:** Should return success
- **Get Stats:** Should return user statistics

---

## 🔒 Step 7: Security Hardening

### 1. SSL Certificate
- Hostinger provides free SSL
- Enable it in control panel: **SSL** → **Manage**
- Force HTTPS redirects

### 2. Protect Config Files
Create `.htaccess` in `public_html/jetpack/config/`:

```apache
# Deny access to config directory
Deny from all
```

### 3. Enable Error Logging
In your PHP files, for production:
```php
// Disable displaying errors
ini_set('display_errors', 0);
// Enable error logging
ini_set('log_errors', 1);
ini_set('error_log', '/path/to/error.log');
```

### 4. Rate Limiting (Optional)
Add to API files:
```php
// Simple rate limiting
$rate_limit_key = $_SERVER['REMOTE_ADDR'];
// Implement rate limiting logic
```

---

## 📱 Step 8: Configure Android App

Update your Android app with:

1. **Base URL:** `https://your-subdomain.com/jetpack/api`
2. **API Key:** The key you generated in Step 5

Example in Kotlin:
```kotlin
object ApiConfig {
    const val BASE_URL = "https://your-subdomain.com/jetpack/api/"
    const val API_KEY = "your_api_key_from_step_5"
}
```

---

## ✅ Verification Checklist

- [ ] All files uploaded to Hostinger
- [ ] Database created successfully
- [ ] Database credentials updated in `config/database.php`
- [ ] SQL script executed (4 tables created)
- [ ] API key generated and updated
- [ ] Test API page works for all endpoints
- [ ] SSL certificate enabled
- [ ] API key saved for Android app

---

## 🐛 Troubleshooting

### Issue: "Database connection failed"
- Check database credentials in `config/database.php`
- Verify database exists in Hostinger panel
- Check database user has proper privileges

### Issue: "Invalid API key"
- Verify API key in `api_keys` table
- Check `is_active` = 1
- Ensure `X-API-Key` header is sent correctly

### Issue: "404 Not Found"
- Check file paths are correct
- Verify files uploaded to right directory
- Check .htaccess isn't blocking requests

### Issue: "500 Internal Server Error"
- Check PHP error logs in Hostinger
- Enable error display temporarily:
  ```php
  ini_set('display_errors', 1);
  error_reporting(E_ALL);
  ```
- Check file permissions (should be 644 for files, 755 for folders)

---

## 📞 Support

If you encounter issues:
1. Check Hostinger error logs
2. Review PHP version (should be 7.4+)
3. Verify database connection
4. Test with API test page first

---

## 🎉 Next Steps

Once backend is working:
1. Implement Android app integration
2. Test user registration flow
3. Test session tracking
4. Monitor database for incoming data
5. Set up backup schedule for database

---

## 📝 Important URLs

- **API Base URL:** `https://your-subdomain.com/jetpack/api/`
- **Test Page:** `https://your-subdomain.com/jetpack/test_api.html`
- **Endpoints:**
  - Register: `/user_register.php`
  - Session Start: `/session_start.php`
  - Session End: `/session_end.php`
  - Get Stats: `/get_user_stats.php?firebase_uid={uid}`

---

**Good luck with your deployment! 🚀**
