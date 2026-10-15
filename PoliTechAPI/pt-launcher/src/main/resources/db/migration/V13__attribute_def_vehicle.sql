-- Vehicle / Vehicle Model / Vehicle Document под insuredObject (схема INSURANCE_CONTRACT).
-- Имена по docs/curdoc/doc.md §12.15–12.16. Вставка для каждого тенанта, у которого есть insuredObject.

-- =============================================================================
-- io_vehicle (OBJECT)
-- =============================================================================
INSERT INTO mt_attribute_def (
    id, parent_id, tenant_id, document_id,
    var_code, var_name, var_path, var_ord, var_type, var_data_type, var_value, var_list, is_system
)
SELECT nextval('mt_seq'), io.id, io.tenant_id, 'INSURANCE_CONTRACT',
       'io_vehicle', 'Транспортное средство', 'insuredObjects[0].vehicle', 30,
       'OBJECT', 'OBJECT', NULL, NULL, false
FROM mt_attribute_def io
WHERE io.document_id = 'INSURANCE_CONTRACT'
  AND io.var_code = 'insuredObject'
  AND NOT EXISTS (
      SELECT 1 FROM mt_attribute_def x
      WHERE x.tenant_id = io.tenant_id
        AND x.document_id = 'INSURANCE_CONTRACT'
        AND x.var_code = 'io_vehicle'
  );

-- =============================================================================
-- Vehicle attributes
-- =============================================================================
INSERT INTO mt_attribute_def (
    id, parent_id, tenant_id, document_id,
    var_code, var_name, var_path, var_ord, var_type, var_data_type, var_value, var_list, is_system
)
SELECT nextval('mt_seq'), v.id, v.tenant_id, 'INSURANCE_CONTRACT',
       t.var_code, t.var_name, t.var_path, t.var_ord, t.var_type, t.var_data_type, t.var_value, t.var_list, false
FROM mt_attribute_def v
CROSS JOIN (
    VALUES
        ('io_vehicle_vin',             'VIN',                              'insuredObjects[0].vehicle.vin',             1::bigint,  'IN', 'STRING', NULL::varchar, NULL::varchar),
        ('io_vehicle_bodyNumber',      'Номер кузова',                     'insuredObjects[0].vehicle.bodyNumber',      2::bigint,  'IN', 'STRING', NULL, NULL),
        ('io_vehicle_chassisNumber',   'Номер шасси',                      'insuredObjects[0].vehicle.chassisNumber',   3::bigint,  'IN', 'STRING', NULL, NULL),
        ('io_vehicle_usedSince',       'Дата начала эксплуатации',         'insuredObjects[0].vehicle.usedSince',       4::bigint,  'IN', 'STRING', NULL, NULL),
        ('io_vehicle_mileage',         'Пробег',                           'insuredObjects[0].vehicle.mileage',         5::bigint,  'IN', 'NUMBER', NULL, NULL),
        ('io_vehicle_cost',            'Стоимость транспортного средства', 'insuredObjects[0].vehicle.cost',            6::bigint,  'IN', 'NUMBER', NULL, NULL),
        ('io_vehicle_manufactureDate', 'Дата выпуска',                     'insuredObjects[0].vehicle.manufactureDate', 7::bigint,  'IN', 'STRING', NULL, NULL),
        ('io_vehicle_isRegistered',    'Признак регистрации',              'insuredObjects[0].vehicle.isRegistered',    8::bigint,  'IN', 'STRING', NULL, 'yesNo'),
        ('io_vehicle_categoryCode',    'Категория транспортного средства', 'insuredObjects[0].vehicle.categoryCode',    9::bigint,  'IN', 'STRING', NULL, NULL),
        ('io_vehicle_typeCode',        'Тип транспортного средства',       'insuredObjects[0].vehicle.typeCode',        10::bigint, 'IN', 'STRING', NULL, NULL)
) AS t(var_code, var_name, var_path, var_ord, var_type, var_data_type, var_value, var_list)
WHERE v.document_id = 'INSURANCE_CONTRACT'
  AND v.var_code = 'io_vehicle'
  AND NOT EXISTS (
      SELECT 1 FROM mt_attribute_def x
      WHERE x.tenant_id = v.tenant_id
        AND x.document_id = 'INSURANCE_CONTRACT'
        AND x.var_code = t.var_code
  );

-- =============================================================================
-- io_vehicle_model (OBJECT) — Vehicle Model
-- =============================================================================
INSERT INTO mt_attribute_def (
    id, parent_id, tenant_id, document_id,
    var_code, var_name, var_path, var_ord, var_type, var_data_type, var_value, var_list, is_system
)
SELECT nextval('mt_seq'), v.id, v.tenant_id, 'INSURANCE_CONTRACT',
       'io_vehicle_model', 'Модель ТС', 'insuredObjects[0].vehicle.model', 20,
       'OBJECT', 'OBJECT', NULL, NULL, false
FROM mt_attribute_def v
WHERE v.document_id = 'INSURANCE_CONTRACT'
  AND v.var_code = 'io_vehicle'
  AND NOT EXISTS (
      SELECT 1 FROM mt_attribute_def x
      WHERE x.tenant_id = v.tenant_id
        AND x.document_id = 'INSURANCE_CONTRACT'
        AND x.var_code = 'io_vehicle_model'
  );

