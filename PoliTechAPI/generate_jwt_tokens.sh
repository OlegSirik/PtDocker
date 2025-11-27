#!/bin/bash

# Скрипт для генерации тестовых JWT токенов
# Использование: ./generate_jwt_tokens.sh

echo "=== PoliTech API - JWT Token Generator ==="
echo ""

# Функция для генерации base64url encoded строки
base64url_encode() {
    echo -n "$1" | openssl base64 -e | tr -d '=' | tr '/+' '_-' | tr -d '\n'
}

# Header (алгоритм HS256)
header='{"alg":"HS256","typ":"JWT"}'
header_base64=$(base64url_encode "$header")

# Секретный ключ (замените на ваш)
secret="your-secret-key-change-this-in-production"

echo "📋 Генерация токенов..."
echo ""

# ADMIN токен
echo "1️⃣ ADMIN TOKEN"
admin_payload='{"sub":"admin","exp":1999999999,"iat":1700000000,"role":"ADMIN"}'
admin_payload_base64=$(base64url_encode "$admin_payload")
admin_token_unsigned="$header_base64.$admin_payload_base64"

# Простая подпись (для тестирования, без реальной криптографии)
admin_signature=$(echo -n "$admin_token_unsigned" | openssl dgst -sha256 -hmac "$secret" -binary | base64 | tr -d '=' | tr '/+' '_-' | tr -d '\n')
admin_token="$admin_token_unsigned.$admin_signature"

echo "Username: admin"
echo "Role: ADMIN"
echo "Token:"
echo "$admin_token"
echo ""

# USER токен
echo "2️⃣ USER TOKEN"
user_payload='{"sub":"user","exp":1999999999,"iat":1700000000,"role":"USER"}'
user_payload_base64=$(base64url_encode "$user_payload")
user_token_unsigned="$header_base64.$user_payload_base64"
user_signature=$(echo -n "$user_token_unsigned" | openssl dgst -sha256 -hmac "$secret" -binary | base64 | tr -d '=' | tr '/+' '_-' | tr -d '\n')
user_token="$user_token_unsigned.$user_signature"

echo "Username: user"
echo "Role: USER"
echo "Token:"
echo "$user_token"
echo ""

# PRODUCT_MANAGER токен
echo "3️⃣ PRODUCT_MANAGER TOKEN"
pm_payload='{"sub":"product_manager","exp":1999999999,"iat":1700000000,"role":"USER"}'
pm_payload_base64=$(base64url_encode "$pm_payload")
pm_token_unsigned="$header_base64.$pm_payload_base64"
pm_signature=$(echo -n "$pm_token_unsigned" | openssl dgst -sha256 -hmac "$secret" -binary | base64 | tr -d '=' | tr '/+' '_-' | tr -d '\n')
pm_token="$pm_token_unsigned.$pm_signature"

echo "Username: product_manager"
echo "Role: USER"
echo "Token:"
echo "$pm_token"
echo ""

# Сохранение токенов в файл
cat > jwt_tokens.txt << EOF
=== PoliTech API JWT Tokens ===

1. ADMIN TOKEN:
$admin_token

2. USER TOKEN:
$user_token

3. PRODUCT_MANAGER TOKEN:
$pm_token

=== Как использовать ===

В Postman:
1. Откройте Environment "PoliTech API Environment"
2. Скопируйте нужный токен
3. Вставьте в переменную jwt_token или admin_token/user_token

В curl:
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/auth/me

=== Создание пользователей в БД ===

psql -U postgres -d pt-db

-- ADMIN пользователь
INSERT INTO acc_logins (id, tid, user_login) VALUES (nextval('account_seq'), 1, 'admin');
INSERT INTO acc_account_logins (id, tid, user_login, client_id, account_id, user_role, is_default)
VALUES (nextval('account_seq'), 1, 'admin', 1, 1, 'ADMIN', true);

-- USER пользователь
INSERT INTO acc_logins (id, tid, user_login) VALUES (nextval('account_seq'), 1, 'user');
INSERT INTO acc_account_logins (id, tid, user_login, client_id, account_id, user_role, is_default)
VALUES (nextval('account_seq'), 1, 'user', 1, 1, 'USER', true);

-- PRODUCT_MANAGER пользователь
INSERT INTO acc_logins (id, tid, user_login) VALUES (nextval('account_seq'), 1, 'product_manager');
INSERT INTO acc_account_logins (id, tid, user_login, client_id, account_id, user_role, is_default)
VALUES (nextval('account_seq'), 1, 'product_manager', 1, 1, 'USER', true);

EOF

echo "✅ Токены сохранены в файл: jwt_tokens.txt"
echo ""
echo "🔗 Проверить токены на: https://jwt.io"
echo ""
echo "⚠️  ВАЖНО: Эти токены для тестирования! Не используйте в production!"

