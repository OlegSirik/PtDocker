# Быстрый старт - Система авторизации

## Что было создано

### 1. Классы безопасности (пакет `ru.pt.auth.security`)

- **UserDetailsImpl** - контекст пользователя (аналог UserDetails)
- **UserDetailsServiceImpl** - сервис загрузки пользователей из БД
- **BasicAuthenticationProvider** - провайдер Basic Auth
- **SecurityContextHelper** - утилита для доступа к текущему пользователю
- **SecurityConfig** - конфигурация Spring Security

### 2. Обновления в Entity

- **LoginEntity** - добавлено поле `userPassword`
- **AccountLoginEntity** - уже имеет поле `userRole`

### 3. Обновления в Repository

- **LoginRepository** - добавлены методы поиска по логину и паролю
- **AccountLoginRepository** - добавлены методы поиска

### 4. Контроллер для тестирования

- **AuthController** - демонстрация работы с UserDetailsImpl

### 5. Миграции БД

- **V2__add_auth_fields.sql** - добавление полей для авторизации

## Как использовать

### В контроллерах

```java
@RestController
public class MyController {
    
    // Способ 1: Через @AuthenticationPrincipal
    @GetMapping("/my-endpoint")
    public ResponseEntity<?> myEndpoint(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long accountId = userDetails.getAccountId();
        String username = userDetails.getUsername();
        Set<String> roles = userDetails.getProductRoles();
        
        // Ваша логика
        return ResponseEntity.ok(data);
    }
    
    // Способ 2: Через SecurityContextHelper
    @Autowired
    private SecurityContextHelper securityHelper;
    
    @GetMapping("/another-endpoint")
    public ResponseEntity<?> anotherEndpoint() {
        Long accountId = securityHelper.getCurrentAccountId()
            .orElseThrow(() -> new UnauthorizedException());
        
        // Ваша логика
        return ResponseEntity.ok(data);
    }
}
```

### Проверка прав доступа

```java
// Аннотации Spring Security
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasRole('PRODUCT_CODE_READ')")

// Программно в коде
if (!userDetails.canPerformAction("PRODUCT_CODE", "READ")) {
    throw new ForbiddenException();
}

// Через SecurityContextHelper
if (!securityHelper.canPerformAction("PRODUCT_CODE", "READ")) {
    throw new ForbiddenException();
}
```

### Формат токена

```bash
# Basic Auth формат
Authorization: Basic base64(login:password)

# Пример с curl
curl -u admin:admin123 http://localhost:8080/api/auth/me
```

## Настройка БД

### 1. Применить миграции

Flyway автоматически применит миграции при старте приложения если настроен в `application.properties`:

```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
```

### 2. Добавить тестового пользователя

```sql
-- Добавить пароль существующему пользователю
UPDATE acc_logins 
SET user_password = 'admin123' 
WHERE user_login = 'admin';

-- Или создать нового
INSERT INTO acc_logins (id, tid, user_login, user_password) 
VALUES (nextval('account_seq'), 1, 'testuser', 'password123');
```

## Настройка application.properties

```properties
# Включить Spring Security
spring.security.user.name=admin
spring.security.user.password=admin123

# Для отладки (опционально)
logging.level.org.springframework.security=DEBUG
```

## Тестирование

### 1. Запустить приложение

```bash
./gradlew bootRun
```

### 2. Проверить аутентификацию

```bash
# Получить информацию о текущем пользователе
curl -u admin:admin123 http://localhost:8080/api/auth/me

# Проверить доступ к защищённому endpoint
curl -u admin:admin123 http://localhost:8080/api/auth/context
```

### 3. Ожидаемый ответ

```json
{
  "id": 1,
  "username": "admin",
  "tenantId": 1,
  "accountId": 100,
  "accountName": "Admin Account",
  "clientId": 1,
  "clientName": "Default Client",
  "userRole": "ADMIN",
  "productRoles": ["PRODUCT_CODE", "PRODUCT_CODE_READ", "PRODUCT_CODE_POLICY"],
  "authorities": [
    {"authority": "ROLE_ADMIN"},
    {"authority": "ROLE_PRODUCT_CODE"},
    {"authority": "ROLE_PRODUCT_CODE_READ"}
  ],
  "isDefault": true
}
```

## Проблемы и решения

### Ошибка: Cannot resolve symbol 'UserDetails'

**Причина:** Не подключена зависимость Spring Security

**Решение:** В `build.gradle.kts` раскомментируйте:
```kotlin
implementation("org.springframework.boot:spring-boot-starter-security")
```

### Ошибка: 401 Unauthorized

**Причина:** Неверные credentials или отсутствует пользователь в БД

**Решение:**
1. Проверьте формат токена: `Basic base64(login:password)`
2. Проверьте наличие пользователя в таблице `acc_logins`
3. Проверьте правильность пароля

### Ошибка: Flyway миграции не применяются

**Причина:** Flyway не настроен

**Решение:** Добавьте в `application.properties`:
```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

## Дополнительная документация

Подробная документация доступна в файле `SECURITY_README.md`

## Примеры интеграции

### Обновить существующий сервис

```java
@Service
public class YourService {
    
    @Autowired
    private SecurityContextHelper securityHelper;
    
    public Data getData() {
        // Получить ID аккаунта текущего пользователя
        Long accountId = securityHelper.getCurrentAccountId()
            .orElseThrow(() -> new UnauthorizedException());
        
        // Фильтровать данные по аккаунту
        return repository.findByAccountId(accountId);
    }
}
```

### Добавить проверку прав в контроллер

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping("/{code}")
    public ResponseEntity<Product> getProduct(
            @PathVariable String code,
            @AuthenticationPrincipal UserDetailsImpl user) {
        
        // Проверить право на чтение
        if (!user.canPerformAction(code, "READ")) {
            throw new ForbiddenException("No read access");
        }
        
        // Получить данные для аккаунта пользователя
        return ResponseEntity.ok(
            productService.getProduct(code, user.getAccountId())
        );
    }
}
```

## Контакты

При возникновении вопросов см. `SECURITY_README.md` или документацию Spring Security.

