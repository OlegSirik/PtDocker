# Pre-project: LLM-интеграция (DeepSeek и другие провайдеры)

См. также: [vars.md](../development/vars.md) — словарь данных продукта.

## 1. Цель

Ассистент в админке продукта: пользователь вводит **текст на естественном языке**, модель возвращает **черновик** для применения через CRUD.

Сценарии (v1+):

| Сценарий | Промпт | Результат |
|----------|--------|-----------|
| **Правило** | `llm/prompts/rule.txt` | `{ rule: { code, name, condition } }` — CEL для `pt_rules` |
| **Покрытие** | `llm/prompts/cover.txt` | `{ cover: { code, name, vars[] } }` — узлы в `product.vars` |
| **Калькулятор** | `llm/prompts/calculator.txt` | `{ calculator: { formulas, coefficients, vars } }` |

**Контекст для модели** — не статический список `ph_*` / `io_*`, а **`product.vars`** текущей версии продукта (`ProductVersionModel.vars`, `List<PvVar>`). Это тот же словарь, что используется в quote/save и калькуляторе.

Оркестратор полиса LLM **не вызывает**.

---

## 2. As-is

| Компонент | Сейчас |
|-----------|--------|
| Словарь | `product.vars` — дерево `PvVar` ([vars.md](../development/vars.md)) |
| Правила | `pt_rules`, CEL, `CelRuleEngine` |
| Калькулятор | `CalculatorModel` (formulas, coefficients, vars) |
| LLM | нет |

---

## 3. To-be

```
PoliTechFront
  POST /api/v1/products/llm/assist
    { taskType, userMessage, productId, versionNo }
        │
        ▼
LlmAssistantService (ru.pt.product.llm)
  1. ProductService → ProductVersionModel
  2. VarsContextFormatter → компактный JSON из product.vars
  3. system: llm/prompts/{taskType}.txt
  4. user: userMessage + vars + метаданные продукта
  5. LlmGateway → DeepSeek
  6. *ResponseProcessor по taskType
        │
        ▼
LlmAssistResponse → UI preview → apply (CRUD)
```

### Размещение

| Слой | Путь |
|------|------|
| Модуль | `pt-product` |
| Подмодуль | `ru.pt.product.llm` |
| Промпты | `pt-product/src/main/resources/llm/prompts/*.txt` |
| API | `pt-api` — `LlmAssistantService`, DTO |

**Зависимости:** `pt-product` → `pt-api`, `pt-rules` (CEL для `RULE`).

---

## 4. Контекст: `product.vars`

Сервер **сам** загружает словарь по `productId` + `versionNo`. Клиент vars не передаёт.

### 4.1 `VarsContextFormatter`

Преобразует `List<PvVar>` в компактный JSON для промпта (без значений полисов):

```json
[
  {
    "varCode": "ph_age_issue",
    "varName": "Страхователь.возраст на дату выпуска",
    "varType": "MAGIC",
    "varDataType": "NUMBER",
    "isTarifFactor": false,
    "parent_id": 341
  },
  {
    "varCode": "io_sumInsured",
    "varName": "Страховая сумма объекта",
    "varType": "IN",
    "isTarifFactor": true,
    "parent_id": 120
  }
]
```

Правила форматирования:

- пропускать `isDeleted == true`;
- для RULE — все узлы с `varCode` (IN, MAGIC, VAR, OBJECT);
- для CALCULATOR — акцент на `isTarifFactor` + CONST/VAR калькулятора;
- для COVER — дерево под узлом `covers` / существующие `co_*`.

Ссылка: тот же каталог, что попадает в `VariableContext` при оформлении ([policy-execution-context](../product/policy-execution-context-preproject.md)).

### 4.2 User-сообщение (сборка)

```
Запрос пользователя:
Для страхователя от 18 лет до 50 доступна страховая сумма от 100000 до 2000000

Продукт: NS_CLASSIC (ACC), версия 1

Доступные переменные (product.vars):
[ ... JSON от VarsContextFormatter ... ]
```

---

## 5. Публичный API (`pt-api`)

### 5.1 Тип задачи

```java
public enum LlmTaskType {
    RULE,        // CEL-правило → pt_rules
    COVER,       // новое покрытие → product.vars
    CALCULATOR   // formulas/coefficients → CalculatorModel
}
```

### 5.2 Запрос / ответ

```java
public class LlmAssistRequest {
    LlmTaskType taskType;
    String userMessage;
    Long productId;
    Long versionNo;           // optional, default — активная версия
    String providerCode;
    String model;
}

public class LlmAssistResponse {
    boolean success;
    LlmTaskType taskType;
    Object result;            // LlmRuleDraft | LlmCoverDraft | LlmCalculatorDraft
    String rawContent;
    List<String> warnings;    // напр. varCode не из product.vars
    List<String> errors;
    LlmUsage usage;
}
```

```java
// RULE
public class LlmRuleDraft {
    String code;
    String name;
    String condition;         // CEL
}

// COVER
public class LlmCoverDraft {
    String code;
    String name;
    Boolean isMandatory;
    List<PvVar> vars;         // новые узлы для merge в product.vars
}

// CALCULATOR
public class LlmCalculatorDraft {
    List<FormulaDef> formulas;
    List<CoefficientDef> coefficients;
    List<PvVar> vars;
}
```

---

## 6. Промпты (несколько, общий контекст vars)

```
llm/prompts/
  rule.txt        — CEL-правила
  cover.txt       — добавление покрытия
  calculator.txt  — описание калькулятора
```

Каждый промпт:

- описывает **роль** и **формат JSON-ответа**;
- **не содержит** полный справочник переменных — только ссылка «используй блок Доступные переменные»;
- синтаксис CEL — только в `rule.txt`.

