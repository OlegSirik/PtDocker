# Система авторизации - Сводка изменений

## ✅ Что было реализовано

### 1. Классы безопасности (ru.pt.auth.security)

#### UserDetailsImpl.java
Реализация интерфейса UserDetails для Spring Security.

**Содержит:**
- Информацию о пользователе (ID, логин, пароль)
- Контекст аккаунта (accountId, clientId, tenantId)
- Роли пользователя (userRole)
- Роли продуктов (productRoles)
- Методы проверки прав доступа:
  - `hasProductRole(String productRole)` - проверка роли продукта
  - `canPerformAction(String productCode, String action)` - проверка возможности действия

#### UserDetailsServiceImpl.java
Сервис для загрузки пользователей из базы данных.

**Методы:**
- `loadUserByUsername(String username)` - стандартный метод Spring Security
- `loadUserByUsernameAndAccountId(String username, Long accountId)` - загрузка с указанием аккаунта
- `loadUserByUsernameAndClient(String username, String client)` - загрузка с указанием клиента
- `getProductRoles(Long accountId)` - получение ролей продуктов для аккаунта

#### BasicAuthenticationProvider.java
AuthenticationProvider для Basic Authentication.

**Функционал:**
- Парсинг токена формата `Basic base64(login:password)`
- Проверка учетных данных в БД
- Поддержка plain text и BCrypt паролей

#### SecurityContextHelper.java
Утилита для удобного доступа к информации о текущем пользователе.

**Методы:**
- `getCurrentUser()` - получить UserDetailsImpl
- `getCurrentUserId()` - ID пользователя
- `getCurrentUsername()` - логин
- `getCurrentAccountId()` - ID аккаунта
- `getCurrentClientId()` - ID клиента
- `getCurrentTenantId()` - ID тенанта
- `isAuthenticated()` - проверка аутентификации
- `hasProductRole(String productRole)` - проверка роли продукта
- `canPerformAction(String productCode, String action)` - проверка действия

#### SecurityConfig.java
Конфигурация Spring Security.

**Настройки:**
- Basic Authentication
- Stateless сессии
- Публичные endpoints: `/api/public/**`, `/actuator/health`
- BCrypt encoder для паролей

### 2. Обновление Entity

#### LoginEntity.java
**Добавлено поле:**
- `userPassword` (String) - пароль пользователя

**Методы:**
- `getUserPassword()` - геттер
- `setUserPassword(String)` - сеттер

### 3. Обновление Repository

#### LoginRepository.java
**Добавлены методы:**
- `findByUserLogin(String userLogin)` - поиск по логину
- `findByUserLoginAndPassword(String userLogin, String userPassword)` - поиск по логину и паролю

#### AccountLoginRepository.java
**Добавлены методы:**
- `findByUserLogin(String userLogin)` - все аккаунты пользователя
- `findByUserLoginAndAccountId(String userLogin, Long accountId)` - конкретный аккаунт

### 4. Контроллер для демонстрации

#### AuthController.java
Демо-контроллер для тестирования системы авторизации.

**Endpoints:**
- `GET /api/auth/me` - информация о текущем пользователе
- `GET /api/auth/context` - информация через SecurityContextHelper
- `GET /api/auth/check-product-access` - проверка доступа к продуктам
- `GET /api/auth/admin-only` - только для ADMIN
- `GET /api/auth/product-read` - требует права READ на продукт

### 5. Миграции БД

#### V2__add_auth_fields.sql
Миграция для добавления полей авторизации:
- Добавление `user_password` в `acc_logins`
- Добавление `user_role` в `acc_account_logins`
- Создание индексов для оптимизации
- Установка дефолтных значений

### 6. Документация

#### SECURITY_README.md
Полная документация системы авторизации:
- Описание архитектуры
- Примеры использования
- Проверка прав доступа
- Работа с ролями
- Troubleshooting

#### QUICKSTART.md
Краткое руководство по быстрому старту:
- Что было создано
- Как использовать в коде
- Настройка БД
- Тестирование
- Решение проблем

#### CHANGES.md (этот файл)
Сводка всех изменений

### 7. Конфигурация

#### build.gradle.kts (pt-auth)
**Раскомментирована зависимость:**
```kotlin
implementation("org.springframework.boot:spring-boot-starter-security")
```

