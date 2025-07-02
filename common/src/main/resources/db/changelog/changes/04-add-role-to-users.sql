--liquibase formatted sql

--changeset liquibase:1
--comment: Add role column to users table
ALTER TABLE users
    ADD COLUMN role VARCHAR(50);