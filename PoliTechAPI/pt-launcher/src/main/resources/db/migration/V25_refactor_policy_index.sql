-- код страховой компании
alter table policy_index add column ins_company varchar(10)
-- номер версии продлукта по которой был создан договор 

ALTER TABLE policy_index ADD COLUMN product_version_no INTEGER NOT NULL DEFAULT 1;

-- описани ph
ALTER TABLE policy_index ADD COLUMN ph_digest varchar(250);
-- описани io
ALTER TABLE policy_index ADD COLUMN io_digest varchar(250);

-- login пользователя продавшего полис
ALTER TABLE policy_index ADD COLUMN user_login varchar(250);

-- премия по договору
ALTER TABLE policy_index ADD COLUMN premium numeric(18, 2);

-- % агентского вознаграждения
alter table policy_index add agent_kv_percent numeric(18,2)

-- сумма агентского вознаграждения
alter table policy_index add agent_kv_amount numeric(18,2)

alter table policy_index drop column issue_timezone;

alter table acc_tenants add column auth_type varchar(20);

/*
Репозитории без tenant/tid:
ProductRepository — нет tid в таблице и репозитории
ProductVersionRepository — нет tid в таблице и репозитории
LobRepository — нет tid в таблице и репозитории
CalculatorRepository — нет tid в таблице и репозитории
CoefficientDataRepository — нет tid в таблице и репозитории
PolicyRepository — нет tid в таблице и репозитории
PolicyIndexRepository — нет tid, но есть косвенная связь через account_id
NumberGeneratorRepository — нет tid в таблице и репозитории
FileRepository — tid есть в Entity/таблице, но не используется в запросах
*/

alter table pt_lobs add column tid bigint not null default 1;

alter table pt_products add column tid bigint not null default 1;

alter table pt_product_versions add column tid bigint not null default 1;

alter table pt_calculators add column tid bigint not null default 1;

alter table coefficient_data add column tid bigint not null default 1;

alter table policy_index add column tid bigint not null default 1;

alter table policy_data add column tid bigint not null default 1;
alter table policy_data add column cid bigint not null default 1;

alter table number_generators add column tid bigint not null default 1;

alter table files add column tid bigint not null default 1;

