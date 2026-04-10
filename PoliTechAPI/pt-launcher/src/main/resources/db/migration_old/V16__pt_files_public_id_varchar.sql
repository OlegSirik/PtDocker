-- Migration V16: Change pt_files.public_id from UUID to VARCHAR to match entity

ALTER TABLE pt_files ALTER COLUMN public_id TYPE VARCHAR(255) USING public_id::text;
