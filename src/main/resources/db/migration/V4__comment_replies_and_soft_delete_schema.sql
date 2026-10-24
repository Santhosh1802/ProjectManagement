-- V4__comment_replies_and_soft_delete_schema.sql
-- Migration script for Phase 6: Threaded Comment Replies and Soft Deletion
-- Reference migration script to be adopted when transitioning to Flyway migrations.

ALTER TABLE task_comments
    ADD COLUMN IF NOT EXISTS parent_comment_id UUID,
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_task_comments_parent'
    ) THEN
        ALTER TABLE task_comments
            ADD CONSTRAINT fk_task_comments_parent
            FOREIGN KEY (parent_comment_id) REFERENCES task_comments(id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_task_comments_parent_id ON task_comments(parent_comment_id);
