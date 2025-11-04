# Jetpack Admin Panel - Setup & Usage Guide

## 🎉 What's Been Created

A complete admin panel system with:
- ✅ Secure login system
- ✅ Dashboard with statistics
- ✅ Chapters management (CRUD)
- ✅ Topics management with content editor (CRUD)
- ✅ Quizzes management (CRUD)
- ✅ Questions management with 4 options (CRUD)
- ✅ Public API endpoints for Android app
- ✅ Responsive web interface

---

## 📦 Files Created

### **Database:**
- `admin_database_setup.sql` - Creates all tables for admin system

### **Backend API (api/admin/):**
- `login.php` - Admin authentication
- `logout.php` - Logout functionality
- `dashboard.php` - Dashboard statistics
- `chapters.php` - Chapters CRUD
- `topics.php` - Topics CRUD
- `quizzes.php` - Quizzes CRUD
- `questions.php` - Questions CRUD

### **Public API (api/):**
- `get_chapters.php` - Get all chapters with topics (for Android app)
- `get_topic.php` - Get single topic with content (for Android app)
- `get_quizzes.php` - Get all quizzes (for Android app)
- `get_quiz.php` - Get quiz with questions (for Android app)

### **Admin Panel UI (admin/):**
- `index.html` - Main admin interface
- `style.css` - Styling
- `app.js` - JavaScript functionality

### **Config:**
- `config/admin_auth.php` - Admin authentication helper

---

## 🚀 Deployment Steps

### Step 1: Run SQL Script

1. Open **phpMyAdmin**
2. Select your database: `u437576467_jetpack`
3. Go to **SQL** tab
4. Copy and run the entire content of `admin_database_setup.sql`
5. **Verify:** You should see 6 new tables created:
   - `admins`
   - `subjects`
   - `chapters`
   - `topics`
   - `quizzes`
   - `questions`

### Step 2: Upload Files to Hostinger

Upload these folders to your Hostinger:

**Structure:**
```
public_html/jetpack/
├── api/
│   ├── admin/               (NEW - upload this folder)
│   │   ├── login.php
│   │   ├── logout.php
│   │   ├── dashboard.php
│   │   ├── chapters.php
│   │   ├── topics.php
│   │   ├── quizzes.php
│   │   └── questions.php
│   ├── get_chapters.php     (NEW)
│   ├── get_topic.php        (NEW)
│   ├── get_quizzes.php      (NEW)
│   └── get_quiz.php         (NEW)
├── config/
│   └── admin_auth.php       (NEW)
└── admin/                   (NEW - upload this folder)
    ├── index.html
    ├── style.css
    └── app.js
```

### Step 3: Update API URLs

Edit `admin/app.js` and update line 2-3:

```javascript
const API_BASE_URL = 'https://jetpack.dolphincoder.com/api/admin';  // Update if different
const API_URL = 'https://jetpack.dolphincoder.com/api';
```

Replace `jetpack.dolphincoder.com` with your actual subdomain.

### Step 4: Access Admin Panel

Open in browser:
```
https://jetpack.dolphincoder.com/admin/
```

**Default Login:**
- Username: `admin`
- Password: `admin123`

⚠️ **IMPORTANT:** Change the default password after first login!

---

## 🔐 Change Default Password

### Method 1: Using phpMyAdmin

1. Go to phpMyAdmin
2. Select `admins` table
3. Click **Browse**
4. Click **Edit** on the admin row
5. In `password_hash` field, paste a new bcrypt hash
6. To generate hash, use: https://bcrypt-generator.com/
7. Enter your new password, generate hash, and paste it

### Method 2: Using SQL

```sql
-- Generate hash for your new password first, then:
UPDATE admins
SET password_hash = 'your_bcrypt_hash_here'
WHERE username = 'admin';
```

---

## 📖 How to Use

### Dashboard
- View overall statistics
- See recent users
- Quick overview of content

### Chapters
1. Click **"+ Add Chapter"**
2. Enter:
   - Title (required)
   - Description
   - Order (for sorting)
3. Click **Save**
4. **Edit** or **Delete** existing chapters

### Topics
1. Click **"+ Add Topic"**
2. Select **Chapter** (required)
3. Enter:
   - Title (required)
   - Description
   - Content (full lesson content)
   - Order
4. Click **Save**

💡 **Tip:** Content supports plain text. For rich formatting, consider adding Markdown or HTML support later.

### Quizzes
1. Click **"+ Add Quiz"**
2. Enter:
   - Title (required)
   - Description
   - Order
3. Click **Save**

