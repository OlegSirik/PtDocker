-- Migration V7: Create schema tables (mt_contract_model, mt_contract_section, mt_entity_def, mt_attribute_def)
-- Recreated based on current entities and script.txt

-- ============================================================================
-- SEQUENCE
-- ============================================================================

CREATE SEQUENCE IF NOT EXISTS mt_seq START WITH 1000 INCREMENT BY 1;

-- ============================================================================
-- CONTRACT MODEL TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS mt_contract_model (
    id BIGINT PRIMARY KEY DEFAULT nextval('mt_seq'),
    tid BIGINT NOT NULL,
    code VARCHAR(50),
    name VARCHAR(300),
    CONSTRAINT mt_contract_model_tid_code_uk UNIQUE (tid, code),
    CONSTRAINT mt_contract_model_tid_name_uk UNIQUE (tid, name)
);

-- ============================================================================
-- CONTRACT SECTION TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS mt_contract_section (
    id BIGINT PRIMARY KEY DEFAULT nextval('mt_seq'),
    tid BIGINT NOT NULL,
    model_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(300) NOT NULL,
    path VARCHAR(100) NOT NULL,
    CONSTRAINT mt_contract_section_model_id_code_uk UNIQUE (model_id, code),
    CONSTRAINT mt_contract_section_model_id_name_uk UNIQUE (model_id, name),
    CONSTRAINT mt_contract_section_model_id_fk FOREIGN KEY (model_id) REFERENCES mt_contract_model(id)
);

-- ============================================================================
-- ENTITY DEFINITION TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS mt_entity_def (
    id BIGINT PRIMARY KEY DEFAULT nextval('mt_seq'),
    tid BIGINT NOT NULL,
    section_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(300) NOT NULL,
    path VARCHAR(100) NOT NULL,
    cardinality VARCHAR(10) NOT NULL,
    CONSTRAINT mt_entity_def_section_id_code_uk UNIQUE (section_id, code),
    CONSTRAINT mt_entity_def_section_id_name_uk UNIQUE (section_id, name),
    CONSTRAINT mt_entity_def_section_id_fk FOREIGN KEY (section_id) REFERENCES mt_contract_section(id)
);

-- ============================================================================
-- ATTRIBUTE DEFINITION TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS mt_attribute_def (
    id BIGINT PRIMARY KEY DEFAULT nextval('mt_seq'),
    tid BIGINT NOT NULL,
    entity_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(300) NOT NULL,
    path VARCHAR(100) NOT NULL,
    nr BIGINT NOT NULL,
    var_code VARCHAR(50) NOT NULL,
    var_name VARCHAR(300) NOT NULL,
    var_path VARCHAR(100) NOT NULL,
    var_type VARCHAR(20) NOT NULL,
    var_value VARCHAR(500),
    var_cdm VARCHAR(100) NOT NULL,
    var_data_type VARCHAR(10) NOT NULL,
    CONSTRAINT mt_attribute_def_entity_id_code_uk UNIQUE (entity_id, code),
    CONSTRAINT mt_attribute_def_entity_id_name_uk UNIQUE (entity_id, name),
    CONSTRAINT mt_attribute_def_entity_id_fk FOREIGN KEY (entity_id) REFERENCES mt_entity_def(id)
);

-- ============================================================================
-- INDEXES (optional, for performance)
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_mt_contract_model_tid ON mt_contract_model(tid);
CREATE INDEX IF NOT EXISTS idx_mt_contract_section_tid ON mt_contract_section(tid);
CREATE INDEX IF NOT EXISTS idx_mt_contract_section_model_id ON mt_contract_section(model_id);
CREATE INDEX IF NOT EXISTS idx_mt_entity_def_tid ON mt_entity_def(tid);
CREATE INDEX IF NOT EXISTS idx_mt_entity_def_section_id ON mt_entity_def(section_id);
CREATE INDEX IF NOT EXISTS idx_mt_attribute_def_tid ON mt_attribute_def(tid);
CREATE INDEX IF NOT EXISTS idx_mt_attribute_def_entity_id ON mt_attribute_def(entity_id);

INSert into  mt_contract_model (id, tid, code, name) values (1, 1, 'box1', 'Box Insurance');

insert into mt_contract_section ( id , tid, model_id, code, name, path) values ( 2,1,1,'policy','Договор','policy');
insert into mt_contract_section ( id , tid, model_id, code, name, path) values ( 3,1,1,'policyHolder','Страхователь','policyHolder');
insert into mt_contract_section ( id , tid, model_id, code, name, path) values ( 4,1,1,'insuredObject','Объект страхования','insuredObject[0]');

insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 15, 1, 2, 'policy','Атрибуты договора','null', 'NULL');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 16, 1, 2, 'commission','комиссия по договору','commission', 'SINGLE');

insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 10, 1, 3, 'person','Физ.лицо','person', 'SINGLE');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 11, 1, 3, 'contacts','Контакты','contacts', 'SINGLE');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 12, 1, 3, 'organization','Юр.лицо','organization', 'SINGLE');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 13, 1, 3, 'identifiers','Документ','identifiers[?(@.isPrimary)]', 'MULT');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 14, 1, 3, 'addresses','Адрес','addresses[?(@.isPrimary)]', 'MULT');


insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 20, 1, 4, 'person','Физ.лицо','person', 'SINGLE');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 21, 1, 4, 'contacts','Контакты','contacts', 'SINGLE');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 22, 1, 4, 'organization','Юр.лицо','organization', 'SINGLE');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 23, 1, 4, 'identifiers','Документ','identifiers[?(@.isPrimary)]', 'MULT');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 24, 1, 4, 'addresses','Адрес','addresses[?(@.isPrimary)]', 'MULT');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 25, 1, 4, 'device','Электронное устройство','device', 'SINGLE');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 26, 1, 4, 'property','Недвижимость','property', 'SINGLE');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 27, 1, 4, 'travelSegments','Сегмент авиаперевозки','travelSegments', 'MULT');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 28, 1, 4, 'riskFactors','Доп. риски','riskFactors', 'SINGLE');
insert into mt_entity_def (id ,tid,section_id ,code ,name ,path ,cardinality) values ( 29, 1, 4, 'insuredObject','Атрибуты объекта страхования','insuredObject', 'NULL');

insert into mt_attribute_def ( tid, entity_id, code, name, path, nr, var_code, var_name, var_path, var_type, var_value, var_cdm, var_data_type ) VALUES 
 (1,10,'age_end','Страхователь.возраст на дату окончания договора','null',141,'ph_age_end','Страхователь.возраст на дату окончания договора','null','MAGIC','NULL','policyHolder.person.age_end','NUMBER'),
 (1,10,'isResident','Страхователь.резидент РФ','isResident',28,'ph_isResident','Страхователь.резидент РФ','policyHolder.person.isResident','IN','true','policyHolder.person.isResident','STRING'),
 (1,10,'ext_id','Страхователь.внешний ID','ext_id',31,'ph_ext_id','Страхователь.внешний ID','policyHolder.person.ext_id','IN','NULL','policyHolder.person.ext_id','STRING'),
 (1,10,'birthPlace','Страхователь.место рождения','birthPlace',13,'ph_birthPlace','Страхователь.место рождения','policyHolder.person.birthPlace','IN','NULL','policyHolder.person.birthPlace','STRING'),
 (1,10,'fullNameEn','Страхователь.полное ФИО англ','fullNameEn',12,'ph_fullNameEn','Страхователь.полное ФИО англ','policyHolder.person.fullNameEn','IN','NULL','policyHolder.person.fullNameEn','STRING'),
 (1,10,'fullName','Страхователь.полное ФИО','fullName',9,'ph_fullName','Страхователь.полное ФИО','policyHolder.person.fullName','IN','NULL','policyHolder.person.fullName','STRING'),
 (1,10,'citizenship','Страхователь.гражданство','citizenship',16,'ph_citizenship','Страхователь.гражданство','policyHolder.person.citizenship','IN','RU','policyHolder.person.citizenship','STRING'),
 (1,10,'age_issue','Страхователь.возраст на дату выпуска договора','null',142,'ph_age_issue','Страхователь.возраст на дату выпуска договора','null','MAGIC','NULL','policyHolder.person.age_issue','NUMBER'),
 (1,10,'familyState','Страхователь.семейное положение','familyState',20,'ph_familyState','Страхователь.семейное положение','policyHolder.person.familyState','IN','SINGLE','policyHolder.person.familyState','STRING'),
 (1,10,'birthDate','Страхователь.дата рождения','birthDate',7,'ph_birthDate','Страхователь.дата рождения','policyHolder.person.birthDate','IN','NULL','policyHolder.person.birthDate','STRING'),
 (1,10,'middleName','Страхователь.отчество','middleName',5,'ph_middleName','Страхователь.отчество','policyHolder.person.middleName','IN','NULL','policyHolder.person.middleName','STRING'),
 (1,10,'lastName','Страхователь.фамилия','lastName',3,'ph_lastName','Страхователь.фамилия','policyHolder.person.lastName','IN','NULL','policyHolder.person.lastName','STRING'),
 (1,10,'gender','Страхователь.пол','gender',18,'ph_gender','Страхователь.пол','policyHolder.person.gender','IN','NULL','policyHolder.person.gender','STRING'),
 (1,10,'firstName','Страхователь.имя','firstName',2,'ph_firstName','Страхователь.имя','policyHolder.person.firstName','IN','NULL','policyHolder.person.firstName','STRING'),
 (1,10,'isPublicOfficial','Страхователь.признак ПДЛ','isPublicOfficial',26,'ph_isPublicOfficial','Страхователь.признак ПДЛ','policyHolder.person.isPublicOfficial','IN','NULL','policyHolder.person.isPublicOfficial','STRING'),
 (1,11,'phone','Страхователь.телефон','phone',49,'ph_phone','Страхователь.телефон','policyHolder.contacts.phone','IN','NULL','policyHolder.contacts.phone','STRING'),
 (1,11,'email','Страхователь.email','email',52,'ph_email','Страхователь.email','policyHolder.contacts.email','IN','NULL','policyHolder.contacts.email','STRING'),
 (1,12,'org_bic','Страхователь.БИК','org_bic',44,'ph_org_bic','Страхователь.БИК','policyHolder.organization.bic','IN','NULL','policyHolder.organization.bic','STRING'),
 (1,12,'org_inn','Страхователь.ИНН юр.лица','org_inn',36,'ph_org_inn','Страхователь.ИНН юр.лица','policyHolder.organization.inn','IN','NULL','policyHolder.organization.inn','STRING'),
 (1,12,'org_nciCode','Страхователь.НСИ код','org_nciCode',48,'ph_org_nciCode','Страхователь.НСИ код','policyHolder.organization.nciCode','IN','NULL','policyHolder.organization.nciCode','STRING'),
 (1,12,'org_ext_id','Страхователь.внешний ID (юрик)','org_ext_id',47,'ph_org_ext_id','Страхователь.внешний ID (юрик)','policyHolder.organization.ext_id','IN','NULL','policyHolder.organization.ext_id','STRING'),
 (1,12,'country','Страхователь.код страны регистрации','country',35,'ph_org_country','Страхователь.код страны регистрации','policyHolder.organization.country','IN','NULL','policyHolder.organization.country','STRING'),
 (1,12,'org_group','Страхователь.Gруппа страхователя ЮЛ','org_group',46,'ph_org_group','Страхователь.Gруппа страхователя ЮЛ','policyHolder.organization.group','IN','NULL','policyHolder.organization.group','STRING'),
 (1,12,'org_isResident','Страхователь.резидент РФ юр.лица','org_isResident',45,'ph_org_isResident','Страхователь.резидент РФ юр.лица','policyHolder.organization.isResident','IN','true','policyHolder.organization.isResident','STRING'),
 (1,12,'org_okpo','Страхователь.ОКПО','org_okpo',43,'ph_org_okpo','Страхователь.ОКПО','policyHolder.organization.okpo','IN','NULL','policyHolder.organization.okpo','STRING'),
 (1,12,'org_ogrn','Страхователь.ОГРН','org_ogrn',42,'ph_org_ogrn','Страхователь.ОГРН','policyHolder.organization.ogrn','IN','NULL','policyHolder.organization.ogrn','STRING'),
 (1,12,'org_kpp','Страхователь.КПП','org_kpp',41,'ph_org_kpp','Страхователь.КПП','policyHolder.organization.kpp','IN','NULL','policyHolder.organization.kpp','STRING'),
 (1,12,'org_legalForm','Страхователь.организационно-правовая форма','org_legalForm',40,'ph_org_legalForm','Страхователь.организационно-правовая форма','policyHolder.organization.legalForm','IN','NULL','policyHolder.organization.legalForm','STRING'),
 (1,12,'org_shortName','Страхователь.краткое наименование юр.лица','org_shortName',39,'ph_org_shortName','Страхователь.краткое наименование юр.лица','policyHolder.organization.shortName','IN','NULL','policyHolder.organization.shortName','STRING'),
 (1,12,'org_fullNameEn','Страхователь.полное наименование  юр.лица англ','org_fullNameEn',38,'ph_org_fullNameEn','Страхователь.полное наименование  юр.лица англ','policyHolder.organization.fullNameEn','IN','NULL','policyHolder.organization.fullNameEn','STRING'),
 (1,12,'org_fullName','Страхователь.полное наименование юр.лица','org_fullName',37,'ph_org_fullName','Страхователь.полное наименование юр.лица','policyHolder.organization.fullName','IN','NULL','policyHolder.organization.fullName','STRING'),
 (1,13,'doc_dateIssue','ph.Документ.дата выдачи','doc_dateIssue',61,'ph_doc_dateIssue','ph.Документ.дата выдачи','policyHolder.identifiers[?(@.isPrimary)].dateIssue','IN','NULL','policyHolder.identifiers.dateIssue','STRING'),
 (1,13,'doc_divisionCode','ph.Документ.код подразделения','doc_divisionCode',67,'ph_doc_divisionCode','ph.Документ.код подразделения','policyHolder.identifiers[?(@.isPrimary)].divisionCode','IN','NULL','policyHolder.identifiers.divisionCode','STRING'),
 (1,13,'doc_validUntil','ph.Документ.действительно до','doc_validUntil',64,'ph_doc_validUntil','ph.Документ.действительно до','policyHolder.identifiers[?(@.isPrimary)].validUntil','IN','NULL','policyHolder.identifiers.validUntil','STRING'),
 (1,13,'doc_whom','ph.Документ.кем выдан','doc_whom',65,'ph_doc_whom','ph.Документ.кем выдан','policyHolder.identifiers[?(@.isPrimary)].whom','IN','NULL','policyHolder.identifiers.whom','STRING'),
 (1,13,'doc_ext_id','ph.Документ.внешний ID','doc_ext_id',70,'ph_doc_ext_id','ph.Документ.внешний ID','policyHolder.identifiers[?(@.isPrimary)].ext_id','IN','NULL','policyHolder.identifiers.ext_id','STRING'),
 (1,13,'doc_countryCode','ph.Документ.страна выдачи документа','doc_countryCode',72,'ph_doc_countryCode','ph.Документ.страна выдачи документа','policyHolder.identifiers[?(@.isPrimary)].countryCode','IN','RU','policyHolder.identifiers.countryCode','STRING'),
 (1,13,'doc_typeCode','ph.Документ.код типа документа','doc_typeCode',55,'ph_doc_typeCode','ph.Документ.код типа документа','policyHolder.identifiers[?(@.isPrimary)].typeCode','IN','NULL','policyHolder.identifiers.typeCode','STRING'),
 (1,13,'doc_serial','ph.Документ.серия документа','doc_serial',57,'ph_doc_serial','ph.Документ.серия документа','policyHolder.identifiers[?(@.isPrimary)].serial','IN','NULL','policyHolder.identifiers.serial','STRING'),
 (1,13,'doc_number','ph.Документ.номер документа','doc_number',60,'ph_doc_number','ph.Документ.номер документа','policyHolder.identifiers[?(@.isPrimary)].number','IN','NULL','policyHolder.identifiers.number','STRING'),
 (1,14,'addr_region','ph.Адрес.регион','addr_region',77,'ph_addr_region','ph.Адрес.регион','policyHolder.addresses[?(@.isPrimary)].region','IN','NULL','policyHolder.addresses.region','STRING'),
 (1,14,'addr_typeCode','ph.Адрес.тип адреса','addr_typeCode',74,'ph_addr_typeCode','ph.Адрес.тип адреса','policyHolder.addresses[?(@.isPrimary)].typeCode','IN','REGISTRATION','policyHolder.addresses.typeCode','STRING'),
 (1,14,'addr_fiasId','ph.Адрес.код ФИАС','addr_fiasId',95,'ph_addr_fiasId','ph.Адрес.код ФИАС','policyHolder.addresses[?(@.isPrimary)].fiasId','IN','NULL','policyHolder.addresses.fiasId','STRING'),
 (1,14,'addr_street','ph.Адрес.улица','addr_street',81,'ph_addr_street','ph.Адрес.улица','policyHolder.addresses[?(@.isPrimary)].street','IN','NULL','policyHolder.addresses.street','STRING'),
 (1,14,'addr_house','ph.Адрес.дом','addr_house',83,'ph_addr_house','ph.Адрес.дом','policyHolder.addresses[?(@.isPrimary)].house','IN','NULL','policyHolder.addresses.house','STRING'),
 (1,14,'addr_flat','ph.Адрес.квартира','addr_flat',87,'ph_addr_flat','ph.Адрес.квартира','policyHolder.addresses[?(@.isPrimary)].flat','IN','NULL','policyHolder.addresses.flat','STRING'),
 (1,14,'addr_addressStrEn','ph.Адрес.адресная строка англ','addr_addressStrEn',99,'ph_addr_addressStrEn','ph.Адрес.адресная строка англ','policyHolder.addresses[?(@.isPrimary)].addressStrEn','IN','NULL','policyHolder.addresses.addressStrEn','STRING'),
 (1,14,'addr_ext_id','ph.Адрес.внешний ID','addr_ext_id',101,'ph_addr_ext_id','ph.Адрес.внешний ID','policyHolder.addresses[?(@.isPrimary)].ext_id','IN','NULL','policyHolder.addresses.ext_id','STRING'),
 (1,14,'addr_addressStr','ph.Адрес.адресная строка','addr_addressStr',98,'ph_addr_addressStr','ph.Адрес.адресная строка','policyHolder.addresses[?(@.isPrimary)].addressStr','IN','NULL','policyHolder.addresses.addressStr','STRING'),
 (1,14,'addr_countryCode','ph.Адрес.код страны','addr_countryCode',76,'ph_addr_countryCode','ph.Адрес.код страны','policyHolder.addresses[?(@.isPrimary)].countryCode','IN','RU','policyHolder.addresses.countryCode','STRING'),
 (1,14,'addr_kladrId','ph.Адрес.код КЛАДР','addr_kladrId',93,'ph_addr_kladrId','ph.Адрес.код КЛАДР','policyHolder.addresses[?(@.isPrimary)].kladrId','IN','NULL','policyHolder.addresses.kladrId','STRING'),
 (1,14,'addr_zipCode','ph.Адрес.индекс','addr_zipCode',92,'ph_addr_zipCode','ph.Адрес.индекс','policyHolder.addresses[?(@.isPrimary)].zipCode','IN','NULL','policyHolder.addresses.zipCode','STRING'),
 (1,14,'addr_room','ph.Адрес.комната','addr_room',89,'ph_addr_room','ph.Адрес.комната','policyHolder.addresses[?(@.isPrimary)].room','IN','NULL','policyHolder.addresses.room','STRING'),
 (1,14,'addr_building','ph.Адрес.строение','addr_building',86,'ph_addr_building','ph.Адрес.строение','policyHolder.addresses[?(@.isPrimary)].building','IN','NULL','policyHolder.addresses.building','STRING'),
 (1,14,'addr_city','ph.Адрес.город','addr_city',79,'ph_addr_city','ph.Адрес.город','policyHolder.addresses[?(@.isPrimary)].city','IN','NULL','policyHolder.addresses.city','STRING'),
 
 (1,15,'premium','премия по договору','premium',32,'pl_premium','премия по договору','premium','IN','NULL','policy.policy.premium','STRING'),
 (1,15,'startDate','дата начала действия договора','startDate',23,'pl_startDate','дата начала действия договора','startDate','IN','NULL','policy.policy.startDate','STRING'),
 (1,15,'policyNumber','номер договора','policyNumber',30,'pl_policyNumber','номер договора','policyNumber','IN','NULL','policy.policy.policyNumber','STRING'),
 (1,15,'endDate','дата окончания договора','endDate',29,'pl_endDate','дата окончания договора','endDate','IN','NULL','policy.policy.endDate','STRING'),
 (1,15,'gross_up_factor','gross_up_factor 1/(1-kV)','gross_up_factor',138,'gross_up_factor','gross_up_factor 1/(1-kV)','null','MAGIC','NULL','policy.policy.grossUpFactor','NUMBER'),
 (1,15,'termDays','Срок полиса в днях','mull',144,'pl_TermDays','Срок полиса в днях','null','MAGIC','NULL','policy.policy.termDays','NUMBER'),
 (1,15,'termMonths','Срок полиса в месяцах','null',143,'pl_TermMonths','Срок полиса в месяцах','null','MAGIC','NULL','policy.policy.termMonths','NUMBER'),
 (1,15,'issueDate','дата выпуска договора','issueDate',34,'pl_issueDate','дата выпуска договора','issueDate','IN','NULL','policy.policy.issueDate','STRING'),
 (1,15,'productCode','Код продукта','productCode',22,'pl_productCode','Код продукта','productCode','IN','NULL','policy.policy.productCode','STRING'),
 
 (1,16,'commRate','Процент кВ по договору','commRate',137,'pl_commRate','Процент кВ по договору','appliedCommissionRate','IN','NULL','policy.commission.appliedCommissionRate','NUMBER'),

 (1,29,'sumInsured','Страховая сумма объекта страхования','sumInsured',19,'io_sumInsured','Страховая сумма объекта страхования','insuredObjects[0].sumInsured','IN','100000','insuredObject.insuredObject.sumInsured','NUMBER'),
 (1,29,'packageCode','Код пакета','packageCode',24,'io_packageCode','Код пакета','insuredObjects[0].packageCode','IN','0','insuredObject.insuredObject.packageCode','STRING'),
 
 (1,20,'birthPlace','Застрахованный.место рождения','birthPlace',14,'io_birthPlace','Застрахованный.место рождения','insuredObjects[0].person.birthPlace','IN','NULL','insuredObject.person.birthPlace','STRING'),
 (1,20,'citizenship','Застрахованный.гражданство','citizenship',15,'io_citizenship','Застрахованный.гражданство','insuredObjects[0].person.citizenship','IN','RU','insuredObject.person.citizenship','STRING'),
 (1,20,'gender','Застрахованный.пол','gender',17,'io_gender','Застрахованный.пол','insuredObjects[0].person.gender','IN','NULL','insuredObject.person.gender','STRING'),
 (1,20,'familyState','Застрахованный.семейное положение','familyState',21,'io_familyState','Застрахованный.семейное положение','insuredObjects[0].person.familyState','IN','SINGLE','insuredObject.person.familyState','STRING'),
 (1,20,'isPublicOfficial','Застрахованный.признак ПДЛ','isPublicOfficial',25,'io_isPublicOfficial','Застрахованный.признак ПДЛ','insuredObjects[0].person.isPublicOfficial','IN','NULL','insuredObject.person.isPublicOfficial','STRING'),
 (1,20,'firstName','Застрахованный.имя','firstName',1,'io_firstName','Застрахованный.имя','insuredObjects[0].person.firstName','IN','NULL','insuredObject.person.firstName','STRING'),
 (1,20,'lastName','Застрахованный.фамилия','lastName',4,'io_lastName','Застрахованный.фамилия','insuredObjects[0].person.lastName','IN','NULL','insuredObject.person.lastName','STRING'),
 (1,20,'middleName','Застрахованный.отчество','middleName',6,'io_middleName','Застрахованный.отчество','insuredObjects[0].person.middleName','IN','NULL','insuredObject.person.middleName','STRING'),
 (1,20,'birthDate','Застрахованный.дата рождения','birthDate',8,'io_birthDate','Застрахованный.дата рождения','insuredObjects[0].person.birthDate','IN','NULL','insuredObject.person.birthDate','STRING'),
 (1,20,'fullName','Застрахованный.полное ФИО','fullName',10,'io_fullName','Застрахованный.полное ФИО','insuredObjects[0].person.fullName','IN','NULL','insuredObject.person.fullName','STRING'),
 (1,20,'isResident','Застрахованный.резидент РФ','isResident',27,'io_isResident','Застрахованный.резидент РФ','insuredObjects[0].person.isResident','IN','true','insuredObject.person.isResident','STRING'),
 (1,20,'ext_id','Застрахованный.внешний ID','ext_id',33,'io_ext_id','Застрахованный.внешний ID','insuredObjects[0].person.ext_id','IN','NULL','insuredObject.person.ext_id','STRING'),
 (1,20,'fullNameEn','Застрахованный.полное ФИО англ','fullNameEn',11,'io_fullNameEn','Застрахованный.полное ФИО англ','insuredObjects[0].person.fullNameEn','IN','NULL','insuredObject.person.fullNameEn','STRING'),
 (1,20,'age_issue','Возраст застрахованного на дату выпуска полиса','age_issue',139,'io_age_issue','Возраст застрахованного на дату выпуска полиса','null','MAGIC','NULL','insuredObject.person.age_issue','NUMBER'),
 (1,20,'age_end','Возраст застрахованного на дату окончания полиса','age_end',140,'io_age_end','Возраст застрахованного на дату окончания полиса','null','MAGIC','NULL','insuredObject.person.age_end','NUMBER'),
 (1,21,'email','Застрахованный.email','email',51,'io_email','Застрахованный.email','insuredObjects[0].contacts.email','IN','NULL','insuredObject.contacts.email','STRING'),
 (1,21,'phone','Застрахованный.телефон','phone',50,'io_phone','Застрахованный.телефон','insuredObjects[0].contacts.phone','IN','NULL','insuredObject.contacts.phone','STRING'),
 (1,23,'doc_validUntil','io.Документ.действительно до','doc_validUntil',63,'io_doc_validUntil','io.Документ.действительно до','insuredObjects[0].identifiers[?(@.isPrimary)].validUntil','IN','NULL','insuredObject.identifiers.validUntil','STRING'),
 (1,23,'doc_whom','io.Документ.кем выдан','doc_whom',66,'io_doc_whom','io.Документ.кем выдан','insuredObjects[0].identifiers[?(@.isPrimary)].whom','IN','NULL','insuredObject.identifiers.whom','STRING'),
 (1,23,'doc_countryCode','io.Документ.страна выдачи документа','doc_countryCode',71,'io_doc_countryCode','io.Документ.страна выдачи документа','insuredObjects[0].identifiers[?(@.isPrimary)].countryCode','IN','RU','insuredObject.identifiers.countryCode','STRING'),
 (1,23,'doc_number','io.Документ.номер документа','doc_number',59,'io_doc_number','io.Документ.номер документа','insuredObjects[0].identifiers[?(@.isPrimary)].number','IN','NULL','insuredObject.identifiers.number','STRING'),
 (1,23,'doc_serial','io.Документ.серия документа','doc_serial',58,'io_doc_serial','io.Документ.серия документа','insuredObjects[0].identifiers[?(@.isPrimary)].serial','IN','NULL','insuredObject.identifiers.serial','STRING'),
 (1,23,'doc_ext_id','io.Документ.внешний ID','doc_ext_id',69,'io_doc_ext_id','io.Документ.внешний ID','insuredObjects[0].identifiers[?(@.isPrimary)].ext_id','IN','NULL','insuredObject.identifiers.ext_id','STRING'),
 (1,23,'doc_typeCode','io.Документ.код типа документа','doc_typeCode',54,'io_doc_typeCode','io.Документ.код типа документа','insuredObjects[0].identifiers[?(@.isPrimary)].typeCode','IN','NULL','insuredObject.identifiers.typeCode','STRING'),
 (1,23,'doc_divisionCode','io.Документ.код подразделения','doc_divisionCode',68,'io_doc_divisionCode','io.Документ.код подразделения','insuredObjects[0].identifiers[?(@.isPrimary)].divisionCode','IN','NULL','insuredObject.identifiers.divisionCode','STRING'),
 (1,23,'doc_dateIssue','io.Документ.дата выдачи','doc_dateIssue',62,'io_doc_dateIssue','io.Документ.дата выдачи','insuredObjects[0].identifiers[?(@.isPrimary)].dateIssue','IN','NULL','insuredObject.identifiers.dateIssue','STRING'),
 (1,24,'addr_fiasId','io.Адрес.код ФИАС','addr_fiasId',96,'io_addr_fiasId','io.Адрес.код ФИАС','insuredObjects[0].addresses[?(@.isPrimary)].fiasId','IN','NULL','insuredObject.addresses.fiasId','STRING'),
 (1,24,'addr_kladrId','io.Адрес.код КЛАДР','addr_kladrId',94,'io_addr_kladrId','io.Адрес.код КЛАДР','insuredObjects[0].addresses[?(@.isPrimary)].kladrId','IN','NULL','insuredObject.addresses.kladrId','STRING'),
 (1,24,'addr_zipCode','io.Адрес.индекс','addr_zipCode',91,'io_addr_zipCode','io.Адрес.индекс','insuredObjects[0].addresses[?(@.isPrimary)].zipCode','IN','NULL','insuredObject.addresses.zipCode','STRING'),
 (1,24,'addr_room','io.Адрес.комната','addr_room',90,'io_addr_room','io.Адрес.комната','insuredObjects[0].addresses[?(@.isPrimary)].room','IN','NULL','insuredObject.addresses.room','STRING'),
 (1,24,'addr_flat','io.Адрес.квартира','addr_flat',88,'io_addr_flat','io.Адрес.квартира','insuredObjects[0].addresses[?(@.isPrimary)].flat','IN','NULL','insuredObject.addresses.flat','STRING'),
 (1,24,'addr_building','io.Адрес.строение','addr_building',85,'io_addr_building','io.Адрес.строение','insuredObjects[0].addresses[?(@.isPrimary)].building','IN','NULL','insuredObject.addresses.building','STRING'),
 (1,24,'addr_house','io.Адрес.дом','addr_house',84,'io_addr_house','io.Адрес.дом','insuredObjects[0].addresses[?(@.isPrimary)].house','IN','NULL','insuredObject.addresses.house','STRING'),
 (1,24,'addr_street','io.Адрес.улица','addr_street',82,'io_addr_street','io.Адрес.улица','insuredObjects[0].addresses[?(@.isPrimary)].street','IN','NULL','insuredObject.addresses.street','STRING'),
 (1,24,'addr_region','io.Адрес.регион','addr_region',78,'io_addr_region','io.Адрес.регион','insuredObjects[0].addresses[?(@.isPrimary)].region','IN','NULL','insuredObject.addresses.region','STRING'),
 (1,24,'addr_countryCode','io.Адрес.код страны','addr_countryCode',75,'io_addr_countryCode','io.Адрес.код страны','insuredObjects[0].addresses[?(@.isPrimary)].countryCode','IN','RU','insuredObject.addresses.countryCode','STRING'),
 (1,24,'addr_ext_id','io.Адрес.внешний ID','addr_ext_id',102,'io_addr_ext_id','io.Адрес.внешний ID','insuredObjects[0].addresses[?(@.isPrimary)].ext_id','IN','NULL','insuredObject.addresses.ext_id','STRING'),
 (1,24,'addr_typeCode','io.Адрес.тип адреса','addr_typeCode',73,'io_addr_typeCode','io.Адрес.тип адреса','insuredObjects[0].addresses[?(@.isPrimary)].typeCode','IN','REGISTRATION','insuredObject.addresses.typeCode','STRING'),
 (1,24,'addr_city','io.Адрес.город','addr_city',80,'io_addr_city','io.Адрес.город','insuredObjects[0].addresses[?(@.isPrimary)].city','IN','NULL','insuredObject.addresses.city','STRING'),
 (1,24,'addr_addressStrEn','io.Адрес.адресная строка англ','addr_addressStrEn',100,'io_addr_addressStrEn','io.Адрес.адресная строка англ','insuredObjects[0].addresses[?(@.isPrimary)].addressStrEn','IN','NULL','insuredObject.addresses.addressStrEn','STRING'),
 (1,24,'addr_addressStr','io.Адрес.адресная строка','addr_addressStr',97,'io_addr_addressStr','io.Адрес.адресная строка','insuredObjects[0].addresses[?(@.isPrimary)].addressStr','IN','NULL','insuredObject.addresses.addressStr','STRING'),
 (1,25,'device_licenseKey','Застрахованное ус-во. ключ лицензии','device_licenseKey',114,'io_device_licenseKey','Застрахованное ус-во. ключ лицензии','insuredObjects[0].device.licenseKey','IN','1234567890','insuredObject.device.licenseKey','STRING'),
 (1,25,'device_osName','Застрахованное ус-во. название ОС','device_osName',117,'io_device_osName','Застрахованное ус-во. название ОС','insuredObjects[0].device.osName','IN','Android','insuredObject.device.osName','STRING'),
 (1,25,'device_typeCode','Застрахованное ус-во. код типа','device_typeCode',106,'io_device_typeCode','Застрахованное ус-во. код типа','insuredObjects[0].device.deviceTypeCode','IN','PHONE','insuredObject.device.deviceTypeCode','STRING'),
 (1,25,'device_name','Застрахованное ус-во. название','device_name',103,'io_device_name','Застрахованное ус-во. название','insuredObjects[0].device.deviceName','IN','Телефон','insuredObject.device.deviceName','STRING'),
 (1,25,'device_devicePrice','Застрахованное ус-во. цена','device_devicePrice',119,'io_device_devicePrice','Застрахованное ус-во. цена','insuredObjects[0].device.devicePrice','IN','10000','insuredObject.device.devicePrice','STRING'),
 (1,25,'device_countryCode','Застрахованное ус-во. код страны','device_countryCode',118,'io_device_countryCode','Застрахованное ус-во. код страны','insuredObjects[0].device.countryCode','IN','RU','insuredObject.device.countryCode','STRING'),
 (1,25,'device_tradeMark','Застрахованное ус-во. торговая марка','device_tradeMark',108,'io_device_tradeMark','Застрахованное ус-во. торговая марка','insuredObjects[0].device.tradeMark','IN','Samsung','insuredObject.device.tradeMark','STRING'),
 (1,25,'device_osVersion','Застрахованное ус-во. версия ОС','device_osVersion',116,'io_device_osVersion','Застрахованное ус-во. версия ОС','insuredObjects[0].device.osVersion','IN','10','insuredObject.device.osVersion','STRING'),
 (1,25,'device_imei','Застрахованное ус-во. IMEI','device_imei',115,'io_device_imei','Застрахованное ус-во. IMEI','insuredObjects[0].device.imei','IN','1234567890','insuredObject.device.imei','STRING'),
 (1,25,'device_model','Застрахованное ус-во. модель','device_model',110,'io_device_model','Застрахованное ус-во. модель','insuredObjects[0].device.model','IN','Galaxy S21','insuredObject.device.model','STRING'),
 (1,25,'device_serialNr','Застрахованное ус-во. серийный номер','device_serialNr',112,'io_device_serialNr','Застрахованное ус-во. серийный номер','insuredObjects[0].device.serialNr','IN','1234567890','insuredObject.device.serialNr','STRING'),
 (1,26,'ceilingMaterial','Имущество.материал перекрытий','ceilingMaterial',123,'io_ceilingMaterial','Имущество.материал перекрытий','insuredObjects[0].property.ceilingMaterial','IN','Смешанные','insuredObject.property.ceilingMaterial','STRING'),
 (1,26,'isNewBuilding','Имущество.новостройка','isNewBuilding',133,'io_isNewBuilding','Имущество.новостройка','insuredObjects[0].property.isNewBuilding','IN','NULL','insuredObject.property.isNewBuilding','STRING'),
 (1,26,'propertyLocation','Имущество.расположение имущества','propertyLocation',132,'io_propertyLocation','Имущество.расположение имущества','insuredObjects[0].property.propertyLocation','IN','В многоквартирном доме','insuredObject.property.propertyLocation','STRING'),
 (1,26,'numberOfFloors','Имущество.количество этажей','numberOfFloors',131,'io_numberOfFloors','Имущество.количество этажей','insuredObjects[0].property.numberOfFloors','IN','0','insuredObject.property.numberOfFloors','NUMBER'),
 (1,26,'wearCoefficient','Имущество.коэффициент износа','wearCoefficient',130,'io_wearCoefficient','Имущество.коэффициент износа','insuredObjects[0].property.wearCoefficient','IN','0','insuredObject.property.wearCoefficient','NUMBER'),
 (1,26,'buildingValue','Имущество.стоимость здания','buildingValue',129,'io_buildingValue','Имущество.стоимость здания','insuredObjects[0].property.buildingValue','IN','0','insuredObject.property.buildingValue','NUMBER'),
 (1,26,'landArea','Имущество.площадь участка','landArea',128,'io_landArea','Имущество.площадь участка','insuredObjects[0].property.landArea','IN','0','insuredObject.property.landArea','NUMBER'),
 (1,26,'buildingArea','Имущество.площадь здания','buildingArea',127,'io_buildingArea','Имущество.площадь здания','insuredObjects[0].property.buildingArea','IN','0','insuredObject.property.buildingArea','NUMBER'),
 (1,26,'repairYear','Имущество.год ремонта','repairYear',126,'io_repairYear','Имущество.год ремонта','insuredObjects[0].property.repairYear','IN','NULL','insuredObject.property.repairYear','STRING'),
 (1,26,'constructionYear','Имущество.год постройки','constructionYear',125,'io_constructionYear','Имущество.год постройки','insuredObjects[0].property.constructionYear','IN','NULL','insuredObject.property.constructionYear','STRING'),
 (1,26,'ceilingMaterialOther','Имущество.материал перекрытий другой','ceilingMaterialOther',124,'io_ceilingMaterialOther','Имущество.материал перекрытий другой','insuredObjects[0].property.ceilingMaterialOther','IN','Смешанные','insuredObject.property.ceilingMaterialOther','STRING'),
 (1,26,'wallsMaterialOther','Имущество.материал стен другой','wallsMaterialOther',122,'io_wallsMaterialOther','Имущество.материал стен другой','insuredObjects[0].property.wallsMaterialOther','IN','Каменные, кирпичные','insuredObject.property.wallsMaterialOther','STRING'),
 (1,26,'wallsMaterial','Имущество.материал стен','wallsMaterial',121,'io_wallsMaterial','Имущество.материал стен','insuredObjects[0].property.wallsMaterial','IN','Каменные, кирпичные','insuredObject.property.wallsMaterial','STRING'),
 (1,26,'floor','Имущество.этаж','floor',136,'io_floor','Имущество.этаж','insuredObjects[0].property.floor','IN','0','insuredObject.property.floor','NUMBER'),
 (1,26,'cadastrNr','Имущество.кадастровый номер','cadastrNr',120,'io_cadastrNr','Имущество.кадастровый номер','insuredObjects[0].property.cadastrNr','IN','77:07:0018002:2590','insuredObject.property.cadastrNr','STRING'),
 (1,26,'propertyTypeCode','Имущество.тип имущества код','propertyTypeCode',104,'io_propertyTypeCode','Имущество.тип имущества код','insuredObjects[0].property.propertyType.code','IN','NULL','insuredObject.property.propertyType.code','STRING'),
 (1,26,'commissioningDate','Имущество.дата ввода в эксплуатацию','commissioningDate',135,'io_commissioningDate','Имущество.дата ввода в эксплуатацию','insuredObjects[0].property.commissioningDate','IN','NULL','insuredObject.property.commissioningDate','STRING'),
 (1,26,'propertyValue','Имущество.стоимость имущества','propertyValue',134,'io_propertyValue','Имущество.стоимость имущества','insuredObjects[0].property.propertyValue','IN','0','insuredObject.property.propertyValue','NUMBER'),
 (1,27,'arrivalCity','город прилета','arrivalCity',113,'io_arrivalCity','город прилета','insuredObjects[0].travelSegments[*].arrivalCity','IN','NULL','insuredObject.travelSegments.arrivalCity','STRING'),
 (1,27,'departureCity','город вылета','departureCity',111,'io_departureCity','город вылета','insuredObjects[0].travelSegments[*].departureCity','IN','NULL','insuredObject.travelSegments.departureCity','STRING'),
 (1,27,'ticketPrice','стоимость билета','ticketPrice',53,'io_ticketPrice','стоимость билета','insuredObjects[0].travelSegments[*].ticketPrice.sum()','IN','NULL','insuredObject.travelSegments.ticketPrice','NUMBER'),
 (1,27,'legs','количество перелетов','legs',56,'io_legs','количество перелетов','insuredObjects[0].travelSegments[*].count()','IN','NULL','insuredObject.travelSegments.legs','NUMBER'),
 (1,27,'departureTime','время вылета','departureTime',109,'io_departureTime','время вылета','insuredObjects[0].travelSegments[*].departureTime','IN','NULL','insuredObject.travelSegments.departureTime','STRING'),
 (1,27,'departureDate','дата вылета','departureDate',107,'io_departureDate','дата вылета','insuredObjects[0].travelSegments[*].departureDate','IN','NULL','insuredObject.travelSegments.departureDate','STRING'),
 (1,27,'ticketNr','номер билета','ticketNr',105,'io_ticketNr','номер билета','insuredObjects[0].travelSegments[*].ticketNr','IN','NULL','insuredObject.travelSegments.ticketNr','STRING'),
 (1,28,'rf_sport5','Доп.риск спорт #5','rf_sport5',149,'rf_sport5','Доп.риск спорт #5','insuredObjects[0].riskFactors.sport5','IN','0','insuredObject.riskFactors.sport5','STRING'),
 (1,28,'rf_sport1','Доп.риск спорт #1','rf_sport1',145,'rf_sport1','Доп.риск спорт #1','insuredObjects[0].riskFactors.sport1','IN','0','insuredObject.riskFactors.sport1','STRING'),
 (1,28,'rf_sport3','Доп.риск спорт #3','rf_sport3',147,'rf_sport3','Доп.риск спорт #3','insuredObjects[0].riskFactors.sport3','IN','0','insuredObject.riskFactors.sport3','STRING'),
 (1,28,'rf_sport2','Доп.риск спорт #2','rf_sport2',146,'rf_sport2','Доп.риск спорт #2','insuredObjects[0].riskFactors.sport2','IN','0','insuredObject.riskFactors.sport2','STRING'),
 (1,28,'rf_sport4','Доп.риск спорт #4','rf_sport4',148,'rf_sport4','Доп.риск спорт #4','insuredObjects[0].riskFactors.sport4','IN','0','insuredObject.riskFactors.sport4','STRING'),
 (1,28,'rf_profSport','Профессиональный спорт','rf_profSport',150,'rf_profSport','Профессиональный спорт','insuredObjects[0].riskFactors.profSport','IN','0','insuredObject.riskFactors.profSport','STRING');