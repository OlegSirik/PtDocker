# Проверка прав доступа в контроллерах

## ✅ Что было сделано

Добавлена проверка прав доступа во все контроллеры приложения с использованием JWT авторизации.

## 📋 Обновленные контроллеры

### 1. AdminProductController
**Путь:** `/admin/products`  
**Требуемая роль:** `ADMIN`  
**Тип проверки:** `@PreAuthorize("hasRole('ADMIN')")` на уровне класса

#### Endpoints с проверкой:
- `GET /admin/products` - список продуктов (ADMIN)
- `POST /admin/products` - создание продукта (ADMIN)
- `GET /admin/products/{id}/versions/{versionNo}` - получение версии (ADMIN)
- `POST /admin/products/{id}/versions/{versionNo}/cmd/create` - создание версии (ADMIN)
- `PUT /admin/products/{id}/versions/{versionNo}` - обновление версии (ADMIN)
- `DELETE /admin/products/{id}` - удаление продукта (ADMIN)
- `DELETE /admin/products/{id}/versions/{versionNo}` - удаление версии (ADMIN)
- `GET /admin/products/{id}/versions/{versionNo}/example_quote` - пример quote (ADMIN)
- `GET /admin/products/{id}/versions/{versionNo}/example_save` - пример save (ADMIN)

### 2. AdminFileController
**Путь:** `/admin/files`  
**Требуемая роль:** `ADMIN`  
**Тип проверки:** `@PreAuthorize("hasRole('ADMIN')")` на уровне класса

#### Endpoints с проверкой:
- `POST /admin/files` - создание метаданных файла (ADMIN)
- `POST /admin/files/{id}` - загрузка файла (ADMIN)
- `DELETE /admin/files/{id}` - удаление файла (ADMIN)
- `GET /admin/files` - список файлов (ADMIN)
- `GET /admin/files/{id}` - скачивание файла (ADMIN)
- `POST /admin/files/{id}/cmd/process` - обработка файла (ADMIN)

### 3. AdminCalculatorController
**Путь:** `/admin/**`  
**Требуемая роль:** `ADMIN`  
**Тип проверки:** `@PreAuthorize("hasRole('ADMIN')")` на уровне класса

#### Endpoints с проверкой:
- `GET /admin/products/{productId}/versions/{versionNo}/packages/{packageNo}/calculator` - получение калькулятора (ADMIN)
- `POST /admin/products/{productId}/versions/{versionNo}/packages/{packageNo}/calculator` - создание калькулятора (ADMIN)
- `PUT /admin/products/{productId}/versions/{versionNo}/packages/{packageNo}/calculator` - обновление калькулятора (ADMIN)
- `GET /admin/calculator/{calculatorId}/coefficients/{code}` - получение коэффициентов (ADMIN)
- `POST /admin/calculator/{calculatorId}/coefficients/{code}` - создание коэффициентов (ADMIN)
- `PUT /admin/calculator/{calculatorId}/coefficients/{code}` - обновление коэффициентов (ADMIN)
- `POST /admin/calculator/{id}/prc/syncvars` - синхронизация переменных (ADMIN)

### 4. AdminLobController
**Путь:** `/admin/lobs`  
**Требуемая роль:** `ADMIN`  
**Тип проверки:** `@PreAuthorize("hasRole('ADMIN')")` на уровне класса

#### Endpoints с проверкой:
- `GET /admin/lobs` - список LOB (ADMIN)
- `GET /admin/lobs/{code}` - получение LOB по коду (ADMIN)
- `POST /admin/lobs` - создание LOB (ADMIN)
- `PUT /admin/lobs/{code}` - обновление LOB (ADMIN)
- `DELETE /admin/lobs/{id}` - удаление LOB (ADMIN)
- `GET /admin/lobs/{code}/example` - пример JSON (ADMIN)

### 5. DbController
**Путь:** `/db`  
**Требуемая роль:** Аутентифицированный пользователь  
**Тип проверки:** Программная проверка в каждом методе

