DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_schema = 'public'
                     AND table_name = 'vacancies'
                     AND column_name = 'applied_at') THEN
            ALTER TABLE public.vacancies
                RENAME COLUMN applied_at TO open_at;
        END IF;
    END
$$;

UPDATE vacancies
SET status = 'OPEN'
WHERE status = 'APPLIED';
UPDATE vacancies
SET status = 'OPEN'
WHERE status = 'OPENED';
UPDATE vacancies
SET status = 'OPEN'
WHERE status = 'FILLED';