# 🚀 Быстрый запуск тестирования PoliTech API

## 📦 Содержимое

Для тестирования API созданы следующие файлы:

1. **PoliTech_API.postman_collection.json** - Postman коллекция с 40+ запросами
2. **PoliTech_API.postman_environment.json** - Environment с переменными
3. **POSTMAN_GUIDE.md** - Подробная инструкция по использованию
4. **generate_jwt_tokens.sh** - Скрипт генерации JWT токенов
5. **create_test_users.sql** - SQL скрипт создания тестовых пользователей
6. **QUICK_START.md** - Этот файл

## ⚡ Быстрый старт (5 минут)

### Шаг 1: Запустите базу данных

```bash
# Запуск PostgreSQL через Docker
docker-compose up -d postgres

# Или если уже запущен
docker ps | grep postgres
```

### Шаг 2: Создайте тестовых пользователей

```bash
# Подключитесь к БД и выполните SQL скрипт
psql -h localhost -p 5432 -U postgres -d pt-db -f create_test_users.sql

# Введите пароль: postgres
```

Будут созданы 3 пользователя:
- **admin** (роль ADMIN)
- **user** (роль USER)  
- **product_manager** (роль USER)

### Шаг 3: Сгенерируйте JWT токены

**Способ A: Через скрипт (рекомендуется)**

```bash
./generate_jwt_tokens.sh
```

Токены будут сохранены в файл `jwt_tokens.txt`

**Способ B: Через jwt.io**

1. Откройте https://jwt.io
2. Вставьте payload:
```json
{
  "sub": "admin",
  "exp": 1999999999,
  "iat": 1700000000
}
```
3. Скопируйте токен из поля "Encoded"

### Шаг 4: Импортируйте в Postman

1. Откройте Postman
2. Import → выберите `PoliTech_API.postman_collection.json`
3. Import → выберите `PoliTech_API.postman_environment.json`
4. Выберите Environment "PoliTech API Environment" в правом верхнем углу
5. Откройте Environment → вставьте токен в переменную `jwt_token`

### Шаг 5: Запустите приложение

```bash
cd PoliTechAPI
./gradlew bootRun
```

Ждите сообщения: `Started PoliTechApplication in X.XXX seconds`

### Шаг 6: Протестируйте API

**В Postman:**

1. **Health Check** → 200 OK ✅
2. **Auth → Get Current User** → 200 OK ✅
3. **Admin Products → List Products** → 200 OK ✅

**В терминале:**

```bash
# Health check (без авторизации)
curl http://localhost:8080/actuator/health

# Get current user (с JWT токеном)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/auth/me
```

## 📋 Структура коллекции

### 🔐 Auth (4 запроса)
- Get Current User
- Get User Context
- Check Product Access
- Admin Only Endpoint

### 🏭 Admin Products (9 запросов)
- List, Create, Get, Update, Delete
- Version management
- JSON examples

### 📁 Admin Files (6 запросов)
- List, Upload, Download
- Process with variables
- Delete

### 🧮 Admin Calculator (7 запросов)
- Calculator CRUD
- Coefficients management
- Sync variables

### 📊 Admin LOBs (6 запросов)
- LOB management
- JSON examples

### 💾 Database Operations (3 запроса)
- Policy CRUD

### ❤️ Health Check (1 запрос)
- System health

## 🔑 JWT Токены

### ADMIN токен (полный доступ)

```json
{
  "sub": "admin",
  "exp": 1999999999,
  "iat": 1700000000,
  "role": "ADMIN"
}
```

**Доступ:**
- ✅ Все `/admin/**` endpoints
- ✅ Все `/db/**` endpoints
- ✅ Все `/api/auth/**` endpoints

### USER токен (ограниченный доступ)

```json
{
  "sub": "user",
  "exp": 1999999999,
  "iat": 1700000000,
  "role": "USER"
}
```

**Доступ:**
- ❌ `/admin/**` endpoints (403 Forbidden)
- ✅ `/db/**` endpoints
- ✅ `/api/auth/**` endpoints (кроме admin-only)

## 🧪 Примеры тестирования

### Тест 1: Проверка авторизации

```bash
# С ADMIN токеном → 200 OK
curl -H "Authorization: Bearer ADMIN_TOKEN" \
     http://localhost:8080/api/auth/admin-only

# Без токена → 401 Unauthorized
curl http://localhost:8080/api/auth/admin-only

# С USER токеном → 403 Forbidden
curl -H "Authorization: Bearer USER_TOKEN" \
     http://localhost:8080/api/auth/admin-only
```

### Тест 2: CRUD продуктов

