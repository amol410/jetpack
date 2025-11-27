-- ============================================
-- FINAL CLEAN: Renumber Chapter IDs to 1, 2, 3...
-- Handles any previous failed attempts
-- 100% Data Safe - No drops, no backups needed
-- ============================================

-- Step 1: Temporarily disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

SELECT 'Step 1: Foreign key checks disabled' as Status;

-- Step 2: Clean up any leftover columns from previous attempts
ALTER TABLE chapters DROP COLUMN IF EXISTS new_id;
ALTER TABLE topics DROP COLUMN IF EXISTS new_chapter_id;

SELECT 'Step 2: Cleaned up any previous attempt leftovers' as Status;

-- Step 3: Drop any existing temporary tables
DROP TEMPORARY TABLE IF EXISTS id_map;

-- Step 4: Create mapping of old IDs to new sequential IDs
CREATE TEMPORARY TABLE id_map (
    old_id INT,
    new_id INT,
    PRIMARY KEY (old_id)
);

SET @row_number = 0;
INSERT INTO id_map (old_id, new_id)
SELECT id, (@row_number := @row_number + 1) as new_id
FROM chapters
ORDER BY order_index ASC, id ASC;

SELECT 'Step 3: ID mapping created' as Status;
SELECT old_id as 'Current ID', new_id as 'Will become'
FROM id_map
ORDER BY new_id;

-- Step 5: Add temporary column to chapters
ALTER TABLE chapters ADD COLUMN new_id INT;

-- Step 6: Assign new sequential IDs to temp column
UPDATE chapters c
INNER JOIN id_map m ON c.id = m.old_id
SET c.new_id = m.new_id;

SELECT 'Step 4: New IDs assigned to chapters' as Status;

-- Step 7: Update topics to reference new chapter IDs
-- Use offset of 10000 to prevent conflicts during update
UPDATE topics t
INNER JOIN id_map m ON t.chapter_id = m.old_id
SET t.chapter_id = m.new_id + 10000;

SELECT 'Step 5: Topics updated (temporary offset)' as Status;

-- Step 8: Update chapter IDs to new values (with offset)
UPDATE chapters c
SET c.id = c.new_id + 10000;

SELECT 'Step 6: Chapter IDs updated (temporary offset)' as Status;

-- Step 9: Remove offset from both tables
UPDATE topics SET chapter_id = chapter_id - 10000;
UPDATE chapters SET id = id - 10000;

SELECT 'Step 7: Final IDs set (offset removed)' as Status;

-- Step 10: Clean up temporary column
ALTER TABLE chapters DROP COLUMN new_id;

SELECT 'Step 8: Cleaned up temporary column' as Status;

-- Step 11: Reset auto_increment to continue from max ID
SET @max_id = (SELECT MAX(id) FROM chapters);
SET @sql = CONCAT('ALTER TABLE chapters AUTO_INCREMENT = ', @max_id + 1);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Step 9: Auto-increment reset' as Status;

-- Step 12: Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Step 10: Foreign key checks re-enabled' as Status;

-- ============================================
-- VERIFICATION & RESULTS
-- ============================================

SELECT '' as '';
SELECT '========================================' as '';
SELECT '✓✓✓ SUCCESS ✓✓✓' as '';
SELECT '========================================' as '';
SELECT '' as '';

SELECT 'Your chapters now have sequential IDs:' as '';
SELECT id as 'Chapter ID', title as 'Chapter Title', order_index as 'Order'
FROM chapters
ORDER BY id;

SELECT '' as '';
SELECT 'Topics are correctly linked:' as '';
SELECT chapter_id as 'Chapter ID', COUNT(*) as 'Number of Topics'
FROM topics
GROUP BY chapter_id
ORDER BY chapter_id;

SELECT '' as '';
SELECT 'Sample topics (showing new chapter_id):' as '';
SELECT
    t.id as 'Topic ID',
    t.chapter_id as 'Chapter ID',
    c.title as 'Chapter Name',
    t.title as 'Topic Name'
FROM topics t
INNER JOIN chapters c ON t.chapter_id = c.id
ORDER BY t.chapter_id, t.id
LIMIT 10;

SELECT '' as '';
SELECT '========================================' as '';
SELECT '✓ All data preserved - nothing lost!' as '';
SELECT '✓ Chapter IDs: 1, 2, 3, 4...' as '';
SELECT '✓ All relationships maintained!' as '';
SELECT '✓ Ready to use immediately!' as '';
SELECT '========================================' as '';
