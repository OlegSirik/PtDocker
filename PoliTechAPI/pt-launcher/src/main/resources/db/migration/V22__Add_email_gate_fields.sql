-- Add email gateway configuration fields to client configuration table
ALTER TABLE acc_client_configuration
    ADD COLUMN IF NOT EXISTS email_gate VARCHAR(255);

ALTER TABLE acc_client_configuration
    ADD COLUMN IF NOT EXISTS email_login VARCHAR(255);

ALTER TABLE acc_client_configuration
    ADD COLUMN IF NOT EXISTS email_password VARCHAR(255);
