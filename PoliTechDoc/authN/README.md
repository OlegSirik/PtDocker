# Процесс аутентификации (AuthN)

В документе описан поток аутентификации во время выполнения, реализованный в
цепочке фильтров Spring Security, стратегии разрешения идентичности и фабрика
UserDetails, используемая платформой.

Область: вход запроса → заполнение SecurityContext. Авторизация (AuthZ) описана
отдельно в `authZ/*`.

## Общая схема потока

```
HTTP-запрос
  |
  v
TenantResolutionFilter
  |
  v
AccountResolutionFilter
  |
  v
IdentityResolutionFilter
  |
  v
TenantImpersonationFilter
  |
  v
ContextCleanupFilter
  |
  v
Controller
```

## Цепочка фильтров (фактический порядок)

Настраивается в `ru.pt.auth.configuration.SecurityConfig`.

```
UsernamePasswordAuthenticationFilter
  ^ (TenantResolutionFilter вставляется перед ним)
TenantResolutionFilter
  -> AccountResolutionFilter
    -> IdentityResolutionFilter
      -> TenantImpersonationFilter
        -> ContextCleanupFilter
```

### TenantResolutionFilter
Источник: `ru.pt.auth.security.filter.TenantResolutionFilter`

Обязанности:
- Извлечение кода тенанта из URL `/api/v{n}/{tenantCode}/...`
- Сохранение тенанта в `RequestContext`
- Пропуск, если запрос соответствует `security.publicUrls`

### AccountResolutionFilter
Источник: `ru.pt.auth.security.filter.AccountResolutionFilter`

Обязанности:
- Получение `TenantSecurityConfig` и выбор `IdentitySourceStrategy`
- Разрешение идентичности (JWT, заголовки, API-ключ и т.д.)
- Вызов `AccountResolverService.resolveAccounts()` для определения id аккаунта
- Сохранение client/login/account в `RequestContext`

!: X-Client-ID
TenantSecurityConfig получается из настроек тенанта. В тенанте указывается метод аутентификации используемый для всего тенанта.
Но есть исключения. Если какой-то клиент, не может использовать этот метод, то его можно переопределить для конкретного клиента.
Чтобы было понятно, что сейчас нужно брать не стандартный метод, передается ИД клиента, на котором этот метод определен. 
Если передали код не своего клиента, то далее будет ошибка. Она возникнет если клиент, аторизовавшийся не совпадет с X-CLient-ID.
Так исключается подмена метода авторизации на более легкий. 

!: X-Account-ID
К одному логину может быть "привязано" несколько различных Accounts. Например я могу быть админом и продавцом.
Но для каждой транзакции нужно явно понимать, кем в данный момент я представляюсь. От этого зависят permitions.
Если account не передан явно, то выберется account с флагом isDefaualt = true. Если такого нет, то любой.
Чтобы не полагаться на случай, можно явно передать account-id. Он должен принадлежать аутентифицирующемуся пользователю. Иначе возникнет ошибка.


### IdentityResolutionFilter
Источник: `ru.pt.auth.security.filter.IdentityResolutionFilter`

Обязанности:
- При отсутствии аутентификации — построение `UserDetails` по id аккаунта
- Установка аутентификации в Spring `SecurityContextHolder`
- При ошибке — возврат `401`

### TenantImpersonationFilter
Источник: `ru.pt.auth.security.filter.TenantImpersonationFilter`

Обязанности:
- Разрешение SYS_ADMIN устанавливать заголовок `X-Imp-Tenant`
- Сохранение имперсонированного тенанта в `UserDetailsImpl`

!:X-Imp-Tenant 
SYS_ADMIN создается в системном тенанте sys. Чтобы он мог выполнять какието действия в других тенантах, он должен имперсонироваться.
Явно передать код тенанта, в котором он хочет работать. При этом permition не меняются. Изменяется только тенант.

