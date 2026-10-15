# Модель договора и методы настройки продукта

> **Канон перенесён.** Актуальный SoT: [`docs/curdoc/doc.md`](../curdoc/doc.md) — **§12** (модель договора), **§13** (схема→LOB→продукт), **§14** (рычаги настройки), **§15** (калькулятор / quote-save / тест). Runtime: [`POLICY_QUOTE_SAVE.md`](../../PoliTechAPI/docs/POLICY_QUOTE_SAVE.md). Этот файл сохранён как исторический расширенный черновик; не править как второй SoT.

Документ описывает **модель страхового договора** в PoliTech, **ограничения на данные**, **правила CEL**, **рычаги кастомизации продукта** и **как на этой модели строить LLM-ассистента настройки**.

Связанные материалы:

| Документ | Тема |
|----------|------|
| [model.md](./model.md) | Краткие заметки по датам и генератору номера |
| [POLICY_QUOTE_SAVE.md](../../PoliTechAPI/docs/POLICY_QUOTE_SAVE.md) | Поток quote/save |
| [validations.md](./validations.md) | Логика этапов валидации (продуктовый взгляд) |
| [Text-Template.md](../development/Text-Template.md) | Текстовые шаблоны digests / ПФ |
| [llm-integration-preproject.md](./llm-integration-preproject.md) | Технический pre-project LLM API |
| [POLICY_WIRE_ADAPTERS.md](../../PoliTechAPI/docs/POLICY_WIRE_ADAPTERS.md) | Wire-форматы |

> **Статус:** as-is по коду (`StdPolicy`, `PolicyDTO`, `ProductVersionModel`, `pt-rules`, оркестратор). Целевые расширения помечены явно.

---

## 1. Модель договора

### 1.1. Три слоя

```text
HTTP / БД JSON          →  policyv3.PolicyDTO          (wire)
Процесс quote/save      →  StdPolicy                   (интерфейс)
Реализация v3           →  InsuranceContractPolicy     (обёртка над PolicyDTO + VariableContext)
```

Оркестратор и storage работают с **`StdPolicy`**. Wire JSON (`PolicyDTO`) — вход API, ответ API и колонка `policy_data.policy`.

Отдельного «std JSON» нет: в БД лежит тот же wire-формат, уже обогащённый процессом (премия, номер, id, commission, installments, status).

### 1.2. Структура договора (логическая)

```text
Policy (шапка)
├── productCode / productName / productVersion
├── issueDate / startDate / endDate
├── waitingPeriod / policyTerm          (ISO-8601 периоды)
├── installmentType
├── premium / policyNumber / id / publicId / statusCode
├── insurer
├── commission
├── installments[]
├── options[]                           (add-ons)
├── policyHolder                        (открытый bag атрибутов)
├── insuredObjects[]                    ← сейчас фактически 1 объект
│     ├── packageCode
│     ├── sumInsured / ioType / objectId
│     ├── additionalAttributes          (открытый bag)
│     └── covers[]
│           ├── cover (code, …)
│           ├── sumInsured / premium
│           ├── deductible (id, text)
│           └── limitMin / limitMax
└── processList                         (runtime: digests, vars, dataScope; в БД не хранится)
```

**Страхователь** и **атрибуты объекта** не зашиты жёсткой схемой Java-полей: это `additionalAttributes` / JsonAnySetter. Словарь полей задаёт **продукт** (`product.vars` + JsonPath).

### 1.3. Переменные (VariableContext)

При `policy.setVars(product)` строится `CalculatorContext` / `VariableContextImpl`:

1. JSON договора + определения `PvVar` продукта;
2. материализация по `varPath` (JsonPath) для типа `IN`;
3. вычисление **MAGIC** (`ph_age_issue`, `pl_TermMonths`, …);
4. дальше калькулятор и CEL читают/пишут vars.

Типы источника (`PvVarDefinition.VarSourceType`): `IN`, `MAGIC`, `CONST`, `COEFFICIENT`, `VAR`, `CALC`, `TEXT`, `OBJECT`.

