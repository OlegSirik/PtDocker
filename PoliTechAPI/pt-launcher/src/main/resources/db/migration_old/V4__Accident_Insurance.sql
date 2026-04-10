insert into pt_metadata (var_code, var_name, var_path, var_type, var_value, var_cdm, nr, var_data_type) values

('rf_sport1', 'Доп.риск спорт #1', 'insuredObjects[0].riskFactors.sport1', 'IN', '0', 'insuredObject.riskFactors.sport1', 2034, 'STRING'),
('rf_sport2', 'Доп.риск спорт #2', 'insuredObjects[0].riskFactors.sport2', 'IN', '0', 'insuredObject.riskFactors.sport2', 2035, 'STRING'),
('rf_sport3', 'Доп.риск спорт #3', 'insuredObjects[0].riskFactors.sport3', 'IN', '0', 'insuredObject.riskFactors.sport3', 2036, 'STRING'),
('rf_sport4', 'Доп.риск спорт #4', 'insuredObjects[0].riskFactors.sport4', 'IN', '0', 'insuredObject.riskFactors.sport4', 2037, 'STRING'),
('rf_sport5', 'Доп.риск спорт #5', 'insuredObjects[0].riskFactors.sport5', 'IN', '0', 'insuredObject.riskFactors.sport5', 2038, 'STRING'),
('rf_profSport', 'Профессиональный спорт', 'insuredObjects[0].riskFactors.profSport', 'IN', '0', 'insuredObject.riskFactors.profSport', 2040, 'STRING')
ON CONFLICT (var_code) DO NOTHING;

