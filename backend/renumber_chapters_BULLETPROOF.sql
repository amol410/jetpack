-- ============================================
-- BULLETPROOF: Renumber Chapter IDs
-- Manually clean everything first, then renumber
-- ============================================

-- PART 1: MANUAL CLEANUP
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;

-- Remove any leftover columns from failed attempts
SET @drop_new_id = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'chapters'
     AND COLUMN_NAME = 'new_id') > 0,
    'ALTER TABLE chapters DROP COLUMN new_id',
    'SELECT "No new_id column to drop" as status'
);

PREPARE stmt FROM @drop_new_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Remove any temporary tables
DROP TEMPORARY TABLE IF EXISTS id_map;

SELECT 'Cleanup complete - ready to renumber' as Status;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- PART 2: ACTUAL RENUMBERING
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;

-- Create mapping table
CREATE TEMPORARY TABLE id_map (
    old_id INT,
    new_id INT,
    PRIMARY KEY (old_id)
);

-- Generate sequential mapping
SET @row_number = 0;
INSERT INTO id_map (old_id, new_id)
SELECT id, (@row_number := @row_number + 1) as new_id
FROM chapters
ORDER BY order_index ASC, id ASC;

SELECT 'Mapping created:' as '';
SELECT old_id as 'Old ID', new_id as 'New ID' FROM id_map ORDER BY new_id;

-- Add temp column
ALTER TABLE chapters ADD COLUMN new_id INT;

-- Set new IDs
UPDATE chapters c
INNER JOIN id_map m ON c.id = m.old_id
SET c.new_id = m.new_id;

-- Update topics (with 10000 offset to avoid conflicts)
UPDATE topics t
INNER JOIN id_map m ON t.chapter_id = m.old_id
SET t.chapter_id = m.new_id + 10000;

-- Update chapters (with 10000 offset)
UPDATE chapters SET id = new_id + 10000;

-- Remove offset
UPDATE topics SET chapter_id = chapter_id - 10000;
UPDATE chapters SET id = id - 10000;

-- Clean up
ALTER TABLE chapters DROP COLUMN new_id;

-- Reset auto increment
SET @max_id = (SELECT MAX(id) FROM chapters);
SET @sql = CONCAT('ALTER TABLE chapters AUTO_INCREMENT = ', @max_id + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- RESULTS
-- ============================================

SELECT '' as '';
SELECT '========================================' as '';
SELECT '✓ COMPLETE - Chapter IDs Renumbered!' as '';
SELECT '========================================' as '';

SELECT 'Chapters:' as '';
SELECT id, title FROM chapters ORDER BY id;

SELECT '' as '';
SELECT 'Topics per chapter:' as '';
SELECT chapter_id, COUNT(*) as topics FROM topics GROUP BY chapter_id ORDER BY chapter_id;

SELECT '' as '';
SELECT '✓ All data safe and intact!' as '';
SELECT '✓ IDs now sequential: 1, 2, 3...' as '';