Типы данных: `STRING`, `NUMBER`, `DATE`, `TIME`, `OBJECT`.

### 1.4. System-bound переменные

Часть кодов **нельзя писать через `setAttribute` / `putVar`** — только typed setters `StdPolicy`:

```text
pl_publicId, pl_draftId, pl_productCode, pl_productName,
pl_waitingPeriod, pl_policyTerm, pl_startDate, pl_endDate, pl_issueDate,
pl_installmentType, pl_insuredObjects, pl_commission, pl_commRate,
pl_policyNumber, pl_productVersion, pl_statusCode, pl_premium, pl_id,
io_packageCode
```

Нарушение → `422` («system-bound»). Это граница между «открытыми атрибутами продукта» и «каркасом договора».

### 1.5. Жизненный цикл статуса

```text
QUOTE  →  (save)  →  ISSUED | UNDERWRITING  →  (оплата)  →  PAID
```

| Статус | Смысл |
|--------|--------|
| `QUOTE` | Черновик расчёта / редактируемый |
| `UNDERWRITING` | Требует ручного рассмотрения (soft-gate на save) |
| `ISSUED` | Выпущен |
| `PAID` | Оплачен |

Значений `NEW` / `IN_PAYMENT` в `PolicyStatus` нет.

---

## 2. Ограничения на данные

### 2.1. Жёсткие (код процесса)

| Ограничение | Где | Эффект |
|-------------|-----|--------|
| **Ровно один** `insuredObject` | `ProcessOrchestratorService.calculatePremium` | иначе 422 |
| JsonPath покрытий вида `insuredObjects[0].…` | `PvVar.varSumInsured` и др. | мультиобъектность не поддержана |
| PreProcess всегда берёт `insuredObjects.get(0)` | `PreProcessService` | пустой список → создаётся один пустой объект |
| Пакет должен существовать в продукте | PreProcess / load package | иначе ошибка продукта |
| Quote: премия **> 0** | `assertPositivePremium` | иначе 422 |
| Save: `assertPositivePremium` **не** вызывается | as-is | возможна запись с нулевой премией |
| System-bound vars | `StdPolicy.assertVarWritable` | только typed API |
| DEV vs PROD `dataScope` | normalize + strip processList | в PROD digests/vars в ответе quote убираются |

### 2.2. Продуктовые (настраиваемые)

| Механизм | Что ограничивает |
|----------|------------------|
| `waitingPeriod` / `policyTerm` (`PeriodRule`: RANGE / LIST / …) | допустимые даты начала и срока |
| `quoteValidator` / `saveValidator` | обязательность, типы, regex, списки, диапазоны чисел |
| CEL PRE/POST_* | произвольные условия на vars |
| Пакет / covers / deductibles / limits | состав покрытий и допустимые франшизы/лимиты |
| Комиссия (min/max rate на account×product) | запрошенная ставка |
| Number generator | формат и уникальность номера (в рамках tid) |
| AuthZ | право QUOTE / SELL на продукт и договор |

### 2.3. Открытость vs каркас

```text
Каркас (фиксирован платформой)     Открытый слой (продукт / LOB)
─────────────────────────────     ─────────────────────────────
даты, премия, номер, статус       policyHolder.* attributes
insurer, commission shape         insuredObject.additionalAttributes
insuredObjects[0] + covers[]      набор PvVar / JsonPath
installments, options             CEL, validators, calculator
```

Кастомизация продукта **не меняет** каркас Java-модели; она наполняет открытый слой и правила вокруг него.

---

## 3. Модель продукта (что настраивается)

### 3.1. Версия продукта (`ProductVersionModel`)

Основные блоки конфигурации:

```text
ProductVersion
├── code, name, lob, insCompanyId, versionNo, versionStatus
├── phType / ioType
├── waitingPeriod / policyTerm          PeriodRule
├── vars[]                              PvVar — словарь данных
├── packages[]
│     ├── code, name
│     ├── covers[] (deductibles, limits, mandatory, terms)
│     ├── calculatorId
│     └── files[]                       печатные формы
├── quoteValidator[] / saveValidator[]
├── numberGenerator                     mask, reset, xor
└── rules                               PvProductRules (напр. insuredEqualsPolicyHolder)
```

