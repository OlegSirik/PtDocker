-- Фаза 3b / A: var_cdm больше не хранится в схеме договора.
-- Тег CALCULATOR живёт только в JSON калькулятора (PvVar), не в mt_attribute_def.
ALTER TABLE mt_attribute_def DROP COLUMN IF EXISTS var_cdm;
