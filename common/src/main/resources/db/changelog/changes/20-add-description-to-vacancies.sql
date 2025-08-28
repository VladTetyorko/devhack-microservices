-- Add description column to vacancies table
ALTER TABLE vacancies
    ADD COLUMN IF NOT EXISTS description TEXT;