Права на продукт в иерархии Account — отдельный контур (PRODUCT_ADMIN / привязка к Client / наследование; см. [Products.md](../development/Products.md)).

### 3.2. Словарь `product.vars`

Единый каталог полей для:

* материализации значений из JSON договора;
* калькулятора и тарифных факторов (`isTarifFactor`);
* CEL;
* текстовых шаблонов digests / ПФ;
* (в перспективе) UI schema и LLM-контекста.

Каждый `PvVar`: `varCode`, `varName`, `varPath`, `varType`, `varDataType`, флаги `isSystem` / `isOptional` / `isTarifFactor`, иерархия `parent_id`.

### 3.3. Калькулятор (на пакет)

`CalculatorModel`: formulas + coefficient tables + vars. Выполняется в quote и save после pre-validation; пишет премии/СС/франшизы в vars → покрытие/договор.

### 3.4. Валидаторы продукта (не CEL)

Списки `quoteValidator` / `saveValidator`, исполняет `ValidatorService` / `ValidatorImpl`.

Типы правил (as-is): `NOT_NULL`, сравнения, `MATCHES_REGEX`, `IN_LIST`, `RANGE`, плюс форматные (`EMAIL`, `PHONE*`, `INN`, `SNILS`, `PASSPORT`).

Отличие от CEL: декларативные попарные проверки keyLeft/keyRight; без произвольных выражений.

### 3.5. Прочее

| Рычаг | Назначение |
|-------|------------|
| Number generator | маска `{YY}…{XXXX}`, reset YEARLY/MONTHLY/NEVER, xor |
| Commission rates | min/max на SALE/PROLONGATION |
| Add-on pricelists | кросс-продажи / опции на quote |
| TEXT vars + TextDocumentView | `ph_digest`, `io_digest`, строки ПФ |
| Ins company | страховщик на договоре |
| Wire format / adapter | смена схемы JSON (план adapters) |

---

## 4. Правила CEL

### 4.1. Назначение

CEL — **выражения над vars** для бизнес-условий, которые неудобно или невозможно выразить простыми validator rules (комбинации возраста и СС, география, кросс-проверки полей).

Хранение: таблица `pt_rules` (`RuleEntity`). Движок: `CelRuleEngine`. Сервис: `RuleValidationServiceImpl`.

### 4.2. Типы (`RuleType`)

| Тип | isValidation | Использование в quote/save (as-is) |
|-----|--------------|-------------------------------------|
| `PRE_QUOTE_VALIDATION` | да | до расчёта (quote и save) |
| `POST_QUOTE_VALIDATION` | да | после расчёта (quote и save), hard-fail |
| `PRE_SAVE_VALIDATION` | да | до расчёта на save |
| `POST_SAVE_VALIDATION` | да | **в оркестраторе не вызывается** |
| `UNDERWRITING` | нет | вызывается на save, но `processValidation` **пропускает** non-validation типы → фактически не исполняется |
| `QUOTE_CALCULATION`, `WORKFLOW`, `CROSS_SELL`, `FRAUD_CHECK`, `ISSUANCE`, `RENEWAL` | нет | зарезервированы |

Семантика выражения: **`true` = правило выполнено (pass)**; `false` → собирается `message` правила.

### 4.3. Области видимости (`scope`)

Порядок применения слоёв:

```text
PRODUCT → LOB → TENANT → CLIENT
```

У каждого правила: `scopeType` + `scopeCode`, `priority`, `expression`, `message`, `recordStatus`.

Это позволяет держать общие правила на LOB/TENANT и уточнять на PRODUCT/CLIENT.

### 4.4. Контекст выполнения

`RuleValidationContext`:

* `tid`, `tenantCode`, `productCode`, `lobCode`, `clientId`;
* `variables` — map из `VariableContext` (числа часто как plain string для CEL helpers `num()` / `str()`).