### ContextCleanupFilter
Источник: `ru.pt.auth.security.filter.ContextCleanupFilter`

Обязанности:
- Всегда очищает `RequestContext` после запроса

## RequestContext (ThreadLocal)

`RequestContext` реализован через `ThreadLocalContext`.

```
RequestContext:
  tenant  -> код текущего тенанта
  client  -> id клиента аутентификации (client_id)
  login   -> логин пользователя (user_login)
  account -> разрешённый id аккаунта
```

Эти значения заполняются выбранной `IdentitySourceStrategy` и
уточняются в `AccountResolverService`.

## Стратегии источника идентичности

`AccountResolutionFilter` выбирает стратегию по `TenantSecurityConfig.authType`.

```
AuthType.LOCAL_JWT -> LocalJwtAuthenticationStrategy
AuthType.JWT      -> JwtAuthenticationStrategy
AuthType.HEADERS  -> HeaderAuthenticationStrategy
AuthType.API_KEY  -> ApiKeyIdentityStrategy
AuthType.KEYCLOAK -> KeycloakIdentityStrategy
AuthType.NONE     -> NoAuthenticationStrategy
```

## Получение токена по login/password

Источник: `ru.pt.api.security.AuthenticationController` (`POST /api/v1/{tenantCode}/auth/login`)

Поток:
- По `tenantCode` загружается тенант и его `authType` (если не задан/некорректен, используется `LOCAL_JWT`)
- Контроллер выбирает реализацию из `List<ExternalJwtAuthenticator>` по правилу
  `authenticator.supports(authType, tenant.authConfig)`
- Если подходящей реализации нет, возвращается `400`
- Если реализация найдена, вызывается:
  `authenticate(tenantCode, clientId, login, password, authConfig)`

Текущие реализации `ExternalJwtAuthenticator`:
- `LocalJwtPasswordAuthenticator` (для `AuthType.LOCAL_JWT`)
  - Делегирует в `SimpleAuthService.authenticate(...)`
  - Проверяет локальный пароль пользователя и выдает внутренний JWT
- `KeycloakPasswordGrantAuthenticator` (для `AuthType.KEYCLOAK` и `AuthType.JWT`)
  - Выполняет password grant к внешнему IdP (Keycloak)
  - Использует `issuer` для построения token endpoint
  - Использует `adminClientId`/`adminClientSecret` как `client_id`/`client_secret`
  - Возвращает `access_token` из ответа IdP

### JwtAuthenticationStrategy
Источник: `ru.pt.auth.security.strategy.JwtAuthenticationStrategy`

Входные данные:
- `Authorization: Bearer <jwt>`
- опционально `X-Account-Id`

Действия:
- Если для тенанта задан `authType = JWT` и заполнен `auth_config`
  - Используются настройки из `Tenant.authConfig`:
    - `issuer` — ожидаемое значение `iss` в токене
    - `jwksUri` — (зарезервировано для проверки подписи по JWKS; пока не используется)
  - Выполняются проверки:
    - базовая валидация через `JwtTokenUtil.validateToken(jwt)` (формат, `sub`, `exp`)
    - при наличии `issuer` — совпадение с `iss` из токена
- Если `auth_config` пустой или тенант не найден — используется прежний алгоритм `JwtTokenUtil.validateToken(jwt)`
- После успешной проверки:
  - извлекаются `username` (`sub`) и `client_id`
  - сохраняются `client_id` + `username` в `RequestContext`
  - при наличии `X-Account-Id` — сохраняется `account` (проверка как числовое значение)

### LocalJwtAuthenticationStrategy
Источник: `ru.pt.auth.security.strategy.LocalJwtAuthenticationStrategy`

Входные данные:
- `Authorization: Bearer <jwt>`
- опционально `X-Account-Id`

Действия:
- Проверяет токен через `JwtTokenUtil.validateToken(jwt)` (локальная валидация)
- Извлекает `username` (`sub`) и `client_id`
- Сохраняет `client`, `login`, `account` в `RequestContext`

