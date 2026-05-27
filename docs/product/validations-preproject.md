# Pre-project: Rule Engine (CEL) — `pt_rules`

Источник: [validations.txt](./validations.txt)

## 1. Цель

Централизованный движок правил на **CEL** (Common Expression Language) с хранением в `pt_rules`, вызовом из процесса оформления (quote/save) и CRUD для админки.

Интерпретация результата — у **вызывателя** (процесс решает: блокировать quote, warning, underwriting).

---

## 2. Текущее состояние (as-is)

| Компонент | Сейчас |
|-----------|--------|
| Валидация quote/save | `ValidatorServiceImpl` + `ValidatorRule` в JSON продукта (`quoteValidator` / `saveValidator`) |
| Типы | `ValidatorType.QUOTE`, `ValidatorType.SAVE` |
| Движок | `ValidatorImpl` — фиксированные типы (`IN_LIST`, `NOT_NULL`, `AND`) |
| Контекст | `VariableContext` / `CalculatorContext` (переменные `io_*`, `pl_*`) |
| Вызов | `ProcessOrchestratorService` ~424 (quote), ~584 (quote+save) |
| Tenant / LOB / Client rules | **нет** |
| CEL | **нет** |

**Вывод:** CEL-правила хранятся **только в `pt_rules`**. Legacy `quoteValidator`/`saveValidator` в JSON продукта пока могут работать параллельно; **в JSON новые правила не копируем** (экспорт в продукт — возможно позже).

---

## 3. Целевая архитектура (to-be)

```
┌─────────────────────────────────────────────────────────────┐
│  ProcessOrchestratorService                                  │
│    validateQuote() / validateSave()                          │
└──────────────────────────┬──────────────────────────────────┘
                           │
         ┌─────────────────┴─────────────────┐
         ▼                                   ▼
┌─────────────────────┐           ┌─────────────────────┐
│ ValidatorService    │           │ RuleValidationService│  ← NEW
│ (legacy JSON rules) │           │ processValidation()  │
└─────────────────────┘           └──────────┬──────────┘
                                             │
                                  ┌──────────▼──────────┐
                                  │ CelRuleEngine         │
                                  │  compile + evaluate   │
                                  └──────────┬──────────┘
                                             │
                                  ┌──────────▼──────────┐
                                  │ RuleRepository        │
                                  │  pt_rules + cache     │
                                  └─────────────────────┘
```

### Модуль Gradle (предложение)

| Вариант | Плюсы | Минусы |
|---------|-------|--------|
| **A. `pt-rules`** (рекомендуется) | Изоляция, тесты, переиспользование | +1 модуль |
| B. Расширить `pt-product` | Меньше модулей | Смешение CRUD продукта и CEL |
| C. Расширить `pt-process` | Близко к вызову | Циклические зависимости с product |

**Рекомендация:** `pt-rules` → зависит от `pt-api`, `pt-db`; `pt-process` и `pt-launcher` подключают его.

---

## 4. Модель данных

### 4.1 Таблица `pt_rules`

```sql
-- V7__rules.sql (черновик)
create table if not exists pt_rules (
    id              bigint primary key default nextval('pt_seq'),
    tid             bigint not null,
    code            varchar(64) not null,
    name            varchar(300) not null,
    scope_type      varchar(16) not null,  -- PRODUCT | LOB | TENANT | CLIENT
    scope_code      varchar(64) not null,  -- см. § «Решения»: product / lob / tenant / client_id
    rule_type       varchar(32) not null,  -- RuleType enum
    priority        int not null default 100,
    record_status   varchar(16) not null default 'ACTIVE',
    expression_language varchar(16) not null default 'CEL',
    expression      text not null,
    message         varchar(500) not null,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    constraint chk_pt_rules_scope check (scope_type in ('PRODUCT','LOB','TENANT','CLIENT')),
    constraint chk_pt_rules_status check (record_status in ('ACTIVE','INACTIVE','DELETED'))
);

create unique index if not exists ux_pt_rules_tid_code_active
    on pt_rules (tid, code)
    where record_status = 'ACTIVE';

create index if not exists idx_pt_rules_lookup
    on pt_rules (tid, rule_type, scope_type, scope_code, record_status, priority);
```

### 4.2 Хранение правил (зафиксировано)

- **Единый источник:** все CEL-правила — только в `pt_rules`.
- **Правила продукта:** `scope_type = PRODUCT`, `scope_code = productCode` (например `AIR_PAX_COMBO`).
- **Не дублируем** в JSON версии продукта на v1; копирование в продукт — отдельная задача в backlog.
- Legacy `quoteValidator` / `saveValidator` в JSON — по желанию оставить до миграции; новые правила туда **не добавляем**.

---

## 5. Доменные типы (`pt-api`)

