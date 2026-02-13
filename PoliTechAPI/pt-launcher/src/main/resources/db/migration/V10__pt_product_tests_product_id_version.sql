-- V10: pt_product_tests - use product_id + version_no (pt_products.id) instead of product_version_id
DROP TABLE IF EXISTS pt_product_tests;

CREATE TABLE pt_product_tests (
    product_id INTEGER NOT NULL,
    version_no INTEGER NOT NULL,
    quote_example JSONB,
    policy_example JSONB,
    PRIMARY KEY (product_id, version_no)
);

COMMENT ON TABLE pt_product_tests IS 'Cached JSON examples for quote and policy by product id and version';
