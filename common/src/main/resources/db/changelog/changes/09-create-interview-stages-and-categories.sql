BEGIN;

--
-- 1. Categories table
--
CREATE TABLE interview_stage_category
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    code       VARCHAR(50)  NOT NULL UNIQUE,
    label      VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

--
-- 2. Stages table
--
CREATE TABLE interview_stage
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    code          VARCHAR(50)  NOT NULL UNIQUE,
    label         VARCHAR(100) NOT NULL,
    order_index   INTEGER,
    color_class   VARCHAR(30),
    icon_class    VARCHAR(50),
    active        BOOLEAN,
    final_stage   BOOLEAN,
    internal_only BOOLEAN,
    category_id   UUID         NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ,
    CONSTRAINT fk_stage_category
        FOREIGN KEY (category_id)
            REFERENCES interview_stage_category (id)
            ON DELETE RESTRICT
);

-- Insert categories
INSERT INTO interview_stage_category (code, label)
VALUES ('INITIAL', 'Initial Stage'),
       ('SOFT_SKILLS', 'Soft Skills Assessment'),
       ('TECHNICAL', 'Technical Assessment'),
       ('STAKEHOLDER', 'Stakeholder Review'),
       ('FINAL', 'Final Stage');

-- Insert stages
INSERT INTO interview_stage (code, label, order_index, active, final_stage, category_id)
SELECT 'APPLIED', 'Applied', 1, true, false, id
FROM interview_stage_category
WHERE code = 'INITIAL';

INSERT INTO interview_stage (code, label, order_index, active, final_stage, category_id)
SELECT 'PRE_SCREEN', 'Pre-Screen', 2, true, false, id
FROM interview_stage_category
WHERE code = 'INITIAL';

INSERT INTO interview_stage (code, label, order_index, active, final_stage, category_id)
SELECT 'SCREENING', 'Screening', 3, true, false, id
FROM interview_stage_category
WHERE code = 'SOFT_SKILLS';

INSERT INTO interview_stage (code, label, order_index, active, final_stage, category_id)
SELECT 'TECHNICAL_INTERVIEW', 'Technical Interview', 4, true, false, id
FROM interview_stage_category
WHERE code = 'TECHNICAL';

INSERT INTO interview_stage (code, label, order_index, active, final_stage, category_id)
SELECT 'PM_INTERVIEW', 'PM Interview', 5, true, false, id
FROM interview_stage_category
WHERE code = 'STAKEHOLDER';

INSERT INTO interview_stage (code, label, order_index, active, final_stage, category_id)
SELECT 'STAKEHOLDER_INTERVIEW', 'Stakeholder Interview', 6, true, false, id
FROM interview_stage_category
WHERE code = 'STAKEHOLDER';

INSERT INTO interview_stage (code, label, order_index, active, final_stage, category_id)
SELECT 'OFFER', 'Offer', 7, true, false, id
FROM interview_stage_category
WHERE code = 'FINAL';

INSERT INTO interview_stage (code, label, order_index, active, final_stage, category_id)
SELECT 'REJECTED', 'Rejected', 8, true, true, id
FROM interview_stage_category
WHERE code = 'FINAL';

INSERT INTO interview_stage (code, label, order_index, active, final_stage, category_id)
SELECT 'ACCEPTED', 'Accepted', 9, true, true, id
FROM interview_stage_category
WHERE code = 'FINAL';

-- Add new column
ALTER TABLE vacancy_responses
    ADD COLUMN interview_stage_id UUID;

-- Migrate data
UPDATE vacancy_responses vr
SET interview_stage_id = stage.id
FROM interview_stage stage
WHERE (
          (vr.interview_stage = 'APPLIED' AND stage.code = 'APPLIED')
              OR (vr.interview_stage = 'PRE_SCREEN' AND stage.code = 'PRE_SCREEN')
              OR (vr.interview_stage = 'SCREENING' AND stage.code = 'SCREENING')
              OR (vr.interview_stage = 'TECHNICAL_INTERVIEW' AND stage.code = 'TECHNICAL_INTERVIEW')
              OR (vr.interview_stage = 'PM_INTERVIEW' AND stage.code = 'PM_INTERVIEW')
              OR (vr.interview_stage = 'STAKEHOLDER_INTERVIEW' AND stage.code = 'STAKEHOLDER_INTERVIEW')
              OR (vr.interview_stage = 'OFFER' AND stage.code = 'OFFER')
              OR (vr.interview_stage = 'REJECTED' AND stage.code = 'REJECTED')
              OR (vr.interview_stage = 'ACCEPTED' AND stage.code = 'ACCEPTED')
          );

CREATE TABLE vacancy_response_stage
(
    vacancy_response_id UUID        NOT NULL,
    interview_stage_id  UUID        NOT NULL,
    changed_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (vacancy_response_id, interview_stage_id, changed_at),
    CONSTRAINT fk_vrstag_vr
        FOREIGN KEY (vacancy_response_id)
            REFERENCES vacancy_responses (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_vrstag_stage
        FOREIGN KEY (interview_stage_id)
            REFERENCES interview_stage (id)
            ON DELETE CASCADE
);

-- Insert initial records into history table
INSERT INTO vacancy_response_stage (vacancy_response_id, interview_stage_id, changed_at)
SELECT vr.id         as vacancy_response_id,
       vr.interview_stage_id,
       vr.created_at as changed_at
FROM vacancy_responses vr
WHERE vr.interview_stage_id IS NOT NULL;

-- Add foreign key constraint after data migration
ALTER TABLE vacancy_responses
    ADD CONSTRAINT fk_vacancyresponse_stage
        FOREIGN KEY (interview_stage_id)
            REFERENCES interview_stage (id)
            ON DELETE SET NULL;

-- Drop deprecated column after successful migration
ALTER TABLE vacancy_responses
    DROP COLUMN IF EXISTS interview_stage;

COMMIT;