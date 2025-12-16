-- Rename client configuration column to align with JPA mapping
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'acc_clients'
          AND column_name = 'client_configuration_id'
    )
    AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'acc_clients'
          AND column_name = 'client_configuration_entity_id'
    ) THEN
        ALTER TABLE acc_clients
            RENAME COLUMN client_configuration_id TO client_configuration_entity_id;
    END IF;
END
$$;

ALTER TABLE pt_files ALTER COLUMN file_type DROP NOT NULL;
ALTER TABLE pt_files ALTER COLUMN file_desc DROP NOT NULL;
ALTER TABLE pt_files ALTER COLUMN product_code DROP NOT NULL;
ALTER TABLE pt_files ALTER COLUMN package_code DROP NOT NULL;
ALTER TABLE pt_files ADD COLUMN tid BIGINT;

UPDATE pt_files
SET tid = 1
WHERE tid IS NULL;

ALTER TABLE pt_files
ALTER COLUMN tid SET NOT NULL;
