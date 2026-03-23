-- Ссылка продукта на страховую компанию (справочник pt_insurance_company)
ALTER TABLE pt_products
    ADD COLUMN IF NOT EXISTS ins_company_id BIGINT NULL
    REFERENCES pt_insurance_company (id);

CREATE INDEX IF NOT EXISTS idx_pt_products_ins_company_id ON pt_products (ins_company_id);

COMMENT ON COLUMN pt_products.ins_company_id IS 'FK to pt_insurance_company.id; optional';

UPDATE pt_products SET ins_company_id = 1;
