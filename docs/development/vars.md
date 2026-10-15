# Словарь данных vars

> **Канон:** [`docs/curdoc/doc.md`](../curdoc/doc.md) — **§13** (схема→LOB→продукт), **§14.3** (`product.vars`), **§15.1** (калькулятор и `varCdm`).

Платформа на вход quote/save получает договор как JSON (wire). Обязательные поля задаёт каркас (`StdPolicy` / `PolicyDTO`); расширяемый словарь — `product.vars` (снимок LOB/`mpVars`).

## Уровни словаря

| Уровень | Где | Роль |
|---------|-----|------|
| Tenant schema | `mt_attribute_def` | Канон атрибутов; add только здесь |
| LOB | `mpVars` | Снимок + value/list/`isDeleted` |
| Product version | `vars` | Снимок для VariableContext; правки только DEV |

Каскад и правила правок — **§13**.

## Поля узла (as-is)

Типичный узел в `product.vars` / `mpVars`:

| Поле | Смысл |
|------|--------|
| `varCode` | Уникальный код в словаре |
| `varName` | Подпись |
| `varPath` | JsonPath к wire JSON |
| `varType` | `IN`, `MAGIC`, `CONST`, `VAR`, `OBJECT`, `TEXT`, … |
| `varDataType` | `STRING`, `NUMBER`, `OBJECT`, … |
| `varValue` / `varList` | Значение / ключ справочника |
| `isSystem` / `isDeleted` / `isTarifFactor` / `isOptional` | Флаги |
| `parent_id` / `id` | Дерево |

### `varType`

- **IN** — из входного JSON (`varPath`); set через произвольный `putVar` для system-bound запрещён.
- **OBJECT** — группировка в дереве.
- **CONST** — константа (часто в калькуляторе).
- **VAR** — переменная; system-bound (`isSystem`) пишет процесс (номер, премия, …).
- **MAGIC** — расчётные от входа (возраст, срок, …).
- **TEXT** — шаблоны `{{varCode}}` (as-is часто в том же словаре).

### `varCdm` (узкий смысл)

- В **схеме** (`mt_attribute_def`) колонки `var_cdm` **нет** (удалена миграциями V9–V12).
- В **LOB** (`LobVar`) поля **нет**.
- В **калькуляторе** / части `PvVar`: `varCdm = "CALCULATOR"` — маркер vars калькулятора. Других живых семантик `varCdm` на бэкенде нет.

## Зачем словарь

Flatten nested JSON → VariableContext для validators, CEL, калькулятора, digests/ПФ и UI. Дерево словаря не обязано совпадать 1:1 с формой wire JSON; связь — через `varPath`.

После получения запроса JSON материализуется в контекст вместе с картой словаря версии продукта.
