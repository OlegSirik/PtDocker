ALTER TABLE acc_tenants ADD COLUMN IF NOT EXISTS token_auth BOOLEAN DEFAULT TRUE;

ALTER TABLE acc_clients ALTER COLUMN client_id NAME TO idp_client_id;
