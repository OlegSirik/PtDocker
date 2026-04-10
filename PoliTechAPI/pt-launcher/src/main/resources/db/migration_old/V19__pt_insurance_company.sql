-- Страховые компании (per-tenant)
CREATE SEQUENCE IF NOT EXISTS pt_ins_company_seq START WITH 100 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS pt_insurance_company (
    id BIGINT PRIMARY KEY DEFAULT nextval('pt_ins_company_seq'::regclass),
    tid BIGINT NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(300) NOT NULL,
    status VARCHAR(30) NOT NULL,
    other_props JSONB,
    CONSTRAINT pt_insurance_company_tid_code_uk UNIQUE (tid, code)
);

CREATE INDEX IF NOT EXISTS idx_pt_insurance_company_tid ON pt_insurance_company (tid);

COMMENT ON TABLE pt_insurance_company IS 'Insurance company directory; extra fields in other_props as key-value JSON';

-- Начальная запись (tenant 100)
INSERT INTO pt_insurance_company (id, tid, code, name, status, other_props)
VALUES (1, 100, 'VSK', 'ВСК', 'ACTIVE', '{}'::jsonb);
