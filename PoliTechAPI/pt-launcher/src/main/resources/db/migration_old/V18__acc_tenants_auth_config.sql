-- Migration V18: Add auth_config JSONB to acc_tenants

ALTER TABLE acc_tenants ADD COLUMN IF NOT EXISTS auth_config JSONB;