### HeaderAuthenticationStrategy
Источник: `ru.pt.auth.security.strategy.HeaderAuthenticationStrategy`

Назначение:
- аутентификации после AuthProxy. Прокси валидирует токен и кладет в заголовки ИД клиента и пользователя, которые были в токене.
Такой метод используется в ВСК.

Входные данные:
- `X-Client-Id`, `X-User-Id`, опционально `X-Account-Id`

Замечания:
- Сейчас проверяет наличие обязательных заголовков, но ещё не
  сохраняет их в `RequestContext`.

### ApiKeyIdentityStrategy
Источник: `ru.pt.auth.security.strategy.ApiKeyIdentityStrategy`

Входные данные:
- `X-API-Key`

Замечания:
- Заглушка; контекст запроса пока не заполняется.

### KeycloakIdentityStrategy
Источник: `ru.pt.auth.security.strategy.KeycloakIdentityStrategy`

Входные данные:
- `Authorization: Bearer <jwt>` — access token, выданный внешним Keycloak

Дополнительные настройки:
- В `Tenant.authType` указано `KEYCLOAK`
- В `Tenant.authConfig` задаются:
  - `issuer` — ожидаемый `iss` (например, `http://localhost:18080/realms/pt`)
  - `audience` — ожидаемый `azp`/`aud` (идентификатор клиента в Keycloak)

Действия:
- Извлекает токен из заголовка `Authorization`
- Проверяет, что:
  - в `RequestContext` уже установлен `tenantCode`
  - для данного тенанта `authType = KEYCLOAK`
- При наличии `auth_config`:
  - проверяет истечение токена (`exp`) через `JwtTokenUtil.isTokenExpired(jwt)`
  - при наличии `issuer` — сравнивает его с `iss` в токене
  - при наличии `audience` — сравнивает с `azp` или `aud` в токене
- Извлекает идентичность:
  - `preferred_username` или `email`, или `sub` → сохраняется как `login` в `RequestContext`
  - `azp` или `client_id` → сохраняется как `client` в `RequestContext`
- При любой ошибке валидации генерирует `BadCredentialsException`, что приводит к `401`

### NoAuthenticationStrategy
Источник: `ru.pt.auth.security.strategy.NoAuthenticationStrategy`

Входные данные:
- `X-Account-Id`

Действия:
- Загрузка аккаунта по id, установка `tenant/client/login/account`

## Разрешение аккаунта

`AccountResolverService.resolveAccounts()` уточняет выбор аккаунта:

```
Если ClientAuthLevel == CLIENT:
  account := defaultAccountId (принудительно)
Иначе (USER):
  если задан X-Account-Id -> использовать
  иначе -> выбрать аккаунт по умолчанию для пользователя
```
!:
При создании клиента указывается, какой уровень аутентификации использовать. Клиентский или пользовательский.
Клиентский (CLEINT), используется для АПИ интеграций, когда взаимодействуют 2 системы, и нет конкретных пользователей.
Пользовательский (USER), требует наличия учетной записи в системе для каждого пользователя. 

## Фабрика UserDetails (UserFactory)

Создание пользователя выполняется в `UserDetailsServiceImpl` и фабричным методом
`UserDetailsImpl.build(...)`.

```
RequestContext
  -> tenantCode + clientId + login + accountId
  -> AccountLoginRepository.findByAll4Fields(...)
  -> ProductRoleRepository.findAllProductRolesByAccountId(...)
  -> AccountRepository.findByTenantCodeAndId(...)
  -> UserDetailsImpl.build(...)
  -> SecurityContextHolder.setAuthentication(...)
```

`UserDetailsImpl.build()` — каноническая «UserFactory», так как принимает
доменные сущности и создаёт `UserDetails`, используемый в системе.

### Разрешение действующего аккаунта