#### build.gradle.kts (pt-api)
**Добавлена совместимость:**
```kotlin
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

## 📋 Структура файлов

```
PoliTechAPI/pt-auth/
├── src/main/java/ru/pt/auth/
│   ├── controller/
│   │   └── AuthController.java (НОВЫЙ)
│   ├── entity/
│   │   ├── LoginEntity.java (ОБНОВЛЕН)
│   │   └── AccountLoginEntity.java (БЕЗ ИЗМЕНЕНИЙ)
│   ├── repository/
│   │   ├── LoginRepository.java (ОБНОВЛЕН)
│   │   └── AccountLoginRepository.java (ОБНОВЛЕН)
│   ├── security/ (НОВАЯ ПАПКА)
│   │   ├── UserDetailsImpl.java (НОВЫЙ)
│   │   ├── UserDetailsServiceImpl.java (НОВЫЙ)
│   │   ├── BasicAuthenticationProvider.java (НОВЫЙ)
│   │   ├── SecurityContextHelper.java (НОВЫЙ)
│   │   └── SecurityConfig.java (НОВЫЙ)
│   └── service/
│       └── AccountServiceImpl.java (БЕЗ ИЗМЕНЕНИЙ)
├── src/main/resources/
│   └── db/migration/
│       └── V2__add_auth_fields.sql (НОВЫЙ)
├── build.gradle.kts (ОБНОВЛЕН)
├── SECURITY_README.md (НОВЫЙ)
├── QUICKSTART.md (НОВЫЙ)
└── CHANGES.md (НОВЫЙ)
```

## 🚀 Как начать использовать

### 1. Обновить зависимости
```bash
./gradlew clean build
```

### 2. Применить миграции
Flyway автоматически применит миграции при запуске приложения.

### 3. Добавить тестовых пользователей
```sql
UPDATE acc_logins SET user_password = 'password123' WHERE user_login = 'your_login';
```

### 4. Использовать в коде
```java
@RestController
public class MyController {
    
    @GetMapping("/my-data")
    public ResponseEntity<?> getData(
            @AuthenticationPrincipal UserDetailsImpl user) {
        
        Long accountId = user.getAccountId();
        // Ваша логика
    }
}
```

## 🔑 Примеры аутентификации

### cURL
```bash
curl -u admin:admin123 http://localhost:8080/api/auth/me
```

### HTTP Header
```
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

### Java
```java
String credentials = "admin:admin123";
String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
headers.set("Authorization", "Basic " + encoded);
```

## 🛡️ Проверка прав

### Аннотации
```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasRole('PRODUCT_CODE_READ')")
```

### Программно
```java
if (!user.canPerformAction("PRODUCT_CODE", "READ")) {
    throw new ForbiddenException();
}
```

## 📊 Роли продуктов

Роли формируются автоматически из прав в `acc_product_roles`:

- `can_read` → `PRODUCT_CODE_READ`
- `can_quote` → `PRODUCT_CODE_QUOTE`
- `can_policy` → `PRODUCT_CODE_POLICY`
- `can_addendum` → `PRODUCT_CODE_ADDENDUM`
- `can_cancel` → `PRODUCT_CODE_CANCEL`
- `can_prolongate` → `PRODUCT_CODE_PROLONGATE`

## ⚠️ Известные проблемы

### IntelliJ IDEA показывает ошибки "Cannot resolve symbol"
**Причина:** IDE не подгрузила зависимости

**Решение:**
1. File → Invalidate Caches / Restart
2. Gradle → Reload All Gradle Projects
3. Подождать завершения индексации

### Конфликт версий Java между модулями
**Причина:** Разные версии JVM Toolchain

**Решение:** Установить Java 21 для всех модулей в build.gradle.kts:
```kotlin
kotlin {
    jvmToolchain(21)
}
```

## 📚 Дополнительная информация

- **Полная документация:** `SECURITY_README.md`
- **Быстрый старт:** `QUICKSTART.md`
- **Spring Security Docs:** https://docs.spring.io/spring-security/reference/

## ✨ Основные преимущества

1. ✅ Полная интеграция с Spring Security
2. ✅ Поддержка multi-tenancy (tenant, client, account)
3. ✅ Гибкая система ролей продуктов
4. ✅ Удобные утилиты для доступа к контексту
5. ✅ Поддержка как plain text, так и BCrypt паролей
6. ✅ Миграции БД через Flyway
7. ✅ Подробная документация с примерами

## 🎯 Следующие шаги

1. Интегрировать в существующие контроллеры
2. Добавить проверки прав в бизнес-логику
3. Настроить BCrypt для паролей (опционально)
4. Добавить JWT токены (опционально)
5. Настроить CORS (опционально)

---

**Дата создания:** 18 ноября 2025  
**Версия:** 1.0  
**Статус:** ✅ Готово к использованию

