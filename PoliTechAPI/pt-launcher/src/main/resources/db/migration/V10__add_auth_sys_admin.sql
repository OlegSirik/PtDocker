-- Миграция для создания системного администратора (SYS_ADMIN)
-- Создает ROOT tenant, client, account и привязывает к нему пользователя sys_admin

-- 1. Создать ROOT tenant, если его еще нет
INSERT INTO acc_tenants (id, name, is_deleted, created_at, updated_at)
SELECT 1, 'SYS', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM acc_tenants WHERE id = 1);

-- 2. Создать ROOT client, если его еще нет
INSERT INTO acc_clients (tid, id, client_id, name, is_deleted, created_at, updated_at)
SELECT 1, 1, 'SYS', 'ROOT Client', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM acc_clients WHERE id = 1);

-- 3. Создать ROOT account с типом ROOT, если его еще нет
INSERT INTO acc_accounts (id, tid, client_id, parent_id, node_type, name, created_at, updated_at)
SELECT 1, 1, NULL, NULL, 'TENANT', 'SYS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM acc_accounts WHERE id = 1);

INSERT INTO acc_accounts (id, tid, client_id, parent_id, node_type, name, created_at, updated_at)
SELECT 2, 1, 1, 1, 'CLIENT', 'Admin Client', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM acc_accounts WHERE id = 2);

-- 4. Создать логин sys_admin, если его еще нет
INSERT INTO acc_logins (tid, user_login, created_at, updated_at)
SELECT 1, 'sys_admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM acc_logins WHERE tid = 1 AND user_login = 'sys_admin');

-- 5. Привязать sys_admin к ROOT account с ролью SYS_ADMIN
INSERT INTO acc_account_logins (tid, client_id, account_id, user_login, is_default, user_role, created_at, updated_at)
SELECT 1, 1, 2, 'sys_admin', TRUE, 'SYS_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM acc_account_logins
    WHERE tid = 1 AND user_login = 'sys_admin' AND account_id = 1
);

-- 6. Обновить последовательности, чтобы они начинались с правильных значений
SELECT setval('acc_tenants_seq', (SELECT COALESCE(MAX(id), 2) FROM acc_tenants));
SELECT setval('acc_clients_seq', (SELECT COALESCE(MAX(id), 2) FROM acc_clients));
SELECT setval('acc_accounts_seq', (SELECT COALESCE(MAX(id), 2) FROM acc_accounts));

