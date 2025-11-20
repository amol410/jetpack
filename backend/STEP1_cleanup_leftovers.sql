-- ============================================
-- STEP 1: Clean Up Leftover Tables
-- Run this FIRST to remove failed attempt tables
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;

-- Drop all leftover tables from previous attempts
DROP TABLE IF EXISTS chapters_backup;
DROP TABLE IF EXISTS chapters_new;
DROP TABLE IF EXISTS topics_backup;
DROP TABLE IF EXISTS topics_new;
DROP TABLE IF EXISTS notes_backup;
DROP TABLE IF EXISTS notes_new;

-- Drop any leftover temporary tables
DROP TEMPORARY TABLE IF EXISTS id_map;
DROP TEMPORARY TABLE IF EXISTS id_mapping;
DROP TEMPORARY TABLE IF EXISTS chapter_mapping;

-- Remove any leftover columns
ALTER TABLE chapters DROP COLUMN IF EXISTS new_id;
ALTER TABLE topics DROP COLUMN IF EXISTS new_chapter_id;

SET FOREIGN_KEY_CHECKS = 1;

-- Verify cleanup
SELECT 'Cleanup Complete!' as Status;
SELECT 'Remaining tables (should only be your real tables):' as '';
SHOW TABLES;
