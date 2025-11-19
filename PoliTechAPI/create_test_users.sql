-- SQL скрипт для создания тестовых пользователей
-- Использование: psql -U postgres -d pt-db -f create_test_users.sql

-- Проверка и создание тенанта
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM acc_tenants WHERE id = 1) THEN
        INSERT INTO acc_tenants (id, name, is_deleted)
        VALUES (1, 'Test Tenant', false);
    END IF;
END $$;

-- Проверка и создание клиента
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM acc_clients WHERE id = 1) THEN
        INSERT INTO acc_clients (id, tid, client_id, name, is_deleted)
        VALUES (1, 1, 'test-client', 'Test Client', false);
    END IF;
END $$;

-- Проверка и создание аккаунта
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM acc_accounts WHERE id = 1) THEN
        INSERT INTO acc_accounts (id, tid, client_id, name, node_type)
        VALUES (1, 1, 1, 'Test Account', 'ACCOUNT');
    END IF;
END $$;

-- Создание пользователя ADMIN
DO $$
BEGIN
    -- Удаляем если существует
    DELETE FROM acc_account_logins WHERE user_login = 'admin';
    DELETE FROM acc_logins WHERE user_login = 'admin';

    -- Создаем логин
    INSERT INTO acc_logins (id, tid, user_login)
    VALUES (nextval('account_seq'), 1, 'admin');

    -- Привязываем к аккаунту с ролью ADMIN
    INSERT INTO acc_account_logins (id, tid, user_login, client_id, account_id, user_role, is_default)
    VALUES (nextval('account_seq'), 1, 'admin', 1, 1, 'ADMIN', true);

    RAISE NOTICE 'Пользователь ADMIN создан: username=admin, role=ADMIN';
END $$;

-- Создание пользователя USER
DO $$
BEGIN
    -- Удаляем если существует
    DELETE FROM acc_account_logins WHERE user_login = 'user';
    DELETE FROM acc_logins WHERE user_login = 'user';

    -- Создаем логин
    INSERT INTO acc_logins (id, tid, user_login)
    VALUES (nextval('account_seq'), 1, 'user');

    -- Привязываем к аккаунту с ролью USER
    INSERT INTO acc_account_logins (id, tid, user_login, client_id, account_id, user_role, is_default)
    VALUES (nextval('account_seq'), 1, 'user', 1, 1, 'USER', true);

    RAISE NOTICE 'Пользователь USER создан: username=user, role=USER';
END $$;

-- Создание пользователя PRODUCT_MANAGER
DO $$
BEGIN
    -- Удаляем если существует
    DELETE FROM acc_account_logins WHERE user_login = 'product_manager';
    DELETE FROM acc_logins WHERE user_login = 'product_manager';

    -- Создаем логин
    INSERT INTO acc_logins (id, tid, user_login)
    VALUES (nextval('account_seq'), 1, 'product_manager');

    -- Привязываем к аккаунту с ролью USER
    INSERT INTO acc_account_logins (id, tid, user_login, client_id, account_id, user_role, is_default)
    VALUES (nextval('account_seq'), 1, 'product_manager', 1, 1, 'USER', true);

    RAISE NOTICE 'Пользователь PRODUCT_MANAGER создан: username=product_manager, role=USER';
END $$;

-- Добавление прав на продукты для product_manager (опционально)
DO $$
BEGIN
    -- Создаем тестовый продукт если не существует
    IF NOT EXISTS (SELECT 1 FROM acc_product_roles WHERE account_id = 1) THEN
        INSERT INTO acc_product_roles (
            id, tid, account_id, role_product_id, role_account_id,
            is_deleted, can_read, can_quote, can_policy, can_addendum, can_cancel, can_prolongate
        ) VALUES (
            nextval('account_seq'), 1, 1, 1, 1,
            false, true, true, true, false, false, false
        );

        RAISE NOTICE 'Права на продукт добавлены для account_id=1';
    END IF;
END $$;

-- Вывод информации о созданных пользователях
SELECT
    l.user_login as "Username",
    al.user_role as "Role",
    al.is_default as "Is Default",
    a.name as "Account Name",
    c.name as "Client Name"
FROM acc_logins l
JOIN acc_account_logins al ON l.user_login = al.user_login AND l.tid = al.tid
JOIN acc_accounts a ON al.account_id = a.id
JOIN acc_clients c ON al.client_id = c.id
WHERE l.user_login IN ('admin', 'user', 'product_manager')
ORDER BY l.user_login;

-- Информация о правах на продукты
SELECT
    al.user_login as "Username",
    pr.role_product_id as "Product ID",
    pr.can_read as "Can Read",
    pr.can_quote as "Can Quote",
    pr.can_policy as "Can Policy"
FROM acc_account_logins al
JOIN acc_product_roles pr ON al.account_id = pr.account_id
WHERE al.user_login IN ('admin', 'user', 'product_manager')
ORDER BY al.user_login;

\echo ''
\echo '=== Тестовые пользователи созданы ==='
\echo ''
\echo 'Используйте скрипт generate_jwt_tokens.sh для генерации JWT токенов'
\echo 'или создайте токены вручную на https://jwt.io'
\echo ''
\echo 'Примеры JWT payload:'
\echo ''
\echo '1. ADMIN: {"sub":"admin","exp":1999999999,"iat":1700000000,"role":"ADMIN"}'
\echo '2. USER: {"sub":"user","exp":1999999999,"iat":1700000000,"role":"USER"}'
\echo '3. PRODUCT_MANAGER: {"sub":"product_manager","exp":1999999999,"iat":1700000000,"role":"USER"}'

