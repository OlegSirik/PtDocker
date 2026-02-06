-- Commission rates table for account-product commission configuration
CREATE SEQUENCE IF NOT EXISTS acc_commission_rates_seq START WITH 100 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS acc_commission_rates (
    id BIGINT PRIMARY KEY DEFAULT nextval('acc_commission_rates_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    account_id BIGINT NOT NULL REFERENCES acc_accounts(id),
    product_id INT NOT NULL,
    action VARCHAR(50) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    rate_value DECIMAL(10, 4),
    fixed_amount DECIMAL(12, 2),
    min_amount DECIMAL(12, 2),
    max_amount DECIMAL(12, 2),
    commission_min_rate DECIMAL(10, 4),
    commission_max_rate DECIMAL(10, 4),
    agd_number VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_acc_commission_rates_uk
    ON acc_commission_rates(account_id, product_id, action)
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_acc_commission_rates_tid ON acc_commission_rates(tid);
CREATE INDEX IF NOT EXISTS idx_acc_commission_rates_account_id ON acc_commission_rates(account_id);
CREATE INDEX IF NOT EXISTS idx_acc_commission_rates_product_id ON acc_commission_rates(product_id);
