
# Accounts and data



### Введение

В многоуровневых страховых информационных системах часто возникает необходимость разделять бизнес-логику (портфели, продукты, договоры) и доступ пользователей к данным. Простое сопоставление «пользователь → права» быстро перестаёт работать, когда на систему выходят брокеры, маркетплейсы и крупные компании с разветвлённой структурой.

Для решения этой задачи используется account — базовая авторизационная единица.

### Что такое Account

Account — это анонимная функциональная сущность в дереве компании. Он:

существует независимо от конкретных физических пользователей;

наделён определёнными ролями и правами доступа;

может рассматриваться как контейнер для бизнес-сущностей: договоров, клиентов, комиссий, заявок;

в страховой системе часто приравнивается к страховому портфелю.

Таким образом, account — это "виртуальный субъект", от имени которого работают пользователи.

### Привязка пользователей

К одному account может быть привязано несколько логинов (учётных записей людей).\
Это даёт следующие преимущества:

несколько сотрудников могут работать с одним и тем же портфелем (например, отдел корпоративных продаж);

один пользователь может иметь доступ к нескольким account (например, агент ведёт несколько страховых портфелей);

при смене сотрудников нет необходимости изменять сам account — достаточно переназначить привязку логинов.

### Роли и права

У каждого account определяется набор ролей и прав, который определяет:

какие действия доступны (создание договора, изменение тарифа, просмотр аналитики);

какие продукты или клиенты входят в зону ответственности;

в каких границах действуют бизнес-ограничения (например, лимиты по сумме комиссий или тарифам).

Таким образом, права назначаются на account, а пользователи наследуют эти права через привязку к нему.

### Account в дереве компании

Account встроен в иерархию компании:

на верхнем уровне могут быть глобальные account (например, маркетплейс в целом);

ниже — account брокеров или филиалов;

ещё ниже — account отдельных портфелей или команд.

Такое дерево позволяет:

централизованно контролировать права и настройки;

гибко делегировать доступ и ответственность;

агрегировать статистику и отчёты по уровням.

### Account как портфель

В страховании account можно рассматривать как портфель:

в него входят договоры, клиенты, продукты;

к нему привязаны пользователи (агенты, менеджеры);

на уровне account настраиваются комиссии, лимиты и права;

статистика строится по account (объём премий, убытки, доходность).

Это позволяет бизнесу мыслить привычными категориями: портфель клиента = account в системе.

### Преимущества модели

Гибкость управления доступом\
Роли и права настраиваются для account, а пользователи просто подключаются к ним.

Масштабируемость\
Дерево account позволяет описывать структуры от малых агентств до крупных холдингов.

Устойчивость к изменениям персонала\

При смене сотрудников не нарушается бизнес-логика.

Прозрачная отчетность\
Все операции и результаты аккумулируются на уровне account (портфеля).




## Реализация

### Диаграмма потоков аутентификации (multi-tenant)

~~~
┌─────────────────────────┐
│        HTTP Request      │
│ /api/v1/{tenant}/...     │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ TenantResolutionFilter   │
│ - Извлекает tenant       │
│ - Сохраняет в контекст   │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ AccountResolutionFilter  │
│ - Получает tenant из     │
│   контекста              │
│ - Определяет стратегию   │
│   authType               │
│ - Извлекает clientId,    │
│   userLogin, accountId   │
│ - Валидирует данные      │
│ - Сохраняет все в контекст│
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ IdentityResolutionFilter │
│ - Берет accountId из     │
│   контекста              │
│ - Загружает UserDetails  │
│ - Проверяет консистентность: │
│   tenantId, clientId, userLogin │
│ - Создает Authentication │
│ - Устанавливает в        │
│   SecurityContextHolder  │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│   Контроллер / Сервис    │
│ - Доступ через SecurityContext│
│   (UserDetails, роли)    │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ ContextCleanupFilter     │
│ - Очищает ThreadLocal    │
│ - tenant, clientId, user,│
│   account                │
└─────────────────────────┘

~~~


# ADR-001: Multi-Tenant Authentication Pipeline

## Контекст

Система поддерживает **multi-tenant** структуру с возможностью:

* различной аутентификации по tenant
* использования clientId, userLogin, accountId
* JWT токенов или header-based аутентификации
* строгого контроля целостности данных

Для управления этим используется **цепочка фильтров Spring Security**, каждый фильтр имеет свою четкую ответственность.

---

## Фильтры

### 1️⃣ TenantResolutionFilter

**Цель:**
Извлечь `tenantId` из URL запроса и поместить его в **контекст запроса** (`RequestContext` / `TenantContext`).

**Поведение:**

