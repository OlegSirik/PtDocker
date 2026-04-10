import type { BusinessLineCoefficientColumn } from '../../shared/services/business-line-edit.service';

function normalizeOperator(op: string | undefined | null): string | null {
  if (op == null) {
    return null;
  }
  const s = op.trim().toUpperCase();
  switch (s) {
    case '=':
    case '>':
    case '<':
    case '>=':
    case '<=':
    case '<>':
      return s;
    case 'LIKE':
      return 'LIKE';
    default:
      return null;
  }
}

function normalizeOrder(order: string | undefined | null): string | null {
  if (order == null) {
    return null;
  }
  const s = order.trim().toUpperCase();
  switch (s) {
    case 'ASC':
    case 'DESC':
      return s;
    default:
      return null;
  }
}

/** Как CoefficientServiceImpl#getSQL (coefficient_data); calculatorId=0 — плейсхолдер для ЛБ. */
export function buildCoefficientDataSqlTemplate(
  calculatorId: number | string,
  coefficientCode: string,
  columns: BusinessLineCoefficientColumn[],
): string | null {
  if (coefficientCode == null || columns == null) {
    return null;
  }

  let sql = 'select result_value from coefficient_data where calculator_id = ';
  sql += String(calculatorId);
  sql += " and coefficient_code = '";
  sql += String(coefficientCode).replace(/'/g, "''");
  sql += "'";

  let orderBy = '';

  for (const col of columns) {
    if (col == null) {
      continue;
    }
    const varCode = col.varCode;
    const nr1 = parseInt(String(col.nr).trim(), 10);
    if (Number.isNaN(nr1)) {
      return null;
    }
    const nr = String(nr1 - 1);
    const op = col.conditionOperator;
    const sortOrder = col.sortOrder;
    const varDataType = col.varDataType;

    if (varCode == null || op == null) {
      return null;
    }
    if (!/^(10|[0-9])$/.test(nr)) {
      return null;
    }

    const operator = normalizeOperator(op);
    if (operator == null) {
      return null;
    }

    if (varDataType != null && varDataType.toUpperCase() === 'NUMBER') {
      sql += ` AND to_number(col${nr},'9999999999.99') ${operator}:${varCode}`;
    } else {
      sql += ` AND col${nr} ${operator} :${varCode}`;
    }

    const ord = normalizeOrder(sortOrder);
    if (ord != null) {
      if (orderBy.length === 0) {
        orderBy += ' order by ';
      } else {
        orderBy += ', ';
      }
      if (varDataType != null && varDataType.toUpperCase() === 'NUMBER') {
        orderBy += `to_number(col${nr},'9999999999.99') `;
      } else {
        orderBy += `col${nr} `;
      }
      orderBy += ` ${ord}`;
    }
  }

  if (orderBy.length > 0) {
    sql += orderBy;
  }
  sql += ' limit 1';
  return sql;
}
