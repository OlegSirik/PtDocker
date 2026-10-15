-- Фаза 1: колонка name дублировала var_name; отображаемое имя — только var_name.
ALTER TABLE mt_attribute_def DROP COLUMN IF EXISTS name;
