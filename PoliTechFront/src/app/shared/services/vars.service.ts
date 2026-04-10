import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, BehaviorSubject } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
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
  varCdm: string;
  /** С API может прийти числом или строкой (Jackson / LobVar). */
  varNr?: string | number;
  id: number;
  parent_id: number | null;
  varList: string | null;
  isSystem: boolean;
  isDeleted: boolean;
  name: string;
}

@Injectable({
    providedIn: 'root'
  })

export class VarsService {
  private allVars: LobVar[] = [];
  private varsLoaded$ = new BehaviorSubject<boolean>(false);

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {
    // Load vars from API on startup
    this.loadVarsFromApi();
  }

  /**
   * Load PvVars from API
   * GET /api/v1/{tenantCode}/admin/products/vars
   */
  private loadVarsFromApi(): void {
    const url = `${this.authService.baseApiUrl}/admin/products/vars`;
    this.http.get<LobVar[]>(url).pipe(
      catchError(error => {
        console.error('Error loading vars from API:', error);
        return of([]);
      })
    ).subscribe(vars => {
      this.allVars = vars || [];
      this.varsLoaded$.next(true);
    });
  }

  /**
   * Get all vars from API (Observable)
   */
  getAPIVars(): Observable<LobVar[]> {
    const url = `${this.authService.baseApiUrl}/admin/products/vars`;
    return this.http.get<LobVar[]>(url);
  }

  /**
   * Get all cached vars (sync)
   */
  getAllVars(): LobVar[] {
    return this.allVars;
  }

  /**
   * Check if vars are loaded
   */
  isVarsLoaded(): Observable<boolean> {
    return this.varsLoaded$.asObservable();
  }

  getPhCategories(type: string): string[] | any[] {
    if (type === 'person') {
      return [
        "person",
        "contacts",
        "organization",
        "identifiers",
        "addresses",
        "additionalProperties"
      ];
    }
    if (type === 'organization') {
      return [
        "organization",
        "identifiers",
        "addresses",
        "additionalProperties"
      ];
    }
    return [];
  }

  getPhDefVars(type: string): LobVar[] {
    if (type === 'person') {
      return this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.person')).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.contacts'))).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.organization'))).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.identifiers'))).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.addresses'))).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.additionalProperties')));
    }
    if (type === 'organization') {
      return this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.organization')).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.identifiers'))).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.contacts'))).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.addresses')));
    }
    return [];
  }

  getIoDefVars(type: string): LobVar[] | any[]{
    if (type === 'person') {

        return [
        ...this.getIoVars(),
        ...this.getIoPersonVars(),
        ...this.getIoOrganizationVars(),
        ...this.getIoContactsVars(),
        ...this.getIoPersonIdentifiersVars(),
        ...this.getIoAddressVars(),
        ...this.getIoRiskFactorsVars()
      ];

    }
    if (type === 'device') {
        return [
          ...this.getIoVars(),
          ...this.getIoDeviceVars()
        ];
    }
  
    if (type === 'property') {
        return [
          ...this.getIoVars()
                ];
    }
    if (type === 'avia-ns') {
      return [
        ...this.getIoVars(),
        ...this.getIoPersonVars(),
        ...this.getIoContactsVars(),
        ...this.getIoTravelSegmentsVars()
      ];
  }
  return [];
  }

  getIoCategories(type: string): string[] | any[] {
    if (type === 'person') {
      return [
        "person",
        "contacts",
        "organization",
        "identifiers",
        "addresses",
        "additionalProperties",
        "riskFactors"
      ];
    }
    if (type === 'device') {
      return [
        "device",
        "additionalProperties",
        "riskFactors"
      ];
    }
    if (type === 'property') {
      return [
        "property",
        "additionalProperties",
        "riskFactors"
      ];
    }
    if (type === 'avia-ns') {
      return [
        "person",
        "contacts",
        "travelSegments",
        "additionalProperties",
        "riskFactors"
      ];
    }
    return [];
  }

  getIoPersonVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.person'));
  }
  getIoOrganizationVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.organization'));
  }
  getIoContactsVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.contacts'));
  }
  getIoPersonIdentifiersVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.identifiers'));
  }
  getIoAddressVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.addresses'));
  }
  getIoTravelSegmentsVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.travelSegments'));
  }

  getIoVars(): LobVar[] {
    return this.allVars.filter(v => 
      v.varCdm?.startsWith('insuredObject') && 
      (v.varCdm?.split('.').length - 1) === 1
    );
  }
  getIoDeviceVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.device'));
  }
  getIoPropertyVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.property'));
  }  
  getIoRiskFactorsVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.riskFactors'));
  }

  // ---------- Schema API (contract model = 'box') ----------

  private schemaUrl(contractModel: string): string {
    return `${this.authService.baseApiUrl}/admin/schema/${contractModel}`;
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