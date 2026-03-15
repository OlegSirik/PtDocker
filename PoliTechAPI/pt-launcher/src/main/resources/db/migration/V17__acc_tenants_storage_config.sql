-- Migration V17: Add storage_config JSONB to acc_tenants

ALTER TABLE acc_tenants ADD COLUMN IF NOT EXISTS storage_config JSONB;