```java
public enum RuleType {
    PRE_QUOTE_VALIDATION,
    POST_QUOTE_VALIDATION,
    PRE_SAVE_VALIDATION,
    POST_SAVE_VALIDATION,
    // v2+
    QUOTE_CALCULATION, UNDERWRITING, WORKFLOW, CROSS_SELL,
    FRAUD_CHECK, ISSUANCE, RENEWAL
}

public enum RuleScopeType {
    PRODUCT, LOB, TENANT, CLIENT
}

public record RuleDto(
    Long id, String code, String name,
    RuleScopeType scopeType, String scopeCode,
    RuleType ruleType, int priority,
    String expressionLanguage, String expression, String message,
    String recordStatus
);

public record RuleValidationContext(
    Long tid,
    String tenantCode,        // код тенанта из JWT / request
    String productCode,
    String lobCode,
    String clientId,            // client_id из JWT (например "sys")
    Map<String, Object> vars    // из VariableContext
);
```


### Семантика CEL (важно)

По примеру `io_age <= 75` + message *«не должен превышать 75»*:

- **Выражение = условие допустимости** (true → OK, false → нарушение → добавить `message`).
- Альтернатива (не рекомендуется): expression = условие нарушения — путаница для авторов правил.

Зафиксировать в UI/документации: *«выражение должно быть true для успешной валидации»*.

---

## 6. `RuleValidationService` (ядро)

```java
public interface RuleValidationService {
    /** @return список сообщений об ошибках (пустой = OK) */
    List<String> processValidation(
        RuleType ruleType,
        RuleValidationContext ctx
    );

    void reloadCache();
}
```

### Порядок загрузки (как в spec)

Все уровни — из **`pt_rules`**, фильтр `tid` + `rule_type` + `record_status = ACTIVE`:

| # | scope_type | scope_code | order by |
|---|------------|------------|----------|
| 1 | `PRODUCT` | `productCode` из контекста | priority ASC |
| 2 | `LOB` | `lobCode` из продукта | priority ASC |
| 3 | `TENANT` | **`tenantCode`** (текущий тенант) | priority ASC |
| 4 | `CLIENT` | **`clientId` из JWT** | priority ASC |

`ruleType` ∈ `{ PRE_QUOTE_VALIDATION, POST_QUOTE_VALIDATION, PRE_SAVE_VALIDATION, POST_SAVE_VALIDATION }` для v1.

### Ошибки выполнения CEL (зафиксировано)

Поведение зависит от **`rule_type`** (вызывающий метод / интерпретация):

| Группа | rule_type | Ошибка compile/runtime |
|--------|-----------|-------------------------|
| **Валидация** | `PRE_*`, `POST_*` (quote/save) | Текст ошибки **в ответ** клиенту (`ValidationError` / `ErrorModel` 400). Полис **не проходит** этап. |
| **Прочие** (v2+) | `QUOTE_CALCULATION`, `UNDERWRITING`, … | Отдельная политика на вызывателе (не в scope v1). |

Для validation-типов при runtime exception CEL:

```text
[{rule.code}] Ошибка выполнения правила: <текст исключения>
```

При compile error при CRUD правила — **400** с текстом компилятора (правило не сохраняется).

### CEL cache

- При старте / `reload`: загрузить все `ACTIVE` rules для tenant → скомпилировать `cel-java`
- Ключ кэша: `tid + ruleType + scopeType + scopeCode + code`
- При CRUD: инвалидировать затронутые ключи или полный `reload`

**Зависимость:** `dev.cel:cel` + `dev.cel:cel-tools` (или `org.projectness:cel` — уточнить при POC).

### Binding контекста

Маппинг `VariableContext` → CEL activation:

```java
// псевдокод
for (var entry : ctx.vars().entrySet()) {
  builder.put(entry.getKey(), toCelValue(entry.getValue()));
}
```

POC: проверить типы `NUMBER`, `STRING`, `null`, вложенные объекты (если нужны — ограничить v1 плоским map).

---

## 7. REST API

Base: `/api/v1/{tenantCode}/admin/rules` (или `/rules` — согласовать с существующим admin API)

| Method | Path | Описание |
|--------|------|----------|
| GET | `/rules` | Список (фильтры: ruleType, scopeType, scopeCode, status) |
| GET | `/rules/{id}` | Одно правило |
| POST | `/rules` | Создать |
| PUT | `/rules/{id}` | Обновить |
| DELETE | `/rules/{id}` | Soft-delete → `INACTIVE` |
| POST | `/rules/cmd/reload` | Сброс CEL cache (SYS_ADMIN / TNT_ADMIN) |

**Валидация при сохранении:** компиляция CEL; при ошибке — 400 с текстом компилятора.

---

## 8. Интеграция в процесс

### Точки вызова

