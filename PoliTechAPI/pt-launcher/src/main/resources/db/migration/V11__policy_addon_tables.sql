-- V11: Policy Add-on tables - providers, pricelists, addon products, addon policies
CREATE SEQUENCE IF NOT EXISTS po_addon_seq START WITH 100 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS po_providers (
    id BIGINT PRIMARY KEY DEFAULT nextval('po_addon_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    name VARCHAR(300) NOT NULL,
    status VARCHAR(30) NOT NULL,
    execution_mode VARCHAR(30) NOT NULL
);

CREATE UNIQUE INDEX po_providers_tid_name_active_uk ON po_providers (tid, name) WHERE status = 'ACTIVE';

CREATE TABLE IF NOT EXISTS po_pricelists (
    id BIGINT PRIMARY KEY DEFAULT nextval('po_addon_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    provider_id BIGINT NOT NULL REFERENCES po_providers(id),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(300) NOT NULL,
    category_code VARCHAR(50),
    price NUMERIC(18, 2) NOT NULL,
    amount_free BIGINT NOT NULL DEFAULT 0,
    amount_booked BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    product JSONB
);

CREATE TABLE IF NOT EXISTS po_addon_products (
    id BIGINT PRIMARY KEY DEFAULT nextval('po_addon_seq'),
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    product_id INT NOT NULL REFERENCES pt_products(id),
    addon_id BIGINT NOT NULL REFERENCES po_pricelists(id),
    preconditions JSONB,
    CONSTRAINT po_addon_products_uk UNIQUE (tid, product_id, addon_id)
);

CREATE TABLE IF NOT EXISTS po_addon_policies (
    id BIGINT PRIMARY KEY DEFAULT nextval('po_addon_seq'),
    policy_id UUID NOT NULL REFERENCES policy_index(id),
    addon_id BIGINT NOT NULL REFERENCES po_pricelists(id),
    addon_number VARCHAR(50),
    addon_status VARCHAR(30) NOT NULL,
    amount BIGINT NOT NULL,
    price NUMERIC(18, 2) NOT NULL,
    total_amount NUMERIC(18, 2) NOT NULL,
    policy_data JSONB
);

COMMENT ON TABLE po_providers IS 'Add-on service providers (LOCAL or API execution)';
COMMENT ON TABLE po_pricelists IS 'Add-on pricelist items with availability';
COMMENT ON TABLE po_addon_products IS 'Product-to-addon mapping with preconditions';
COMMENT ON TABLE po_addon_policies IS 'Policy add-ons (NEW, BOOKED, PAID)';
