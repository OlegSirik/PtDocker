-- Миграция для создания системного администратора (SYS_ADMIN)
-- Создает ROOT tenant, client, account и привязывает к нему пользователя sys_admin

-- 1. Создать ROOT tenant, если его еще нет
INSERT INTO acc_tenants (id, name, code, auth_type, storage_type, auth_config, record_status, created_at, updated_at)
SELECT 1, 'sys', 'sys', 'LOCAL_JWT', 'DB', '{}', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM acc_tenants WHERE id = 1);

-- 2. Создать ROOT client, если его еще нет
INSERT INTO acc_clients (tid, id, auth_client_id, name, auth_level, record_status, created_at, updated_at)
SELECT 1, 2, 'sys', 'ROOT Client', 'USER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM acc_clients WHERE id = 1);

-- 3. Создать ROOT account с типом ROOT, если его еще нет
INSERT INTO acc_accounts (id, tid, client_id, parent_id, node_type, name, created_at, updated_at, id_path)
values( 1, 1, NULL, NULL, 'TENANT', 'SYS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '1');

INSERT INTO acc_accounts (id, tid, client_id, parent_id, node_type, name, created_at, updated_at, id_path)
values( 2, 1, 2, 1, 'CLIENT', 'Admin Client', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'1.2');

INSERT INTO acc_accounts (id, tid, client_id, parent_id, node_type, name, created_at, updated_at, id_path)
values( 3, 1, 2, 2, 'SYS_ADMIN', 'Sys Admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'1');

INSERT INTO acc_accounts (id, tid, client_id, parent_id, node_type, name, created_at, updated_at, id_path)
values( 4, 1, 2, 2, 'ACCOUNT', 'Default account', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'1.2.4');

UPDATE acc_clients SET default_account_id = 4 where id = 2;

-- 4. Создать логин sys_admin, если его еще нет
INSERT INTO acc_logins (tid, user_login, full_name, created_at, updated_at)
SELECT 1, 'sys_admin', 'sys_admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM acc_logins WHERE tid = 1 AND user_login = 'sys_admin');

update acc_logins
    set "password" = ('$2a$10$cT1hdYm9sAvP8oXcGwE/euBMoSmoJq7IlXQSfknwBTgDcH2OD/aI.')
where user_login = 'sys_admin';

-- 5. Привязать sys_admin к ROOT account с ролью SYS_ADMIN
INSERT INTO acc_account_logins (tid, client_id, account_id, user_login, is_default, created_at, updated_at)
SELECT 1, 2, 3, 'sys_admin', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM acc_account_logins
    WHERE tid = 1 AND user_login = 'sys_admin' AND account_id = 3
);

---

-- Создает DEMO tenant, client, account и привязывает к нему пользователя tnt_admin

-- 1. Создать ROOT tenant, если его еще нет
INSERT INTO acc_tenants (id, name, code, auth_type, storage_type, auth_config, record_status, created_at, updated_at)
SELECT 10, 'demo', 'demo', 'LOCAL_JWT', 'DB', '{}', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM acc_tenants WHERE id = 10);

-- 2. Создать ROOT client, если его еще нет
INSERT INTO acc_clients (tid, id, auth_client_id, name, auth_level, record_status, created_at, updated_at)
SELECT 10, 12, 'sys', 'ROOT Client', 'USER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM acc_clients WHERE id = 12);

-- 3. Создать ROOT account с типом ROOT, если его еще нет
INSERT INTO acc_accounts (id, tid, client_id, parent_id, node_type, name, created_at, updated_at, id_path)
values( 10, 10, NULL, NULL, 'TENANT', 'DEMO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '10');

INSERT INTO acc_accounts (id, tid, client_id, parent_id, node_type, name, created_at, updated_at, id_path)
values( 12, 10, 12, 10, 'CLIENT', 'Default Admin App Client', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'10.12');

INSERT INTO acc_accounts (id, tid, client_id, parent_id, node_type, name, created_at, updated_at, id_path)
values( 14, 10, 12, 12, 'TNT_ADMIN', 'TNT_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'10');

INSERT INTO acc_accounts (id, tid, client_id, parent_id, node_type, name, created_at, updated_at, id_path)
values( 16, 10, 12, 12, 'PRODUCT_ADMIN', 'PRODUCT_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'10');

INSERT INTO acc_accounts (id, tid, client_id, parent_id, node_type, name, created_at, updated_at, id_path)
values( 18, 10, 12, 12, 'ACCOUNT', 'Default account for client', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,'10.12.18');

UPDATE acc_clients SET default_account_id = 18 where id = 12;

-- 4. Создать логин sys_admin, если его еще нет
INSERT INTO acc_logins (tid, user_login, full_name, created_at, updated_at)
SELECT 10, 'demo', 'demo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM acc_logins WHERE tid = 10 AND user_login = 'demo');

update acc_logins
    set "password" = ('$2a$10$atj8WRXny4D2eLFjTv5eR.PI4tfo6DGM1yWyC8DpBgu2pmUw3XXMm')
where user_login = 'demo';

-- 5. Привязать sys_admin к ROOT account с ролью SYS_ADMIN
INSERT INTO acc_account_logins (tid, client_id, account_id, user_login, is_default, created_at, updated_at)
SELECT 10, 12, 14, 'demo', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM acc_account_logins
    WHERE tid = 10 AND user_login = 'demo' AND account_id = 14
);

INSERT INTO acc_account_logins (tid, client_id, account_id, user_login, is_default, created_at, updated_at)
SELECT 10, 12, 16, 'demo', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM acc_account_logins
    WHERE tid = 10 AND user_login = 'demo' AND account_id = 16
);
