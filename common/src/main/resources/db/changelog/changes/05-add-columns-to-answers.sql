--liquibase formatted sql

--changeset liquibase:1
--comment: Add ai_score column to answers table
ALTER TABLE answers
    ADD COLUMN ai_score DOUBLE PRECISION;

--changeset liquibase:2
--comment: Add ai_feedback column to answers table
ALTER TABLE answers
    ADD COLUMN ai_feedback TEXT;

--changeset liquibase:3
--comment: Add is_correct column to answers table
ALTER TABLE answers
    ADD COLUMN is_correct BOOLEAN;

--changeset liquibase:4
--comment: Add updated_at column to answers table
ALTER TABLE answers
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;