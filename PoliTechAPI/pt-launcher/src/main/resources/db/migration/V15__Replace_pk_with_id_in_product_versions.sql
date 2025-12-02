-- Migration: Replace pk with id in pt_product_versions table
-- This migration renames the primary key column from 'pk' to 'id'

-- Step 1: Alter table to rename column pk to id
alter table pt_product_versions rename column pk to id;

-- Step 2: Recreate the primary key constraint
alter table pt_product_versions
  drop constraint pt_product_versions_pkey,
  add primary key (id);

-- Step 3: Ensure the unique index exists with the new column name
drop index if exists pt_product_versions_uk;
create unique index if not exists pt_product_versions_uk on pt_product_versions(product_id, version_no);

