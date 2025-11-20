-- ============================================
-- STEP 4: Create Notes Table
-- This table was missing after the renumbering
-- ============================================

-- Create notes table
CREATE TABLE IF NOT EXISTS notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    topic_id INT NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT,
    order_index INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE,
    INDEX idx_topic_id (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT '✓ Notes table created successfully!' as Status;

-- Verify it was created
SELECT '' as '';
SELECT 'All tables in database:' as '';
SHOW TABLES;

SELECT '' as '';
SELECT 'Notes table structure:' as '';
DESCRIBE notes;

SELECT '' as '';
SELECT '================================================' as '';
SELECT '✓ Notes table is now ready!' as '';
SELECT '✓ You can now add notes from admin panel' as '';
SELECT '✓ API will work correctly' as '';
SELECT '================================================' as '';
