# Security Checks Inventory

Сводка всех проверок безопасности в проекте: Resource + Action и роли.

## 1. AuthorizationService.check() — проверки на уровне сервисов

### ClientService
| Resource | Action | Контекст |
|----------|--------|----------|
| CLIENT | VIEW | getClient(clientId) |
| CLIENT | VIEW | getDefaultAccount(clientId, defaultAccountId) |
| CLIENT | MANAGE | createClient() |
| CLIENT | MANAGE | updateClient(client, clientAccount) |
| CLIENT | LIST | getClients() |
| CLIENT | VIEW | getClientAccount(id, account) |
| CLIENT_PRODUCTS | LIST | getClientProducts(clientId, account) |
| CLIENT_PRODUCTS | MANAGE | updateClientProducts(clientId, account) |

### AccountServiceImpl
| Resource | Action | Контекст |
|----------|--------|----------|
| ACCOUNT | VIEW | getAccountById(id) |
| ACCOUNT | MANAGE | createGroup(parentId) |
| ACCOUNT | VIEW | getGroups(parentId) |
| ACCOUNT | MANAGE | createAccount(parentId) |
| ACCOUNT | LIST | getAccounts(parentId) |
| ACCOUNT | MANAGE | createSubaccount(parentId) |
| ACCOUNT | VIEW | getSubaccounts(parentId) |
| ACCOUNT_PRODUCT | VIEW | getProductRole(accountId, productId) |
| ACCOUNT_PRODUCT | MANAGE | grantProduct(accountId) |
| ACCOUNT_PRODUCT | MANAGE | revokeProduct(accountId, productId) |
| ACCOUNT_PRODUCT | LIST | getProductRolesByAccountId(accountId) |
| ACCOUNT | VIEW | getPathToRoot(accountId) |

### AccountLoginServiceImpl
| Resource | Action | Контекст |
|----------|--------|----------|
| LOGIN | MANAGE | addLogin(accountId, login) |
| LOGIN | LIST | getLoginsByAccountId(accountId) |
| LOGIN | MANAGE | deleteLogin(accountId, userLogin) |

### AccountTokenServiceImpl
| Resource | Action | Контекст |
|----------|--------|----------|
| TOKEN | MANAGE | addToken(accountId, token) |
| TOKEN | LIST | getTokens(accountId) |
| TOKEN | MANAGE | deleteToken(accountId, token) |

### SchemaServiceImpl
| Resource | Action | Контекст |
|----------|--------|----------|
| CONTRACT | LIST | getModels() |
| CONTRACT | MANAGE | createModel() |
| CONTRACT | MANAGE | updateModel() |
| CONTRACT | MANAGE | deleteModel() |
| CONTRACT | LIST | getSections() |
| CONTRACT | MANAGE | createSection() |
| CONTRACT | MANAGE | updateSection() |
| CONTRACT | MANAGE | deleteSection() |
| CONTRACT | LIST | getEntities() |
| CONTRACT | MANAGE | createEntity() |
| CONTRACT | MANAGE | updateEntity() |
| CONTRACT | MANAGE | deleteEntity() |
| CONTRACT | LIST | getAttributes() |
| CONTRACT | MANAGE | createAttribute() |
| CONTRACT | MANAGE | updateAttribute() |
| CONTRACT | MANAGE | deleteAttribute() |

### ProductServiceImpl (checkProductAccess)
| Resource | Action | Контекст |
|----------|--------|----------|
| PRODUCT | LIST | listSummaries() |
| PRODUCT | ALL | create() |
| PRODUCT | GO2PROD | go2Prod(productId) |
| PRODUCT | VIEW | getVersion(id) |
| PRODUCT | CREATE | createVersion(id) |
| PRODUCT | ALL | updateVersion(id), deleteVersion(id), replaceVersion(id) |
| PRODUCT | VIEW | getVersionByCode(code), getVersionByProductCode(code) |
| PRODUCT | VIEW | getVersionByAccount(accountId) |
| PRODUCT | VIEW | getVersionByProductCodeAndAccount(code, accountId) |
| PRODUCT | ALL | syncVars(productId) |

### LobServiceImpl
| Resource | Action | Контекст |
|----------|--------|----------|
| LOB | LIST | list() |
| LOB | VIEW | getByCode(code), getById(id) |
| LOB | MANAGE | create(), update(id), delete(id), updateByCode() |

### ProcessOrchestratorService
| Resource | Action | Контекст |
|----------|--------|----------|
| POLICY | TEST | userHasPermition (quote flow) — доступ к DEV-версии |
| PRODUCT | QUOTE | quote() |
| POLICY | TEST | userHasPermition (sell flow) — доступ к DEV-версии |
| PRODUCT | SELL | sell() |

---

## 2. @PreAuthorize — проверки на уровне контроллеров

### TenantManagementController
| Роли | Endpoint |
|------|----------|
| SYS_ADMIN, TNT_ADMIN | GET /admin/tenants |
| SYS_ADMIN, TNT_ADMIN | POST /admin/tenants |
| SYS_ADMIN, TNT_ADMIN | PUT /admin/tenants/{tid} |
| SYS_ADMIN | DELETE /admin/tenants/{tid} |

