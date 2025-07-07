-- Create table vacancies

CREATE TABLE vacancies
(
    id             UUID PRIMARY KEY,
    company_name   VARCHAR(255) NOT NULL,
    position       VARCHAR(255) NOT NULL,
    technologies   VARCHAR(255),
    source         VARCHAR(255),
    url            VARCHAR(512),
    applied_at     TIMESTAMP,
    status         VARCHAR(50),
    contact_person VARCHAR(255),
    contact_email  VARCHAR(255),
    deadline       TIMESTAMP,
    remote_allowed BOOLEAN   DEFAULT FALSE,
    created_at     TIMESTAMP DEFAULT now(),
    updated_at     TIMESTAMP DEFAULT now()
);


-- Update vacancy_responses table to add vacancy_id and remove company-related columns

-- Add vacancy_id column
ALTER TABLE vacancy_responses
    ADD COLUMN vacancy_id UUID;


-- Create a foreign key constraint
ALTER TABLE vacancy_responses
    ADD CONSTRAINT fk_vacancy_responses_vacancy
        FOREIGN KEY (vacancy_id) REFERENCES vacancies (id);

-- Make vacancy_id not nullable after data migration
-- This should be done after migrating data
-- ALTER TABLE vacancy_responses ALTER COLUMN vacancy_id SET NOT NULL;

-- Remove company-related columns after data migration
-- This should be done after migrating data
-- ALTER TABLE vacancy_responses DROP COLUMN company_name;
-- ALTER TABLE vacancy_responses DROP COLUMN position;
-- ALTER TABLE vacancy_responses DROP COLUMN technologies;

-- Note: The commented out parts should be executed after data migration
-- to ensure data integrity. A separate migration script should be created
-- for this purpose after the data has been migrated.