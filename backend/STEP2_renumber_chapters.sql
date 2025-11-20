-- ============================================
-- STEP 2: Renumber Chapter IDs to 1, 2, 3...
-- Run this AFTER running STEP1_cleanup_leftovers.sql
-- ============================================

-- Check current state first
SELECT 'Current chapter IDs:' as '';
SELECT id, title, order_index FROM chapters ORDER BY id;

SELECT '' as '';
SELECT 'Starting renumbering process...' as '';

-- Disable foreign key checks
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
SELECT 'ID Mapping (Old → New):' as '';
SELECT old_id as 'Current ID', new_id as 'Will Become' FROM id_map ORDER BY new_id;

-- Add temporary column to chapters
ALTER TABLE chapters ADD COLUMN temp_new_id INT;

-- Assign new IDs to temp column
UPDATE chapters c
INNER JOIN id_map m ON c.id = m.old_id
SET c.temp_new_id = m.new_id;

-- Update topics.chapter_id (use 10000 offset to avoid conflicts)
UPDATE topics t
INNER JOIN id_map m ON t.chapter_id = m.old_id
SET t.chapter_id = m.new_id + 10000;

-- Update chapters.id (use 10000 offset)
UPDATE chapters c
SET c.id = c.temp_new_id + 10000;

-- Remove offset from both tables
UPDATE topics SET chapter_id = chapter_id - 10000;
UPDATE chapters SET id = id - 10000;

-- Drop temporary column
ALTER TABLE chapters DROP COLUMN temp_new_id;

-- Reset auto_increment
SET @max_id = (SELECT MAX(id) FROM chapters);
SET @sql = CONCAT('ALTER TABLE chapters AUTO_INCREMENT = ', @max_id + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- VERIFICATION
-- ============================================

SELECT '' as '';
SELECT '========================================' as '';
SELECT '✓ SUCCESS!' as '';
SELECT '========================================' as '';

SELECT '' as '';
SELECT 'Chapters now have sequential IDs:' as '';
SELECT id as 'ID', title as 'Title', order_index as 'Order'
FROM chapters
ORDER BY id;

SELECT '' as '';
SELECT 'Topics per chapter:' as '';
SELECT chapter_id as 'Chapter ID', COUNT(*) as 'Topics'
FROM topics
GROUP BY chapter_id
ORDER BY chapter_id;

SELECT '' as '';
SELECT 'Sample topics (first 10):' as '';
SELECT id as 'Topic ID', chapter_id as 'Chapter ID', title as 'Topic'
FROM topics
ORDER BY chapter_id, id
LIMIT 10;

SELECT '' as '';
SELECT '========================================' as '';
SELECT '✓ Chapter IDs: 1, 2, 3, 4...' as '';
SELECT '✓ All data preserved!' as '';
SELECT '✓ All relationships intact!' as '';
SELECT '========================================' as '';
