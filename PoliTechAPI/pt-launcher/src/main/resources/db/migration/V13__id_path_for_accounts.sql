-- V13: Add id_path columns for account hierarchy navigation

-- 1. Add id_path column to core tables
ALTER TABLE acc_accounts
    ADD COLUMN IF NOT EXISTS id_path VARCHAR(300);

ALTER TABLE acc_product_roles
    ADD COLUMN IF NOT EXISTS id_path VARCHAR(300);

ALTER TABLE policy_index
    ADD COLUMN IF NOT EXISTS id_path VARCHAR(300);

-- 2. Populate id_path for existing acc_accounts using recursive CTE (root -> leaf)
WITH RECURSIVE acc_tree AS (
    SELECT
        id,
        parent_id,
        CAST(id AS VARCHAR(300)) AS path
    FROM acc_accounts
    WHERE parent_id IS NULL

    UNION ALL

    SELECT
        a.id,
        a.parent_id,
        (t.path || '.' || a.id)::VARCHAR(300) AS path
    FROM acc_accounts a
    JOIN acc_tree t ON a.parent_id = t.id
)
UPDATE acc_accounts a
SET id_path = t.path
FROM acc_tree t
WHERE a.id = t.id;

-- 3. Copy id_path to acc_product_roles (by account_id)
UPDATE acc_product_roles pr
SET id_path = a.id_path
FROM acc_accounts a
WHERE pr.account_id = a.id
  AND pr.id_path IS NULL;

-- 4. Copy id_path to policy_index (by user_account_id)
UPDATE policy_index p
SET id_path = a.id_path
FROM acc_accounts a
WHERE p.user_account_id = a.id
  AND p.id_path IS NULL;

ALTER TABLE acc_accounts
    ALTER COLUMN id_path SET NOT NULL;

ALTER TABLE acc_product_roles
    ALTER COLUMN id_path SET NOT NULL;

ALTER TABLE policy_index
    ALTER COLUMN id_path SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_acc_accounts_id_path
    ON acc_accounts(id_path);

CREATE INDEX IF NOT EXISTS idx_acc_product_roles_id_path
    ON acc_product_roles(id_path);

CREATE INDEX IF NOT EXISTS idx_policy_index_id_path
    ON policy_index(id_path);

CREATE INDEX IF NOT EXISTS idx_acc_accounts_id_path_like_idx
    ON acc_accounts(id_path varchar_pattern_ops);

CREATE INDEX IF NOT EXISTS idx_acc_product_roles_id_path_like_idx
    ON acc_product_roles(id_path varchar_pattern_ops);

CREATE INDEX IF NOT EXISTS policy_index_id_path_like_idx
  ON policy_index (id_path varchar_pattern_ops); 