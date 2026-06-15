CREATE TABLE pt_refdicts (
    tid  BIGINT       NOT NULL,
    code VARCHAR(50)  NOT NULL,
    name VARCHAR(300) NOT NULL,
    CONSTRAINT pt_refdicts_pkey PRIMARY KEY (tid, code)
);

ALTER TABLE pt_refdata ADD COLUMN tid BIGINT;

UPDATE pt_refdata SET tid = 100;

ALTER TABLE pt_refdata ALTER COLUMN tid SET NOT NULL;
ALTER TABLE pt_refdata ALTER COLUMN tid SET DEFAULT 100;

ALTER TABLE pt_refdata DROP CONSTRAINT pt_refdata_pkey;
ALTER TABLE pt_refdata ADD CONSTRAINT pt_refdata_pkey PRIMARY KEY (tid, ref_code, md_code);

INSERT INTO pt_refdicts (tid, code, name)
SELECT DISTINCT tid, ref_code, ref_code
FROM pt_refdata;

-- Template tenant (tid=1) for copying to new tenants
INSERT INTO pt_refdicts (tid, code, name)
SELECT 1, code, name
FROM pt_refdicts
WHERE tid = 100
ON CONFLICT (tid, code) DO NOTHING;

INSERT INTO pt_refdata (tid, ref_code, md_code, md_name)
SELECT 1, ref_code, md_code, md_name
FROM pt_refdata
WHERE tid = 100
ON CONFLICT (tid, ref_code, md_code) DO NOTHING;