`UserDetailsServiceImpl` вычисляет `actingAccountId` по типу узла аккаунта:

```
SUB           -> родительский аккаунт
ACCOUNT       -> тот же аккаунт
SYS_ADMIN     -> корневой аккаунт тенанта
TNT_ADMIN     -> корневой аккаунт тенанта
PRODUCT_ADMIN -> корневой аккаунт тенанта
GROUP_ADMIN   -> родительский аккаунт
GROUP/CLIENT/TENANT -> запрещено
```

## Псевдографические схемы

### Конвейер фильтров с контекстом

```
Запрос
  |
  v
TenantResolutionFilter
  - ctx.tenant = {tenantCode}
  |
  v
AccountResolutionFilter
  - strategy.resolveIdentity()
  - ctx.client/login/account установлены
  - AccountResolverService.resolveAccounts()
  |
  v
IdentityResolutionFilter
  - userDetails = UserDetailsServiceImpl.loadUserByUsername(ctx.account)
  - SecurityContextHolder.setAuthentication(userDetails)
  |
  v
TenantImpersonationFilter
  - если SYS_ADMIN и X-Imp-Tenant -> user.impersonatedTenantCode
  |
  v
ContextCleanupFilter
  - ctx.clear()
```

### Путь JWT (AuthType.JWT)

```
Authorization: Bearer <jwt>
        |
        v
JwtAuthenticationStrategy
  - проверка токена
  - ctx.client = client_id
  - ctx.login  = username
  - ctx.account = X-Account-Id (опционально)
        |
        v
AccountResolverService
  - выбор аккаунта по умолчанию при необходимости
        |
        v
UserDetailsImpl.build(...)
```

### Выбор Client vs User

```
ClientAuthType = CLIENT  -> account := defaultAccountId
ClientAuthType = USER    -> account := X-Account-Id или аккаунт по умолчанию
```

## Предупреждения / пробелы


- `HeaderAuthenticationStrategy`, `ApiKeyIdentityStrategy`,
  — незавершённые заглушки.
- `SecurityConfigurationProperties.publicUrls` управляет пропуском фильтров.

## Entity
1. Logins ( tenant + login ) пара уникальна. проверка что есть такой логин и он не залочен. 
2. account_logins ( tenant + login + account_id ) - список account_id для текцего пользователя.
3. accounts (id, client_id, role ) - отфильтровать список из 2 по текущему client, id из этой entity и есть текущий пользлватель .


## Формат ответа me
{
    "productRoles": [],
    "accountId": 173,                                acc_accounts.ID
    "isDefault": false,                              acc_account_logins.is_Default
    "clientId": 171,                                 acc_accounts.client_id
    "accountName": "TNT_ADMIN",                      acc_accounts.name
    "clientName": "Default Admin App Client",        clients.name
    "id": 177,                                       ???
    "tenantCode": "tnt",                             acc_accounts.tid
    "accounts": [],                                  accounts[]
    "userRole": "TNT_ADMIN",                         acc_accounts.node_type 
    "authorities": [                                 ???
        {
            "authority": "ROLE_TNT_ADMIN"
        }
    ],
    "username": "sirik"                              acc_login.login_name
}



### Параметры сессии


Клиент (tenant_id=1002)
    id_path = "1002"
    │
    ├── Группа "Агенты" (id=1003)
    │       id_path = "1002.1003"
    │       │
    │       ├── Аккаунт "Иванов" (id=1005)
    │       │       id_path = "1002.1003.1005"
    │       │       permissions для продукта "ОСАГО": ["VIEW", "CREATE", "PAY"]
    │       │
    │       └── Аккаунт "Петров" (id=1006)
    │               id_path = "1002.1003.1006"
    │
    └── Группа "Прямые продажи" (id=1004)
            id_path = "1002.1004"
            permissions для продукта "ОСАГО": ["VIEW", "QUOTE"]