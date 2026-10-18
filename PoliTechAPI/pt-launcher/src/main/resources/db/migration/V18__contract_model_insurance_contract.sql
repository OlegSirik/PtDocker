-- Seed mt_contract_model + copy mt_attribute_def from tenant 1 → 10

INSERT INTO mt_contract_model (tid, code, name)
SELECT 1, 'INSURANCE_CONTRACT', 'Договор страхования'
WHERE NOT EXISTS (
    SELECT 1 FROM mt_contract_model WHERE tid = 1 AND code = 'INSURANCE_CONTRACT'
);

INSERT INTO mt_contract_model (tid, code, name)
SELECT 10, 'INSURANCE_CONTRACT', 'Договор страхования'
WHERE NOT EXISTS (
    SELECT 1 FROM mt_contract_model WHERE tid = 10 AND code = 'INSURANCE_CONTRACT'
);

-- Copy full attribute tree from tid=1 to tid=10 (skip rows that already exist by document_id+var_code)
CREATE TEMP TABLE mt_attr_copy_map (
    old_id BIGINT PRIMARY KEY,
    new_id BIGINT NOT NULL
);

INSERT INTO mt_attr_copy_map (old_id, new_id)
SELECT s.id, nextval('mt_seq')
FROM mt_attribute_def s
WHERE s.tenant_id = 1
  AND NOT EXISTS (
      SELECT 1
      FROM mt_attribute_def x
      WHERE x.tenant_id = 10
        AND x.document_id = s.document_id
        AND x.var_code = s.var_code
  );

INSERT INTO mt_attribute_def (
    id,
    parent_id,
    tenant_id,
    document_id,
    var_code,
    var_name,
    var_path,
    var_ord,
    var_type,
    var_data_type,
    var_value,
    var_list,
    is_system,
    default_state
)
SELECT
    m.new_id,
    NULL,
    10,
    s.document_id,
    s.var_code,
    s.var_name,
    s.var_path,
    s.var_ord,
    s.var_type,
    s.var_data_type,
    s.var_value,
    s.var_list,
    s.is_system,
    COALESCE(s.default_state, 'ENABLED')
FROM mt_attribute_def s
JOIN mt_attr_copy_map m ON m.old_id = s.id;

-- Remap parent_id: new copies first, else already-existing tid=10 parent by document_id+var_code
UPDATE mt_attribute_def child
SET parent_id = COALESCE(pm.new_id, existing_parent.id)
FROM mt_attr_copy_map m
JOIN mt_attribute_def s ON s.id = m.old_id
LEFT JOIN mt_attr_copy_map pm ON pm.old_id = s.parent_id
LEFT JOIN mt_attribute_def src_parent ON src_parent.id = s.parent_id
LEFT JOIN mt_attribute_def existing_parent
    ON existing_parent.tenant_id = 10
   AND existing_parent.document_id = src_parent.document_id
   AND existing_parent.var_code = src_parent.var_code
WHERE child.id = m.new_id
  AND s.parent_id IS NOT NULL;

DROP TABLE IF EXISTS mt_attr_copy_map;
