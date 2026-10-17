-- Seed policy forms for every tenant that has matching LOBs (V15 only seeded tid=1)

INSERT INTO pt_policy_forms (tid, page_id, title, io_type, record_status)
SELECT t.id, 'ns-policy', 'Несчастный случай', 'PERSON', 'ACTIVE'
FROM acc_tenants t
WHERE t.record_status = 'ACTIVE'
  AND EXISTS (
      SELECT 1 FROM pt_lobs l
      WHERE l.tid = t.id AND l.code = 'ACC' AND l.record_status = 'ACTIVE'
  )
  AND NOT EXISTS (
      SELECT 1 FROM pt_policy_forms f
      WHERE f.tid = t.id AND f.page_id = 'ns-policy' AND f.record_status = 'ACTIVE'
  );

INSERT INTO pt_policy_forms (tid, page_id, title, io_type, record_status)
SELECT t.id, 'policy_gad001', 'Страхование техники', 'DEVICE', 'ACTIVE'
FROM acc_tenants t
WHERE t.record_status = 'ACTIVE'
  AND EXISTS (
      SELECT 1 FROM pt_lobs l
      WHERE l.tid = t.id AND l.code = 'GAD' AND l.record_status = 'ACTIVE'
  )
  AND NOT EXISTS (
      SELECT 1 FROM pt_policy_forms f
      WHERE f.tid = t.id AND f.page_id = 'policy_gad001' AND f.record_status = 'ACTIVE'
  );

INSERT INTO pt_lob_policy_forms (tid, lob_code, page_id)
SELECT l.tid, l.code, 'ns-policy'
FROM pt_lobs l
WHERE l.code = 'ACC' AND l.record_status = 'ACTIVE'
  AND EXISTS (
      SELECT 1 FROM pt_policy_forms f
      WHERE f.tid = l.tid AND f.page_id = 'ns-policy' AND f.record_status = 'ACTIVE'
  )
  AND NOT EXISTS (
      SELECT 1 FROM pt_lob_policy_forms x
      WHERE x.tid = l.tid AND x.lob_code = 'ACC' AND x.page_id = 'ns-policy'
  );

INSERT INTO pt_lob_policy_forms (tid, lob_code, page_id)
SELECT l.tid, l.code, 'policy_gad001'
FROM pt_lobs l
WHERE l.code = 'GAD' AND l.record_status = 'ACTIVE'
  AND EXISTS (
      SELECT 1 FROM pt_policy_forms f
      WHERE f.tid = l.tid AND f.page_id = 'policy_gad001' AND f.record_status = 'ACTIVE'
  )
  AND NOT EXISTS (
      SELECT 1 FROM pt_lob_policy_forms x
      WHERE x.tid = l.tid AND x.lob_code = 'GAD' AND x.page_id = 'policy_gad001'
  );
