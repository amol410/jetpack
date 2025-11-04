-- ============================================
-- DATABASE VERIFICATION SCRIPT
-- Run this to check if quiz linking is already set up
-- ============================================

-- Check if required columns exist in quizzes table
SELECT
    'Quizzes Table Schema Check' AS 'Verification',
    '' AS '';

SELECT
    COLUMN_NAME AS 'Column',
    DATA_TYPE AS 'Type',
    IS_NULLABLE AS 'Nullable',
    COLUMN_DEFAULT AS 'Default'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'quizzes'
AND TABLE_SCHEMA = DATABASE()
ORDER BY ORDINAL_POSITION;

-- Check foreign keys
SELECT
    '' AS '',
    'Foreign Key Constraints' AS 'Verification';

SELECT
    CONSTRAINT_NAME AS 'Constraint',
    REFERENCED_TABLE_NAME AS 'References',
    REFERENCED_COLUMN_NAME AS 'Column'
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME = 'quizzes'
AND TABLE_SCHEMA = DATABASE()
AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Check indexes
SELECT
    '' AS '',
    'Indexes' AS 'Verification';

SELECT DISTINCT
    INDEX_NAME AS 'Index',
    COLUMN_NAME AS 'Column'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_NAME = 'quizzes'
AND TABLE_SCHEMA = DATABASE()
ORDER BY INDEX_NAME;

-- Final verification
SELECT
    '' AS '',
    'RESULT' AS 'Verification';

SELECT
    CASE
        WHEN (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
              WHERE TABLE_NAME = 'quizzes' AND COLUMN_NAME = 'chapter_id') > 0
             AND (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
              WHERE TABLE_NAME = 'quizzes' AND COLUMN_NAME = 'topic_id') > 0
        THEN '✅ Quiz linking columns EXIST - You can skip Step 1 (Database Update)!'
        ELSE '❌ Quiz linking columns MISSING - Run database_check_and_update.sql'
    END AS 'Status';
