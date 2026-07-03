ALTER TABLE acc_tenants
    ADD COLUMN IF NOT EXISTS llm_config_enc TEXT;

COMMENT ON COLUMN acc_tenants.llm_config_enc IS 'AES-GCM encrypted tenant LLM config (API keys). NULL = LLM not configured.';
