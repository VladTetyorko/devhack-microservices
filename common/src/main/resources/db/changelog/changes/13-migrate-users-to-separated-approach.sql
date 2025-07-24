DO
$$
BEGIN
  -- Skip if we’ve already created the new user_profiles table
  IF
to_regclass('public.user_profiles') IS NOT NULL THEN
    RAISE NOTICE 'User split migration already applied, skipping.';
    RETURN;
END IF;

  -- Make sure the original users table is present
  IF
to_regclass('public.users') IS NULL THEN
    RAISE EXCEPTION 'Source table "users" not found; cannot migrate.';
END IF;

  -- Ensure pgcrypto is available
  CREATE
EXTENSION IF NOT EXISTS "pgcrypto";

  -- 1) Rename the old users table
ALTER TABLE users RENAME TO old_users;

-- 2) Create the new normalized tables
CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE user_profiles
(
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                   UUID         NOT NULL UNIQUE REFERENCES users (id),
    name                      VARCHAR(255) NOT NULL UNIQUE,
    cv_file_href              VARCHAR(512),
    cv_file_name              VARCHAR(255),
    cv_file_type              VARCHAR(100),
    cv_file_size              BIGINT,
    cv_storage_path           TEXT,
    cv_uploaded_at            TIMESTAMP WITHOUT TIME ZONE,
    cv_parsed_successfully    BOOLEAN,
    ai_usage_enabled          BOOLEAN,
    ai_preferred_language     VARCHAR(10),
    ai_cv_score               INTEGER,
    ai_skills_summary         TEXT,
    ai_suggested_improvements TEXT,
    is_visible_to_recruiters  BOOLEAN,
    created_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE user_auth_providers
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL REFERENCES users (id),
    provider      VARCHAR(20) NOT NULL,
    email         VARCHAR(255),
    password_hash VARCHAR(255),
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    UNIQUE (user_id, provider)
);

CREATE TABLE user_access
(
    id               UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL UNIQUE REFERENCES users (id),
    role             VARCHAR(100) NOT NULL,
    ai_usage_allowed BOOLEAN,
    account_locked   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- 3) Migrate the data

-- 3.1 Core users
INSERT INTO users (id, created_at, updated_at)
SELECT old_users.uuid    AS id,
       old_users.created_at,
       CURRENT_TIMESTAMP AS updated_at
FROM old_users;

-- 3.2 Profiles
INSERT INTO user_profiles (user_id, name,
                           cv_file_href, cv_file_name, cv_file_type, cv_file_size,
                           cv_storage_path, cv_uploaded_at, cv_parsed_successfully,
                           ai_usage_enabled, ai_preferred_language, ai_cv_score,
                           ai_skills_summary, ai_suggested_improvements,
                           is_visible_to_recruiters,
                           created_at, updated_at)
SELECT old_users.uuid       AS user_id,
       old_users.name,
       old_users.cv_file_href,
       old_users.cv_file_name,
       old_users.cv_file_type,
       old_users.cv_file_size,
       old_users.cv_storage_path,
       old_users.cv_uploaded_at,
       old_users.cv_parsed_successfully,
       old_users.ai_usage_enabled,
       old_users.ai_preferred_language,
       old_users.ai_cv_score,
       old_users.ai_skills_summary,
       old_users.ai_suggested_improvements,
       old_users.is_visible_to_recruiters,
       old_users.created_at AS created_at,
       CURRENT_TIMESTAMP    AS updated_at
FROM old_users;

-- 3.3 LOCAL credentials
INSERT INTO user_auth_providers (user_id, provider, email, password_hash,
                                 created_at, updated_at)
SELECT old_users.uuid       AS user_id,
       'LOCAL'              AS provider,
       old_users.email,
       old_users.password   AS password_hash,
       old_users.created_at AS created_at,
       CURRENT_TIMESTAMP    AS updated_at
FROM old_users;

-- 3.4 Admin settings
INSERT INTO user_access (user_id, role, ai_usage_allowed, account_locked,
                         created_at, updated_at)
SELECT old_users.uuid       AS user_id,
       old_users.role,
       old_users.ai_usage_allowed,
       FALSE                AS account_locked,
       old_users.created_at AS created_at,
       CURRENT_TIMESTAMP    AS updated_at
FROM old_users;

-- 4) Drop the legacy table
DROP TABLE old_users;

RAISE
NOTICE 'User split migration applied successfully.';
END
$$
LANGUAGE plpgsql;
