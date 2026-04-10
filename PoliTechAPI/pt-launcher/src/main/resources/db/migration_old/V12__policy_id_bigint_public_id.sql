-- V12: policy_index - add public_id UUID, change id to BIGINT; policy_data - change id to BIGINT
-- policy_seq already exists in V1

-- Step 1: Create mapping from old UUID to new BIGINT
CREATE TEMP TABLE policy_id_mapping AS
SELECT id AS old_uuid, nextval('policy_seq') AS new_id FROM policy_data;

-- Step 2: policy_index - add public_id, copy from id
ALTER TABLE policy_index ADD COLUMN public_id UUID;
UPDATE policy_index SET public_id = id;
ALTER TABLE policy_index ALTER COLUMN public_id SET NOT NULL;
CREATE UNIQUE INDEX policy_index_public_id_uk ON policy_index(public_id);

-- Step 3: policy_index - add id_new BIGINT
ALTER TABLE policy_index ADD COLUMN id_new BIGINT;
UPDATE policy_index pi SET id_new = m.new_id
FROM policy_id_mapping m WHERE pi.id = m.old_uuid;

-- Step 4: policy_data - add id_new BIGINT
ALTER TABLE policy_data ADD COLUMN id_new BIGINT;
UPDATE policy_data pd SET id_new = m.new_id
FROM policy_id_mapping m WHERE pd.id = m.old_uuid;

-- Step 5: Drop FK from po_addon_policies to policy_index
ALTER TABLE po_addon_policies DROP CONSTRAINT IF EXISTS po_addon_policies_policy_id_fkey;

-- Step 6: Drop FK from policy_index to policy_data
ALTER TABLE policy_index DROP CONSTRAINT IF EXISTS policy_index_id_fkey;

-- Step 7: policy_index - replace id
ALTER TABLE policy_index DROP COLUMN id;
ALTER TABLE policy_index RENAME COLUMN id_new TO id;
ALTER TABLE policy_index ALTER COLUMN id SET NOT NULL;
ALTER TABLE policy_index ADD PRIMARY KEY (id);

-- Step 8: policy_data - replace id
ALTER TABLE policy_data DROP COLUMN id;
ALTER TABLE policy_data RENAME COLUMN id_new TO id;
ALTER TABLE policy_data ALTER COLUMN id SET NOT NULL;
ALTER TABLE policy_data ALTER COLUMN id SET DEFAULT nextval('policy_seq');
ALTER TABLE policy_data ADD PRIMARY KEY (id);

-- Step 9: policy_index - add FK to policy_data
ALTER TABLE policy_index ADD CONSTRAINT policy_index_id_fkey
    FOREIGN KEY (id) REFERENCES policy_data(id) ON DELETE CASCADE;

-- Step 10: po_addon_policies - change policy_id to BIGINT
ALTER TABLE po_addon_policies ADD COLUMN policy_id_new BIGINT;
UPDATE po_addon_policies ap SET policy_id_new = pi.id
FROM policy_index pi WHERE pi.public_id = ap.policy_id;
ALTER TABLE po_addon_policies DROP COLUMN policy_id;
ALTER TABLE po_addon_policies RENAME COLUMN policy_id_new TO policy_id;
ALTER TABLE po_addon_policies ALTER COLUMN policy_id SET NOT NULL;
ALTER TABLE po_addon_policies ADD CONSTRAINT po_addon_policies_policy_id_fkey
    FOREIGN KEY (policy_id) REFERENCES policy_index(id);
