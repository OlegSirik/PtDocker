-- Full Database Schema
-- This file contains the complete database schema as it exists after all migrations V1-V25
-- Use this file to initialize a fresh database without running individual migrations

-- ============================================================================
-- SEQUENCES
-- ============================================================================

CREATE SEQUENCE IF NOT EXISTS pt_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS pt_lobs_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS pt_products_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS pt_product_versions_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS pt_files_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS pt_calculators_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS coefficient_data_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS policy_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS account_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS acc_accounts_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS acc_product_roles_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS acc_logins_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS acc_account_logins_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS acc_account_tokens_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS acc_client_configuration_seq START WITH 100 INCREMENT BY 1;

-- ============================================================================
-- TENANT AND CLIENT TABLES
-- ============================================================================

CREATE TABLE IF NOT EXISTS acc_tenants (
    id BIGINT PRIMARY KEY,
    name VARCHAR(250),
    code VARCHAR(50) NOT NULL,
    auth_type VARCHAR(20),
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT acc_tenants_code_uk UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS acc_clients (
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    id BIGINT PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL,
    default_account_id BIGINT,
    name VARCHAR(250),
    client_configuration_entity_id BIGINT,
    auth_type VARCHAR(10) NOT NULL DEFAULT 'CLIENT',
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_acc_clients_client_configuration UNIQUE (client_configuration_entity_id),
    CONSTRAINT acc_client_auth_client_id UNIQUE (tid, client_id)
);

CREATE TABLE IF NOT EXISTS acc_client_configuration (
    id BIGINT PRIMARY KEY DEFAULT nextval('acc_client_configuration_seq'),
    payment_gate VARCHAR(255),
    send_email_after_buy BOOLEAN DEFAULT FALSE,
    send_sms_after_buy BOOLEAN DEFAULT FALSE,
    pg_agent_number VARCHAR(255),
    pg_client_login VARCHAR(255),
    pg_client_password VARCHAR(255),
    client_employee_email VARCHAR(255),
    email_gate VARCHAR(255),
    email_login VARCHAR(255),
    email_password VARCHAR(255)
);

ALTER TABLE acc_clients
    ADD CONSTRAINT fk_acc_clients_client_configuration
        FOREIGN KEY (client_configuration_entity_id)
        REFERENCES acc_client_configuration(id);

-- ============================================================================
-- ACCOUNT TABLES
-- ============================================================================

CREATE TABLE IF NOT EXISTS acc_accounts (
    id BIGINT PRIMARY KEY DEFAULT nextval('acc_accounts_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    client_id BIGINT REFERENCES acc_clients(id),
    parent_id BIGINT REFERENCES acc_accounts(id),
    node_type VARCHAR(20) NOT NULL,
    name VARCHAR(250),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS acc_product_roles (
    id BIGINT PRIMARY KEY DEFAULT nextval('acc_product_roles_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    client_id BIGINT REFERENCES acc_clients(id),
    account_id BIGINT NOT NULL REFERENCES acc_accounts(id),
    role_product_id BIGINT NOT NULL,
    role_account_id BIGINT NOT NULL REFERENCES acc_accounts(id),
    is_deleted BOOLEAN DEFAULT FALSE,
    can_read BOOLEAN DEFAULT FALSE,
    can_quote BOOLEAN DEFAULT FALSE,
    can_policy BOOLEAN DEFAULT FALSE,
    can_addendum BOOLEAN DEFAULT FALSE,
    can_cancel BOOLEAN DEFAULT FALSE,
    can_prolongate BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT acc_product_roles_uk UNIQUE (account_id, role_product_id, role_account_id)
);

CREATE TABLE IF NOT EXISTS acc_logins (
    id BIGINT PRIMARY KEY DEFAULT nextval('acc_logins_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    user_login VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    full_name VARCHAR(255) NOT NULL,
    position VARCHAR(255),
    client_id BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_acc_logins_user_login_tid UNIQUE (user_login, tid)
);

CREATE TABLE IF NOT EXISTS acc_account_logins (
    id BIGINT PRIMARY KEY DEFAULT nextval('acc_account_logins_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    client_id BIGINT REFERENCES acc_clients(id),
    account_id BIGINT NOT NULL REFERENCES acc_accounts(id),
    user_login VARCHAR(255) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT acc_account_logins_uk UNIQUE (user_login, account_id)
);

ALTER TABLE acc_account_logins
    ADD CONSTRAINT fk_acc_account_logins_login
        FOREIGN KEY (tid, user_login)
        REFERENCES acc_logins(tid, user_login);

CREATE TABLE IF NOT EXISTS acc_account_tokens (
    id BIGINT PRIMARY KEY DEFAULT nextval('acc_account_tokens_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    client_id BIGINT NOT NULL REFERENCES acc_clients(id),
    account_id BIGINT NOT NULL REFERENCES acc_accounts(id),
    token VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT acc_account_tokens_uk UNIQUE (token, client_id)
);

-- ============================================================================
-- PRODUCT TABLES
-- ============================================================================

CREATE TABLE IF NOT EXISTS pt_lobs (
    id BIGINT PRIMARY KEY DEFAULT nextval('account_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    code VARCHAR(128) NOT NULL,
    name VARCHAR(512) NOT NULL,
    lob JSONB NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pt_lobs_uk UNIQUE (tid, code)
);

CREATE TABLE IF NOT EXISTS pt_products (
    id INT PRIMARY KEY DEFAULT nextval('account_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    lob VARCHAR(30) NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(250) NOT NULL,
    prod_version_no INTEGER,
    dev_version_no INTEGER,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS pt_product_versions (
    id INT PRIMARY KEY DEFAULT nextval('account_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    product_id INT NOT NULL,
    version_no INTEGER NOT NULL,
    product JSONB NOT NULL,
    CONSTRAINT pt_product_versions_uk UNIQUE (product_id, version_no)
);

CREATE TABLE IF NOT EXISTS pt_calculators (
    id INT PRIMARY KEY DEFAULT nextval('account_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    product_id INT NOT NULL,
    product_code VARCHAR(30) NOT NULL,
    version_no INT NOT NULL,
    package_no INT NOT NULL,
    calculator JSONB NOT NULL,
    CONSTRAINT pt_calculators_uk UNIQUE (product_id, version_no, package_no)
);

CREATE TABLE IF NOT EXISTS coefficient_data (
    id INT PRIMARY KEY DEFAULT nextval('account_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    calculator_id INT NOT NULL,
    coefficient_code VARCHAR(128) NOT NULL,
    col0 VARCHAR(255), col1 VARCHAR(255), col2 VARCHAR(255), col3 VARCHAR(255), col4 VARCHAR(255),
    col5 VARCHAR(255), col6 VARCHAR(255), col7 VARCHAR(255), col8 VARCHAR(255), col9 VARCHAR(255), col10 VARCHAR(255),
    result_value FLOAT
);

CREATE TABLE IF NOT EXISTS pt_number_generators (
    id INT PRIMARY KEY DEFAULT nextval('account_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    product_code VARCHAR(100) NOT NULL,
    mask VARCHAR(255) NOT NULL,
    reset_policy VARCHAR(20) NOT NULL,
    max_value INT NOT NULL DEFAULT 999999,
    last_reset DATE NOT NULL DEFAULT CURRENT_DATE,
    current_value INT NOT NULL DEFAULT 0,
    xor_mask VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS pt_files (
    id BIGINT PRIMARY KEY DEFAULT nextval('account_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    file_type VARCHAR(30),
    file_desc VARCHAR(300),
    product_code VARCHAR(30),
    package_code INT DEFAULT 0,
    file_body BYTEA,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- ============================================================================
-- POLICY TABLES
-- ============================================================================

CREATE TABLE IF NOT EXISTS policy_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    cid BIGINT NOT NULL DEFAULT 1,
    policy JSONB NOT NULL
);

CREATE TABLE IF NOT EXISTS policy_index (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    draft_id VARCHAR(30),
    policy_nr VARCHAR(30),
    version_no INTEGER,
    top_version BOOLEAN DEFAULT FALSE,
    product_code VARCHAR(30),
    product_version_no INTEGER NOT NULL DEFAULT 1,
    create_date TIMESTAMP WITH TIME ZONE,
    issue_date TIMESTAMP WITH TIME ZONE,
    payment_date TIMESTAMP WITH TIME ZONE,
    start_date TIMESTAMP WITH TIME ZONE,
    end_date TIMESTAMP WITH TIME ZONE,
    user_account_id BIGINT,
    client_account_id BIGINT,
    version_status VARCHAR(30),
    policy_status VARCHAR(20) NOT NULL,
    payment_order_id VARCHAR(100),
    ins_company VARCHAR(10),
    ph_digest VARCHAR(250),
    io_digest VARCHAR(250),
    user_login VARCHAR(250),
    premium NUMERIC(18, 2),
    agent_kv_percent NUMERIC(18, 2),
    agent_kv_amount NUMERIC(18, 2),
    FOREIGN KEY (id) REFERENCES policy_data(id) ON DELETE CASCADE,
    CONSTRAINT policy_index_policy_nr UNIQUE (tid, policy_nr)
);

-- ============================================================================
-- INDEXES
-- ============================================================================

-- Account indexes
CREATE INDEX IF NOT EXISTS idx_acc_clients_tid ON acc_clients(tid);
CREATE INDEX IF NOT EXISTS idx_acc_clients_client_id ON acc_clients(client_id);
CREATE INDEX IF NOT EXISTS idx_acc_accounts_tid ON acc_accounts(tid);
CREATE INDEX IF NOT EXISTS idx_acc_accounts_client_id ON acc_accounts(client_id);
CREATE INDEX IF NOT EXISTS idx_acc_accounts_parent_id ON acc_accounts(parent_id);
CREATE INDEX IF NOT EXISTS idx_acc_product_roles_tid ON acc_product_roles(tid);
CREATE INDEX IF NOT EXISTS idx_acc_product_roles_account_id ON acc_product_roles(account_id);
CREATE INDEX IF NOT EXISTS idx_acc_product_roles_role_account_id ON acc_product_roles(role_account_id);
CREATE INDEX IF NOT EXISTS idx_acc_logins_user_login_tid ON acc_logins(user_login, tid);
CREATE INDEX IF NOT EXISTS idx_acc_logins_tid ON acc_logins(tid);
CREATE INDEX IF NOT EXISTS idx_acc_account_logins_tid ON acc_account_logins(tid);
CREATE INDEX IF NOT EXISTS idx_acc_account_logins_client_id ON acc_account_logins(client_id);
CREATE INDEX IF NOT EXISTS idx_acc_account_logins_account_id ON acc_account_logins(account_id);
CREATE INDEX IF NOT EXISTS idx_acc_account_logins_user_login ON acc_account_logins(user_login);
CREATE INDEX IF NOT EXISTS idx_acc_account_tokens_tid ON acc_account_tokens(tid);
CREATE INDEX IF NOT EXISTS idx_acc_account_tokens_client_id ON acc_account_tokens(client_id);
CREATE INDEX IF NOT EXISTS idx_acc_account_tokens_account_id ON acc_account_tokens(account_id);
CREATE INDEX IF NOT EXISTS idx_acc_account_tokens_token ON acc_account_tokens(token);
CREATE INDEX IF NOT EXISTS idx_acc_tenants_code ON acc_tenants(code);

-- Product indexes
CREATE INDEX IF NOT EXISTS coefficient_data_calc_code_idx ON coefficient_data(calculator_id, coefficient_code);

-- Policy indexes
CREATE INDEX IF NOT EXISTS policy_index_draft_id_idx ON policy_index(draft_id, top_version);
CREATE INDEX IF NOT EXISTS policy_index_policy_nr_idx ON policy_index(policy_nr, top_version);
CREATE INDEX IF NOT EXISTS policy_index_user_account_idx ON policy_index(user_account_id);
CREATE INDEX IF NOT EXISTS idx_policy_index_order_id ON policy_index(payment_order_id);

-- ============================================================================
-- VIEWS
-- ============================================================================

CREATE OR REPLACE VIEW pt_lobs_vw AS
SELECT 
    t.id,
    t.code,
    var_data ->> 'varCode' as var_code,
    var_data ->> 'varName' as var_name,
    var_data ->> 'varPath' as var_path,
    var_data ->> 'varType' as var_type,
    var_data ->> 'varValue' as var_value,
    var_data ->> 'varDataType' as var_data_type
FROM pt_lobs t,
jsonb_array_elements(t.lob -> 'mpVars') AS var_data
WHERE t.lob -> 'mpVars' IS NOT NULL;

-- ============================================================================
-- COMMENTS
-- ============================================================================

COMMENT ON COLUMN acc_logins.full_name IS 'Full name of the user (ФИО)';
COMMENT ON COLUMN acc_logins.position IS 'Position/Title of the user (Должность)';
COMMENT ON COLUMN acc_logins.is_deleted IS 'Soft delete flag. True - inactive (deleted), false - active';
COMMENT ON COLUMN acc_logins.password IS 'Password hash for authentication';
COMMENT ON COLUMN acc_tenants.code IS 'Unique code identifier for tenant (e.g., VSK, ALPHA)';

CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_code_not_deleted_pt_lobs 
ON pt_lobs(code) 
WHERE is_Deleted = false;