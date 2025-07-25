--liquibase formatted sql

--changeset liquibase:1
--comment: Add updated_at column to tags table
ALTER TABLE tags
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

--changeset liquibase:2
--comment: Add updated_at column to audits table
ALTER TABLE audits
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

--changeset liquibase:3
--comment: Add updated_at column to interview_questions table
ALTER TABLE interview_questions
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

--changeset liquibase:6
--comment: Update existing records to set updated_at to current timestamp
UPDATE tags
SET updated_at = CURRENT_TIMESTAMP
WHERE updated_at IS NULL;
UPDATE audits
SET updated_at = CURRENT_TIMESTAMP
WHERE updated_at IS NULL;
UPDATE interview_questions
SET updated_at = CURRENT_TIMESTAMP
WHERE updated_at IS NULL;