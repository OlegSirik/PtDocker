/**
 * Ответ GET дерева схемы (сериализация `LobVar` на бэкенде). Только поля с сервера —
 * без клиентских вычислений и без UI-состояния.
 *
 * Остальные свойства {@link TreeTableSourceRow} задаются на клиенте (мердж с продуктом,
 * правки в диалоге, флаг `isDeleted` после `filterTree` в `vars.service` и т.д.).
 */
export interface SchemaTreeApiRow {
  id: number;
  parent_id: number | null;
  /** Порядок узла (`var_ord` / `varNr`); с API может прийти строкой */
  varNr?: string | number | null;
  varCode?: string | null;
  varName?: string | null;
  varPath?: string | null;
  varType?: string | null;
  varDataType?: string | null;
  varValue?: string | null;
  varCdm?: string | null;
  varList?: string | null;
  isSystem?: boolean;
  isDeleted?: boolean;
  /** См. LobVar / PvVar на бэкенде; при отсутствии в JSON клиент подставляет из varName */
  name?: string | null;
}

function parseSchemaVarNr(value: string | number | null | undefined): number {
  if (value == null || value === '') return 0;
  if (typeof value === 'number') return Number.isFinite(value) ? value : 0;
  const n = Number(String(value).trim());
  return Number.isFinite(n) ? n : 0;
}

/** Собрать строку для таблицы из сырого ответа API */
export function schemaTreeApiRowToSourceRow(row: SchemaTreeApiRow): TreeTableSourceRow {
  return {
    id: row.id,
    parent_id: row.parent_id,
    varNr: parseSchemaVarNr(row.varNr),
    varName: row.varName ?? '',
    varCode: row.varCode ?? '',
    varType: row.varType ?? undefined,
    varDataType: row.varDataType ?? undefined,
    isSystem: row.isSystem ?? false,
    isDeleted: row.isDeleted ?? false,
    varList: row.varList ?? undefined,
    varPath: row.varPath ?? '',
    varValue: row.varValue ?? '',
    varCdm: row.varCdm ?? '',
    name: row.name?.trim() || row.varName?.trim() || '',
  };
}

export function mapSchemaTreeApiToSourceRows(rows: SchemaTreeApiRow[] | null | undefined): TreeTableSourceRow[] {
  return (rows ?? []).map(schemaTreeApiRowToSourceRow);
}

/**
 * Модель строки дерева в UI: обязательные поля для {@link TreeTableComponent} плюс опциональные
 * данные, часть которых приходит не из GET tree, а считается или подмешивается на клиенте.
 *
 * Под конкретное дерево добавляйте поля через `extends` или {@link ExtendTreeTableRow}:
 *
 * @example
 * ```ts
 * export interface SchemaTreeRow extends TreeTableSourceRow {
 *   varType?: string;
 *   documentId?: string;
 * }
 *
 * treeData: SchemaTreeRow[] = [];
 * // <app-tree-table [rows]="treeData" ...> — в шаблонах #actionsCell let-item: item имеет тип SchemaTreeRow
 * ```
 *     
 */
export interface TreeTableSourceRow {
  id: number;
  parent_id: number | null;
  varNr: number;
  varName: string;
  varCode: string;
  /** Произвольные данные для колонки действий (кнопки и т.д.) */
  actions?: unknown;
  /** Скрыть строку после {@link filterTree} (код не в фильтре или нет живых потомков) */
  isDeleted?: boolean;
  varType?: string; // "varType": "OBJECT" или все остальное
  varDataType?: string;
  isSystem?: boolean; // признак системной переменной (если true, то строка не удаляется из дерева)
  /** Тарифный фактор (PvVar / продукт) */
  isTarifFactor?: boolean;
  /** Опциональная переменная (PvVar / продукт) */
  isOptional?: boolean;
  varList?: string; // название список кодов переменных, которые относятся к этой переменной
  varListFilter?: string; // фильтр для списка кодов переменных, которые относятся к этой переменной. Копируется из валидатора
  varPath: string;
  varValue: string;
  varCdm: string;
  name: string;
  /**
   * Только клиент: узел создан в сессии ({@code new}) или изменён и ещё не сохранён в документе
   * (очищается после успешного save родительского экрана).
   */
  unsavedSchemaRow?: 'new' | 'modified';
}

/** Узел-контейнер схемы (папка), не редактируется как переменная */
export function isObjectVarTypeRow(row: TreeTableSourceRow): boolean {
  return (row.varType?.trim().toUpperCase() ?? '') === 'OBJECT';
}

/** Сборка строки дерева: базовые поля + ваш объект с доп. полями */
export type ExtendTreeTableRow<Extra extends object> = TreeTableSourceRow & Extra;

/** Одна видимая строка в MatTable */
export interface TreeTableFlatRow<T extends TreeTableSourceRow = TreeTableSourceRow> {
  data: T;
  level: number;
  expandable: boolean;
  expanded: boolean;
}
