--liquibase formatted sql

--changeset liquibase:1
--comment: Add created_at and updated_at columns to ai_prompts_categories table
ALTER TABLE ai_prompts_categories
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

--changeset liquibase:2
--comment: Add updated_at column to ai_prompt_usage_logs table
ALTER TABLE ai_prompt_usage_logs
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

--changeset liquibase:3
--comment: Update existing records to set updated_at to current timestamp
UPDATE ai_prompts_categories
SET updated_at = CURRENT_TIMESTAMP
WHERE updated_at IS NULL;
UPDATE ai_prompt_usage_logs
SET updated_at = CURRENT_TIMESTAMP
WHERE updated_at IS NULL;