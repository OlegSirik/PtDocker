-- V24: Greenfield baseline — full DDL for a new database (structure only, no reference/seed data).
-- Consolidates logical schema from V1 through V22 after application of V23 archival renames on an existing DB;
-- can also be executed alone on an empty database (baseline Flyway separately if needed).
--
-- Requires: CREATE EXTENSION pgcrypto; (for gen_random_uuid on clients that use it elsewhere).

--CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =============================================================================
-- SEQUENCES
-- =============================================================================

CREATE TABLE ref_record_statuses (
    code VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

INSERT INTO ref_record_statuses (code, name) VALUES
('ACTIVE', 'Активна'),
('SUSPENDED', 'Приостановлена'),
('DELETED', 'Удалена');

CREATE TABLE ref_storage_types (
    code VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

INSERT INTO ref_storage_types (code, name) VALUES
('FS', 'Файловая система'),
('DB', 'База данных');

CREATE TABLE ref_tenant_auth_types (
    code VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

INSERT INTO ref_tenant_auth_types (code, name) VALUES
('LOCAL_JWT', 'Local JWT'),
('JWT', 'JWT'),
('HEADERS', 'Headers'),
('NONE', 'None'),
('APIKEY', 'API Key'),
('KEYCLOAK', 'Keycloak');


CREATE TABLE ref_client_auth_levels (
    code VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

INSERT INTO ref_client_auth_levels (code, name) VALUES
('CLIENT', 'Клиент'),
('USER', 'Пользователь');

CREATE TABLE ref_client_auth_types (
    code VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

INSERT INTO ref_client_auth_types (code, name) VALUES
('HEADERS', 'Headers'),
('APIKEY', 'API Key');

CREATE TABLE ref_account_node_types (
    code VARCHAR(30) PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

INSERT INTO ref_account_node_types (code, name) VALUES
('TENANT', 'Tenant'),
('CLIENT', 'Client'),
('GROUP', 'Group'),
('ACCOUNT', 'Account'),
('SUB', 'Sub'),
('SYS_ADMIN', 'System Administrator'),
('TNT_ADMIN', 'Tenant Administrator'),
('GROUP_ADMIN', 'Group Administrator'),
('PRODUCT_ADMIN', 'Product Administrator');

-- =============================================================================
-- TENANTS & CLIENT CONFIG
-- =============================================================================

CREATE SEQUENCE IF NOT EXISTS acc_seq START WITH 1000 INCREMENT BY 1;

CREATE TABLE acc_tenants (
    -- +++
    id           BIGINT PRIMARY KEY DEFAULT nextval('acc_seq'),
    name         VARCHAR(250) NOT NULL,
    code         VARCHAR(30) NOT NULL,
    auth_type    VARCHAR(30) REFERENCES ref_tenant_auth_types (code),
    storage_type VARCHAR(30) REFERENCES ref_storage_types (code),
    storage_config JSONB,
    auth_config    JSONB,

    record_status   VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' REFERENCES ref_record_statuses (code),
    created_at   TIMESTAMP not null DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP          DEFAULT CURRENT_TIMESTAMP    
);

CREATE UNIQUE INDEX acc_tenants_code_uk ON acc_tenants (code) where record_status = 'ACTIVE';

CREATE TABLE acc_clients (
    -- ----
    id                          BIGINT PRIMARY KEY DEFAULT nextval('acc_seq'),
    tid                         BIGINT NOT NULL REFERENCES acc_tenants (id),
    auth_client_id              VARCHAR(255) NOT NULL,
    name                        VARCHAR(250),
    default_account_id          BIGINT,
    client_configuration_entity_id BIGINT,
    auth_level                  VARCHAR(10) NOT NULL REFERENCES ref_client_auth_levels (code),
    auth_type                   VARCHAR(30) REFERENCES ref_client_auth_types (code),

    record_status               VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' REFERENCES ref_record_statuses (code),
    created_at                  TIMESTAMP not null DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP    
);

CREATE UNIQUE INDEX acc_clients_auth_client_id_uk ON acc_clients (tid, auth_client_id) where record_status = 'ACTIVE';


CREATE TABLE acc_client_configuration (
    id               BIGINT PRIMARY KEY DEFAULT nextval('acc_seq'),
    payment_gate     VARCHAR(255),
    send_email_after_buy BOOLEAN DEFAULT FALSE,
    send_sms_after_buy   BOOLEAN DEFAULT FALSE,
    pg_agent_number  VARCHAR(255),
    pg_client_login  VARCHAR(255),
    pg_client_password VARCHAR(255),
    client_employee_email VARCHAR(255),
    email_gate       VARCHAR(255),
    email_login      VARCHAR(255),
    email_password   VARCHAR(255)
);

CREATE TABLE acc_accounts (
    id          BIGINT PRIMARY KEY DEFAULT nextval('acc_seq'),
    tid         BIGINT NOT NULL REFERENCES acc_tenants (id),
    client_id   BIGINT REFERENCES acc_clients (id),
    parent_id   BIGINT REFERENCES acc_accounts (id),
    node_type   VARCHAR(20) NOT NULL REFERENCES ref_account_node_types (code),
    name        VARCHAR(250),
    id_path     VARCHAR(300) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE acc_product_roles (
    id              BIGINT PRIMARY KEY DEFAULT nextval('acc_seq'),
    tid             BIGINT NOT NULL REFERENCES acc_tenants (id),
    client_id       BIGINT REFERENCES acc_clients (id),
    account_id      BIGINT NOT NULL REFERENCES acc_accounts (id),
    role_product_id BIGINT NOT NULL,
    role_account_id BIGINT NOT NULL REFERENCES acc_accounts (id),
    id_path         VARCHAR(300) NOT NULL,
    can_read        BOOLEAN   DEFAULT FALSE,
    can_quote       BOOLEAN   DEFAULT FALSE,
    can_policy      BOOLEAN   DEFAULT FALSE,
    can_addendum    BOOLEAN   DEFAULT FALSE,
    can_cancel      BOOLEAN   DEFAULT FALSE,
    can_prolongate  BOOLEAN   DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT acc_product_roles_uk UNIQUE (account_id, role_product_id, role_account_id)
);

CREATE TABLE acc_logins (
    id         BIGINT PRIMARY KEY DEFAULT nextval('acc_seq'),
    tid        BIGINT NOT NULL REFERENCES acc_tenants (id),
    user_login VARCHAR(255) NOT NULL,
    password   VARCHAR(255),
    full_name  VARCHAR(255) NOT NULL,
    position   VARCHAR(255),
    client_id  BIGINT,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' REFERENCES ref_record_statuses (code),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_acc_logins_user_login_tid UNIQUE (user_login, tid)
);

CREATE TABLE acc_account_logins (
    id          BIGINT PRIMARY KEY DEFAULT nextval('acc_seq'),
    tid         BIGINT NOT NULL REFERENCES acc_tenants (id),
    client_id   BIGINT REFERENCES acc_clients (id),
    account_id  BIGINT NOT NULL REFERENCES acc_accounts (id),
    user_login  VARCHAR(255) NOT NULL,
    is_default  BOOLEAN   DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT acc_account_logins_uk UNIQUE (user_login, account_id),
    CONSTRAINT fk_acc_account_logins_login FOREIGN KEY (tid, user_login)
        REFERENCES acc_logins (tid, user_login)
);

CREATE TABLE acc_account_tokens (
    id         BIGINT PRIMARY KEY DEFAULT nextval('acc_seq'),
    tid        BIGINT NOT NULL REFERENCES acc_tenants (id),
    client_id  BIGINT NOT NULL REFERENCES acc_clients (id),
    account_id BIGINT NOT NULL REFERENCES acc_accounts (id),
    token      VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT acc_account_tokens_uk UNIQUE (token, client_id)
);

CREATE TABLE acc_commission_rates (
    id                    BIGINT PRIMARY KEY DEFAULT nextval('acc_seq'),
    tid                   BIGINT NOT NULL REFERENCES acc_tenants (id),
    account_id            BIGINT NOT NULL REFERENCES acc_accounts (id),
    product_id            BIGINT NOT NULL,
    action                VARCHAR(50) NOT NULL,
    rate_value            DECIMAL(10, 4),
    fixed_amount          DECIMAL(12, 2),
    min_amount            DECIMAL(12, 2),
    max_amount            DECIMAL(12, 2),
    commission_min_rate   DECIMAL(10, 4),
    commission_max_rate   DECIMAL(10, 4),
    agd_number            VARCHAR(100),
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);




-- =============================================================================
-- PRODUCT DOMAIN
-- =============================================================================

CREATE SEQUENCE IF NOT EXISTS pt_seq START WITH 1000 INCREMENT BY 1;

CREATE TABLE pt_insurance_company (
    id            BIGINT PRIMARY KEY DEFAULT nextval('pt_seq'),
    tid           BIGINT NOT NULL,
    code          VARCHAR(30) NOT NULL,
    name          VARCHAR(250) NOT NULL,
    status        VARCHAR(30) NOT NULL,
    other_props   JSONB,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' REFERENCES ref_record_statuses (code),
    created_at  TIMESTAMP not null DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP    
);

CREATE UNIQUE INDEX pt_insurance_company_tid_code_uk ON pt_insurance_company (tid, code) where record_status = 'ACTIVE';

CREATE TABLE pt_lobs (
    id         BIGINT PRIMARY KEY DEFAULT nextval('pt_seq'),
    tid        BIGINT NOT NULL REFERENCES acc_tenants (id),
    code       VARCHAR(30) NOT NULL,
    name       VARCHAR(250) NOT NULL,
    lob        JSONB        NOT NULL,
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' REFERENCES ref_record_statuses (code),
    created_at  TIMESTAMP not null DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP    
);

CREATE UNIQUE INDEX pt_lobs_tid_code_uk ON pt_lobs (tid, code) where record_status = 'ACTIVE';

CREATE TABLE pt_products (
    id              BIGINT PRIMARY KEY DEFAULT nextval('pt_seq'),
    tid             BIGINT NOT NULL REFERENCES acc_tenants (id),
    lob             VARCHAR(30) NOT NULL,
    code            VARCHAR(30) NOT NULL,
    name            VARCHAR(250) NOT NULL,
    prod_version_no BIGINT,
    dev_version_no  BIGINT,
    ins_company_id  BIGINT REFERENCES pt_insurance_company (id),
    record_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' REFERENCES ref_record_statuses (code),
    created_at  TIMESTAMP not null DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP    
);

CREATE UNIQUE INDEX pt_products_tid_code_uk ON pt_products (tid, code) where record_status = 'ACTIVE';

CREATE TABLE pt_product_versions (
    id         BIGINT PRIMARY KEY DEFAULT nextval('pt_seq'),
    tid        BIGINT NOT NULL REFERENCES acc_tenants (id),
    product_id BIGINT NOT NULL,
    version_no BIGINT NOT NULL,
    product    JSONB NOT NULL,
    created_at  TIMESTAMP not null DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP    
);

CREATE UNIQUE INDEX pt_product_versions_product_id_version_no_uk ON pt_product_versions (product_id, version_no);

CREATE TABLE pt_calculators (
    id            BIGINT PRIMARY KEY DEFAULT nextval('pt_seq'),
    tid           BIGINT NOT NULL REFERENCES acc_tenants (id),
    product_id    BIGINT NOT NULL,
    product_code  VARCHAR(30) NOT NULL,
    version_no    BIGINT NOT NULL,
    package_no    VARCHAR(30) NOT NULL,
    calculator    JSONB NOT NULL,
    CONSTRAINT pt_calculators_uk UNIQUE (product_id, version_no, package_no)
);

CREATE TABLE coefficient_data (
    id                BIGINT PRIMARY KEY DEFAULT nextval('pt_seq'),
    tid               BIGINT NOT NULL REFERENCES acc_tenants (id),
    calculator_id     BIGINT NOT NULL,
    coefficient_code  VARCHAR(128) NOT NULL,
    col0 VARCHAR(255), col1 VARCHAR(255), col2 VARCHAR(255), col3 VARCHAR(255), col4 VARCHAR(255),
    col5 VARCHAR(255), col6 VARCHAR(255), col7 VARCHAR(255), col8 VARCHAR(255), col9 VARCHAR(255), col10 VARCHAR(255),
    result_value DOUBLE PRECISION
);

CREATE INDEX coefficient_data_calc_code_idx ON coefficient_data (calculator_id, coefficient_code);

CREATE TABLE pt_number_generators (
    id            BIGINT PRIMARY KEY DEFAULT nextval('pt_seq'),
    tid           BIGINT NOT NULL REFERENCES acc_tenants (id),
    last_reset    DATE NOT NULL DEFAULT CURRENT_DATE,
    current_value BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE pt_files (
    id           BIGINT PRIMARY KEY DEFAULT nextval('pt_seq'),
    tid          BIGINT NOT NULL REFERENCES acc_tenants (id),
    public_id    VARCHAR(255) NOT NULL,
    filename     VARCHAR(512),
    content_type VARCHAR(255),
    size         BIGINT,
    file_body    BYTEA,
    CONSTRAINT pt_files_public_id_uk UNIQUE (public_id)
);

CREATE TABLE pt_product_tests (
    product_id     BIGINT NOT NULL,
    version_no     BIGINT NOT NULL,
    quote_example  JSONB,
    policy_example JSONB,
    PRIMARY KEY (product_id, version_no)
);

-- =============================================================================
-- POLICY STORAGE
-- =============================================================================

CREATE SEQUENCE IF NOT EXISTS policy_seq START WITH 1000 INCREMENT BY 1;

CREATE TABLE policy_data (
    id     BIGINT PRIMARY KEY DEFAULT nextval('policy_seq'),
    tid    BIGINT NOT NULL REFERENCES acc_tenants (id),
    cid    BIGINT NOT NULL DEFAULT 1,
    policy JSONB NOT NULL
);

CREATE TABLE policy_index (
    id                 BIGINT PRIMARY KEY,
    public_id          UUID NOT NULL,
    tid                BIGINT NOT NULL REFERENCES acc_tenants (id),
    draft_id           VARCHAR(30),
    policy_nr          VARCHAR(30),
    version_no         BIGINT,
    top_version        BOOLEAN DEFAULT FALSE,
    product_code       VARCHAR(30),
    product_version_no BIGINT NOT NULL DEFAULT 1,
    create_date        TIMESTAMP WITH TIME ZONE,
    issue_date         TIMESTAMP WITH TIME ZONE,
    payment_date       TIMESTAMP WITH TIME ZONE,
    start_date         TIMESTAMP WITH TIME ZONE,
    end_date           TIMESTAMP WITH TIME ZONE,
    user_account_id    BIGINT,
    client_account_id  BIGINT,
    id_path            VARCHAR(300) NOT NULL,
    data_scope         VARCHAR(30),
    policy_status      VARCHAR(20) NOT NULL,
    payment_order_id   VARCHAR(100),
    ins_company        VARCHAR(10),
    ph_digest          VARCHAR(250),
    io_digest          VARCHAR(250),
    user_login         VARCHAR(250),
    premium            NUMERIC(18, 2),
    agent_kv_percent   NUMERIC(18, 2),
    agent_kv_amount    NUMERIC(18, 2),
    CONSTRAINT policy_index_id_fkey FOREIGN KEY (id) REFERENCES policy_data (id) ON DELETE CASCADE,
    CONSTRAINT policy_index_policy_nr UNIQUE (tid, policy_nr)
);

CREATE UNIQUE INDEX policy_index_public_id_uk ON policy_index (public_id);

-- =============================================================================
-- ADD-ON (POLICY OPTIONS)
-- =============================================================================
CREATE SEQUENCE IF NOT EXISTS po_addon_seq START WITH 1000 INCREMENT BY 1;

CREATE TABLE po_providers (
    id             BIGINT PRIMARY KEY DEFAULT nextval('po_addon_seq'),
    tid            BIGINT NOT NULL REFERENCES acc_tenants (id),
    name           VARCHAR(300) NOT NULL,
    record_status  VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' REFERENCES ref_record_statuses (code),
    execution_mode VARCHAR(30) NOT NULL
);

CREATE UNIQUE INDEX po_providers_tid_name_active_uk ON po_providers (tid, name) WHERE record_status = 'ACTIVE';

CREATE TABLE po_pricelists (
    id             BIGINT PRIMARY KEY DEFAULT nextval('po_addon_seq'),
    tid            BIGINT NOT NULL REFERENCES acc_tenants (id),
    provider_id    BIGINT NOT NULL REFERENCES po_providers (id),
    code           VARCHAR(50) NOT NULL,
    name           VARCHAR(300) NOT NULL,
    category_code  VARCHAR(50),
    price          NUMERIC(18, 2) NOT NULL,
    amount_free    BIGINT NOT NULL DEFAULT 0,
    amount_booked  BIGINT NOT NULL DEFAULT 0,
    record_status  VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' REFERENCES ref_record_statuses (code),
    product        JSONB
);

CREATE TABLE po_addon_products (
    id           BIGINT PRIMARY KEY DEFAULT nextval('po_addon_seq'),
    tid          BIGINT NOT NULL REFERENCES acc_tenants (id),
    product_id   BIGINT NOT NULL REFERENCES pt_products (id),
    addon_id     BIGINT NOT NULL REFERENCES po_pricelists (id),
    preconditions JSONB,
    CONSTRAINT po_addon_products_uk UNIQUE (tid, product_id, addon_id)
);

CREATE TABLE po_addon_policies (
    id           BIGINT PRIMARY KEY DEFAULT nextval('po_addon_seq'),
    policy_id    BIGINT NOT NULL REFERENCES policy_index (id),
    addon_id     BIGINT NOT NULL REFERENCES po_pricelists (id),
    addon_number VARCHAR(50),
    addon_status VARCHAR(30) NOT NULL,
    amount       BIGINT NOT NULL,
    price        NUMERIC(18, 2) NOT NULL,
    total_amount NUMERIC(18, 2) NOT NULL,
    policy_data  JSONB
);

COMMENT ON TABLE po_providers IS 'Add-on service providers (LOCAL or API execution)';
COMMENT ON TABLE po_pricelists IS 'Add-on pricelist items with availability';
COMMENT ON TABLE po_addon_products IS 'Product-to-addon mapping with preconditions';
COMMENT ON TABLE po_addon_policies IS 'Policy add-ons (NEW, BOOKED, PAID)';

-- =============================================================================
-- CONTRACT MODEL & ATTRIBUTE TREE (post-V21)
-- =============================================================================
CREATE SEQUENCE IF NOT EXISTS mt_seq START WITH 1000 INCREMENT BY 1;

CREATE TABLE mt_contract_model (
    id   BIGINT PRIMARY KEY DEFAULT nextval('mt_seq'),
    tid  BIGINT NOT NULL,
    code VARCHAR(50),
    name VARCHAR(300),
    CONSTRAINT mt_contract_model_tid_code_uk UNIQUE (tid, code),
    CONSTRAINT mt_contract_model_tid_name_uk UNIQUE (tid, name)
);

CREATE TABLE mt_attribute_def (
    id               BIGINT         NOT NULL DEFAULT nextval('mt_seq'),
    parent_id        BIGINT,
    tenant_id        BIGINT         NOT NULL,
    document_id      VARCHAR(30)    NOT NULL,
    var_code         VARCHAR(50)    NOT NULL,
    var_name         VARCHAR(300)   NOT NULL,
    var_path         VARCHAR(500)   NOT NULL,
    var_ord          BIGINT         NOT NULL DEFAULT 0,
    var_type         VARCHAR(30)    NOT NULL,
    var_cardinality  VARCHAR(10)    NOT NULL DEFAULT 'SINGLE',
    var_data_type    VARCHAR(20)    NOT NULL,
    var_value        VARCHAR(500),
    var_cdm          VARCHAR(500),
    var_list         VARCHAR(100),
    code             VARCHAR(50),
    name             VARCHAR(250),
    is_system        BOOLEAN        NOT NULL DEFAULT FALSE,
    CONSTRAINT mt_attribute_def_pkey PRIMARY KEY (id),
    CONSTRAINT uk_attribute_def UNIQUE (tenant_id, document_id, var_code),
    CONSTRAINT mt_attribute_def_parent_fk FOREIGN KEY (parent_id) REFERENCES mt_attribute_def (id)
);

CREATE TABLE pt_refdata (
    ref_code VARCHAR(50),
    md_code  VARCHAR(50) NOT NULL,
    md_name  VARCHAR(100) NOT NULL,
    PRIMARY KEY (ref_code, md_code)
);


