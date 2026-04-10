-- V8: introduce auth_level column and relax auth_type
-- acc_clients initially had:
--   auth_type VARCHAR(10) NOT NULL DEFAULT 'CLIENT'
-- After this migration:
--   auth_level stores the previous auth_type value (NOT NULL)
--   auth_type is widened and becomes nullable, without default

ALTER TABLE acc_clients
    ADD COLUMN IF NOT EXISTS auth_level VARCHAR(10);

-- Migrate existing auth_type values into auth_level (only where not already set)
UPDATE acc_clients
SET auth_level = auth_type
WHERE auth_type IS NOT NULL
  AND (auth_level IS NULL OR auth_level = '');

-- Ensure auth_level is mandatory
ALTER TABLE acc_clients
    ALTER COLUMN auth_level SET NOT NULL;

-- Widen auth_type and make it nullable, removing default
ALTER TABLE acc_clients
    ALTER COLUMN auth_type TYPE VARCHAR(30);

ALTER TABLE acc_clients
    ALTER COLUMN auth_type DROP NOT NULL;

ALTER TABLE acc_clients
    ALTER COLUMN auth_type DROP DEFAULT;

-- Clear auth_type values so that new semantics can be applied later
UPDATE acc_clients
SET auth_type = NULL;

