/** Плоская запись из API / БД для иерархической таблицы */
export interface TreeTableSourceRow {
  id: number;
  parent_id: number | null;
  order_nr: number;
  name: string;
  code: string;
  /** Произвольные данные для колонки действий (кнопки и т.д.) */
  actions?: unknown;
}

/** Одна видимая строка в MatTable */
export interface TreeTableFlatRow<T extends TreeTableSourceRow = TreeTableSourceRow> {
  data: T;
  level: number;
  expandable: boolean;
  expanded: boolean;
}
