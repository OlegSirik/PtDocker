# Система авторизации pt-auth

## Описание

Система авторизации на базе Spring Security с поддержкой Basic Authentication. Реализует контекст пользователя по типу UserDetails с полной поддержкой ролей и прав доступа к продуктам.

## Архитектура

### Основные компоненты

1. **UserDetailsImpl** - реализация UserDetails, содержит информацию о пользователе:
   - ID пользователя
   - Логин и пароль
   - Tenant ID, Client ID, Account ID
   - Роль пользователя (userRole)
   - Роли продуктов (productRoles)
   - Права доступа (authorities)

2. **UserDetailsServiceImpl** - сервис для загрузки пользователя из БД:
   - `loadUserByUsername(String username)` - базовый метод Spring Security
   - `loadUserByUsernameAndAccountId(String username, Long accountId)` - загрузка с учётом аккаунта
   - `loadUserByUsernameAndClient(String username, String client)` - загрузка с учётом клиента

3. **BasicAuthenticationProvider** - провайдер для Basic Auth:
   - Парсит токен формата `Basic base64(login:password)`
   - Проверяет учётные данные в БД
   - Поддерживает как plain text, так и BCrypt пароли

4. **SecurityContextHelper** - утилита для доступа к текущему пользователю:
   - Получение текущего пользователя
   - Проверка прав доступа
   - Проверка ролей продуктов

5. **SecurityConfig** - конфигурация Spring Security

## Использование

### 1. Получение текущего пользователя в контроллере

#### Через @AuthenticationPrincipal

```java
@GetMapping("/me")
public ResponseEntity<UserInfo> getCurrentUser(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
    
    Long accountId = userDetails.getAccountId();
    String username = userDetails.getUsername();
    Set<String> productRoles = userDetails.getProductRoles();
    
    return ResponseEntity.ok(new UserInfo(username, accountId, productRoles));
}
```

#### Через SecurityContextHelper

```java
@RestController
public class MyController {
    
    private final SecurityContextHelper securityContextHelper;
    
    @GetMapping("/my-data")
    public ResponseEntity<Data> getMyData() {
        Long accountId = securityContextHelper.getCurrentAccountId()
            .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        
        Long tenantId = securityContextHelper.getCurrentTenantId()
            .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        
        // Бизнес-логика
        return ResponseEntity.ok(data);
    }
}
```

### 2. Проверка прав доступа

#### Через аннотации Spring Security

```java
@GetMapping("/admin-only")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<String> adminEndpoint() {
    return ResponseEntity.ok("Admin content");
}

@GetMapping("/product-read")
@PreAuthorize("hasRole('PRODUCT_CODE_READ')")
public ResponseEntity<String> readProduct() {
    return ResponseEntity.ok("Product data");
}

// Проверка нескольких ролей
@PostMapping("/product-policy")
@PreAuthorize("hasRole('PRODUCT_CODE_POLICY') and hasRole('ADMIN')")
public ResponseEntity<String> createPolicy() {
    return ResponseEntity.ok("Policy created");
}
```

#### Программная проверка в коде

```java
@GetMapping("/product/{productCode}")
public ResponseEntity<Product> getProduct(
        @PathVariable String productCode,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
    
    // Проверка конкретной роли продукта
    if (!userDetails.hasProductRole(productCode)) {
        throw new ForbiddenException("No access to product: " + productCode);
    }
    
    // Проверка возможности выполнить действие
    if (!userDetails.canPerformAction(productCode, "READ")) {
        throw new ForbiddenException("Cannot read product: " + productCode);
    }
    
    // Бизнес-логика
    return ResponseEntity.ok(product);
}
```

#### Через SecurityContextHelper

```java
@Service
public class ProductService {
    
    private final SecurityContextHelper securityContextHelper;
    
    public Product getProduct(String productCode) {
        if (!securityContextHelper.hasProductRole(productCode)) {
            throw new ForbiddenException("No access to product");
        }
        
        if (!securityContextHelper.canPerformAction(productCode, "READ")) {
            throw new ForbiddenException("Cannot read product");
        }
        
        // Бизнес-логика
        return product;
    }
}
```

### 3. Аутентификация через Basic Auth

#### Формат запроса

```bash
# Заголовок Authorization
Authorization: Basic base64(login:password)

# Пример с curl
curl -u admin:admin123 http://localhost:8080/api/auth/me

# Или явно с заголовком
curl -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" http://localhost:8080/api/auth/me
```

#### Пример кода (Java)

```java
// Создание Basic Auth токена
String credentials = "admin:admin123";
String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes());
String authHeader = "Basic " + base64Credentials;

// Использование в HTTP клиенте
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", authHeader);
```

## Структура ролей

### Роль пользователя (userRole)

Основная роль пользователя в системе, например:
- `ADMIN` - администратор
- `USER` - обычный пользователь
- `MANAGER` - менеджер

В Spring Security автоматически добавляется префикс `ROLE_`, т.е. для проверки используется `ROLE_ADMIN`, `ROLE_USER` и т.д.

### Роли продуктов (productRoles)

Определяют доступ к конкретным продуктам и операциям над ними:

- `PRODUCT_CODE` - базовый доступ к продукту
- `PRODUCT_CODE_READ` - право на чтение
- `PRODUCT_CODE_QUOTE` - право на создание расчётов
- `PRODUCT_CODE_POLICY` - право на создание полисов
- `PRODUCT_CODE_ADDENDUM` - право на создание дополнительных соглашений
- `PRODUCT_CODE_CANCEL` - право на отмену
- `PRODUCT_CODE_PROLONGATE` - право на пролонгацию

## База данных

### Таблицы

1. **acc_logins** - пользователи
   - `id` - ID записи
   - `tid` - ID тенанта
   - `user_login` - логин
   - `user_password` - пароль (plain text или BCrypt)

