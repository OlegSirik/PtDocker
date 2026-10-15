-- Фаза 2: колонка code дублировала var_code; ключ metadata JSON — только var_code.
ALTER TABLE mt_attribute_def DROP COLUMN IF EXISTS code;
