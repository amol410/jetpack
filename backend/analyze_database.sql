-- ============================================
-- Database Analysis Script
-- Run this first to see current state
-- ============================================

-- Show all tables
SELECT 'Current Tables:' as '';
SHOW TABLES;

-- Show chapter data
SELECT '' as '';
SELECT 'Current Chapters:' as '';
SELECT id, title, order_index FROM chapters ORDER BY id;

-- Show foreign key constraints
SELECT '' as '';
SELECT 'Foreign Key Constraints:' as '';
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
    AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME;

-- Check if notes table exists
SELECT '' as '';
SELECT 'Notes table exists:' as '';
SELECT COUNT(*) as notes_table_exists
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'notes';

-- If notes exists, show count
SELECT '' as '';
SELECT 'Notes count (if table exists):' as '';
SELECT COUNT(*) as total_notes FROM notes;
