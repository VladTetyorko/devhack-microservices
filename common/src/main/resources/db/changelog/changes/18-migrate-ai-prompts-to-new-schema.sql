--liquibase formatted sql

--changeset liquibase:1
--comment: Rename code column to key in ai_prompts table
ALTER TABLE ai_prompts RENAME COLUMN code TO key;

--changeset liquibase:2
--comment: Rename prompt column to user_template in ai_prompts table
ALTER TABLE ai_prompts RENAME COLUMN prompt TO user_template;

--changeset liquibase:3
--comment: Rename active column to enabled in ai_prompts table
ALTER TABLE ai_prompts RENAME COLUMN active TO enabled;

--changeset liquibase:4
--comment: Add system_template column to ai_prompts table
ALTER TABLE ai_prompts
    ADD COLUMN system_template TEXT;

--changeset liquibase:5
--comment: Add args_schema column to ai_prompts table
ALTER TABLE ai_prompts
    ADD COLUMN args_schema JSONB NOT NULL DEFAULT '{}';

--changeset liquibase:6
--comment: Add defaults column to ai_prompts table
ALTER TABLE ai_prompts
    ADD COLUMN defaults JSONB NOT NULL DEFAULT '{}';

--changeset liquibase:7
--comment: Add model column to ai_prompts table
ALTER TABLE ai_prompts
    ADD COLUMN model VARCHAR(50) NOT NULL DEFAULT 'gpt-3.5-turbo';

--changeset liquibase:8
--comment: Add parameters column to ai_prompts table
ALTER TABLE ai_prompts
    ADD COLUMN parameters JSONB NOT NULL DEFAULT '{}';

--changeset liquibase:9
--comment: Add response_contract column to ai_prompts table
ALTER TABLE ai_prompts
    ADD COLUMN response_contract JSONB;

--changeset liquibase:10
--comment: Add version column to ai_prompts table
ALTER TABLE ai_prompts
    ADD COLUMN version INTEGER NOT NULL DEFAULT 1;

--changeset liquibase:11
--comment: Drop language column from ai_prompts table (moved to parameters)
ALTER TABLE ai_prompts
DROP
COLUMN IF EXISTS language;

--changeset liquibase:12
--comment: Drop amount_of_arguments column from ai_prompts table (replaced by args_schema)
ALTER TABLE ai_prompts
DROP
COLUMN IF EXISTS amount_of_arguments;

--changeset liquibase:13
--comment: Drop args_description column from ai_prompts table (replaced by args_schema)
ALTER TABLE ai_prompts
DROP
COLUMN IF EXISTS args_description;