#### Endpoints с проверкой:
- `POST /db/policies` - создание политики (требуется аутентификация)
- `PUT /db/policies/{policyNumber}` - обновление политики (требуется аутентификация)
- `GET /db/policies/{id}` - получение политики (требуется аутентификация)

> **Note:** В DbController добавлены TODO комментарии для проверки прав на конкретный продукт после извлечения productCode из запроса/политики.

## 🔧 Базовый класс SecuredController

Создан базовый класс для всех контроллеров с проверкой прав:

```java
public abstract class SecuredController {
    
    // Проверка роли ADMIN
    protected void requireAdmin(UserDetailsImpl user)
    
    // Проверка права на чтение продукта
    protected void requireProductRead(UserDetailsImpl user, String productCode)
    
    // Проверка права на создание расчётов
    protected void requireProductQuote(UserDetailsImpl user, String productCode)
    
    // Проверка права на создание полисов
    protected void requireProductPolicy(UserDetailsImpl user, String productCode)
    
    // Проверка права на изменения продукта
    protected void requireProductWrite(UserDetailsImpl user, String productCode)
    
    // Проверка аутентификации
    protected void requireAuthenticated(UserDetailsImpl user)
    
    // Получение ID аккаунта
    protected Long getAccountId(UserDetailsImpl user)
    
    // Получение ID клиента
    protected Long getClientId(UserDetailsImpl user)
    
    // Получение ID тенанта
    protected Long getTenantId(UserDetailsImpl user)
}
```

## 🔐 Типы проверки прав

### 1. Аннотации Spring Security (на уровне класса)

```java
@RestController
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController extends SecuredController {
    // Все методы требуют роль ADMIN
}
```

### 2. Аннотации Spring Security (на уровне метода)

```java
@GetMapping("/product/{code}")
@PreAuthorize("hasRole('PRODUCT_' + #code + '_READ')")
public ResponseEntity<Product> getProduct(@PathVariable String code) {
    // Динамическая проверка прав
}
```

### 3. Программная проверка

```java
@GetMapping("/product/{code}")
public ResponseEntity<Product> getProduct(
        @AuthenticationPrincipal UserDetailsImpl user,
        @PathVariable String code) {
    requireProductRead(user, code);
    // Бизнес-логика
}
```

## 📝 Примеры использования

### Пример 1: Контроллер только для ADMIN

```java
@RestController
@RequestMapping("/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
public class SettingsController extends SecuredController {
    
    @GetMapping
    public ResponseEntity<Settings> getSettings(
            @AuthenticationPrincipal UserDetailsImpl user) {
        requireAdmin(user); // Дополнительная проверка
        return ResponseEntity.ok(settings);
    }
}
```

### Пример 2: Проверка прав на продукт

```java
@RestController
@RequestMapping("/api/quotes")
public class QuoteController extends SecuredController {
    
    @PostMapping
    public ResponseEntity<Quote> createQuote(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody QuoteRequest request) {
        
        String productCode = request.getProductCode();
        requireProductQuote(user, productCode);
        
        Long accountId = getAccountId(user);
        Quote quote = quoteService.create(request, accountId);
        
        return ResponseEntity.ok(quote);
    }
}
```

### Пример 3: Комбинированная проверка

```java
@RestController
@RequestMapping("/api/policies")
public class PolicyController extends SecuredController {
    
    @PostMapping
    public ResponseEntity<Policy> createPolicy(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody PolicyRequest request) {
        
        // Проверяем аутентификацию
        requireAuthenticated(user);
        
        // Проверяем права на продукт
        String productCode = request.getProductCode();
        requireProductPolicy(user, productCode);
        
        // Или используем аннотацию + проверку ADMIN
        if (request.isSpecialCase()) {
            requireAdmin(user);
        }
        
        // Создаем политику с контекстом пользователя
        Long accountId = getAccountId(user);
        Long tenantId = getTenantId(user);
        
        Policy policy = policyService.create(request, accountId, tenantId);
        return ResponseEntity.ok(policy);
    }
}
```

