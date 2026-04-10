-- V9: pt_product_tests - cached quote/policy JSON examples per product version
CREATE TABLE IF NOT EXISTS pt_product_tests (
    id INTEGER PRIMARY KEY,
    quote_example JSONB,
    policy_example JSONB
);

COMMENT ON TABLE pt_product_tests IS 'Cached JSON examples for quote and policy by product version id';