### ProductRoleManagementController
| Роли | Endpoint |
|------|----------|
| TNT_ADMIN, GROUP_ADMIN | POST /admin/product-roles |
| TNT_ADMIN, GROUP_ADMIN | PUT /admin/product-roles/{id} |
| TNT_ADMIN, GROUP_ADMIN | DELETE /admin/product-roles/{id} |
| TNT_ADMIN, GROUP_ADMIN, PRODUCT_ADMIN | GET /admin/product-roles/account/{accountId} |

### LoginManagementController
| Роли | Endpoint |
|------|----------|
| SYS_ADMIN, TNT_ADMIN, CLIENT_ADMIN, GROUP_ADMIN | POST /logins |
| SYS_ADMIN, TNT_ADMIN, CLIENT_ADMIN, GROUP_ADMIN | PATCH /logins/{loginId} |
| SYS_ADMIN, TNT_ADMIN, CLIENT_ADMIN, GROUP_ADMIN, SALES | PUT /logins/{loginId} |
| SYS_ADMIN, TNT_ADMIN | GET /logins |
| SYS_ADMIN, TNT_ADMIN, GROUP_ADMIN | DELETE /logins/{loginId} |

### ClientConfigurationController
| Роли | Endpoint |
|------|----------|
| TNT_ADMIN, SYS_ADMIN | GET /admin/clients/{clientId}/configuration |
| TNT_ADMIN, SYS_ADMIN | PUT /admin/clients/{clientId}/configuration |

### FileController
| Роли | Endpoint |
|------|----------|
| SYS_ADMIN | POST /admin/files (commented) |
| (none, commented) | POST /admin/files upload |

### Закомментированные @PreAuthorize
| Контроллер | Роль | Статус |
|------------|------|--------|
| CalculatorController | SYS_ADMIN | закомментировано |
| ProductController | SYS_ADMIN, TNT_ADMIN, PRODUCT_ADMIN | закомментировано |
| LobController | SYS_ADMIN | закомментировано |
| AuthenticationController | SYS_ADMIN | закомментировано |

---

## 3. SecurityConfig — HTTP-уровень

```java
authorizeHttpRequests(authz -> {
    authz.requestMatchers(security.publicUrls).permitAll();
    authz.anyRequest().authenticated();
});
```

- Публичные URL из `security.publicUrls` — без аутентификации
- Все остальные запросы — требуют аутентификации

---

## 4. SecuredController — программные проверки

| Метод | Условие | Исключение |
|-------|---------|------------|
| requireAdmin() | SYS_ADMIN | UnauthorizedException |
| requireRole(role) | указанная роль | UnauthorizedException |
| getCurrentUser() | authenticated | UnauthorizedException |
| requireProductRead(productCode) | read access | UnauthorizedException |
| requireProductQuote(productCode) | quote access | (закомментировано) |
| requireProductPolicy(productCode) | policy access | UnauthorizedException |
| requireProductWrite(productCode) | write access | UnauthorizedException |

---

## 5. AuthZ — полный перечень ResourceType и Action

### ResourceType
- TENANT, CLIENT, CLIENT_PRODUCTS, TENANT_ADMIN
- LOB, PRODUCT, ACCOUNT, ACCOUNT_PRODUCT
- TOKEN, LOGIN, POLICY, CONTRACT

### Action
- ALL, VIEW, LIST, MANAGE
- GO2PROD, CREATE
- QUOTE, SELL, TEST

---

## 6. Матрица ролей (AuthZMatrix)

См. `ru.pt.auth.security.permitions.AuthZMatrix` — определяет, какие Action доступны для каждого ResourceType и Role.

---

## 7. Сводная таблица Resource + Action (authService.check)

| Resource | Action | Сервис |
|----------|--------|--------|
| ACCOUNT | VIEW | AccountServiceImpl |
| ACCOUNT | MANAGE | AccountServiceImpl |
| ACCOUNT | LIST | AccountServiceImpl |
| ACCOUNT_PRODUCT | VIEW | AccountServiceImpl |
| ACCOUNT_PRODUCT | MANAGE | AccountServiceImpl |
| ACCOUNT_PRODUCT | LIST | AccountServiceImpl |
| CLIENT | VIEW | ClientService |
| CLIENT | MANAGE | ClientService |
| CLIENT | LIST | ClientService |
| CLIENT_PRODUCTS | LIST | ClientService |
| CLIENT_PRODUCTS | MANAGE | ClientService |
| CONTRACT | LIST | SchemaServiceImpl |
| CONTRACT | MANAGE | SchemaServiceImpl |
| LOB | LIST | LobServiceImpl |
| LOB | VIEW | LobServiceImpl |
| LOB | MANAGE | LobServiceImpl |
| LOGIN | MANAGE | AccountLoginServiceImpl |
| LOGIN | LIST | AccountLoginServiceImpl |
| POLICY | TEST | ProcessOrchestratorService (userHasPermition) |
| PRODUCT | LIST | ProductServiceImpl |
| PRODUCT | ALL | ProductServiceImpl |
| PRODUCT | GO2PROD | ProductServiceImpl |
| PRODUCT | VIEW | ProductServiceImpl |
| PRODUCT | CREATE | ProductServiceImpl |
| PRODUCT | QUOTE | ProcessOrchestratorService |
| PRODUCT | SELL | ProcessOrchestratorService |
| TOKEN | MANAGE | AccountTokenServiceImpl |
| TOKEN | LIST | AccountTokenServiceImpl |
