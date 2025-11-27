-- Add missing fields to acc_logins table according to documentation
-- Migration V12: Add full_name, position, and is_deleted fields

-- Add full_name field (required)
ALTER TABLE acc_logins
ADD COLUMN full_name VARCHAR(255);

-- Add position field (optional)
ALTER TABLE acc_logins
ADD COLUMN position VARCHAR(255);

-- Add is_deleted field with default false
ALTER TABLE acc_logins
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Update existing records to have a default full_name (can be updated later via API)
UPDATE acc_logins
SET full_name = user_login
WHERE full_name IS NULL;

-- Make full_name NOT NULL after setting default values
ALTER TABLE acc_logins
ALTER COLUMN full_name SET NOT NULL;

-- Rename password_hash to password to match documentation
ALTER TABLE acc_logins
RENAME COLUMN password_hash TO password;

-- Add comments to columns
COMMENT ON COLUMN acc_logins.full_name IS 'Full name of the user (ФИО)';
COMMENT ON COLUMN acc_logins.position IS 'Position/Title of the user (Должность)';
COMMENT ON COLUMN acc_logins.is_deleted IS 'Soft delete flag. True - inactive (deleted), false - active';
COMMENT ON COLUMN acc_logins.password IS 'Password hash for authentication';

-- Create index on is_deleted for faster queries
CREATE INDEX IF NOT EXISTS idx_acc_logins_is_deleted ON acc_logins(is_deleted);

-- Create composite index for filtering active users
CREATE INDEX IF NOT EXISTS idx_acc_logins_tid_active ON acc_logins(tid, is_deleted) WHERE is_deleted = FALSE;

