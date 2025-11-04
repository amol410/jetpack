-- Database Update: Link Quizzes to Chapters/Topics
-- Run this SQL script to add chapter and topic linking to quizzes

-- Add chapter_id and topic_id columns to quizzes table
ALTER TABLE quizzes
ADD COLUMN chapter_id INT NULL AFTER subject_id,
ADD COLUMN topic_id INT NULL AFTER chapter_id;

-- Add foreign key constraints
ALTER TABLE quizzes
ADD CONSTRAINT fk_quiz_chapter
FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE SET NULL;

ALTER TABLE quizzes
ADD CONSTRAINT fk_quiz_topic
FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE SET NULL;

-- Add indexes for better query performance
ALTER TABLE quizzes
ADD INDEX idx_chapter_id (chapter_id),
ADD INDEX idx_topic_id (topic_id);

-- Note: Quizzes can be linked to:
-- 1. Just a chapter (chapter_id set, topic_id NULL)
-- 2. A specific topic within a chapter (both chapter_id and topic_id set)
-- 3. General quiz (both NULL - existing behavior)