### Questions
1. Click **"+ Add Question"**
2. Select **Quiz** (required)
3. Enter:
   - Question text
   - Option A, B, C, D (all required)
   - Select correct answer (0=A, 1=B, 2=C, 3=D)
   - Explanation (shown after quiz submission)
   - Order
4. Click **Save**

---

## 📱 Android App Integration

### API Endpoints Available:

#### 1. Get All Chapters with Topics
```
GET https://jetpack.dolphincoder.com/api/get_chapters.php
Headers: X-API-Key: your_api_key

Response:
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Chapter 1",
      "description": "...",
      "topic_count": 5,
      "topics": [
        {
          "id": 1,
          "title": "Topic 1",
          "description": "..."
        }
      ]
    }
  ]
}
```

#### 2. Get Single Topic Content
```
GET https://jetpack.dolphincoder.com/api/get_topic.php?id=1
Headers: X-API-Key: your_api_key

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "chapter_id": 1,
    "chapter_title": "Chapter 1",
    "title": "Topic 1",
    "description": "...",
    "content": "Full lesson content here..."
  }
}
```

#### 3. Get All Quizzes
```
GET https://jetpack.dolphincoder.com/api/get_quizzes.php
Headers: X-API-Key: your_api_key

Response:
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Quiz 1",
      "description": "...",
      "question_count": 10
    }
  ]
}
```

#### 4. Get Quiz with Questions
```
GET https://jetpack.dolphincoder.com/api/get_quiz.php?id=1
Headers: X-API-Key: your_api_key

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Quiz 1",
    "description": "...",
    "questions": [
      {
        "text": "Question text?",
        "options": ["A", "B", "C", "D"],
        "correctAnswerIndex": 2,
        "explanation": "Explanation here"
      }
    ]
  }
}
```

**Format matches your Android app exactly!** ✅

---

## 🔧 Troubleshooting

### Can't Login
- Check if `admin_database_setup.sql` was run successfully
- Verify `admins` table exists and has the default admin user
- Check browser console for errors

### "Invalid API key" on public endpoints
- Make sure you're sending `X-API-Key` header
- Use the same API key from your `api_keys` table
- Current key: `a1b2c3d4e5f6g7h8i9j0`

### Can't add chapters/topics/quizzes
- Check browser console for errors
- Verify session is active (try logging out and in again)
- Check PHP error logs on Hostinger

### Data not showing in Android app
- Verify API endpoints return data when tested in browser
- Check API key is correctly set in Android app
- Ensure content exists in database

---

## 🎨 Customization

### Change Colors
Edit `admin/style.css`:
```css
/* Primary color */
.btn-primary { background: #3498db; } /* Change this */

/* Sidebar */
.sidebar { background: #2c3e50; } /* Change this */
```

### Add More Fields
1. Modify SQL table (ALTER TABLE)
2. Update PHP API to include new field
3. Update HTML form in `app.js`
4. Update save function to include new field

---

## 📊 Database Structure

### subjects
- Default subject "General" (hidden from users)

### chapters
- Belongs to subject
- Has many topics

### topics
- Belongs to chapter
- Contains full lesson content

### quizzes
- Belongs to subject
- Has many questions

### questions
- Belongs to quiz
- 4 options (A, B, C, D)
- Single correct answer (0-3 index)
- Explanation for after submission

---

## ✅ Testing Checklist

- [ ] Admin panel loads at `/admin/`
- [ ] Can login with default credentials
- [ ] Dashboard shows stats
- [ ] Can add a new chapter
- [ ] Can add a topic to that chapter
- [ ] Can add a new quiz
- [ ] Can add questions to that quiz
- [ ] Public API returns chapters: `/api/get_chapters.php`
- [ ] Public API returns specific quiz: `/api/get_quiz.php?id=1`
- [ ] Android app can fetch and display data

---

## 🚨 Security Notes

1. **Change default password immediately**
2. **Use HTTPS only** (Hostinger provides free SSL)
3. **Keep API key secure**
4. **Don't expose admin panel to public** (optional: add IP whitelist)
5. **Regular backups** of database

---

## 📝 Next Steps

Once admin panel is working:
1. Add some chapters and topics
2. Create quizzes with questions
3. Test public API endpoints
4. Integrate with Android app
5. (Optional) Enhance with rich text editor for content
6. (Optional) Add image upload for topics
7. (Optional) Add bulk import/export

---

**Need help?** Check:
- Browser console (F12) for JavaScript errors
- Hostinger PHP error logs
- Network tab to see API responses

🎉 **Enjoy your new admin panel!**
