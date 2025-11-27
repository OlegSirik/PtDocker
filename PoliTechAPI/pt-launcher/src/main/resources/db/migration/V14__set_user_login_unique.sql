-- Add code field to acc_tenants table for tenant identification
-- Migration V14: Add tenant code field

-- Add unique constraint on login
ALTER TABLE acc_logins
ADD CONSTRAINT user_login UNIQUE (user_login);

-- Create index on code for faster lookups
CREATE INDEX IF NOT EXISTS idx_acc_logins_login ON acc_logins (user_login);

