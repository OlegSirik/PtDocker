-- Повторяемость массивов в metadata JSON определяетсяется JsonPath (var_path), не колонкой.
ALTER TABLE mt_attribute_def DROP COLUMN IF EXISTS var_cardinality;
