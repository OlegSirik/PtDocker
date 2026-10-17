-- Catalog of LK policy-entry pages (Angular pageId) + LOB assignment

CREATE TABLE pt_policy_forms (
    id            BIGINT PRIMARY KEY DEFAULT nextval('pt_seq'),
    tid           BIGINT       NOT NULL REFERENCES acc_tenants (id),
    page_id       VARCHAR(80)  NOT NULL,
    title         VARCHAR(250) NOT NULL,
    io_type       VARCHAR(30),
    record_status VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE' REFERENCES ref_record_statuses (code),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX pt_policy_forms_tid_page_uk
    ON pt_policy_forms (tid, page_id)
    WHERE record_status = 'ACTIVE';

CREATE TABLE pt_lob_policy_forms (
    tid      BIGINT      NOT NULL REFERENCES acc_tenants (id),
    lob_code VARCHAR(30) NOT NULL,
    page_id  VARCHAR(80) NOT NULL,
    CONSTRAINT pt_lob_policy_forms_pkey PRIMARY KEY (tid, lob_code, page_id)
);

CREATE INDEX pt_lob_policy_forms_lob_idx ON pt_lob_policy_forms (tid, lob_code);

-- Seed for template tenant tid=1 (ACC / NS, GAD / COMPLEX_PROTECTION)
INSERT INTO pt_policy_forms (tid, page_id, title, io_type, record_status)
SELECT 1, 'ns-policy', 'Несчастный случай', 'PERSON', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM pt_policy_forms WHERE tid = 1 AND page_id = 'ns-policy' AND record_status = 'ACTIVE'
);

INSERT INTO pt_policy_forms (tid, page_id, title, io_type, record_status)
SELECT 1, 'policy_gad001', 'Страхование техники', 'DEVICE', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM pt_policy_forms WHERE tid = 1 AND page_id = 'policy_gad001' AND record_status = 'ACTIVE'
);

INSERT INTO pt_lob_policy_forms (tid, lob_code, page_id)
SELECT 1, 'ACC', 'ns-policy'
WHERE NOT EXISTS (
    SELECT 1 FROM pt_lob_policy_forms WHERE tid = 1 AND lob_code = 'ACC' AND page_id = 'ns-policy'
);

INSERT INTO pt_lob_policy_forms (tid, lob_code, page_id)
SELECT 1, 'GAD', 'policy_gad001'
WHERE NOT EXISTS (
    SELECT 1 FROM pt_lob_policy_forms WHERE tid = 1 AND lob_code = 'GAD' AND page_id = 'policy_gad001'
);