2. **acc_account_logins** - привязка пользователей к аккаунтам
   - `id` - ID записи
   - `tid` - ID тенанта
   - `user_login` - логин пользователя
   - `client_id` - ID клиента
   - `account_id` - ID аккаунта
   - `is_default` - является ли аккаунт дефолтным
   - `user_role` - роль пользователя (ADMIN, USER, и т.д.)

3. **acc_product_roles** - роли продуктов
   - `account_id` - ID аккаунта
   - `role_product_id` - ID продукта
   - `can_read`, `can_quote`, `can_policy` и т.д. - права доступа

### Миграции Flyway

Создайте файл миграции `src/main/resources/db/migration/V1__create_auth_system.sql` (уже создан в проекте).

## Конфигурация

### application.properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/pt-db
spring.datasource.username=postgres
spring.datasource.password=postgres

# Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.schemas=public

# Security (опционально, для отладки)
logging.level.org.springframework.security=DEBUG
```

### Отключение Security для определенных endpoints

В `SecurityConfig` настройте публичные endpoints:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/public/**", "/actuator/health").permitAll()
    .anyRequest().authenticated()
)
```

## Примеры использования

### Пример контроллера с разными уровнями доступа

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;
    private final SecurityContextHelper securityContextHelper;
    
    // Публичный endpoint
    @GetMapping("/public/list")
    public ResponseEntity<List<Product>> getPublicProducts() {
        return ResponseEntity.ok(productService.getPublicProducts());
    }
    
    // Требуется аутентификация
    @GetMapping("/{code}")
    public ResponseEntity<Product> getProduct(
            @PathVariable String code,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        if (!userDetails.canPerformAction(code, "READ")) {
            throw new ForbiddenException("Cannot read product: " + code);
        }
        
        Long accountId = userDetails.getAccountId();
        return ResponseEntity.ok(productService.getProduct(code, accountId));
    }
    
    // Требуется роль ADMIN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }
    
    // Требуется право на создание расчётов
    @PostMapping("/{code}/quote")
    @PreAuthorize("hasRole(#code + '_QUOTE')")
    public ResponseEntity<Quote> createQuote(
            @PathVariable String code,
            @RequestBody QuoteRequest request) {
        
        Long accountId = securityContextHelper.getCurrentAccountId()
            .orElseThrow(() -> new UnauthorizedException());
        
        return ResponseEntity.ok(productService.createQuote(code, request, accountId));
    }
}
```

## Тестирование

### Создание тестовых пользователей

```sql
-- Создать тенант
INSERT INTO acc_tenants (id, name) VALUES (1, 'Test Tenant');

-- Создать пользователя
INSERT INTO acc_logins (id, tid, user_login, user_password) 
VALUES (1, 1, 'testuser', 'password123');

-- Создать клиента
INSERT INTO acc_clients (id, tid, client_id, name) 
VALUES (1, 1, 'test-client', 'Test Client');

-- Создать аккаунт
INSERT INTO acc_accounts (id, tid, client_id, name, node_type) 
VALUES (1, 1, 1, 'Test Account', 'ACCOUNT');

-- Привязать пользователя к аккаунту
INSERT INTO acc_account_logins (id, tid, user_login, client_id, account_id, is_default, user_role) 
VALUES (1, 1, 'testuser', 1, 1, true, 'USER');

-- Добавить роль продукта
INSERT INTO acc_product_roles (id, tid, account_id, role_product_id, role_account_id, can_read, can_quote, can_policy) 
VALUES (1, 1, 1, 100, 1, true, true, false);
```

### Unit тесты

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetCurrentUser() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Basic " + 
                    Base64.getEncoder().encodeToString("testuser:password123".getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.accountId").exists());
    }
    
    @Test
    void testUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
```

## Troubleshooting

### Проблема: "Cannot resolve symbol 'UserDetails'"

**Решение:** Убедитесь, что в `build.gradle.kts` раскомментирована зависимость:
```kotlin
implementation("org.springframework.boot:spring-boot-starter-security")
```

### Проблема: "401 Unauthorized" при валидных credentials

**Решение:** 
1. Проверьте формат токена: `Basic base64(login:password)`
2. Проверьте наличие пользователя в БД
3. Включите debug логирование: `logging.level.org.springframework.security=DEBUG`

### Проблема: Пароль не совпадает

**Решение:** 
- Для plain text паролей они должны совпадать напрямую
- Для BCrypt паролей используйте BCryptPasswordEncoder для генерации хеша:
```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String encodedPassword = encoder.encode("password123");
```

## Дополнительные возможности

### Кастомные проверки доступа

```java
@Component("productSecurity")
public class ProductSecurityExpression {
    
    private final SecurityContextHelper securityContextHelper;
    
    public boolean canAccessProduct(String productCode) {
        return securityContextHelper.canPerformAction(productCode, "READ");
    }
}

// Использование в контроллере
@GetMapping("/{code}")
@PreAuthorize("@productSecurity.canAccessProduct(#code)")
public ResponseEntity<Product> getProduct(@PathVariable String code) {
    // ...
}
```

### Получение информации о всех аккаунтах пользователя

```java
@Service
public class UserAccountService {
    
    private final AccountLoginRepository accountLoginRepository;
    
    public List<AccountInfo> getUserAccounts(String username) {
        List<AccountLoginEntity> accountLogins = 
            accountLoginRepository.findByUserLogin(username);
        
        return accountLogins.stream()
            .map(this::mapToAccountInfo)
            .collect(Collectors.toList());
    }
}
```

## Контакты и поддержка

При возникновении вопросов обращайтесь к документации Spring Security:
- https://docs.spring.io/spring-security/reference/

