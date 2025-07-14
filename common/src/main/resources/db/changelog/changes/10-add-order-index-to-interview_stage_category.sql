BEGIN;

ALTER TABLE interview_stage_category
    ADD COLUMN order_index INT NOT NULL DEFAULT 0;

UPDATE interview_stage_category
SET order_index = 100
WHERE code = 'INITIAL';

UPDATE interview_stage_category
SET order_index = 200
WHERE code = 'SOFT_SKILLS';

UPDATE interview_stage_category
SET order_index = 300
WHERE code = 'TECHNICAL';

UPDATE interview_stage_category
SET order_index = 400
WHERE code = 'STAKEHOLDER';

UPDATE interview_stage_category
SET order_index = 500
WHERE code = 'FINAL';

END;