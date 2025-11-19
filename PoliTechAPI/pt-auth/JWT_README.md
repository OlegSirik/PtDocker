# JWT Authentication - Быстрый старт

## ✅ Система авторизации на базе JWT

Система авторизации переработана для работы с JWT токенами без проверки пароля.

### 🔑 Основные компоненты

#### 1. JwtTokenUtil
Утилита для парсинга JWT токенов без проверки подписи:
- Извлекает username (subject) из токена
- Проверяет срок действия токена
- Извлекает claims из payload

#### 2. JwtAuthenticationFilter
Фильтр Spring Security для JWT:
- Извлекает токен из заголовка `Authorization: Bearer <token>`
- Парсит username из токена
- Загружает UserDetails из БД по username
- Устанавливает аутентификацию в SecurityContext

#### 3. UserDetailsImpl
Контекст пользователя без пароля:
- ID пользователя, логин
- Tenant ID, Client ID, Account ID
- Роль пользователя и роли продуктов
- Методы проверки прав доступа

#### 4. SecurityConfig
Конфигурация Spring Security с JWT фильтром

## 🚀 Как использовать

### 1. Формат запроса

```bash
# HTTP заголовок
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Пример с curl
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/auth/me
```

### 2. Формат JWT токена

JWT токен должен содержать в payload:
```json
{
  "sub": "username",  // Обязательно - логин пользователя
  "exp": 1700000000,  // Обязательно - время истечения (unix timestamp)
  "iat": 1699900000   // Опционально - время создания
}
```

### 3. Использование в коде

```java
@RestController
public class MyController {
    
    // Получение текущего пользователя
    @GetMapping("/my-data")
    public ResponseEntity<?> getData(
            @AuthenticationPrincipal UserDetailsImpl user) {
        
        Long accountId = user.getAccountId();
        String username = user.getUsername();
        Long tenantId = user.getTenantId();
        
        // Ваша бизнес-логика
        return ResponseEntity.ok(data);
    }
    
    // Через SecurityContextHelper
    @Autowired
    private SecurityContextHelper securityHelper;
    
    @GetMapping("/another")
    public ResponseEntity<?> another() {
        Long accountId = securityHelper.getCurrentAccountId()
            .orElseThrow(() -> new UnauthorizedException());
        
        return ResponseEntity.ok(data);
    }
}
```

### 4. Проверка прав доступа

```java
// Аннотации Spring Security
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasRole('PRODUCT_CODE_READ')")

// Программно
if (!user.canPerformAction("PRODUCT_CODE", "READ")) {
    throw new ForbiddenException("No read access");
}

// Через SecurityContextHelper
if (!securityHelper.canPerformAction("PRODUCT_CODE", "QUOTE")) {
    throw new ForbiddenException("No quote access");
}
```

## 📋 Настройка

### application.properties

```properties
# JWT секрет (опционально, используется только если нужна проверка подписи)
jwt.secret=your-secret-key-here

# База данных
spring.datasource.url=jdbc:postgresql://localhost:5432/pt-db
spring.datasource.username=postgres
spring.datasource.password=postgres

# Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration

# Security debug (опционально)
logging.level.org.springframework.security=DEBUG
logging.level.ru.pt.auth.security=DEBUG
```

## 🗄️ База данных

### Применение миграций

Flyway автоматически применит миграцию при запуске:
- Добавит поле `user_role` в `acc_account_logins`
- Создаст индексы для оптимизации

### Создание пользователя

```sql
-- Пользователь в таблице acc_logins (без пароля)
INSERT INTO acc_logins (id, tid, user_login) 
VALUES (nextval('account_seq'), 1, 'testuser');

-- Привязка к аккаунту с ролью
INSERT INTO acc_account_logins (id, tid, user_login, client_id, account_id, is_default, user_role) 
VALUES (nextval('account_seq'), 1, 'testuser', 1, 100, true, 'USER');

-- Добавление прав на продукт
INSERT INTO acc_product_roles (id, tid, account_id, role_product_id, role_account_id, can_read, can_quote) 
VALUES (nextval('account_seq'), 1, 100, 1, 100, true, true);
```

## 🧪 Тестирование

### 1. Создать тестовый JWT токен

Используйте https://jwt.io для создания токена:

```json
{
  "sub": "testuser",
  "exp": 1999999999,
  "iat": 1700000000
}
```

### 2. Протестировать endpoints

```bash
# Получить информацию о текущем пользователе
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/auth/me

# Проверить контекст
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/auth/context

# Проверить доступ к продуктам
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/auth/check-product-access
```

