# Keycloak аутентификация (AuthType.KEYCLOAK)

Документ описывает, как платформа интегрируется с внешним Keycloak для:

- валидации JWT токенов при входе запросов;
- заполнения `RequestContext` (tenant/client/login);
- привязки клиентов и логинов к пользователям и клиентам в Keycloak (на уровне админ‑API).

## 1. Общая идея

- Для тенанта в БД задаётся:
  - `authType = KEYCLOAK`
  - `auth_config` (JSONB) с настройками Keycloak:
    - `issuer` — ожидаемый `iss` в токене
    - `jwksUri` — JWKS endpoint (зарезервировано под будущую проверку подписи)
    - `audience` — ожидаемый `azp`/`aud`
    - `adminUrl`, `adminClientId`, `adminClientSecret` — опционально, для работы с Keycloak Admin API
- В цепочке фильтров `AccountResolutionFilter` выбирает стратегию
  `KeycloakIdentityStrategy` для таких тенантов.

Дополнительно для login/password:
- В `POST /api/v1/{tenantCode}/auth/login` токен получается через
  `List<ExternalJwtAuthenticator>`
- Для `authType = KEYCLOAK` (и совместимо для `JWT`) выбирается
  `KeycloakPasswordGrantAuthenticator`
- Аутентификатор делает password grant в Keycloak и возвращает `access_token`

## 2. KeycloakIdentityStrategy

Источник: `ru.pt.auth.security.strategy.KeycloakIdentityStrategy`

### Входные данные

- `Authorization: Bearer <jwt>` — access token от Keycloak
- `RequestContext.tenant` уже заполнен `TenantResolutionFilter` (код тенанта из URL)

### Поведение

1. **Извлечение токена**
   - Из заголовка `Authorization`, ожидается формат `Bearer <token>`.
   - При отсутствии токена → `BadCredentialsException("JWT token missing")`.

2. **Определение тенанта**
   - Берёт `tenantCode` из `RequestContext`.
   - Загружает `Tenant` через `TenantConfig.getTenant(tenantCode)`.
   - Проверяет, что `tenant.authType()` = `KEYCLOAK`, иначе выбрасывает `BadCredentialsException`.

3. **Валидация токена на основе `auth_config`**
   Если `tenant.authConfig` не пустой:

   - **Issuer**
     - Ключ `issuer` (`AuthProperties.ISSUER`).
     - Если указан, сравнивается с `iss` из токена.

   - **Audience / azp**
     - Ключ `audience` (`AuthProperties.AUDIENCE`).
     - Если указан, сравнивается с `azp` или `aud` из токена.

   - **Срок действия**
     - Проверяется через `JwtTokenUtil.isTokenExpired(jwt)` (использует `exp`).

   При несоответствии любого из условий выбрасывается `BadCredentialsException`.

4. **Извлечение идентичности**

   - Логин пользователя:
     - `preferred_username` → или `email` → или `sub` (в этом порядке).
     - Сохраняется в `RequestContext.login`.
   - Клиент:
     - `azp` → или `client_id`.
     - При наличии сохраняется в `RequestContext.client`.

5. **Ошибки**

   - Ошибка валидации claim'ов → `BadCredentialsException` → HTTP 401.
   - Любая неожиданная ошибка логируется и также приводит к `BadCredentialsException("Invalid Keycloak JWT token", e)`.

## 3. Создание клиентов и пользователей в Keycloak

Настройка и подробности см. в `PoliTechAPI/docs/PREPROJECT_IDP_OPENID_TENANT_AUTH.md`. Вкратце:

- При создании `Client` или `Login` в БД:
  - Если у тенанта `authType = KEYCLOAK` и в `auth_config` заданы
    `adminClientId` и `adminClientSecret`, сервисы:
    - `ClientService.createClient` вызывают `KeycloakService.createConfidentialClient(...)`
      для создания OIDC‑клиента в Keycloak.
    - `LoginManagementService.createLogin` вызывают
      `KeycloakService.createUserWithPassword(...)` для создания пользователя.
  - Ошибки на стороне Keycloak приводят к выбросу `InternalServerErrorException` и
    откату транзакции, поэтому запись в БД и объект в IdP создаются/откатываются вместе.

## 4. Получение токена по login/password (password grant)

Источник: `ru.pt.auth.identity.KeycloakPasswordGrantAuthenticator`

Используемые поля `auth_config`:
- `issuer` — базовый issuer (из него строится token endpoint):
  `{issuer}/protocol/openid-connect/token`
- `adminClientId` — используется как `client_id`
- `adminClientSecret` — используется как `client_secret`

Запрос в Keycloak:
- `grant_type=password`
- `username=<login>`
- `password=<password>`
- `client_id=<adminClientId>`
- `client_secret=<adminClientSecret>`

Результат:
- При успехе возвращается `access_token` (JWT)
- При ошибке возвращается ошибка `400` из `/auth/login` с текстом причины

