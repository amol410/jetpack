# Detailed SQL Database Setup Guide for Hostinger

## Method 1: Copy-Paste SQL (Recommended - Easiest)

### Step-by-Step Instructions:

#### 1. Access phpMyAdmin
- Log in to your **Hostinger Control Panel**
- Look for **Databases** section
- Click on **phpMyAdmin**
- A new tab will open with phpMyAdmin interface

#### 2. Select Your Database
- On the **left sidebar**, you'll see a list of databases
- **Click on your database name** (the one you created)
- The database name will become highlighted/selected

#### 3. Open SQL Tab
- At the top of phpMyAdmin, you'll see multiple tabs:
  - Structure
  - SQL ← **Click this one**
  - Search
  - Query
  - Export
  - Import
  - Operations
- Click on the **SQL** tab

#### 4. Copy the SQL Code
- Open the file `database_setup.sql` from your computer
- **OR** copy the SQL code from the section below (I'll provide it)
- Select ALL the SQL code
- Copy it (Ctrl+C or Cmd+C)

#### 5. Paste into SQL Editor
- In phpMyAdmin's SQL tab, you'll see a large text box
- **Paste** all the SQL code into this text box (Ctrl+V or Cmd+V)

#### 6. Execute the SQL
- Below the text box, click the **Go** button (bottom right)
- Wait for it to process (should take 1-2 seconds)

#### 7. Verify Success
- You should see a green success message saying something like:
  - "4 queries executed successfully"
  - "Your SQL query has been executed successfully"
- Click on **Structure** tab to see your tables
- You should see 4 tables:
  1. ✅ users
  2. ✅ user_sessions
  3. ✅ user_activity
  4. ✅ api_keys

---

## Method 2: Import SQL File

### Step-by-Step Instructions:

#### 1. Access phpMyAdmin
- Log in to **Hostinger Control Panel**
- Go to **Databases** → **phpMyAdmin**

#### 2. Select Your Database
- Click on your database name in the left sidebar

#### 3. Open Import Tab
- At the top, click the **Import** tab

#### 4. Choose File
- Click **Choose File** or **Browse**
- Navigate to where you saved `database_setup.sql`
- Select the file
- Click **Open**

#### 5. Configure Import Settings
- **Format:** Should be SQL (auto-detected)
- **Character set:** utf8mb4 (if available)
- Leave other settings as default

#### 6. Execute Import
- Scroll down
- Click the **Go** or **Import** button at the bottom
- Wait for processing

#### 7. Verify Success
- Check for success message
- Go to **Structure** tab
- Verify 4 tables were created

---

## Method 3: Using Hostinger File Manager + MySQL

If phpMyAdmin is not available or not working:

#### 1. Access MySQL from Terminal (if available)
- In Hostinger control panel, look for **Terminal** or **SSH Access**
- If available, connect to MySQL:

```bash
mysql -u your_database_user -p
```

#### 2. Enter Password
- Enter your database password when prompted

#### 3. Select Database
```sql
USE your_database_name;
```

#### 4. Run SQL File
```sql
source /path/to/database_setup.sql;
```

---

## SQL Code to Copy-Paste

If you can't import the file, **copy ALL of this code below** and paste it in phpMyAdmin SQL tab:

```sql
-- Jetpack App Database Setup

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    firebase_uid VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    display_name VARCHAR(255),
    photo_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_firebase_uid (firebase_uid),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create user_sessions table
CREATE TABLE IF NOT EXISTS user_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    firebase_uid VARCHAR(255) NOT NULL,
    device_id VARCHAR(255),
    device_model VARCHAR(255),
    os_version VARCHAR(100),
    app_version VARCHAR(50),
    session_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_end TIMESTAMP NULL,
    session_duration INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_firebase_uid (firebase_uid),
    INDEX idx_session_start (session_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create user_activity table
CREATE TABLE IF NOT EXISTS user_activity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    firebase_uid VARCHAR(255) NOT NULL,
    activity_type VARCHAR(100),
    activity_data JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create API keys table
CREATE TABLE IF NOT EXISTS api_keys (
    id INT AUTO_INCREMENT PRIMARY KEY,
    api_key VARCHAR(64) NOT NULL UNIQUE,
    app_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP NULL,
    INDEX idx_api_key (api_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default API key
INSERT INTO api_keys (api_key, app_name)
VALUES ('jetpack_dev_key_change_this_in_production', 'Jetpack Android App');
```

---

## Troubleshooting

### Issue: "No database selected"
**Solution:** Make sure you clicked on your database name in the left sidebar before going to SQL tab.

### Issue: "Access denied"
**Solution:** Check that your database user has proper privileges. Go to Hostinger panel → Databases → Check user permissions.

### Issue: "Table already exists"
**Solution:** Tables were already created. You can:
- Skip this step (tables exist)
- Or drop existing tables first (WARNING: deletes all data):
  ```sql
  DROP TABLE IF EXISTS user_activity;
  DROP TABLE IF EXISTS user_sessions;
  DROP TABLE IF EXISTS api_keys;
  DROP TABLE IF EXISTS users;
  ```
  Then run the CREATE TABLE commands again.

### Issue: "Maximum execution time exceeded"
**Solution:** The SQL is too large. Run each CREATE TABLE statement separately:
1. Copy just the first CREATE TABLE (users)
2. Paste and click Go
3. Repeat for each table

### Issue: Can't find phpMyAdmin
**Solution:** In Hostinger:
- Go to **Hosting** → **Manage**
- Look for **Databases** in left menu
- Click **phpMyAdmin** button

---

## Visual Guide: Where to Find Things

### Hostinger Control Panel:
```
Login → Dashboard
  ↓
Hosting → Your Hosting Plan → Manage
  ↓
Left Sidebar:
  - Dashboard
  - Files (File Manager)
  - Databases ← Click Here
      ↓
      - MySQL Databases
      - phpMyAdmin ← Click Here
```

### phpMyAdmin Interface:
```
Top Navigation Tabs:
[Structure] [SQL] [Search] [Query] [Export] [Import] [Operations]
           ↑
    Click SQL tab

Left Sidebar:
- Information_schema
- mysql
- performance_schema
- your_database_name ← Click your database first
```

---

## Verification Steps

After running the SQL:

### 1. Check Structure Tab
- Click **Structure** tab in phpMyAdmin
- You should see a table with 4 rows showing:
  - users
  - user_sessions
  - user_activity
  - api_keys

### 2. Check Each Table
- Click on **users** table in left sidebar
- Click **Structure** tab
- You should see columns: id, firebase_uid, email, display_name, photo_url, created_at, last_login

### 3. Check API Key
- Click on **api_keys** table
- Click **Browse** tab
- You should see 1 row with api_key: `jetpack_dev_key_change_this_in_production`

---

## Next Steps After Success

✅ Tables created successfully
✅ Default API key inserted
→ Now update `config/database.php` with your credentials
→ Test the API with `test_api.html`

---

## Still Having Issues?

If you're still stuck, provide me with:
1. Screenshot of your phpMyAdmin screen
2. Any error message you see
3. What happens when you try to run the SQL

I'll help you troubleshoot!
