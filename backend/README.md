# Jetpack Backend API

Backend API for Jetpack Android app to track users and sessions using Firebase Auth + Hostinger MySQL.

## 📁 Directory Structure

```
backend/
├── api/                      # API endpoints
│   ├── user_register.php     # Register/update user
│   ├── session_start.php     # Start user session
│   ├── session_end.php       # End user session
│   └── get_user_stats.php    # Get user statistics
├── config/
│   └── database.php          # Database configuration
├── models/
│   ├── User.php              # User model
│   └── Session.php           # Session model
├── database_setup.sql        # SQL script to create tables
├── test_api.html             # API testing interface
├── DEPLOYMENT_GUIDE.md       # Step-by-step deployment guide
└── README.md                 # This file
```

## 🚀 Quick Start

1. **Read the deployment guide:** `DEPLOYMENT_GUIDE.md`
2. **Upload files** to Hostinger: `public_html/jetpack/`
3. **Create database** via Hostinger panel
4. **Update credentials** in `config/database.php`
5. **Run SQL script** `database_setup.sql` in phpMyAdmin
6. **Test API** using `test_api.html`

## 🔌 API Endpoints

### 1. Register User
**POST** `/api/user_register.php`

**Headers:**
```
Content-Type: application/json
X-API-Key: your_api_key
```

**Body:**
```json
{
  "firebase_uid": "unique_firebase_uid",
  "email": "user@example.com",
  "display_name": "User Name",
  "photo_url": "https://..."
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "user_id": 1,
    "firebase_uid": "unique_firebase_uid",
    "email": "user@example.com",
    "display_name": "User Name"
  }
}
```

### 2. Start Session
**POST** `/api/session_start.php`

**Headers:**
```
Content-Type: application/json
X-API-Key: your_api_key
```

**Body:**
```json
{
  "firebase_uid": "unique_firebase_uid",
  "device_id": "device_12345",
  "device_model": "Samsung Galaxy S21",
  "os_version": "Android 13",
  "app_version": "2.0"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Session started successfully",
  "data": {
    "session_id": 123,
    "user_id": 1
  }
}
```

### 3. End Session
**POST** `/api/session_end.php`

**Headers:**
```
Content-Type: application/json
X-API-Key: your_api_key
```

**Body:**
```json
{
  "session_id": 123
}
```

**Response:**
```json
{
  "success": true,
  "message": "Session ended successfully"
}
```

### 4. Get User Stats
**GET** `/api/get_user_stats.php?firebase_uid={uid}`

**Headers:**
```
X-API-Key: your_api_key
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "firebase_uid": "unique_firebase_uid",
    "email": "user@example.com",
    "display_name": "User Name",
    "created_at": "2025-01-15 10:30:00",
    "last_login": "2025-01-16 14:20:00",
    "total_sessions": 25,
    "total_session_time": 15600,
    "total_activities": 100
  }
}
```

## 🗄️ Database Schema

### users
- `id` - Primary key
- `firebase_uid` - Unique Firebase user ID
- `email` - User email
- `display_name` - User display name
- `photo_url` - Profile photo URL
- `created_at` - Account creation timestamp
- `last_login` - Last login timestamp

### user_sessions
- `id` - Primary key
- `user_id` - Foreign key to users
- `firebase_uid` - Firebase user ID
- `device_id` - Device identifier
- `device_model` - Device model name
- `os_version` - Operating system version
- `app_version` - App version
- `session_start` - Session start time
- `session_end` - Session end time
- `session_duration` - Duration in seconds
- `created_at` - Record creation time

### user_activity
- `id` - Primary key
- `user_id` - Foreign key to users
- `firebase_uid` - Firebase user ID
- `activity_type` - Type of activity
- `activity_data` - JSON data
- `created_at` - Activity timestamp

### api_keys
- `id` - Primary key
- `api_key` - API key string
- `app_name` - Application name
- `is_active` - Active status
- `created_at` - Creation timestamp
- `last_used` - Last usage timestamp

## 🔐 Security Features

- ✅ API key authentication
- ✅ SQL injection prevention (PDO prepared statements)
- ✅ Input sanitization
- ✅ CORS headers configured
- ✅ HTTPS support

## 📝 Notes

- Default API key: `jetpack_dev_key_change_this_in_production`
- **IMPORTANT:** Change this key before production use!
- All timestamps are in UTC
- Session duration calculated automatically on session end

## 🧪 Testing

Use `test_api.html` to test all endpoints before Android integration.

## 📞 Support

For deployment issues, see `DEPLOYMENT_GUIDE.md`
