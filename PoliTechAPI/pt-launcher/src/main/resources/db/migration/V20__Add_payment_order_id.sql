ALTER TABLE policy_index
    ADD COLUMN IF NOT EXISTS payment_order_id VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_policy_index_order_id
    ON policy_index(payment_order_id);