`PromptAssembler`:

```java
String system = load("llm/prompts/" + taskType.name().toLowerCase() + ".txt");
String varsJson = varsContextFormatter.format(productVersionModel.getVars(), taskType);
String user = userMessage + "\n\nПродукт: " + code + "\n\nДоступные переменные:\n" + varsJson;
```

---

## 7. Обработка ответа

| Processor | Валидация | Apply (после UI) |
|-----------|-----------|------------------|
| `RuleResponseProcessor` | JSON, `CelRuleEngine.validateExpression`, varCode ∈ product.vars | `RuleManagementService.create` |
| `CoverResponseProcessor` | уникальность `co_*`, структура PvVar | `ProductService` merge vars |
| `CalculatorResponseProcessor` | varCode в formulas/coefficients ∈ vars | `CalculatorService` update |

Общий pipeline:

1. `JsonExtractor` — parse JSON;
2. schema / bean validation;
3. доменная проверка (CEL, дубликаты varCode);
4. `warnings` если модель сослалась на varCode вне `product.vars`;
5. **без автозаписи в БД**.

### Пример RULE

Запрос:

> Для страхователя от 18 лет до 50 доступна страховая сумма от 100000 до 2000000

Ответ:

```json
{
  "rule": {
    "code": "PH_AGE_SUM_INSURED_RANGE",
    "name": "Диапазон СС для страхователя 18–50 лет",
    "condition": "ph_age_issue >= 18 && ph_age_issue <= 50 && io_sumInsured >= 100000 && io_sumInsured <= 2000000"
  }
}
```

`ph_age_issue` и `io_sumInsured` должны присутствовать в переданном `product.vars`.

### Пример COVER (черновик)

```json
{
  "cover": {
    "code": "TEMP_DISABILITY",
    "name": "Временная нетрудоспособность",
    "isMandatory": false,
    "vars": [
      { "varCode": "co_TEMP_DISABILITY", "varName": "ВН", "varType": "OBJECT", "varPath": "..." },
      { "varCode": "co_TEMP_DISABILITY_sumInsured", "varName": "Страховая сумма", "varType": "VAR", "parent_id": "..." }
    ]
  }
}
```

### Пример CALCULATOR (черновик)

```json
{
  "calculator": {
    "coefficients": [
      {
        "code": "K_AGE_18_50",
        "name": "Возраст 18–50",
        "conditions": [
          { "when": { "left": "insuredAge", "operator": "BETWEEN", "right": [18, 50] }, "value": 1.0 }
        ]
      }
    ],
    "formulas": []
  }
}
```

`insuredAge` должен быть в `product.vars` с `isTarifFactor=true` (или как IN/MAGIC в дереве).

---

## 8. HTTP API

```
POST /api/v1/products/llm/assist

{
  "taskType": "RULE",
  "userMessage": "Для страхователя от 18 лет до 50 доступна страховая сумма от 100000 до 2000000",
  "productId": 42,
  "versionNo": 1
}
```

---

## 9. Провайдеры

| Provider | URL | Модель по умолчанию |
|----------|-----|---------------------|
| **routerai** (default) | `https://routerai.ru/api/v1/chat/completions` | `deepseek/deepseek-v4-flash` |
| deepseek | `https://api.deepseek.com/chat/completions` | `deepseek-chat` |

Пример RouterAI (эквивалент нашему клиенту):

```bash
curl -X POST "https://routerai.ru/api/v1/chat/completions" \
  -H "Authorization: Bearer $ROUTERAI_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "deepseek/deepseek-v4-flash",
    "messages": [
      {"role": "system", "content": "..."},
      {"role": "user", "content": "..."}
    ],
    "response_format": {"type": "json_object"}
  }'
```

`OpenAiCompatibleLlmProvider` формирует тот же запрос (+ `temperature`, `max_tokens`, `response_format` для JSON-ответа правил).

```yaml
app.llm:
  default-provider: routerai
  providers:
    routerai:
      base-url: https://routerai.ru/api/v1
      api-key: ${ROUTERAI_API_KEY:}
      default-model: deepseek/deepseek-v4-flash
```

---

## 10. Структура пакетов

```
ru.pt.product.llm
  configuration/LlmProperties.java
  provider/...
  prompt/
    PromptAssembler.java
    VarsContextFormatter.java
  service/LlmAssistantServiceImpl.java
  processor/
    LlmResponseProcessor.java
    RuleResponseProcessor.java
    CoverResponseProcessor.java
    CalculatorResponseProcessor.java
  util/JsonExtractor.java
```

---

## 11. План внедрения

### Фаза 1

- [ ] `VarsContextFormatter` + `prompts/rule.txt`
- [ ] `RULE` end-to-end + CEL validate
- [ ] `POST /api/v1/products/llm/assist`

### Фаза 2

- [ ] `COVER` + `cover.txt` + merge vars
- [ ] UI preview для правил и покрытий

### Фаза 3

- [ ] `CALCULATOR` + `calculator.txt`
- [ ] audit, второй провайдер

---

## 12. Риски

| Риск | Митигация |
|------|-----------|
| Большой `product.vars` | лимит токенов: фильтр по taskType, только varCode+varName |
| Выдуманные varCode | сверка с product.vars, warnings |
| Невалидный CEL | `CelRuleEngine` |
| Расхождение vars продукта и калькулятора | для CALCULATOR передавать union product.vars + calculator.vars |

---

## 13. Итог

| Решение | Выбор |
|---------|--------|
| Модуль | `pt-product` / `ru.pt.product.llm` |
| Контекст | **`product.vars`** (форматируется на сервере) |
| Промпты | **несколько** — `rule`, `cover`, `calculator` |
| Правила | CEL в `condition` |
| v1 apply | preview → CRUD |