```bash
TOKEN="YOUR_ADMIN_TOKEN"

# 1. Список продуктов
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/admin/products

# 2. Создать продукт
curl -X POST \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"productCode":"TEST","productName":"Test Product"}' \
     http://localhost:8080/admin/products

# 3. Получить версию
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/admin/products/1/versions/1
```

### Тест 3: Работа с файлами

```bash
TOKEN="YOUR_ADMIN_TOKEN"

# 1. Создать метаданные
curl -X POST \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"fileType":"pdf","fileDescription":"Test","productCode":"TEST","packageCode":"1"}' \
     http://localhost:8080/admin/files

# 2. Загрузить файл
curl -X POST \
     -H "Authorization: Bearer $TOKEN" \
     -F "file=@test.pdf" \
     http://localhost:8080/admin/files/1

# 3. Скачать файл
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/admin/files/1 -o downloaded.pdf
```

## 🐛 Troubleshooting

### ❌ 401 Unauthorized

**Проблема:** Токен отсутствует или невалиден

**Решение:**
1. Проверьте, что токен установлен в Postman Environment
2. Убедитесь, что `exp` (expiration) не истёк
3. Проверьте формат: `Bearer YOUR_TOKEN`

### ❌ 403 Forbidden

**Проблема:** Недостаточно прав

**Решение:**
1. Используйте ADMIN токен для `/admin/**` endpoints
2. Проверьте роль в токене на jwt.io
3. Убедитесь, что пользователь имеет нужную роль в БД

### ❌ Cannot connect to server

**Проблема:** Приложение не запущено

**Решение:**
```bash
# Проверьте статус
ps aux | grep java

# Запустите приложение
./gradlew bootRun

# Проверьте порт
lsof -i :8080
```

### ❌ User not found

**Проблема:** Пользователь не создан в БД

**Решение:**
```bash
# Выполните SQL скрипт
psql -U postgres -d pt-db -f create_test_users.sql

# Или вручную
psql -U postgres -d pt-db
INSERT INTO acc_logins (id, tid, user_login) VALUES (nextval('account_seq'), 1, 'admin');
```

## 📊 Ожидаемые результаты

### ✅ Health Check
```json
{
  "status": "UP"
}
```

### ✅ Get Current User
```json
{
  "id": 1,
  "username": "admin",
  "tenantId": 1,
  "accountId": 1,
  "accountName": "Test Account",
  "clientId": 1,
  "clientName": "Test Client",
  "userRole": "ADMIN",
  "productRoles": [],
  "authorities": [
    {"authority": "ROLE_ADMIN"}
  ],
  "isDefault": true
}
```

### ✅ List Products
```json
[
  {
    "id": 1,
    "productCode": "TEST_PRODUCT",
    "productName": "Test Product",
    "versions": [...]
  }
]
```

## 🔧 Переменные окружения Postman

Вы можете изменить в Environment:

| Переменная | Значение по умолчанию |
|------------|----------------------|
| `base_url` | `http://localhost:8080` |
| `jwt_token` | Ваш JWT токен |
| `admin_token` | ADMIN JWT токен |
| `user_token` | USER JWT токен |
| `product_code` | `TEST_PRODUCT` |
| `product_id` | `1` |

## 📚 Дополнительная документация

- **Полная инструкция Postman:** `POSTMAN_GUIDE.md`
- **JWT авторизация:** `pt-auth/JWT_README.md`
- **Проверка прав:** `pt-launcher/SECURITY_CONTROLLERS.md`
- **Система авторизации:** `pt-auth/SECURITY_README.md`

## ✅ Checklist готовности

- [ ] PostgreSQL запущен
- [ ] Тестовые пользователи созданы
- [ ] JWT токены сгенерированы
- [ ] Postman коллекция импортирована
- [ ] Environment активирован
- [ ] Токен установлен в переменную `jwt_token`
- [ ] Приложение запущено
- [ ] Health Check → 200 OK
- [ ] Get Current User → 200 OK

## 🎯 Следующие шаги

1. ✅ Протестируйте все endpoints из папки "Auth"
2. ✅ Создайте продукт через "Admin Products → Create Product"
3. ✅ Загрузите файл через "Admin Files → Upload File"
4. ✅ Создайте калькулятор через "Admin Calculator"
5. ✅ Протестируйте с разными токенами (ADMIN vs USER)

## 💡 Полезные команды

```bash
# Посмотреть токены
cat jwt_tokens.txt

# Перезапустить приложение
pkill -f java && ./gradlew bootRun

# Проверить логи
tail -f logs/application.log

# Подключиться к БД
psql -U postgres -d pt-db

# Проверить пользователей
SELECT * FROM acc_logins;
SELECT * FROM acc_account_logins;
```

---

**Дата:** 18 ноября 2025  
**Версия:** 1.0  
**Статус:** ✅ Готово к использованию

Удачного тестирования! 🚀

