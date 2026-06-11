# Процесс оформления: изоляция формата документа

## 1. Цель

Изолировать **формат входного/выходного JSON** от оркестратора.

Несколько форматов → несколько адаптеров. Оркестратор работает с **`PolicyProcessDocument`** и **`CalculatorContext`**, не зная структуру JSON конкретного формата.

---

## 2. Было (as-is, `ProcessOrchestratorService`)

```
HTTP JSON
  → policyFromJson() → PolicyDTO
  → preProcess, policyToJson()
  → VariableContextImpl2(JsonPath)     ← формат зашит в orchestrator
  → validate / CEL / calculator
  → policyToJson() → ответ
```

---

## 3. Стало (реализовано в `ProcessOrchestratorService2`)

```
HTTP JSON + format code
  → PolicyFormatAdapterRegistry.resolve(format)
  → adapter.deserialize(json) → PolicyProcessDocument
  → prepareQuote/SaveDocument(document)
  → product, auth, preProcessService.applyProductMetadata
  → VariableContextImpl2 + adapter.buildContext(document, varCtx)
  → validate / CEL / calculatePremium / commission / save
  → adapter.serialize(document) → ответ
```

### Схема

```
  JSON + "POLICY_V1"
        │
        ▼
  PolicyFormatAdapterRegistry
        │
        ▼
  PolicyV1FormatAdapter
    deserialize / serialize / buildContext
        │
        ▼
  PolicyProcessDocument  ◄── PolicyDTO (V1)
        │
        ▼
  ProcessOrchestratorService2
    validate, CEL, calc, commission, storage
        │
        ▼
  CalculatorContext  (validator, CEL, calculator)
```

---

## 4. Классы

| Класс | Модуль | Назначение |
|-------|--------|------------|
| `PolicyProcessDocument` | pt-api | интерфейс документа для оркестратора (методы как у `PolicyDTO`) |
| `PolicyDTO` | pt-api | реализация V1, `implements PolicyProcessDocument` |
| `PolicyFormatAdapter` | pt-api | контракт адаптера формата |
| `PolicyFormatAdapterRegistry` | pt-process | стратегия: `List<Adapter>` → resolve по `getCode()` |
| `AbstractPolicyFormatAdapter` | pt-process | Jackson deserialize/serialize |
| `PolicyV1FormatAdapter` | pt-process | V1: `buildContext` = enrichVariables + cover vars |
| `ProcessOrchestratorService2` | pt-process | оркестратор (`@Primary`) |

### `PolicyFormatAdapter`

```java
String getCode();
PolicyProcessDocument deserialize(String json);
String serialize(PolicyProcessDocument document);
void buildContext(PolicyProcessDocument document, CalculatorContext context);
```

`buildContext` **не создаёт** `CalculatorContext` — оркестратор создаёт `VariableContextImpl2`, адаптер только обогащает (covers, enrichVariables).

---

## 5. Поток `calculate` / `save`

Общие шаги:

1. `adapter = registry.resolve(format)`
2. `document = adapter.deserialize(policyJson)`
3. `prepareQuoteDocument` / `prepareSaveDocument`
4. `product = productService.getProductByCode(document.getProductCode(), …)`
5. `preProcessService.applyProductMetadata((PolicyDTO) document, product)` — пока cast для V1
6. `varCtx = buildCalculatorContext(adapter, document, product)`
7. validate, CEL, `calculatePremium`, commission, …
8. `adapter.serialize(document)`

Локальные переменные в методе: `document`, `varCtx`, `user`, `product`, `format`.  
Отдельный «контекст выполнения» (bag object) **не используется** — избыточен при линейном потоке.

---

## 6. Новый формат документа

1. Класс, реализующий `PolicyProcessDocument` (или обёртка).
2. `@Component` — `PolicyXxxFormatAdapter extends AbstractPolicyFormatAdapter<…>`.
3. Spring подхватит в `PolicyFormatAdapterRegistry` автоматически.
4. Оркестратор **не меняется** (кроме cast-границ, пока storage/preProcess завязаны на `PolicyDTO`).

---

## 7. Границы V1 (временные cast)

| Место | Почему cast |
|-------|-------------|
| `preProcessService.applyProductMetadata` | сигнатура принимает `PolicyDTO` |
| `postProcessService.setCovers` | то же |
| `storageService.save` | то же |

При втором формате — расширить эти сервисы под `PolicyProcessDocument` или добавить метод в адаптер.

---

## 8. Не реализовано / TODO

- [ ] `format` — параметр `calculate(json, format)` / `save(json, format)` в `ProcessOrchestrator` и контроллере (сейчас `"POLICY_V1"` в методе)
- [ ] Убрать cast `(PolicyDTO)` на границах сервисов
- [ ] Общий метод `processQuote` / `processSave` — убрать дублирование calculate/save
- [ ] `DtoBackedVariableContext` без JsonPath round-trip (фаза 2)
- [ ] `update`, `addendum` в Service2

---

## 9. Удалено как избыточное

| Артефакт | Причина |
|----------|---------|
| `PolicyExecutionContext` | дублировал локальные переменные, нигде не читался |
| `PolicyFormatMapper` | заменён на `PolicyFormatAdapter` |
| `CanonicalVarCatalog` | не вызывался; переменные берутся из `product.vars` в `VariableContextImpl2` |
| `PolicyCanonicalVar`, `CoverCanonicalVar` | использовались только каталогом |

Проверка «каких переменных не хватает» — при необходимости через `product.getVars()` и `CalculatorContext.getDefinitions()`, без отдельного каталога.
