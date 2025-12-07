alter table coefficient_data
    alter column id set default nextval('account_seq');

alter table pt_calculators
    alter column id set default nextval('account_seq');

alter table pt_files
    alter column id set default nextval('account_seq');

alter table pt_lobs
    alter column id set default nextval('account_seq');

alter table pt_number_generators
    alter column id set default nextval('account_seq');

alter table pt_product_versions
    alter column id set default nextval('account_seq');

alter table pt_products
    alter column id set default nextval('account_seq');

alter table pt_calculators
    alter column id set default nextval('account_seq');

alter table policy_index
    alter column id set default gen_random_uuid();
