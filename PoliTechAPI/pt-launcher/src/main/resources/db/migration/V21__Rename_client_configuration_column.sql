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
