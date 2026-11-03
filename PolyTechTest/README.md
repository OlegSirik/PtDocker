# PolyTechTest

CLI-набор REST-тестов для **PoliTechAPI** (`/api/v1/{tenantCode}/…`).

## Требования

- Java 21
- Запущенный PoliTechAPI (Docker / `bootRun`)
- Пароли через env (в репозиторий не кладутся)

| Env | Назначение |
|-----|------------|
| `PT_DEMO_PASSWORD` | Пароль `users.admin` (логин `demo`) |
| `PT_OTHER_LOGIN` / `PT_OTHER_PASSWORD` | Опционально второй пользователь для ACL |
| `PT_SYS_PASSWORD` | Пароль `users.sys` (`sys_admin`) — нужен для `e2e`, `e2e-hierarchy`, `admin-lifecycle` |

## Команды запуска

```powershell
cd PolyTechTest

# Smoke + regression
$env:PT_DEMO_PASSWORD="..."
.\gradlew.bat run --args="--tags=smoke,regression --report-dir=reports"

# Полный e2e: tenant → продукт CRG (DEV) → sales → load 1000
$env:PT_SYS_PASSWORD="..."
.\gradlew.bat run --args="--tags=e2e"

# Иерархия клиентов / аккаунтов / пользователей / договоров
.\gradlew.bat run --args="--tags=e2e-hierarchy"

# Только нагрузка на demo
.\gradlew.bat run --args="--tags=load"

# Только создание test_<ts> tenant + user
.\gradlew.bat run --args="--tags=admin-lifecycle"

# Fat jar
.\gradlew.bat jar
java -jar build\libs\PolyTechTest-1.0.0.jar --config=config/local.yaml --tags=sales
```

Коды выхода: `0` — ок, `1` — ошибки, `2` — нет сценариев по тегам.

Отчёты: `reports/summary.md`, `reports/junit.xml` (+ копии с timestamp). Нагрузка: `reports/load-*.md`.

## Теги

| Tag | Что запускает |
|-----|----------------|
| `smoke` | Минимальный health-path |
| `auth` | Login / `/auth/me` / 401 |
| `sales` | Quote, policy, list, print |
| `acl` | Логины + видимость чужого полиса |
| `admin` | LOB / products / schema / refdicts (в основном GET) |
| `e2e` | Tenant → install CRG (DEV) → product/sales под PRODUCT_ADMIN → load |
| `e2e-hierarchy` | Tenant → продукт → CLIENT/USER клиенты → дерево аккаунтов → пользователи → договоры + count |
| `load` | Параллельные policy+print на demo |
| `admin-lifecycle` | Только create/delete `test_<ts>` |
| `regression` | Полный недеструктивный набор (по умолчанию) |
| `destructive` | Опасные мутации (только явно) |
| `all` | Все недеструктивные, **без** `load` / `admin-lifecycle` / `e2e` / `e2e-hierarchy` |

Несколько тегов через запятую: `--tags=smoke,sales`.

## Сценарий `e2e` (кратко)

1. SYS создаёт tenant `test_<timestamp>`, user login=password=code, роли `TNT_ADMIN` + `PRODUCT_ADMIN`.
2. После `/auth/me` — `X-Account-Id` для роли.
3. Установка LOB/продукта под **TNT_ADMIN**:
   - версия **PROD** (publish) — для `ACCOUNT` / `GROUP_ADMIN` / API-key (`dataScope=PROD`);
   - отдельная версия **DEV** (`cmd/create` после publish) — для `PRODUCT_ADMIN` (`dataScope=DEV`).
4. Sales + load под **PRODUCT_ADMIN** (DEV).
5. Soft-delete tenant (если `adminLifecycle.deleteTenant=true`).

## Сценарий `e2e-hierarchy` (полный)

1. **Tenant** — как в e2e (`test_<ts>` + TNT/PRODUCT_ADMIN).
2. **Продукт** CARGO_GENERAL: **PROD** + **DEV** версии, гранты на sys-client.
3. **Клиент с `authLevel=CLIENT`**, `authType=APIKEY` (у client допустимы только `HEADERS`/`APIKEY`/null):
   - создаётся клиент;
   - используется / при необходимости создаётся **default account**;
   - грант продукта на client + default account;
   - создаётся **account token** (`POST …/accounts/{id}/tokens`);
   - создаётся **договор** с заголовком `X-Api-Key` (+ `X-Client-ID`).
4. **Смена уровня на `USER`**, `authType=null` (наследует tenant `LOCAL_JWT`):
   - обновление клиента;
   - default account + login (login=password);
   - JWT-логин → ещё один **договор**.
5. **Клиент `sys`** (`authLevel=USER`, `authType=null`):
   - код tenant только **lowercase** (`test_<ts>`);
   - `user_admin1` / `user_admin1` — `GROUP_ADMIN` на client + привязка ко **всем** группам и account;
   - `account1`–`account3` под sys client account;
   - группа `group1`; `account1.1`–`account3.1` под ней;
   - на каждый account — 2 пользователя `user{номер}_{n}` (lowercase);
   - каждый: JWT → **5 договоров**, затем `GET /sales` → **count** (≥ 5).
6. Soft-delete tenant по конфигу.

## Конфиг YAML

### `load:`

| Key | Default | Meaning |
|-----|---------|---------|
| `threads` | 20 | Воркеры steady / e2e load |
| `totalRequests` | 1000 | Всего policy+print |
| `iterationsPerThread` | 5 | Если `totalRequests=0` |
| `concurrencyProbe` | `[1,2,4,8]` | Уровни max-parallel |
| `requirePrint` | `false` | 404 print не ошибка |

### `adminLifecycle:`

| Key | Default | Meaning |
|-----|---------|---------|
| `deleteTenant` | `true` | Soft-delete `test_<ts>` после сценария |

## Матрица endpoint → сценарии

| Area | Endpoints | Scenarios |
|------|-----------|-----------|
| Auth | `POST /auth/login`, `GET /auth/me` | `auth.*` |
| Sales | quotes, policies, list, print | `sales.*`, `e2e*`, `load.*` |
| ACL | logins, чужой полис | `acl.*` |
| Admin | lobs, products, schema, … | `admin.*` |
| Hierarchy | clients, accounts/children, tokens, logins, members, sales | `e2e.hierarchy` |
| Lifecycle | tenants create/delete | `admin.tenant-lifecycle` |

Пакеты сценариев: `ru.pt.test.scenario.packs`.

## GitHub Actions

[`.github/workflows/polytech-test.yml`](../.github/workflows/polytech-test.yml): secrets `PT_DEMO_PASSWORD`, `PT_BASE_URL` (и `PT_SYS_PASSWORD` для e2e).
