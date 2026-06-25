ALTER TABLE policy_index
    ADD COLUMN document_format VARCHAR(50) NOT NULL DEFAULT 'INSURANCE_CONTRACT';

COMMENT ON COLUMN policy_index.document_format IS 'Wire format / StdPolicy mapper id (e.g. INSURANCE_CONTRACT)';