| Этап | RuleType | Где в `ProcessOrchestratorService` |
|------|----------|-----------------------------------|
| До расчёта котировки | `PRE_QUOTE_VALIDATION` | перед `validatorService.validate(QUOTE)` |
| После расчёта | `POST_QUOTE_VALIDATION` | после calculator, до ответа |
| До save | `PRE_SAVE_VALIDATION` | перед save validators |
| После save | `POST_SAVE_VALIDATION` | после persist |

### Объединение ошибок

```java
// CEL-правила (pt_rules)
errors.addAll(ruleValidationService.processValidation(ruleType, ctx).stream()
    .map(msg -> new ValidationError(msg, msg, "rule"))
    .toList());

// Legacy JSON validators — опционально, пока не выведены
errors.addAll(legacyValidator.validate(...));
```

Поведение: **400** + `ErrorModel` (контракт sales без изменений). Сообщения CEL и legacy идут в одном списке `errors`.

---

## 9. Иерархия Rule (будущее)

```
Rule
 ├── ValidationRule      ← v1 (этот pre-project)
 ├── PricingRule           ← v2, QUOTE_CALCULATION
 ├── UnderwritingRule
 ├── RecommendationRule
 ├── WorkflowRule
 └── DocumentRule
```

v1: одна таблица `pt_rules` + поле `rule_type`; подтипы — только в коде/документации, без полиморфных таблиц.

---

## 10. План поставки (фазы)

### Phase 0 — POC (3–5 дн.)

- [ ] Gradle + `cel-java` compile/eval `io_age <= 75`
- [ ] Маппинг 10–20 переменных из `VariableContext` (PAX)
- [ ] Unit-test: true/false → message

### Phase 1 — MVP (2 нед.)

- [ ] Migration `V7__rules.sql`
- [ ] Module `pt-rules`: entity, repository, `CelRuleEngine`, `RuleValidationService`
- [ ] `RuleController` CRUD + reload
- [ ] Интеграция `PRE_QUOTE_VALIDATION` + `PRE_SAVE_VALIDATION` в orchestrator
- [ ] Seed: `PAX_AGE_LIMIT` для tenant demo / product PAX

### Phase 2 — Полный validation scope (1 нед.)

- [ ] `POST_QUOTE_VALIDATION`, `POST_SAVE_VALIDATION`
- [ ] Скрипт миграции старых `quoteValidator` → `pt_rules` (scope `PRODUCT`) — по необходимости
- [ ] Admin UI в PoliTechFront (таблица правил, редактор expression)

### Phase 3 — Расширения (backlog)

- UNDERWRITING, WORKFLOW, PRICING rules
- Версионирование правил, audit log
- Тестирование правил «на полисе» без save (dry-run endpoint)

---

## 11. Решения (зафиксировано)

| # | Вопрос | Решение |
|---|--------|---------|
| 1 | Правила продукта | **Только `pt_rules`**, `scope_type=PRODUCT`, `scope_code=productCode`. В JSON **не копируем** (возможно позже). |
| 2 | `scope_code` для TENANT | **Код текущего тенанта** (`tenantCode`, напр. `demo`) |
| 3 | `scope_code` для CLIENT | **`client_id` из JWT** (строка, напр. `sys`) |
| 4 | Legacy JSON + CEL | Оба могут выполняться; новые правила — только в `pt_rules` |
| 5 | Ошибка CEL | **Зависит от rule_type.** Для validation — **текст в ответ**, этап блокируется. Не fail-open. |
| 6 | Права CRUD | `TNT_ADMIN` / `SYS_ADMIN` (как у прочего admin API) — по умолчанию |

---

## 12. Пример правила (seed)

```json
{
  "code": "PAX_AGE_LIMIT",
  "name": "Возраст застрахованного до 75 лет",
  "scopeType": "PRODUCT",
  "scopeCode": "AIR_PAX_COMBO",
  "ruleType": "PRE_QUOTE_VALIDATION",
  "priority": 100,
  "expressionLanguage": "CEL",
  "expression": "io_age <= 75",
  "message": "Возраст не должен превышать 75 лет"
}
```

---

## 13. Оценка трудозатрат

| Фаза | Backend | Frontend | Итого |
|------|---------|----------|-------|
| POC | 2–3 дн | — | 2–3 дн |
| MVP | 8–10 дн | — | 8–10 дн |
| Phase 2 | 3–4 дн | 5–7 дн | 8–11 дн |

---

## 14. Рекомендация

**Стартовать с Phase 0 + Phase 1** в модуле `pt-rules`. Первый боевой сценарий: **PAX, `PRE_QUOTE_VALIDATION`, `PAX_AGE_LIMIT`** в `pt_rules` (`PRODUCT` / `AIR_PAX_COMBO`).

**Следующий шаг:** `V7__rules.sql`, скелет `pt-rules`, POC CEL + seed tenant `demo`.
