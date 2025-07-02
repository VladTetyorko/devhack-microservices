--liquibase formatted sql

--changeset liquibase:1
--comment: Add is_cheating column to answers table
ALTER TABLE answers
    ADD COLUMN is_cheating BOOLEAN;