В промптах и админке важно: **CEL видит только то, что есть в vars продукта и материализовано из договора**. System-bound поля попадают через vars/typed bindings, но писать их из правила нельзя.

### 4.5. Место в pipeline

```text
normalize → product/auth → commission check → init vars
    → ValidatorService (QUOTE[/SAVE])
    → CEL PRE_QUOTE [/ PRE_SAVE]
    → calculator
    → CEL POST_QUOTE          ← hard fail
    → digests / commission / (quote: add-ons)
    → [save] number + installments
    → [save] UNDERWRITING     ← целевой soft-gate статуса (см. gap выше)
    → [save] storage + payments
```

---

## 5. Способы кастомизации продукта (сводка)

Практический чеклист настройки нового продукта:

1. **LOB / шаблон** — типы PH/IO, базовые covers, seed vars.
2. **Версия продукта** — code, name, страховщик, периоды дат.
3. **Пакеты и покрытия** — обязательность, франшизы, лимиты, привязка калькулятора и файлов ПФ.
4. **Словарь vars** — JsonPath к wire JSON, MAGIC/TEXT, тарифные факторы.
5. **Калькулятор** — формулы и таблицы коэффициентов на пакет.
6. **Валидаторы QUOTE/SAVE** — обязательность и форматы «дешёвых» проверок.
7. **CEL** — сложные условия PRE/POST; в перспективе UNDERWRITING / POST_SAVE.
8. **Комиссия, номер, add-ons** — коммерческие и операционные настройки.
9. **Тексты** — digests и шаблоны печатных форм.
10. **Права** — привязка продукта к Client / группам Account.
11. **UI ввода** — статические блоки policy-entry / будущий schema из vars (см. policy-entry preproject).
12. **Wire adapter** (редко) — другой JSON-каркас под внешний канал.

Кастомизация **не** означает изменения оркестратора под каждый продукт: процесс один, отличается конфигурация вокруг `StdPolicy`.

---

## 6. LLM для настройки продукта (на основе модели договора)

### 6.1. Принцип

LLM **не заменяет** quote/save и **не пишет** договор напрямую. LLM — ассистент админки продукта: по тексту на естественном языке предлагает **черновик конфигурации**, который человек применяет через CRUD.

Контекст модели должен совпадать с тем, чем реально живёт процесс:

```text
product.vars  +  (опционально calculator.vars)  +  метаданные продукта
        │
        ▼
   тот же словарь, что попадает в VariableContext / CEL / калькулятор
```

Каркас `StdPolicy` / system-bound поля LLM **не изобретает** — только ссылается на них как на уже существующие коды (`pl_premium`, `ph_age_issue`, …).

Технический API: [llm-integration-preproject.md](./llm-integration-preproject.md). Конфиг провайдера per-tenant: [llm-tenant-config-preproject.md](./llm-tenant-config-preproject.md).

### 6.2. Задачи, привязанные к модели

| Задача LLM | Что генерирует | Куда попадает | Опирается на модель |
|------------|----------------|---------------|---------------------|
| **RULE** | CEL `expression` + `message` + `ruleType` + scope | `pt_rules` | vars + точки pipeline (PRE/POST_*) |
| **COVER** | код покрытия, PvVar-узлы, deductibles/limits | `packages[].covers` + `product.vars` | структура `insuredObjects[0].covers[]` |
| **CALCULATOR** | formulas / coefficients | `CalculatorModel` пакета | тарифные vars, cover premium/SI |
| **VALIDATOR** (расширение) | `ValidatorRule` NOT_NULL / IN_LIST / RANGE | quote/saveValidator | те же varCode |
| **DIGEST / TEXT** (расширение) | шаблон `{{ph_*}}` | TEXT PvVar / ПФ | TextDocumentView |
| **PERIOD** (расширение) | `waitingPeriod` / `policyTerm` PeriodRule | ProductVersion | даты шапки договора |
| **NUMBER_MASK** (расширение) | маска генератора | numberGenerator | `pl_policyNumber` на save |