## 🧪 Тестирование

### Создание JWT токена для тестирования

```bash
# Токен с ролью ADMIN
{
  "sub": "admin_user",
  "exp": 1999999999,
  "role": "ADMIN"
}

# Токен с правами на продукт
{
  "sub": "product_user",
  "exp": 1999999999,
  "role": "USER"
}
```

### Примеры запросов

```bash
# С токеном ADMIN
curl -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
     http://localhost:8080/admin/products

# Без токена (401 Unauthorized)
curl http://localhost:8080/admin/products

# С токеном USER (403 Forbidden)
curl -H "Authorization: Bearer YOUR_USER_TOKEN" \
     http://localhost:8080/admin/products
```

## ⚠️ Важные моменты

### 1. Порядок проверки

1. JWT фильтр извлекает токен и загружает пользователя
2. Spring Security проверяет `@PreAuthorize` аннотации
3. Контроллер выполняет дополнительные программные проверки
4. Если любая проверка не пройдена → `403 Forbidden` или `401 Unauthorized`

### 2. Ошибки доступа

```java
// AccessDeniedException автоматически преобразуется в 403 Forbidden
protected void requireAdmin(UserDetailsImpl user) {
    if (user == null || !"ADMIN".equals(user.getUserRole())) {
        throw new AccessDeniedException("Admin role required");
    }
}
```

### 3. Контекст пользователя

```java
// Всегда доступен через @AuthenticationPrincipal
@GetMapping("/my-data")
public ResponseEntity<?> getData(@AuthenticationPrincipal UserDetailsImpl user) {
    // user содержит:
    // - username, accountId, clientId, tenantId
    // - userRole (ADMIN, USER, etc.)
    // - productRoles (набор прав на продукты)
    return ResponseEntity.ok(data);
}
```

## 🚀 TODO: Дополнительные улучшения

### 1. DbController - проверка прав на продукт

В DbController нужно добавить извлечение `productCode` из запроса/политики:

```java
@PostMapping("/policies")
public ResponseEntity<PolicyData> createPolicy(
        @AuthenticationPrincipal UserDetailsImpl user,
        @RequestBody String request) {
    requireAuthenticated(user);
    
    // TODO: Парсить request и извлечь productCode
    String productCode = extractProductCodeFromRequest(request);
    requireProductPolicy(user, productCode);
    
    return ResponseEntity.ok(processOrchestrator.createPolicy(request));
}
```

### 2. Аудит действий пользователей

Добавить логирование всех действий с указанием пользователя:

```java
protected void requireAdmin(UserDetailsImpl user) {
    if (user == null || !"ADMIN".equals(user.getUserRole())) {
        logger.warn("Access denied for user: {}", user != null ? user.getUsername() : "anonymous");
        throw new AccessDeniedException("Admin role required");
    }
    logger.info("Admin action by user: {}", user.getUsername());
}
```

### 3. Кэширование прав пользователя

Для оптимизации можно кэшировать права на продукты:

```java
@Cacheable("user-product-roles")
public Set<String> getProductRoles(Long accountId) {
    // Загрузка из БД
}
```

## 📚 Документация

- **JWT авторизация:** `pt-auth/JWT_README.md`
- **Система авторизации:** `pt-auth/SECURITY_README.md`
- **Быстрый старт:** `pt-auth/QUICKSTART.md`

## ✅ Статус

Все контроллеры обновлены и защищены проверкой прав:

✅ AdminProductController - роль ADMIN  
✅ AdminFileController - роль ADMIN  
✅ AdminCalculatorController - роль ADMIN  
✅ AdminLobController - роль ADMIN  
✅ DbController - аутентификация (TODO: проверка прав на продукт)  

---

**Дата создания:** 18 ноября 2025  
**Версия:** 1.0  
**Статус:** ✅ Готово к использованию

