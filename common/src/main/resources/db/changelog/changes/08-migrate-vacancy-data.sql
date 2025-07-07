-- Migrate data from vacancy_responses to link with vacancies

-- Create temporary function to find matching vacancy
CREATE
OR REPLACE FUNCTION find_matching_vacancy(
    p_company_name VARCHAR,
    p_position VARCHAR,
    p_technologies VARCHAR
) RETURNS UUID AS $$
DECLARE
v_vacancy_id UUID;
BEGIN
    -- Try to find an exact match
SELECT id
INTO v_vacancy_id
FROM vacancies
WHERE company_name = p_company_name
  AND position = p_position
  AND (technologies = p_technologies OR (technologies IS NULL AND p_technologies IS NULL)) LIMIT 1;

-- If no exact match, try matching just company and position
IF
v_vacancy_id IS NULL THEN
SELECT id
INTO v_vacancy_id
FROM vacancies
WHERE company_name = p_company_name
  AND position = p_position LIMIT 1;
END IF;
    
    -- If still no match, try matching just company
    IF
v_vacancy_id IS NULL THEN
SELECT id
INTO v_vacancy_id
FROM vacancies
WHERE company_name = p_company_name LIMIT 1;
END IF;

RETURN v_vacancy_id;
END;
$$
LANGUAGE plpgsql;

-- Update vacancy_responses with matching vacancy_id
UPDATE vacancy_responses
SET vacancy_id = find_matching_vacancy(company_name, position, technologies)
WHERE vacancy_id IS NULL;

-- Create vacancies for responses without a match
INSERT INTO vacancies (id,
                       company_name,
                       position,
                       technologies,
                       source,
                       created_at,
                       updated_at)
SELECT gen_random_uuid(),
       vr.company_name,
       vr.position,
       vr.technologies,
       'Migrated from response',
       NOW(),
       NOW()
FROM vacancy_responses vr
WHERE vr.vacancy_id IS NULL;

-- Update remaining vacancy_responses with newly created vacancies
UPDATE vacancy_responses vr
SET vacancy_id = (SELECT id
                  FROM vacancies v
                  WHERE v.company_name = vr.company_name
                    AND v.position = vr.position
                    AND (v.technologies = vr.technologies OR (v.technologies IS NULL AND vr.technologies IS NULL))
                    AND v.source = 'Migrated from response'
    LIMIT 1
    )
WHERE vr.vacancy_id IS NULL;

-- Drop the temporary function
DROP FUNCTION find_matching_vacancy;

-- Make vacancy_id not nullable
ALTER TABLE vacancy_responses
    ALTER COLUMN vacancy_id SET NOT NULL;

-- Remove company-related columns
ALTER TABLE vacancy_responses DROP COLUMN company_name;
ALTER TABLE vacancy_responses DROP COLUMN position;
ALTER TABLE vacancy_responses DROP COLUMN technologies;