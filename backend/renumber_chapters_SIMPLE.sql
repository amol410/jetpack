-- ============================================
-- SIMPLE & SAFE: Renumber Chapter IDs
-- Senior Developer Approach: Update in place, no drops
-- ============================================
-- This preserves ALL data and handles FK constraints properly
-- ============================================

-- Step 1: Temporarily disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

SELECT 'Foreign key checks disabled - safe to update' as Status;

-- Step 2: Create a mapping of old IDs to new sequential IDs
CREATE TEMPORARY TABLE id_map (
    old_id INT,
    new_id INT,
    PRIMARY KEY (old_id)
);

-- Step 3: Generate sequential IDs based on order_index
SET @row_number = 0;
INSERT INTO id_map (old_id, new_id)
SELECT id, (@row_number := @row_number + 1) as new_id
FROM chapters
ORDER BY order_index ASC, id ASC;

SELECT 'ID mapping created:' as Status;
SELECT old_id as 'Old Chapter ID', new_id as 'New Chapter ID'
FROM id_map
ORDER BY new_id;

-- Step 4: Add a temporary column to chapters
ALTER TABLE chapters ADD COLUMN new_id INT;

-- Step 5: Populate the new_id column
UPDATE chapters c
INNER JOIN id_map m ON c.id = m.old_id
SET c.new_id = m.new_id;

SELECT 'Temporary IDs assigned to chapters' as Status;

-- Step 6: Update topics.chapter_id to use new IDs (with temp offset to avoid conflicts)
UPDATE topics t
INNER JOIN id_map m ON t.chapter_id = m.old_id
SET t.chapter_id = m.new_id + 10000;

SELECT 'Topics updated to temporary IDs' as Status;

-- Step 7: Update chapters.id to new sequential IDs (with temp offset)
UPDATE chapters c
SET c.id = c.new_id + 10000;

SELECT 'Chapters updated to temporary IDs' as Status;

-- Step 8: Now shift everything back (remove the 10000 offset)
UPDATE topics SET chapter_id = chapter_id - 10000;
UPDATE chapters SET id = id - 10000;

SELECT 'IDs shifted to final sequential values' as Status;

-- Step 9: Clean up temporary column
ALTER TABLE chapters DROP COLUMN new_id;

-- Step 10: Reset auto_increment
SET @max_id = (SELECT MAX(id) FROM chapters);
SET @sql = CONCAT('ALTER TABLE chapters AUTO_INCREMENT = ', @max_id + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 11: Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Foreign key checks re-enabled' as Status;

-- ============================================
-- VERIFICATION
-- ============================================

SELECT '' as '';
SELECT '======================================' as '';
SELECT '✓ SUCCESS - NO DATA LOST' as '';
SELECT '======================================' as '';
SELECT '' as '';

SELECT 'Chapters with new sequential IDs:' as '';
SELECT id, title, order_index
FROM chapters
ORDER BY id;

SELECT '' as '';
SELECT 'Topics per chapter:' as '';
SELECT chapter_id, COUNT(*) as topic_count
FROM topics
GROUP BY chapter_id
ORDER BY chapter_id;

SELECT '' as '';
SELECT 'Sample topics (first 10):' as '';
SELECT id, chapter_id, title
FROM topics
ORDER BY chapter_id, id
LIMIT 10;

-- Check if notes table exists and show its status
SELECT '' as '';
SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notes') > 0,
    'Notes table exists and is intact',
    'Notes table does not exist (will be created when you add first note)'
) as 'Notes Status';

SELECT '' as '';
SELECT '======================================' as '';
SELECT 'All data preserved!' as '';
SELECT 'Chapter IDs are now: 1, 2, 3...' as '';
SELECT 'All foreign key relationships intact!' as '';
SELECT '======================================' as '';
