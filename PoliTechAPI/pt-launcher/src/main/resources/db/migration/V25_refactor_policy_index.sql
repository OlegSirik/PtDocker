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