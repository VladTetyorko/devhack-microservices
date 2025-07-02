--liquibase formatted sql
CREATE
EXTENSION IF NOT EXISTS pgcrypto;

--changeset liquibase:0
--comment: Drop all tables if they exist
DROP TABLE IF EXISTS vacancy_response_tags CASCADE;
DROP TABLE IF EXISTS vacancy_responses CASCADE;
DROP TABLE IF EXISTS notes CASCADE;
DROP TABLE IF EXISTS answers CASCADE;
DROP TABLE IF EXISTS question_tags CASCADE;
DROP TABLE IF EXISTS interview_questions CASCADE;
DROP TABLE IF EXISTS tags CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS audits CASCADE;

--changeset liquibase:1
--comment: Create users table
CREATE TABLE users
(
    id         UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(50)      NOT NULL UNIQUE,
    email      VARCHAR(100)     NOT NULL UNIQUE,
    password   VARCHAR(255)     NOT NULL,
    created_at TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--changeset liquibase:2
--comment: Create tags table
CREATE TABLE tags
(
    id         UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(50)      NOT NULL UNIQUE,
    created_at TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--changeset liquibase:3
--comment: Create interview_questions table
CREATE TABLE interview_questions
(
    id            UUID PRIMARY KEY NOT NULL default gen_random_uuid(),
    user_id       UUID             NOT NULL,
    question_text TEXT             NOT NULL,
    difficulty    VARCHAR(20),
    source        VARCHAR(100),
    created_at    TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_question_user FOREIGN KEY (user_id) REFERENCES users (id)
);

--changeset liquibase:4
--comment: Create question_tags table
CREATE TABLE question_tags
(
    question_id UUID NOT NULL DEFAULT gen_random_uuid(),
    tag_id      UUID NOT NULL,
    PRIMARY KEY (question_id, tag_id),
    CONSTRAINT fk_qt_question FOREIGN KEY (question_id) REFERENCES interview_questions (id) ON DELETE CASCADE,
    CONSTRAINT fk_qt_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

--changeset liquibase:5
--comment: Create answers table
CREATE TABLE answers
(
    id               UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    user_id          UUID             NOT NULL,
    question_id      UUID             NOT NULL,
    answer_text      TEXT             NOT NULL,
    confidence_level INTEGER,
    created_at       TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_answer_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES interview_questions (id) ON DELETE CASCADE
);

--changeset liquibase:6
--comment: Create notes table
CREATE TABLE notes
(
    id          UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID             NOT NULL,
    question_id UUID             NOT NULL,
    note_text   TEXT             NOT NULL,
    created_at  TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_note_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_note_question FOREIGN KEY (question_id) REFERENCES interview_questions (id) ON DELETE CASCADE
);

--changeset liquibase:7
--comment: Create vacancy_responses table
CREATE TABLE vacancy_responses
(
    id              UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    user_id         UUID             NOT NULL,
    company_name    TEXT             NOT NULL,
    position        TEXT             NOT NULL,
    technologies    TEXT             NOT NULL,
    pros            TEXT,
    cons            TEXT,
    notes           TEXT,
    salary          VARCHAR(100),
    location        VARCHAR(100),
    interview_stage TEXT             NOT NULL,
    created_at      TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vacancy_user FOREIGN KEY (user_id) REFERENCES users (id)
);

--changeset liquibase:8
--comment: Create vacancy_response_tags table
CREATE TABLE vacancy_response_tags
(
    vacancy_response_id UUID NOT NULL DEFAULT gen_random_uuid(),
    tag_id              UUID NOT NULL,
    PRIMARY KEY (vacancy_response_id, tag_id),
    CONSTRAINT fk_vrt_vacancy FOREIGN KEY (vacancy_response_id) REFERENCES vacancy_responses (id) ON DELETE CASCADE,
    CONSTRAINT fk_vrt_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

--changeset liquibase:9
--comment: Create audits table
CREATE TABLE audits
(
    id             UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    operation_type VARCHAR(20)      NOT NULL,
    entity_type    VARCHAR(100)     NOT NULL,
    entity_id      VARCHAR(100),
    user_id        UUID             NOT NULL,
    timestamp      TIMESTAMP        NOT NULL,
    details        VARCHAR(1000),
    created_at     TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users (id)
);