INSERT INTO mt_attribute_def (
    id, parent_id, tenant_id, document_id,
    var_code, var_name, var_path, var_ord, var_type, var_data_type, var_value, var_list, is_system
)
SELECT nextval('mt_seq'), m.id, m.tenant_id, 'INSURANCE_CONTRACT',
       t.var_code, t.var_name, t.var_path, t.var_ord, t.var_type, t.var_data_type, t.var_value, t.var_list, false
FROM mt_attribute_def m
CROSS JOIN (
    VALUES
        ('io_vehicle_model_brand',          'Марка',               'insuredObjects[0].vehicle.model.brand',          1::bigint, 'IN', 'STRING', NULL::varchar, NULL::varchar),
        ('io_vehicle_model_model',          'Модель',              'insuredObjects[0].vehicle.model.model',          2::bigint, 'IN', 'STRING', NULL, NULL),
        ('io_vehicle_model_horsePower',     'Мощность',            'insuredObjects[0].vehicle.model.horsePower',     3::bigint, 'IN', 'NUMBER', NULL, NULL),
        ('io_vehicle_model_engineTypeCode', 'Тип двигателя',       'insuredObjects[0].vehicle.model.engineTypeCode', 4::bigint, 'IN', 'STRING', NULL, NULL),
        ('io_vehicle_model_engineVolume',   'Объём двигателя',     'insuredObjects[0].vehicle.model.engineVolume',   5::bigint, 'IN', 'NUMBER', NULL, NULL),
        ('io_vehicle_model_doors',          'Количество дверей',   'insuredObjects[0].vehicle.model.doors',          6::bigint, 'IN', 'NUMBER', NULL, NULL),
        ('io_vehicle_model_bodyTypeCode',   'Тип кузова',          'insuredObjects[0].vehicle.model.bodyTypeCode',   7::bigint, 'IN', 'STRING', NULL, NULL),
        ('io_vehicle_model_gearTypeCode',   'Тип коробки передач', 'insuredObjects[0].vehicle.model.gearTypeCode',   8::bigint, 'IN', 'STRING', NULL, NULL)
) AS t(var_code, var_name, var_path, var_ord, var_type, var_data_type, var_value, var_list)
WHERE m.document_id = 'INSURANCE_CONTRACT'
  AND m.var_code = 'io_vehicle_model'
  AND NOT EXISTS (
      SELECT 1 FROM mt_attribute_def x
      WHERE x.tenant_id = m.tenant_id
        AND x.document_id = 'INSURANCE_CONTRACT'
        AND x.var_code = t.var_code
  );

-- =============================================================================
-- io_vehicleDocument (OBJECT) — sibling of vehicle under insuredObject
-- =============================================================================
INSERT INTO mt_attribute_def (
    id, parent_id, tenant_id, document_id,
    var_code, var_name, var_path, var_ord, var_type, var_data_type, var_value, var_list, is_system
)
SELECT nextval('mt_seq'), io.id, io.tenant_id, 'INSURANCE_CONTRACT',
       'io_vehicleDocument', 'Документ ТС', 'insuredObjects[0].vehicleDocument', 31,
       'OBJECT', 'OBJECT', NULL, NULL, false
FROM mt_attribute_def io
WHERE io.document_id = 'INSURANCE_CONTRACT'
  AND io.var_code = 'insuredObject'
  AND NOT EXISTS (
      SELECT 1 FROM mt_attribute_def x
      WHERE x.tenant_id = io.tenant_id
        AND x.document_id = 'INSURANCE_CONTRACT'
        AND x.var_code = 'io_vehicleDocument'
  );

INSERT INTO mt_attribute_def (
    id, parent_id, tenant_id, document_id,
    var_code, var_name, var_path, var_ord, var_type, var_data_type, var_value, var_list, is_system
)
SELECT nextval('mt_seq'), d.id, d.tenant_id, 'INSURANCE_CONTRACT',
       t.var_code, t.var_name, t.var_path, t.var_ord, t.var_type, t.var_data_type, t.var_value, t.var_list, false
FROM mt_attribute_def d
CROSS JOIN (
    VALUES
        ('io_vehicleDocument_typeCode',    'Тип документа', 'insuredObjects[0].vehicleDocument.typeCode',    1::bigint, 'IN', 'STRING', NULL::varchar, NULL::varchar),
        ('io_vehicleDocument_serial',      'Серия',         'insuredObjects[0].vehicleDocument.serial',      2::bigint, 'IN', 'STRING', NULL, NULL),
        ('io_vehicleDocument_number',      'Номер',         'insuredObjects[0].vehicleDocument.number',      3::bigint, 'IN', 'STRING', NULL, NULL),
        ('io_vehicleDocument_dateIssue',   'Дата выдачи',   'insuredObjects[0].vehicleDocument.dateIssue',   4::bigint, 'IN', 'STRING', NULL, NULL),
        ('io_vehicleDocument_whom',        'Кем выдан',     'insuredObjects[0].vehicleDocument.whom',        5::bigint, 'IN', 'STRING', NULL, NULL),
        ('io_vehicleDocument_countryCode', 'Страна выдачи', 'insuredObjects[0].vehicleDocument.countryCode', 6::bigint, 'IN', 'STRING', 'RU', 'country2')
) AS t(var_code, var_name, var_path, var_ord, var_type, var_data_type, var_value, var_list)
WHERE d.document_id = 'INSURANCE_CONTRACT'
  AND d.var_code = 'io_vehicleDocument'
  AND NOT EXISTS (
      SELECT 1 FROM mt_attribute_def x
      WHERE x.tenant_id = d.tenant_id
        AND x.document_id = 'INSURANCE_CONTRACT'
        AND x.var_code = t.var_code
  );
