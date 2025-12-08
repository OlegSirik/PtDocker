DROP INDEX IF EXISTS idx_acc_logins_login;
DROP INDEX IF EXISTS idx_acc_logins_user_login;
DROP INDEX IF EXISTS idx_acc_logins_is_deleted;
DROP INDEX IF EXISTS idx_acc_logins_tid_active;

ALTER TABLE acc_logins DROP CONSTRAINT IF EXISTS user_login;

ALTER TABLE acc_logins
ADD CONSTRAINT uk_acc_logins_user_login_tid UNIQUE (user_login, tid);

CREATE INDEX IF NOT EXISTS idx_acc_logins_user_login_tid ON acc_logins (user_login, tid);
CREATE INDEX IF NOT EXISTS idx_acc_logins_tid ON acc_logins (tid);
