--liquibase formatted sql

--changeset liquibase:1
--comment: Add amount_of_arguments column to ai_prompts table
ALTER TABLE ai_prompts
    ADD COLUMN amount_of_arguments INTEGER;

--changeset liquibase:2
--comment: Add args_description column to ai_prompts table
ALTER TABLE ai_prompts
    ADD COLUMN args_description TEXT;