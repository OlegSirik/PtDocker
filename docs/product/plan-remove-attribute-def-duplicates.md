# План: удаление `code`, `name`, `var_cdm` из `mt_attribute_def`

> **Статус:** фазы V9–V12 выполнены. Актуальная модель схемы и каскада: [`docs/curdoc/doc.md`](../curdoc/doc.md) **§13**. Этот файл — исторический план работ.

Цель: убрать дубли и устаревший CDM из схемы атрибутов.  
Порядок: **по одному полю** — сначала самый безопасный.

Область: колонки таблицы `mt_attribute_def` и связанные поля в `AttributeDefEntity` / `LobVar` / `PvVar` / API / UI.  
Не трогать: `mt_contract_model.code/name` (это модель договора, не атрибут).

Критерий каждой фазы: миграция БД + код компилируется + smoke: schema tree, metadata JSON, LOB/product vars, quote.

---

## Фаза 0. Инвентаризация и правила (без DROP)

1. Зафиксировать эталон:
   - `GET …/admin/schema/{code}/attributes`
   - `GET …/admin/schema/{code}/attributes/metadata`
   - LOB example JSON (если есть)
2. Правило отображения UI: везде **`varName`**, не `name`.
3. Правило ключа metadata: везде **`varCode`**, не `code`.
4. Правило группировки (вместо CDM): **`varPath`** и/или дерево `parent_id` (решение до фазы 3).

**Выход:** чеклист файлов + выбранный fallback (`varPath` vs parent tree).

---

## Фаза 1. Удалить `name` (низкий риск) — **сделано**

### 1.1. Код (ещё с колонкой в БД)

| Шаг | Действие |
|-----|----------|
| 1 | `SchemaServiceImpl.toLobVar` — не маппить `name` (или всегда `name = varName` временно) |
| 2 | `applyLobVarToEntity` — убрать `e.setName(...)` |
| 3 | Копирование схемы при new tenant — не копировать `name` |
| 4 | `LobVar` / `PvVar`: поле `name` deprecated → постепенно убрать с API |
| 5 | Фронт: tree-table / contract-schema / lob-edit — только `varName` |

### 1.2. БД

```sql
-- новая Flyway, напр. V9__attribute_def_drop_name.sql
ALTER TABLE mt_attribute_def DROP COLUMN IF EXISTS name;
```

### 1.3. Entity

Удалить поле `name` из `AttributeDefEntity`.

### 1.4. Проверка

- [ ] Schema admin: дерево, edit/save
- [ ] Metadata JSON без регрессии ключей
- [ ] Product/LOB сохранение vars

---

## Фаза 2. Удалить `code` (средний риск) — **сделано** (V10)

### 2.1. Код до DROP

| Шаг | Действие |
|-----|----------|
| 1 | `attributeJsonKey(e)` → **только** `e.getVarCode()` |
| 2 | Снять запись `e.setCode(v.getVarCode())` |
| 3 | Копирование attribute_def — не копировать `code` |
| 4 | Сверить metadata JSON до/после на эталонном тенанте (ключи = varCode) |
| 5 | Убрать `code` из DTO схемы, если есть (`AttributeDefDto`) |

### 2.2. БД

```sql
ALTER TABLE mt_attribute_def DROP COLUMN IF EXISTS code;
```

### 2.3. Entity

Удалить `code` из `AttributeDefEntity`.

### 2.4. Проверка

- [ ] Metadata JSON: ключи совпадают с ожидаемыми `varCode` (не старый короткий `code`, если где-то расходились)
- [ ] ProductService / OpenAPI / примеры LOB, завязанные на metadata
- [ ] Регресс: если в данных `code ≠ var_code` — заранее выравнять или задокументировать смену ключей

**Стоп-критерий перед DROP:**  
`SELECT COUNT(*) FROM mt_attribute_def WHERE code IS NOT NULL AND code <> var_code;` → 0 или осознанная миграция данных.

---

## Фаза 3. Удалить `var_cdm`

### 3a (сделано) — колонка схемы + schema API

- Мёртвые UI-потребители PH/IO prefix убраны ранее.
- `CALCULATOR` остаётся только в JSON калькулятора (`PvVar.varCdm`), не в БД схемы.
- `V11__attribute_def_drop_var_cdm.sql` — `DROP COLUMN var_cdm`.
- `AttributeDefEntity` / `SchemaServiceImpl` / `SchemaTreeDto` / `mapMetadataToVar` — без CDM.
- Schema UI (contract-schema, lob-edit sync) шлёт пустой `varCdm`.

### 3b (позже) — JSON LOB/product

| Осталось | Примечание |
|----------|------------|
| `LobVar` / `PvVar.varCdm` в JSON продукта/LOB | `strings.*`, старые данные; калькулятор — отдельно |
| formly-forms | мёртвый экран |
| Полное удаление поля из DTO фронта | после чистки strings / rename калькуляторного тега |

### 3.4. Проверка

- [x] Calculator vars без зависимости от CDM схемы
- [ ] Schema admin CRUD после миграции V11
- [ ] Quote/save без регрессии

---

## Порядок релизов (рекомендуемый)

```text
Релиз A: Фаза 1 (name)           — код + DROP name
Релиз B: Фаза 2 (code)           — attributeJsonKey → varCode, выравнивание, DROP code
Релиз C: Фаза 3a (cdm schema)    — DROP mt_attribute_def.var_cdm (V11) ✓
Релиз D: Фаза 3b (cdm JSON)      — по желанию: чистка LobVar/PvVar / formly
```

---

## Вне скоупа (не путать)

| Объект | Почему не в этом плане |
|--------|-------------------------|
| `mt_contract_model.code/name` | идентификатор модели договора |
| `PvVar.name` / `varCdm` **внутри JSON продукта** | отдельная чистка product document; можно после фазы 3 |
| `PvVarDefinition.code` | это **varCode** runtime, не колонка `mt_attribute_def.code` |

---

## Чеклист «готово»

- [x] Фаза 1: колонки `name` нет, UI на `varName` (V9__attribute_def_drop_name.sql)
- [x] Фаза 2: колонки `code` нет, metadata только на `varCode` (V10__attribute_def_drop_code.sql)
- [x] Фаза 3a: колонки `var_cdm` нет в схеме (V11__attribute_def_drop_var_cdm.sql); CALCULATOR в calculator JSON
- [ ] Фаза 3b: опциональная чистка `varCdm` в LOB/product JSON / formly
- [ ] Quote/save без регрессии (не зависят от этих колонок напрямую)
