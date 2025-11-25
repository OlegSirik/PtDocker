-- Add password_hash field to acc_logins table for simple authentication
-- Migration V11

ALTER TABLE acc_logins
ADD COLUMN password_hash VARCHAR(255);

-- Add comment to the column
COMMENT ON COLUMN acc_logins.password_hash IS 'BCrypt hashed password for simple authentication';

