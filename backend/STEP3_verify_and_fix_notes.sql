-- ============================================
-- STEP 3: Verify Database State & Fix Notes Table
-- Run this to check everything after renumbering
-- ============================================

-- Check if notes table exists
SELECT 'Checking if notes table exists...' as Status;

SELECT
    CASE
        WHEN COUNT(*) > 0 THEN 'YES - Notes table exists'
        ELSE 'NO - Notes table does NOT exist (needs to be created)'
    END as 'Notes Table Status'
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'notes';

-- Show all tables in database
SELECT '' as '';
SELECT 'All tables in your database:' as '';
SHOW TABLES;

-- Verify chapters are correctly numbered
SELECT '' as '';
SELECT 'Chapters (should be 1, 2, 3...):' as '';
SELECT id, title, order_index FROM chapters ORDER BY id;

-- Verify topics are correctly linked
SELECT '' as '';
SELECT 'Topics linked to chapters:' as '';
SELECT chapter_id, COUNT(*) as topic_count
FROM topics
GROUP BY chapter_id
ORDER BY chapter_id;

-- Show sample topics with their chapter relationship
SELECT '' as '';
SELECT 'Sample topics (first 10):' as '';
SELECT
    t.id as topic_id,
    t.chapter_id,
    c.title as chapter_name,
    t.title as topic_name
FROM topics t
INNER JOIN chapters c ON t.chapter_id = c.id
ORDER BY t.chapter_id, t.id
LIMIT 10;

-- Check if notes table exists and show its contents
SET @notes_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'notes'
);

SELECT '' as '';
SELECT IF(@notes_exists > 0,
    'Notes table exists - checking contents...',
    'Notes table DOES NOT EXIST - you need to create it!'
) as 'Notes Status';

-- If notes table exists, show its contents
-- (This will error if table doesn't exist, which is fine)
