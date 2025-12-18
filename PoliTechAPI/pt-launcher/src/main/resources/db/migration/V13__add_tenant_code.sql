-- Add code field to acc_tenants table for tenant identification
-- Migration V13: Add tenant code field

-- Add code field (unique identifier for tenant)
ALTER TABLE acc_tenants
ADD COLUMN code VARCHAR(50);

-- Generate codes for existing tenants based on their name (can be updated later)
UPDATE acc_tenants
SET code = lower(REGEXP_REPLACE(name, '[^a-zA-Z0-9]', '', 'g'))
WHERE code IS NULL;

-- Make code NOT NULL and UNIQUE after setting default values
ALTER TABLE acc_tenants
ALTER COLUMN code SET NOT NULL;

-- Add unique constraint on code
ALTER TABLE acc_tenants
ADD CONSTRAINT acc_tenants_code_uk UNIQUE (code);

-- Add comment to column
COMMENT ON COLUMN acc_tenants.code IS 'Unique code identifier for tenant (e.g., VSK, ALPHA)';

-- Create index on code for faster lookups
CREATE INDEX IF NOT EXISTS idx_acc_tenants_code ON acc_tenants(code);

