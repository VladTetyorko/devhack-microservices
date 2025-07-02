--liquibase formatted sql

--changeset liquibase:1
--comment: Update interview_questions table to make difficulty column not nullable
ALTER TABLE interview_questions
    ALTER COLUMN difficulty SET NOT NULL;

--changeset liquibase:2
--comment: Update existing records with null difficulty values
UPDATE interview_questions
SET difficulty = 'Medium'
WHERE difficulty IS NULL;
