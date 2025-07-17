BEGIN TRANSACTION;

ALTER TABLE USERS

    ADD COLUMN cv_file_href              VARCHAR(255),
    ADD COLUMN cv_file_name              VARCHAR,
    ADD COLUMN cv_file_type              VARCHAR(50),
    ADD COLUMN cv_file_size              BIGINT,
    ADD COLUMN cv_storage_path           TEXT,
    ADD COLUMN cv_uploaded_at            TIMESTAMP,
    ADD COLUMN ai_usage_allowed          BOOLEAN    DEFAULT FALSE,
    ADD COLUMN ai_usage_enabled          BOOLEAN    DEFAULT FALSE,
    ADD COLUMN ai_preferred_language     VARCHAR(4) DEFAULT 'en',
    ADD COLUMN ai_cv_score               INT,
    ADD COLUMN ai_skills_summary         TEXT,
    ADD COLUMN ai_suggested_improvements TEXT,
    ADD COLUMN cv_parsed_successfully    BOOLEAN    DEFAULT FALSE,
    ADD COLUMN is_visible_to_recruiters  BOOLEAN    DEFAULT TRUE;

UPDATE USERS
SET role = 'USER'
WHERE role IS NULL;

CREATE TABLE ai_prompts_categories
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    name        VARCHAR(100) NOT NULL
);

CREATE TABLE ai_prompts
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    prompt      TEXT         NOT NULL,
    language    VARCHAR(10)      DEFAULT 'en',
    active      BOOLEAN          DEFAULT TRUE,
    created_at  TIMESTAMP        DEFAULT now(),
    updated_at  TIMESTAMP,
    category_id UUID REFERENCES ai_prompts_categories (id) ON DELETE CASCADE
);

CREATE TABLE ai_prompt_usage_logs
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID REFERENCES users (id),
    prompt_id  UUID REFERENCES ai_prompts (id),
    input      TEXT,
    result     TEXT,
    created_at TIMESTAMP        DEFAULT now()
);

COMMIT;
