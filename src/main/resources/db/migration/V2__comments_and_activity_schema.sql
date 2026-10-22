-- V2__comments_and_activity_schema.sql
-- Migration script for Phase 2: Task comments and Activity logs
-- Reference migration script to be adopted when transitioning to Flyway migrations.

CREATE TABLE IF NOT EXISTS task_comments (
    id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    task_id UUID NOT NULL,
    author_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_task_comments_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_task_comments_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_task_comments_task_id ON task_comments(task_id);
CREATE INDEX IF NOT EXISTS idx_task_comments_created_at ON task_comments(created_at);

CREATE TABLE IF NOT EXISTS activity_logs (
    id UUID PRIMARY KEY,
    task_id UUID,
    project_id UUID NOT NULL,
    actor_id UUID,
    actor_name VARCHAR(255),
    actor_email VARCHAR(255),
    action_type VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_activity_logs_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE SET NULL,
    CONSTRAINT fk_activity_logs_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_activity_logs_task_id ON activity_logs(task_id);
CREATE INDEX IF NOT EXISTS idx_activity_logs_project_id ON activity_logs(project_id);
CREATE INDEX IF NOT EXISTS idx_activity_logs_created_at ON activity_logs(created_at);
