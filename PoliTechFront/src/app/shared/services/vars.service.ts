import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from './auth.service';
import {
  isObjectVarTypeRow,
  mapSchemaTreeApiToSourceRows,
  type SchemaTreeApiRow,
  type TreeTableFlatRow,
  type TreeTableSourceRow,
} from '../models/tree-table.models';

/**
 * Помечает строки дерева {@link TreeTableSourceRow.isDeleted}.
 *
 * - Для узлов с {@link TreeTableSourceRow.varType} ≠ OBJECT действует фильтр по {@code varCode} (как раньше).
 * - Узлы OBJECT не сравниваются с {@code filter} по коду; остаются только если есть хотя бы один
 *   «живой» потомок. OBJECT без детей или с полностью отфильтрованными детьми скрываются.
 */
export function filterTree(rows: TreeTableSourceRow[], filter: string[]): TreeTableSourceRow[] {
  const filterSet = new Set(filter);
  const byId = new Map(rows.map((r) => [r.id, r]));
  const childrenByParent = new Map<number, TreeTableSourceRow[]>();

  for (const r of rows) {
    const pid = r.parent_id;
    if (pid != null) {
      let list = childrenByParent.get(pid);
      if (!list) {
        list = [];
        childrenByParent.set(pid, list);
      }
      list.push(r);
    }
  }

  const survivesMemo = new Map<number, boolean>();

  function survives(id: number): boolean {
    const cached = survivesMemo.get(id);
    if (cached !== undefined) {
      return cached;
    }
    const row = byId.get(id);
    if (!row) {
      survivesMemo.set(id, false);
      return false;
    }

    const children = childrenByParent.get(id) ?? [];

    if (isObjectVarTypeRow(row)) {
      if (children.length === 0) {
        survivesMemo.set(id, false);
        return false;
      }
      const ok = children.some((c) => survives(c.id));
      survivesMemo.set(id, ok);
      return ok;
    }

    if (!filterSet.has(row.varCode)) {
      survivesMemo.set(id, false);
      return false;
    }
    if (children.length === 0) {
      survivesMemo.set(id, true);
      return true;
    }
    const ok = children.some((c) => survives(c.id));
    survivesMemo.set(id, ok);
    return ok;
  }

  for (const r of rows) {
    r.isDeleted = !survives(r.id);
  }
  return rows;
}

export interface LobVar {
  varCode: string;
  varType: string;
  varPath: string;
  varName: string;
  varDataType: string;
  varValue: string;
  /** С API может прийти числом или строкой (Jackson / LobVar). */
  varNr?: string | number;
  id: number;
  parent_id: number | null;
  varList: string | null;
  isSystem: boolean;
  isDeleted: boolean;
  name: string;
  /** ENABLED / DISABLED — mt_attribute_def.default_state */
  defaultState?: 'ENABLED' | 'DISABLED' | string;
}

/** Модель договора из {@code mt_contract_model}. */
export interface ContractModel {
  id: number;
  code: string;
  name: string;
}

@Injectable({
    providedIn: 'root'
  })

export class VarsService {
  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  // ---------- Schema API (contract model = 'box') ----------

  private schemaUrl(contractModel: string): string {
    return `${this.authService.baseApiUrl}/admin/schema/${contractModel}`;
  }

  /**
   * Список моделей договора ({@code mt_contract_model}).
   * GET /api/v1/{tenant}/admin/schema/models
   */
  listContractModels(): Observable<ContractModel[]> {
    const url = `${this.authService.baseApiUrl}/admin/schema/models`;
    return this.http.get<ContractModel[]>(url);
  }

  /**
   * Дерево атрибутов схемы договора (бывший GET …/tree).
   * GET /api/v1/{tenant}/admin/schema/{contractCode}/attributes
   */
  loadAttributes(contractModel: string): Observable<TreeTableSourceRow[]> {
    const url = `${this.schemaUrl(contractModel)}/attributes`;
    return this.http
      .get<SchemaTreeApiRow[]>(url)
      .pipe(map(mapSchemaTreeApiToSourceRows));
  }

  /**
   * Полный JSON-скелет документа по {@code var_path}.
   * GET /api/v1/{tenant}/admin/schema/{contractCode}/attributes/metadata
   */
  getAttributesMetadataJson(contractModel: string): Observable<unknown> {
    const url = `${this.schemaUrl(contractModel)}/attributes/metadata`;
    return this.http.get(url, { responseType: 'text' }).pipe(
      map((raw) => {
        try {
          return JSON.parse(raw) as unknown;
        } catch {
          return raw;
        }
      }),
    );
  }

  /**
   * Create attribute.
   * POST /api/v1/{tenant}/admin/schema/{contractCode}/attributes
   */
  createAttribute(contractModel: string, body: LobVar): Observable<LobVar> {
    const url = `${this.schemaUrl(contractModel)}/attributes`;
    return this.http.post<LobVar>(url, body);
  }

  /**
   * Update attribute.
   * PUT /api/v1/{tenant}/admin/schema/{contractCode}/attributes
   */
  updateAttribute(contractModel: string, body: LobVar): Observable<LobVar> {
    const url = `${this.schemaUrl(contractModel)}/attributes`;
    return this.http.put<LobVar>(url, body);
  }

  /**
   * Delete attribute (тело — идентификация узла, как на бэкенде).
   * DELETE /api/v1/{tenant}/admin/schema/{contractCode}/attributes
   */
  deleteAttribute(contractModel: string, body: LobVar): Observable<void> {
    const url = `${this.schemaUrl(contractModel)}/attributes`;
    return this.http.delete<void>(url, { body });
  }
}