-- Миграция для системы авторизации
-- Добавляет поле user_role в таблицу acc_account_logins

-- Добавить поле user_role в acc_account_logins если его нет
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'acc_account_logins' AND column_name = 'user_role') THEN
        ALTER TABLE acc_account_logins ADD COLUMN user_role VARCHAR(30) DEFAULT 'USER';
    END IF;
END $$;

-- Установить дефолтные значения для существующих записей
UPDATE acc_account_logins SET user_role = 'USER' WHERE user_role IS NULL;

-- Сделать поле обязательным
ALTER TABLE acc_account_logins ALTER COLUMN user_role SET NOT NULL;

-- Создать индексы для оптимизации поиска
CREATE INDEX IF NOT EXISTS idx_acc_logins_user_login ON acc_logins(user_login, tid);
CREATE INDEX IF NOT EXISTS idx_acc_account_logins_user ON acc_account_logins(user_login, client_id);
CREATE INDEX IF NOT EXISTS idx_acc_account_tokens_token ON acc_account_tokens(token, client_id);