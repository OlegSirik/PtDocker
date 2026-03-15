-- Migration V15: Add public_id, filename, content_type, size; drop file_type, file_desc, product_code, package_code

ALTER TABLE pt_files DROP COLUMN IF EXISTS file_type;
ALTER TABLE pt_files DROP COLUMN IF EXISTS file_desc;
ALTER TABLE pt_files DROP COLUMN IF EXISTS product_code;
ALTER TABLE pt_files DROP COLUMN IF EXISTS package_code;
ALTER TABLE pt_files DROP COLUMN IF EXISTS is_deleted;

ALTER TABLE pt_files ADD COLUMN IF NOT EXISTS public_id VARCHAR(255);
ALTER TABLE pt_files ADD COLUMN IF NOT EXISTS filename VARCHAR(512);
ALTER TABLE pt_files ADD COLUMN IF NOT EXISTS content_type VARCHAR(255);
ALTER TABLE pt_files ADD COLUMN IF NOT EXISTS size BIGINT;

update pt_files set public_id = gen_random_uuid()::text where public_id is null;
ALTER TABLE pt_files ALTER COLUMN public_id SET NOT NULL;
CREATE UNIQUE INDEX pt_files_public_id_uk ON pt_files(public_id);

ALTER TABLE acc_tenants ADD COLUMN IF NOT EXISTS storage_type VARCHAR(30);