Приоритет v1: **RULE → COVER → CALCULATOR** (как в llm-integration preproject).

### 6.3. Как договор ограничивает промпт

Чтобы LLM не предлагал несовместимое с платформой:

1. **Передавать только реальные `varCode`** из текущей версии продукта (formatter без значений полисов).
2. **Явно перечислять system-bound** — «не создавай write-правила на эти коды; читать можно».
3. **Фиксировать инвариант одного IO** — пути только `insuredObjects[0]…`, не массивы.
4. **Указывать допустимые RuleType** и что PRE_* = hard fail, UNDERWRITING = soft (когда будет включён).
5. **Требовать JSON schema ответа** и серверную валидацию:
   - CEL parse (`CelRuleEngine`);
   - все упомянутые varCode ∈ product.vars (иначе warning/error);
   - уникальность новых `co_*` / varCode.
6. **Никогда auto-commit в БД** — только preview → apply пользователем.

### 6.4. Примеры связки «текст → артефакт → договор»

**Правило (CEL)**

> Страхователь 18–50 лет, страховая сумма 100 000–2 000 000

→ draft:

```json
{
  "ruleType": "PRE_QUOTE_VALIDATION",
  "scopeType": "PRODUCT",
  "expression": "num(\"ph_age_issue\") >= 18 && num(\"ph_age_issue\") <= 50 && num(\"io_sumInsured\") >= 100000 && num(\"io_sumInsured\") <= 2000000",
  "message": "СС доступна только для возраста 18–50"
}
```

На quote/save выражение выполняется над vars того же договора.

**Покрытие**

> Добавить временную нетрудоспособность с франшизами …

→ узлы `co_TEMP_DISABILITY_*` в vars + cover в пакете → при quote появляются cover vars → калькулятор пишет premium/SI в каркас `Cover`.

**Калькулятор**

> Тариф от возраста и суммы …

→ `FormulaDef` / `CoefficientDef` по `isTarifFactor` vars → `runCalculator` заполняет `pl_premium` / cover premiums.

### 6.5. Что LLM делать не должен

| Нельзя | Почему |
|--------|--------|
| Менять Java-каркас / добавлять второй insuredObject «в модели» | hard-limit процесса |
| Писать system-bound через vars | `assertVarWritable` |
| Генерировать произвольный SQL / скрипты процесса | вне модели безопасности |
| Подменять AuthZ / tenant | отдельный контур |
| Записывать результат без preview | риск битой версии продукта |

### 6.6. Карта «модель → LLM»

```text
                    StdPolicy / PolicyDTO
                            │
         ┌──────────────────┼──────────────────┐
         ▼                  ▼                  ▼
   system-bound         open attrs          covers[0]
   (даты, премия…)    (PH/IO bags)         (SI, premium)
         │                  │                  │
         └────────────┬─────┴────────┬─────────┘
                      ▼              ▼
                 product.vars   calculator vars
                      │              │
                      ▼              ▼
              CEL / validators / TEXT / LLM context
                      │
                      ▼
            черновик RULE | COVER | CALCULATOR
                      │
                      ▼
                 CRUD продукта (человек)
                      │
                      ▼
              quote/save на реальном договоре
```

---

## 7. Краткие выводы

1. **Договор** = фиксированный каркас (`StdPolicy` / `PolicyDTO`) + открытые атрибуты и покрытия, наполняемые словарём продукта.
2. **Главное ограничение данных** сегодня — **один объект страхования** и system-bound поля; остальное — продуктовые validators/CEL/периоды.
3. **CEL** — слой сложных условий поверх vars; в hot path реально работают PRE/POST quote и PRE save; UNDERWRITING/POST_SAVE — зазор as-is.
4. **Кастомизация продукта** — vars, пакеты, калькулятор, validators, CEL, номер, комиссия, тексты, add-ons, права — без ветвления оркестратора.
5. **LLM** полезен как генератор **конфигов**, согласованных со словарём и каркасом договора; контекст = `product.vars`, apply = ручной preview, оркестратор полиса LLM не вызывает.