### 3. Ожидаемый ответ

```json
{
  "id": 1,
  "username": "testuser",
  "tenantId": 1,
  "accountId": 100,
  "accountName": "Test Account",
  "clientId": 1,
  "clientName": "Test Client",
  "userRole": "USER",
  "productRoles": ["PRODUCT_CODE", "PRODUCT_CODE_READ", "PRODUCT_CODE_QUOTE"],
  "authorities": [
    {"authority": "ROLE_USER"},
    {"authority": "ROLE_PRODUCT_CODE_READ"}
  ],
  "isDefault": true
}
```

## ⚠️ Важно

### Без проверки подписи
Текущая реализация **НЕ проверяет подпись JWT токена**. Токен парсится напрямую из base64.

Это сделано специально, так как:
1. JWT токен приходит от внешней системы
2. Проверка подлинности происходит в другом сервисе
3. Мы просто извлекаем username и загружаем данные из БД

### Если нужна проверка подписи

Добавьте зависимость в `build.gradle.kts`:
```kotlin
implementation("io.jsonwebtoken:jjwt-api:0.12.3")
implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
```

И обновите `JwtTokenUtil`:
```java
public String getUsernameFromToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
}
```

## 🔒 Безопасность

### Что проверяется:
✅ Формат токена (3 части разделенные точкой)  
✅ Наличие username в payload  
✅ Срок действия токена (exp claim)  
✅ Наличие пользователя в БД  

### Что НЕ проверяется:
❌ Подпись токена (signature)  
❌ Алгоритм шифрования  
❌ Issuer (iss claim)  

## 🛠️ Troubleshooting

### Ошибка: "401 Unauthorized"

**Причины:**
1. Нет заголовка Authorization
2. Неправильный формат токена
3. Токен истек (exp claim)
4. Пользователь не найден в БД

**Решение:**
```bash
# Проверьте формат
Authorization: Bearer YOUR_TOKEN_HERE

# Проверьте токен на jwt.io
# Проверьте наличие пользователя в БД
SELECT * FROM acc_logins WHERE user_login = 'your_username';
```

### Ошибка: "Cannot parse JWT token"

**Причина:** Неправильный формат токена

**Решение:**
- JWT должен состоять из 3 частей: `header.payload.signature`
- Проверьте на https://jwt.io
- Убедитесь что токен не поврежден при передаче

### Ошибка: "User not found"

**Причина:** Пользователь с таким логином отсутствует в `acc_logins`

**Решение:**
```sql
-- Создать пользователя
INSERT INTO acc_logins (id, tid, user_login) 
VALUES (nextval('account_seq'), 1, 'username_from_jwt');

-- Привязать к аккаунту
INSERT INTO acc_account_logins (id, tid, user_login, client_id, account_id, user_role, is_default) 
VALUES (nextval('account_seq'), 1, 'username_from_jwt', 1, 100, 'USER', true);
```

## 📚 Структура файлов

```
pt-auth/
├── src/main/java/ru/pt/auth/
│   ├── controller/
│   │   └── AuthController.java
│   ├── entity/
│   │   └── LoginEntity.java (без поля password)
│   ├── repository/
│   │   └── LoginRepository.java
│   ├── security/
│   │   ├── UserDetailsImpl.java (без password)
│   │   ├── UserDetailsServiceImpl.java
│   │   ├── JwtTokenUtil.java (НОВЫЙ)
│   │   ├── JwtAuthenticationFilter.java (НОВЫЙ)
│   │   ├── SecurityContextHelper.java
│   │   └── SecurityConfig.java (обновлен для JWT)
│   └── service/
├── src/main/resources/
│   └── db/migration/
│       └── V2__add_auth_fields.sql
└── JWT_README.md (этот файл)
```

## 🎯 Основные изменения

✅ Удалено поле `userPassword` из `LoginEntity`  
✅ Удален `BasicAuthenticationProvider`  
✅ Добавлен `JwtTokenUtil` для парсинга JWT  
✅ Добавлен `JwtAuthenticationFilter`  
✅ Обновлен `SecurityConfig` для JWT  
✅ Обновлен `UserDetailsImpl` (без пароля)  
✅ Миграция обновлена (без добавления password)  

## 🚀 Готово к использованию!

Система полностью настроена для работы с JWT токенами без проверки пароля.

---

**Дата обновления:** 18 ноября 2025  
**Версия:** 2.0 (JWT)  
**Статус:** ✅ Готово к использованию