* Читает URL: `/api/v1/{tenant}/...`
* Извлекает `{tenant}`
* Сохраняет tenant в ThreadLocal 
* Не изменяет SecurityContext
* Всегда вызывает `filterChain.doFilter` даже если tenant отсутствует (public URL)

**Пример:**

```java
String tenant = extractTenant(request);
requestContext.setTenant(tenant);
filterChain.doFilter(request, response);
```

**Преимущества:**

* Все последующие фильтры имеют доступ к tenant
* Разделение ответственности
* Поддержка public URL без tenant

---

### 2️⃣ AccountResolutionFilter

**Цель:**
Определить **стратегию аутентификации** для tenant и разрешить `clientId`, `userLogin`, `accountId`.

**Поведение:**

1. Получает tenant из контекста
2. Получает конфигурацию tenant (TenantSecurityConfig)
3. Выбирает стратегию `IdentitySourceStrategy` согласно `authType` tenant
4. Вызов стратегии для извлечения clientId/userLogin/accountId
5. Валидация данных (консистентность, существование аккаунтов)
6. Сохраняет clientId/userLogin/accountId в контексте

**Преимущества:**

* Поддержка нескольких методов аутентификации (JWT, header-based, NONE)
* Все данные для Authentication уже готовы
* Фильтр остаётся чистым от конкретной реализации auth

**Пример:**

```java
IdentitySourceStrategy strategy = strategies.stream()
    .filter(s -> s.supports(config.getAuthType()))
    .findFirst()
    .orElseThrow();
strategy.resolveIdentity(request);
accountResolverService.resolveAccounts();
```

---

### 3️⃣ IdentityResolutionFilter (SecurityAuthenticationFilter)

**Цель:**
Создать **Spring Security Authentication** на основе `accountId`, проверить консистентность с tenantId, clientId и userLogin.

**Поведение:**

* Берет `accountId` из контекста
* Загружает `UserDetails` (account-aware)
* Проверяет соответствие tenant, client, user
* Создает `UsernamePasswordAuthenticationToken` с ролями
* Устанавливает Authentication в `SecurityContextHolder`

**Преимущества:**

* Spring Security видит валидного пользователя
* Все проверки консистентности вынесены в один фильтр
* Поддержка role-based authorization

**Пример:**

```java
UserDetails userDetails = userDetailsService.loadUserByUsername(accountId);
Authentication auth = new UsernamePasswordAuthenticationToken(
    userDetails, null, userDetails.getAuthorities()
);
SecurityContextHolder.getContext().setAuthentication(auth);
```

---

### 4️⃣ ContextCleanupFilter

**Цель:**
Очистить контекст запроса (`RequestContext`) после обработки запроса.

**Поведение:**

* Всегда выполняется в `finally` блоке
* Убирает tenant, clientId, userLogin, accountId
* Предотвращает утечки ThreadLocal при повторном использовании потоков

**Пример:**

```java
try {
    filterChain.doFilter(request, response);
} finally {
    requestContext.clear();
}
```

---

## Порядок вызова фильтров

| Позиция | Фильтр                   | Описание                                    |
| ------- | ------------------------ | ------------------------------------------- |
| 1       | TenantResolutionFilter   | Получение tenant                            |
| 2       | AccountResolutionFilter  | Разрешение clientId/userLogin/accountId     |
| 3       | IdentityResolutionFilter | Валидация account и создание Authentication |
| 4       | ContextCleanupFilter     | Очистка ThreadLocal / RequestContext        |

> В SecurityConfig порядок фильтров задаётся через `addFilterBefore` / `addFilterAfter`:

```java
.addFilterBefore(tenantResolutionFilter, UsernamePasswordAuthenticationFilter.class)
.addFilterAfter(accountResolutionFilter, TenantResolutionFilter.class)
.addFilterAfter(identityResolutionFilter, AccountResolutionFilter.class)
.addFilterAfter(contextCleanupFilter, IdentityResolutionFilter.class)
```

---

## Поток данных

1. **Запрос приходит → TenantResolutionFilter**
   tenant → контекст
2. **AccountResolutionFilter**
   tenant → стратегия → clientId, userLogin, accountId → контекст
3. **IdentityResolutionFilter**
   accountId → UserDetails → Authentication → SecurityContext
4. **ContextCleanupFilter**
   контекст очищен

---

## Принципы

1. **Thread-local контекст** используется для хранения временных данных: tenant, client, user, account.
2. **Authentication создаётся ровно один раз** (IdentityResolutionFilter)
3. **Фильтры разделены по ответственности** — каждый фильтр решает только свою задачу
4. **Стратегии аутентификации** легко расширяемы (JWT, headers, OAuth, NONE)
5. **Масштабируемость** — можно добавлять новые фильтры или auth types без изменения существующих

---




