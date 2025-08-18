--liquibase formatted sql

--changeset liquibase:1
--comment: Enable ltree extension
CREATE
EXTENSION IF NOT EXISTS ltree;

--changeset liquibase:2
--comment: Add parent_id column to tags table
ALTER TABLE tags
    ADD COLUMN parent_id UUID;
ALTER TABLE tags
    ADD CONSTRAINT fk_tag_parent FOREIGN KEY (parent_id) REFERENCES tags (id);

--changeset liquibase:3
--comment: Add slug column to tags table
ALTER TABLE tags
    ADD COLUMN slug VARCHAR(100);

--changeset liquibase:4
--comment: Add path column to tags table
ALTER TABLE tags
    ADD COLUMN path ltree;

--changeset liquibase:6
--comment: Generate slugs for existing tags
UPDATE tags
SET slug = CASE
               WHEN LEFT (LOWER (REGEXP_REPLACE(name, '[^a-zA-Z0-9]+', '_', 'g')), 1) ~ '^[0-9]'
    THEN 't_' || LOWER (REGEXP_REPLACE(name, '[^a-zA-Z0-9]+', '_', 'g'))
    ELSE LOWER (REGEXP_REPLACE(name, '[^a-zA-Z0-9]+', '_', 'g'))
END;

--changeset liquibase:7
--comment: Set path for existing root tags (no parent)
UPDATE tags
SET path = slug::ltree
WHERE parent_id IS NULL;

--changeset liquibase:8
--comment: Create unique constraint on parent_id and slug combination
ALTER TABLE tags
    ADD CONSTRAINT uk_tags_parent_slug UNIQUE (parent_id, slug);

--changeset liquibase:9
--comment: Create GiST index on path for efficient hierarchical queries
CREATE INDEX idx_tags_path_gist ON tags USING GIST (path);

--changeset liquibase:10
--comment: Create index on parent_id for efficient parent lookups
CREATE INDEX idx_tags_parent_id ON tags (parent_id);

--changeset liquibase:11
--comment: Create index on slug for efficient slug lookups
CREATE INDEX idx_tags_slug ON tags (slug);