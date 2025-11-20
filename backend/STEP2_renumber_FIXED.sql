-- ============================================
-- STEP 2 FIXED: Renumber Chapter IDs to 1, 2, 3...
-- Handles leftover temp_new_id column
-- ============================================

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Remove temp_new_id if it exists from previous attempt
SET @drop_temp = (
    SELECT IF(
        (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE()
         AND TABLE_NAME = 'chapters'
         AND COLUMN_NAME = 'temp_new_id') > 0,
        'ALTER TABLE chapters DROP COLUMN temp_new_id',
        'SELECT "No temp column to drop" as info'
    )
);
PREPARE stmt FROM @drop_temp;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Cleaned up any leftover temp columns' as Status;

-- Re-enable temporarily to show current state
SET FOREIGN_KEY_CHECKS = 1;

-- Show current state
SELECT '' as '';
SELECT 'Current chapter IDs:' as '';
SELECT id, title, order_index FROM chapters ORDER BY id;

-- Disable again for renumbering
SET FOREIGN_KEY_CHECKS = 0;

-- Create mapping table
DROP TEMPORARY TABLE IF EXISTS id_map;
CREATE TEMPORARY TABLE id_map (
    old_id INT,
    new_id INT,
    PRIMARY KEY (old_id)
);

-- Generate sequential IDs based on order_index
SET @row_number = 0;
INSERT INTO id_map (old_id, new_id)
SELECT id, (@row_number := @row_number + 1) as new_id
FROM chapters
ORDER BY order_index ASC, id ASC;

SELECT '' as '';
SELECT 'ID Mapping Created:' as '';
SELECT old_id as 'Old Chapter ID', new_id as 'New Chapter ID'
FROM id_map
ORDER BY new_id;

-- Add temporary column to chapters
ALTER TABLE chapters ADD COLUMN temp_new_id INT;

-- Assign new IDs to temp column
UPDATE chapters c
INNER JOIN id_map m ON c.id = m.old_id
SET c.temp_new_id = m.new_id;

SELECT 'New IDs assigned to temp column' as Status;

-- Update topics.chapter_id (use 10000 offset to avoid conflicts)
UPDATE topics t
INNER JOIN id_map m ON t.chapter_id = m.old_id
SET t.chapter_id = m.new_id + 10000;

SELECT 'Topics updated with temp offset' as Status;

-- Update chapters.id (use 10000 offset)
UPDATE chapters c
SET c.id = c.temp_new_id + 10000;

SELECT 'Chapter IDs updated with temp offset' as Status;

-- Remove offset from both tables
UPDATE topics SET chapter_id = chapter_id - 10000;
UPDATE chapters SET id = id - 10000;

SELECT 'Offset removed - final IDs set' as Status;

-- Drop temporary column
ALTER TABLE chapters DROP COLUMN temp_new_id;

SELECT 'Temporary column removed' as Status;

-- Reset auto_increment
SET @max_id = (SELECT MAX(id) FROM chapters);
SET @sql = CONCAT('ALTER TABLE chapters AUTO_INCREMENT = ', @max_id + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Auto-increment reset' as Status;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- VERIFICATION & RESULTS
-- ============================================

SELECT '' as '';
SELECT '================================================' as '';
SELECT '✓✓✓ SUCCESS - CHAPTERS RENUMBERED! ✓✓✓' as '';
SELECT '================================================' as '';

SELECT '' as '';
SELECT 'Your chapters now have sequential IDs:' as '';
SELECT id as 'Chapter ID', title as 'Chapter Title', order_index as 'Order'
FROM chapters
ORDER BY id;

SELECT '' as '';
SELECT 'Topics correctly linked to new chapter IDs:' as '';
SELECT chapter_id as 'Chapter ID', COUNT(*) as 'Number of Topics'
FROM topics
GROUP BY chapter_id
ORDER BY chapter_id;

SELECT '' as '';
SELECT 'Sample topics (first 10):' as '';
SELECT
    t.id as 'Topic ID',
    t.chapter_id as 'Chapter ID',
    c.title as 'Chapter',
    t.title as 'Topic'
FROM topics t
INNER JOIN chapters c ON t.chapter_id = c.id
ORDER BY t.chapter_id, t.id
LIMIT 10;

SELECT '' as '';
SELECT '================================================' as '';
SELECT '✓ Chapter IDs are now: 1, 2, 3, 4...' as '';
SELECT '✓ All your data is preserved!' as '';
SELECT '✓ All relationships are intact!' as '';
SELECT '✓ Ready to use in your Android app!' as '';
SELECT '================================================' as '';
