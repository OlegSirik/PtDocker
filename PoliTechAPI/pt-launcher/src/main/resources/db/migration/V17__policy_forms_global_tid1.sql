-- Policy forms are global hardcoded catalog (tid=1 only). Drop LOB binding table.

DROP TABLE IF EXISTS pt_lob_policy_forms;

DELETE FROM pt_policy_forms WHERE tid <> 1;

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
