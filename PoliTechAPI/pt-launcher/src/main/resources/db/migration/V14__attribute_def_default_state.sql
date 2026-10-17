-- Soft status for schema attributes: ENABLED (default) / DISABLED
ALTER TABLE mt_attribute_def
    ADD COLUMN IF NOT EXISTS default_state VARCHAR(30) NOT NULL DEFAULT 'ENABLED';

UPDATE mt_attribute_def
SET default_state = 'ENABLED'
WHERE default_state IS NULL
   OR btrim(default_state) = ''
   OR upper(default_state) NOT IN ('ENABLED', 'DISABLED');

ALTER TABLE mt_attribute_def
    DROP CONSTRAINT IF EXISTS mt_attribute_def_default_state_chk;

ALTER TABLE mt_attribute_def
    ADD CONSTRAINT mt_attribute_def_default_state_chk
        CHECK (upper(default_state) IN ('ENABLED', 'DISABLED'));
