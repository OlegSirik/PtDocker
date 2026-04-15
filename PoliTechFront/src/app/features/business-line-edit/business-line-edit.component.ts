import { Component, OnInit, Inject, inject, ViewChild } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import {
  BusinessLineEditService,
  BusinessLineEdit,
  BusinessLineVar,
  BusinessLineCover,
  BusinessLineFile,
  BusinessLineCoefficient,
  BusinessLineCoefficientColumn,
} from '../../shared';
import { CalculatorService } from '../../shared/services/calculator.service';
import { BusinessLineCoefficientDialogComponent } from './business-line-coefficient-dialog/business-line-coefficient-dialog.component';
import { BusinessLineColumnDialogComponent } from './business-line-column-dialog/business-line-column-dialog.component';
import { buildCoefficientDataSqlTemplate } from './coefficient-sql.util';
import { SqlDialogComponent } from '../calculator/sql-dialog/sql-dialog.component';
import {JsonPipe} from '@angular/common';
import { VarsService, type LobVar } from '../../shared/services/vars.service';
import {
  Observable,
  of,
  forkJoin,
  from,
} from 'rxjs';
import { switchMap, concatMap, last, map, tap, defaultIfEmpty } from 'rxjs/operators';
import {
  TreeTableComponent,
  type TreeTableChildCreatePayload,
  isObjectVarTypeRow,
} from '../../shared/components/tree-table';
import type { TreeTableSourceRow } from '../../shared/components/tree-table';
import { AuthService } from '../../shared/services/auth.service';

@Component({
    selector: 'app-business-line-edit',
    imports: [
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatTableModule,
    MatSelectModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTabsModule,
    MatPaginatorModule,
    MatSlideToggleModule,
    MatTooltipModule,
    TreeTableComponent,
  ],
    templateUrl: './business-line-edit.component.html',
    styleUrls: ['./business-line-edit.component.scss']
})
export class BusinessLineEditComponent implements OnInit {
  @ViewChild('schemaTreeTable') schemaTreeTable?: TreeTableComponent;

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private svc = inject(BusinessLineEditService);
  private snack = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private authService = inject(AuthService);
  private calculatorService = inject(CalculatorService);
  varService = inject(VarsService);

  businessLine: BusinessLineEdit = {
    id: -1,
    mpCode: '',
    mpName: '',
    mpVars: [],
    mpCovers: [],
    mpFiles: [],
    mpPhType: '',
    mpInsObjectType: '',
    mpCoefficients: [],
  };
  originalBusinessLine: BusinessLineEdit | null = null;
  isNewRecord = true;
  hasChanges = false;

  varDisplayedColumns: string[] = ['varCode', 'varType', 'varPath', 'varName', 'varDataType', 'varActions'];
  coverDisplayedColumns: string[] = ['coverCode', 'coverName', 'risks', 'coverActions'];
  fileDisplayedColumns: string[] = ['fileCode', 'fileName', 'fileActions'];
  policyHolderDisplayedColumns: string[] = ['category', 'field', 'varName', 'varCode', 'actions'];
  policyInsObjectDisplayedColumns: string[] = ['category', 'field', 'varName', 'varCode', 'actions'];
  textVarDisplayedColumns: string[] = ['varCode', 'varName', 'varValue', 'textVarActions'];

  /** Таб «Коэффициенты» — список коэффициентов ЛБ (как на странице калькулятора). */
  lobCoefficientsDisplayedColumns: string[] = [
    'varCode',
    'varName',
    'altVarCode',
    'altVarValue',
    'coeffActions',
  ];
  lobColumnsDisplayedColumns: string[] = [
    'nr',
    'conditionOperator',
    'varCode',
    'sortOrder',
    'varDataType',
    'columnActions',
  ];
  conditionOperatorOptions: string[] = [];
  sortOrderOptions: string[] = [];
  selectedCoefficientIndex = -1;
  selectedCoefficientVarCode: string | null = null;
  selectedLobColumnKey: string | null = null;

  exampleJsonText = '';

  /** Строки дерева схемы: только проекция узлов {@link #isSchemaTreeMpVar} из mpVars. */
  treeTableData: TreeTableSourceRow[] = [];
  treeTableDisplayedColumns: string[] = ['name', 'code', 'actions'];

  /** Показывать узлы схемы с isDeleted (режим восстановления). */
  showDeletedSchemaTree = false;

  readonly isObjectVarTypeRow = isObjectVarTypeRow;

  get paginatedVars(): BusinessLineVar[] {
    return this.businessLine.mpVars.filter(
      (v) =>
        !this.isSchemaTreeMpVar(v) &&
        (v.varType == 'MAGIC' || v.varType == 'VAR' || v.varType == 'CONST'),
    );
  }

  get paginatedCovers(): BusinessLineCover[] {
    return this.businessLine.mpCovers;
  }

  get policyHolderVars(): any[] {
    // Получаем категории страхователя в зависимости от типа
    
    //this.businessLine.mpVars = this.businessLine.mpVars.map(v => this.varService.enrichVar(v));

    const categories = this.varService.getPhCategories(this.businessLine.mpPhType) || [];
    let result: any[] = [];
    let prevCategory = '';
    
    for (const category of categories) {
      // Only keep variables whose varCdm fits category
      const matchedVars = this.businessLine.mpVars
        .filter(v => v.varCdm && v.varCdm.startsWith(`policyHolder.${category}.`))
        .sort((a, b) => (a.varNr ?? 0) - (b.varNr ?? 0))
        .map(v => {
          let cat = '';
          if (category !== prevCategory) {
            cat = category;
            prevCategory = category;
          }
          return {
            category: cat,
            field: v.varCdm.split('.').slice(2).join('.'),
            varName: v.varName,
            varCode: v.varCode,
            original: v
          };
        });
      result = result.concat(matchedVars);
    }
    return result;
  }

  get policyInsObjectVars(): any[] {
    //this.businessLine.mpVars = this.businessLine.mpVars.map(v => this.varService.enrichVar(v));

    const categories = this.varService.getIoCategories(this.businessLine.mpInsObjectType) || [];
    
    let result: any[] = [];
    let prevCategory = '';

    for (const category of categories) {

      const matchedVars = this.businessLine.mpVars
        .filter(v => v.varCdm && v.varCdm.startsWith(`insuredObject.${category}.`))
        .sort((a, b) => (a.varNr ?? 0) - (b.varNr ?? 0))
        .map(v => {
          let cat = '';
          if (category !== prevCategory) {
            cat = category;
            prevCategory = category;
          }
          
          return {
            category: cat,
            field: v.varCdm.split('.').slice(2).join('.'),
            varName: v.varName,
            varCode: v.varCode,
            original: v
          };
        });
      result = result.concat(matchedVars);
    }
    return result;
  }

  ngOnInit(): void {
    const code = this.route.snapshot.paramMap.get('id');

    this.varService.getAllVars();
    this.calculatorService.getConditionOperatorOptions().subscribe((o) => (this.conditionOperatorOptions = o));
    this.calculatorService.getSortOrderOptions().subscribe((o) => (this.sortOrderOptions = o));

    if (code) {
      this.isNewRecord = false;
      this.svc.getBusinessLineByCode(code).subscribe(doc => {
        if (!doc) { this.router.navigate(['/', this.authService.tenant, 'business-line']); return; }
        
        // Normalize mpFiles property names (filename -> fileName)
        /*
        const normalizedFiles = (doc.mpFiles || []).map((file: any) => ({
          fileCode: file.fileCode,
          fileName: file.fileName || file.filename || ''
        }));
        */

        this.businessLine = doc;
        this.normalizeMpCoefficients();
        this.selectedCoefficientIndex = -1;
        this.selectedCoefficientVarCode = null;
        this.selectedLobColumnKey = null;
        this.syncTreeTableFromMpVars();
        /*
        this.businessLine = {
          ...doc,
          mpPhType: doc.mpPhType || '',
          mpInsObjectType: doc.mpInsObjectType || '',
          mpFiles: normalizedFiles
        };
        */
        /*
        this.originalBusinessLine = {
          ...doc,
          mpPhType: doc.mpPhType || '',
          mpInsObjectType: doc.mpInsObjectType || '',
          mpFiles: normalizedFiles
        };
        */
        this.updateChanges();
      });
    } else {
      this.isNewRecord = true;
      this.businessLine.mpVars = [];
      this.businessLine.mpCoefficients = [];
      this.selectedCoefficientIndex = -1;
      this.selectedCoefficientVarCode = null;
      this.selectedLobColumnKey = null;
      this.updateChanges();
      this.loadSchemaTreeIntoMpVarsForNewRecord();
    }
  }

  updateChanges(): void {
    this.hasChanges = !this.originalBusinessLine || JSON.stringify(this.businessLine) !== JSON.stringify(this.originalBusinessLine);
  }

  /**
   * Узел дерева схемы договора в mpVars: {@code varType === 'OBJECT'} или потомок такого узла по {@code parent_id}.
   * {@link BusinessLineVar.varCdm} не подменяется — значение с бэкенда / справочника.
   */
  private isSchemaTreeMpVar(v: BusinessLineVar): boolean {
    if ((v.varType?.trim().toUpperCase() ?? '') === 'OBJECT') {
      return true;
    }
    const p = v.parent_id;
    if (p == null || p === undefined) {
      return false;
    }
    const parent = this.businessLine.mpVars.find((x) => x.id === p);
    return parent ? this.isSchemaTreeMpVar(parent) : false;
  }

  /** Тот же {@code document_id}, что в {@link #loadSchemaTreeIntoMpVarsForNewRecord}. */
  private readonly schemaDocumentId = 'INSURANCE_CONTRACT';

  /** Узлы схемы с локальными правками, которые нужно отправить в API перед сохранением ЛС. */
  private schemaVarsPendingApiSync(): BusinessLineVar[] {
    return this.businessLine.mpVars.filter(
      (v) =>
        this.isSchemaTreeMpVar(v) &&
        (v.unsavedSchemaRow === 'new' || v.unsavedSchemaRow === 'modified') &&
        !v.isDeleted,
    );
  }

  private schemaApiVarCdm(v: BusinessLineVar): string {
    return (v.varCdm ?? '').trim();
  }

  private businessLineVarToSchemaLobVarForApi(
    v: BusinessLineVar,
    idRemap: Map<number, number>,
    mode: 'create' | 'update',
  ): LobVar {
    const p = v.parent_id;
    const parent_id: number | null =
      p == null || p === undefined ? null : (idRemap.get(p) ?? p);

    return {
      varCode: v.varCode,
      varName: v.varName,
      varPath: v.varPath ?? '',
      varType: v.varType ?? 'STRING',
      varDataType: v.varDataType ?? 'STRING',
      varValue: v.varValue ?? '',
      varCdm: this.schemaApiVarCdm(v),
      varNr: String(v.varNr ?? 0),
      id: mode === 'update' ? v.id! : 0,
      parent_id,
      varList: v.varList ?? null,
      isSystem: !!v.isSystem,
      isDeleted: false,
      name: v.name ?? '',
    };
  }

  private mergeLobVarResponseIntoSchemaMpVar(target: BusinessLineVar, lob: LobVar): void {
    const rawNr = lob.varNr;
    const nr =
      rawNr != null && rawNr !== ''
        ? Number(String(rawNr).trim())
        : NaN;
    const vdt = lob.varDataType as unknown;
    const varDataType =
      typeof vdt === 'string' ? vdt : target.varDataType ?? 'STRING';

    target.id = lob.id;
    target.parent_id = lob.parent_id ?? undefined;
    target.varCode = lob.varCode;
    target.varName = lob.varName;
    target.varType = lob.varType ?? target.varType;
    target.varPath = lob.varPath ?? '';
    target.varDataType = varDataType;
    target.varValue = lob.varValue ?? '';
    target.varNr = Number.isFinite(nr) ? nr : target.varNr ?? 0;
    target.varList = lob.varList ?? undefined;
    target.isSystem = lob.isSystem;
    target.varCdm = lob.varCdm ?? '';
    target.unsavedSchemaRow = undefined;
    target.name = lob.name ?? '';
  }

  private remapSchemaParentIds(oldId: number, newId: number): void {
    for (const w of this.businessLine.mpVars) {
      if (this.isSchemaTreeMpVar(w) && w.parent_id === oldId) {
        w.parent_id = newId;
      }
    }
  }

  /**
   * Родители перед детьми: у дочернего {@code parent_id} либо уже в БД, либо уже обработан в этом списке.
   */
  private topologicalNewSchemaVars(vars: BusinessLineVar[]): BusinessLineVar[] {
    const newIds = new Set(vars.map((v) => v.id!).filter((id): id is number => id != null));
    const processedIds = new Set<number>();
    const order: BusinessLineVar[] = [];
    for (let step = 0; step < vars.length + 2; step++) {
      let progressed = false;
      for (const v of vars) {
        const vid = v.id;
        if (vid == null || processedIds.has(vid)) continue;
        const p = v.parent_id;
        const parentReady =
          p == null || p === undefined || !newIds.has(p) || processedIds.has(p);
        if (parentReady) {
          order.push(v);
          processedIds.add(vid);
          progressed = true;
        }
      }
      if (order.length === vars.length) {
        return order;
      }
      if (!progressed) {
        throw new Error('schema tree: cannot order new nodes (parent chain)');
      }
    }
    throw new Error('schema tree: topological sort failed');
  }

  /**
   * POST/PUT атрибутов схемы для помеченных mpVars, ответы мержим в {@link businessLine.mpVars}.
   */
  private syncSchemaAttributesBeforeSave(): Observable<void> {
    const pending = this.schemaVarsPendingApiSync();
    if (!pending.length) {
      return of(void 0);
    }

    const newOnes = pending.filter((v) => v.unsavedSchemaRow === 'new');
    const modifiedOnes = pending.filter((v) => v.unsavedSchemaRow === 'modified');
    const idRemap = new Map<number, number>();
    const code = this.schemaDocumentId;

    const afterCreates: Observable<void> =
      newOnes.length === 0
        ? of(void 0)
        : from(this.topologicalNewSchemaVars(newOnes)).pipe(
            concatMap((v) =>
              this.varService
                .createAttribute(code, this.businessLineVarToSchemaLobVarForApi(v, idRemap, 'create'))
                .pipe(
                  tap((lob) => {
                    const oldId = v.id!;
                    this.mergeLobVarResponseIntoSchemaMpVar(v, lob);
                    if (oldId !== lob.id) {
                      this.remapSchemaParentIds(oldId, lob.id);
                    }
                    idRemap.set(oldId, lob.id);
                  }),
                ),
            ),
            last(),
            map(() => void 0),
            defaultIfEmpty(void 0),
          );

    return afterCreates.pipe(
      switchMap(() =>
        modifiedOnes.length === 0
          ? of(void 0)
          : forkJoin(
              modifiedOnes.map((v) =>
                this.varService
                  .updateAttribute(
                    code,
                    this.businessLineVarToSchemaLobVarForApi(v, idRemap, 'update'),
                  )
                  .pipe(tap((lob) => this.mergeLobVarResponseIntoSchemaMpVar(v, lob))),
              ),
            ).pipe(map(() => void 0)),
      ),
      tap(() => this.syncTreeTableFromMpVars()),
    );
  }

  /** Новый документ: GET attributes → все строки кладём в mpVars как узлы схемы. */
  private loadSchemaTreeIntoMpVarsForNewRecord(): void {
    this.varService.loadAttributes(this.schemaDocumentId).subscribe({
      next: (data) => {
        this.businessLine.mpVars = (data ?? []).map((row) => this.treeSourceRowToMpVar(row));
        this.syncTreeTableFromMpVars();
        this.updateChanges();
      },
      error: () => {
        this.businessLine.mpVars = [];
        this.syncTreeTableFromMpVars();
        this.updateChanges();
      },
    });
  }

  private treeSourceRowToMpVar(row: TreeTableSourceRow): BusinessLineVar {
    return {
      id: row.id,
      parent_id: row.parent_id ?? undefined,
      varCode: row.varCode,
      varName: row.varName,
      varType: row.varType ?? 'STRING',
      varPath: row.varPath ?? '',
      varDataType: row.varDataType ?? 'STRING',
      varValue: row.varValue ?? '',
      varNr: row.varNr ?? 0,
      varCdm: row.varCdm ?? '',
      varList: row.varList,
      isSystem: row.isSystem,
      isDeleted: false,
      unsavedSchemaRow: row.unsavedSchemaRow,
      name: row.name ?? '',
    };
  }

  private mpVarToTreeSourceRow(v: BusinessLineVar): TreeTableSourceRow {
    return {
      id: v.id!,
      parent_id: v.parent_id ?? null,
      varNr: v.varNr ?? 0,
      varName: v.varName,
      varCode: v.varCode,
      varType: v.varType,
      varDataType: v.varDataType,
      isSystem: v.isSystem,
      isDeleted: !!v.isDeleted,
      varList: v.varList,
      varPath: v.varPath ?? '',
      varValue: v.varValue ?? '',
      varCdm: v.varCdm ?? '',
      unsavedSchemaRow: v.unsavedSchemaRow,
      name: v.name ?? '',
    };
  }

  /** После успешного сохранения ЛС — снять маркеры «не сохранено в документе». */
  private clearSchemaTreeUnsavedFlags(): void {
    for (const v of this.businessLine.mpVars) {
      if (this.isSchemaTreeMpVar(v)) {
        v.unsavedSchemaRow = undefined;
      }
    }
    this.syncTreeTableFromMpVars();
  }

  /** Таблица дерева строится только из mpVars (существующий документ или после loadAttributes). */
  private syncTreeTableFromMpVars(): void {
    this.treeTableData = this.businessLine.mpVars
      .filter((v) => this.isSchemaTreeMpVar(v) && (this.showDeletedSchemaTree || !v.isDeleted))
      .map((v) => this.mpVarToTreeSourceRow(v));
  }

  onShowDeletedSchemaChange(): void {
    this.syncTreeTableFromMpVars();
  }

  /** Восстановить узел и цепочку родителей в mpVars (isDeleted = false). */
  onSchemaTreeRestoreNode(node: TreeTableSourceRow): void {
    let id: number | null | undefined = node.id;
    while (id != null) {
      const v = this.businessLine.mpVars.find((x) => x.id === id);
      if (!v) break;
      if (this.isSchemaTreeMpVar(v)) {
        v.isDeleted = false;
        if (v.unsavedSchemaRow !== 'new') {
          v.unsavedSchemaRow = 'modified';
        }
      }
      id = v.parent_id ?? null;
    }
    this.syncTreeTableFromMpVars();
    this.updateChanges();
  }

  /** Синхронизация правок из tree-table / диалога обратно в mpVars. */
  onSchemaTreeRowUpdated(row: TreeTableSourceRow): void {
    const v = this.businessLine.mpVars.find((x) => x.id === row.id);
    if (!v || !this.isSchemaTreeMpVar(v)) return;
    v.varDataType = row.varDataType ?? v.varDataType;
    v.varList = row.varList;
    v.varName = row.varName;
    v.varCode = row.varCode;
    v.varType = row.varType ?? v.varType;
    v.varPath = row.varPath ?? v.varPath;
    v.varValue = row.varValue ?? v.varValue;
    v.name = row.name?.trim() || row.varName || v.name || '';
    if (v.unsavedSchemaRow !== 'new') {
      v.unsavedSchemaRow = 'modified';
    }
    this.syncTreeTableFromMpVars();
    this.updateChanges();
  }

  onSchemaTreeChildCreate(payload: TreeTableChildCreatePayload<TreeTableSourceRow>): void {
    const id = this.nextMpVarId();
    const d = payload.draft;
    const newVar: BusinessLineVar = {
      id,
      parent_id: payload.parentId,
      varCode: d.varCode.trim(),
      varName: d.varName.trim(),
      varType: d.varType ?? 'STRING',
      varPath: d.varPath ?? '',
      varDataType: d.varDataType ?? 'STRING',
      varValue: d.varValue ?? '',
      varNr: d.varNr ?? 0,
      varCdm: (d.varCdm ?? '').trim() || (d.varPath ?? '').trim(),
      varList: d.varList,
      isSystem: false,
      isDeleted: false,
      unsavedSchemaRow: 'new',
      name: d.name?.trim() || d.varName.trim() || '',
    };
    this.businessLine.mpVars = [...this.businessLine.mpVars, newVar];
    this.syncTreeTableFromMpVars();
    this.updateChanges();
  }

  private nextMpVarId(): number {
    const ids = this.businessLine.mpVars.map((v) => v.id).filter((x): x is number => x != null && Number.isFinite(x));
    return ids.length ? Math.max(...ids) + 1 : 1;
  }

  onTreeTableEdit(node: TreeTableSourceRow): void {
    this.schemaTreeTable?.openEditRowDialog(node);
  }

  onTreeTableDelete(node: TreeTableSourceRow): void {
    const markSchemaBranchDeleted = (parentId: number): void => {
      for (const x of this.businessLine.mpVars) {
        if (!this.isSchemaTreeMpVar(x) || x.parent_id !== parentId) continue;
        x.isDeleted = true;
        if (x.unsavedSchemaRow !== 'new') {
          x.unsavedSchemaRow = 'modified';
        }
        if (x.id != null) markSchemaBranchDeleted(x.id);
      }
    };
    const v = this.businessLine.mpVars.find((x) => x.id === node.id);
    if (v && this.isSchemaTreeMpVar(v)) {
      v.isDeleted = true;
      if (v.unsavedSchemaRow !== 'new') {
        v.unsavedSchemaRow = 'modified';
      }
    }
    markSchemaBranchDeleted(node.id);
    this.syncTreeTableFromMpVars();
    this.snack.open(`Скрыто из дерева: ${node.varName} (${node.varCode})`, 'OK', { duration: 3500 });
    this.updateChanges();
  }

  onTreeTableCreate(parent: TreeTableSourceRow): void {
    this.schemaTreeTable?.openCreateChildDialog(parent);
  }

  phTypeChanged(): void {
    
    // Remove all vars whose varPath starts with 'policyHolder.' from mpVars
    if (Array.isArray(this.businessLine.mpVars)) {
      this.businessLine.mpVars = this.businessLine.mpVars.filter(
        v => !(v.varCdm.startsWith('policyHolder'))
      );
    }    
    let newVars: any[] =[];
    newVars = this.varService.getPhDefVars(this.businessLine.mpPhType);

    this.businessLine.mpVars = [...this.businessLine.mpVars, ...newVars];
    this.updateChanges();
  }

  ioChanged(): void {

    // Remove all vars whose varPath starts with 'insuredObject.' from mpVars
    if (Array.isArray(this.businessLine.mpVars)) {
      this.businessLine.mpVars = this.businessLine.mpVars.filter(
        v => !(v.varCdm.startsWith('insuredObject'))
      );
    }
    let newVars: any[] = [];
    newVars = this.varService.getIoDefVars(this.businessLine.mpInsObjectType);

    this.businessLine.mpVars = [...this.businessLine.mpVars, ...newVars];
    this.updateChanges();
  }

  onFieldChange(): void {
    this.updateChanges();
  }

  save(): void {
    if (!this.businessLine.mpCode || !this.businessLine.mpName) {
      this.snack.open('Заполните mpCode и mpName', 'OK', { duration: 2500 });
      return;
    }

    this.syncSchemaAttributesBeforeSave()
      .pipe(
        switchMap(() =>
          this.isNewRecord
            ? this.svc.create(this.businessLine)
            : this.svc.update(this.businessLine.id, this.businessLine),
        ),
      )
      .subscribe({
        next: (saved) => {
          this.businessLine = saved;
          this.normalizeMpCoefficients();
          this.isNewRecord = false;
          this.clearSchemaTreeUnsavedFlags();
          this.updateChanges();
          this.snack.open('Сохранено', 'OK', { duration: 2000 });
        },
        error: (err: unknown) => {
          console.error(err);
          const msg =
            err && typeof err === 'object' && 'error' in err
              ? (err as { error?: { message?: string } }).error?.message
              : undefined;
          this.snack.open(msg ?? 'Ошибка синхронизации схемы или сохранения документа', 'OK', {
            duration: 4500,
          });
        },
      });
  }

  addVar(): void {
    this.openVarDialog({ varCode: '', varType: 'IN', varPath: '', varName: '', varDataType: 'STRING' }, (res) => {
      if (!res) return;
        const model: BusinessLineVar = { 
          varCode: res.varCode  , 
          varType: res.varType, 
          varPath: res.varPath, 
          varName: res.varName, 
          varDataType: res.varDataType, 
          varValue: '', 
          varNr: 0, 
          varCdm: res.varPath };
        this.businessLine.mpVars = [...this.businessLine.mpVars, model];
        this.updateChanges();
    });
  }



  deleteVar(v: BusinessLineVar): void {      
    this.businessLine.mpVars = this.businessLine.mpVars.filter(x => x.varCode !== v.varCode);
    this.updateChanges();
  }

  addCover(): void {
    this.openCoverDialog({ coverCode: '', coverName: '', risks: '' }, (res) => {
      if (!res) return;
        const model: BusinessLineCover = { coverCode: res.coverCode, coverName: res.coverName, risks: res.risks };
        this.businessLine.mpCovers = [...this.businessLine.mpCovers, model];
        this.updateChanges();

    });
  }

  editCover(c: BusinessLineCover): void {
    this.openCoverDialog({ ...c }, (res) => {
      if (!res) return;
      
        const index = this.businessLine.mpCovers.findIndex(x => x.coverCode === c.coverCode);
        if (index !== -1) {
          this.businessLine.mpCovers = [
            ...this.businessLine.mpCovers.slice(0, index),
            { ...c, ...res },
            ...this.businessLine.mpCovers.slice(index + 1)
          ];
        }
        this.updateChanges();
      
      });
  }

  deleteCover(c: BusinessLineCover): void {
    this.openConfirm('Удалить покрытие?', () => {
        this.businessLine.mpCovers = this.businessLine.mpCovers.filter(x => x.coverCode !== c.coverCode);
        this.updateChanges();
    });
  }

  // JSON File operations
  saveToFile(): void {
    const dataStr = JSON.stringify(this.businessLine, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `business-line_${this.businessLine.mpCode || 'new'}_${Date.now()}.json`;
    link.click();
    URL.revokeObjectURL(url);
    this.snack.open('JSON файл сохранен', 'Закрыть', { duration: 2000 });
  }

  goBack(): void {
    this.router.navigate(['/', this.authService.tenant, 'business-line']);
  }

  loadFromFile(): void {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          try {
            const jsonData = JSON.parse(e.target.result);
            jsonData.id = -1;
            this.businessLine = { ...this.businessLine, ...jsonData };
            this.updateChanges();
            this.syncTreeTableFromMpVars();
            this.snack.open('JSON файл загружен успешно', 'Закрыть', { duration: 2000 });
          } catch (error) {
            console.error('Error parsing JSON:', error);
            this.snack.open('Ошибка при загрузке JSON файла', 'Закрыть', { duration: 3000 });
          }
        };
        reader.readAsText(file);
      }
    };
    input.click();
  }

  openVarDialog(data: any, cb: (res?: any)=>void) {
    const ref = this.dialog.open(VarEditDialog, { data });
    ref.afterClosed().subscribe(cb);
  }

  openCoverDialog(data: any, cb: (res?: any)=>void) {
    const ref = this.dialog.open(CoverEditDialog, { data });
    ref.afterClosed().subscribe(cb);
  }

  openConfirm(message: string, onYes: ()=>void) {
    const ref = this.dialog.open(ConfirmDialog, { data: { message } });
    ref.afterClosed().subscribe((yes) => { if (yes) onYes(); });
  }

  onGetExample(): void {
    this.exampleJsonText = JSON.stringify("{ val1: val1, val2: val2, val3: val3 }", null, 2);
  }

  showPolicyHolderJson(): void {
    const lobCode = this.businessLine.mpCode;

    if (!lobCode) {
      this.snack.open('Please set mpCode first', 'Close', { duration: 2000 });
      return;
    }

    this.svc.getExampleJson(lobCode).subscribe(exampleData => {
      this.dialog.open(JsonViewDialog, {
        data: { title: 'PolicyHolder JSON', object: exampleData },
        width: '800px',
        maxHeight: '80vh'
      });
    });
  }

  addPolicyHolderVar(): void {
    const ref = this.dialog.open(AddPolicyHolderVarDialog, {
      width: '500px'
    });

    ref.afterClosed().subscribe(result => {
      if (result && result.varCode && result.varName) {
        // Calculate next varNr
        const maxVarNr = this.businessLine.mpVars.reduce((max, v) => {
          return Math.max(max, v.varNr ?? 0);
        }, 0);

        // Create new variable with policyHolder prefix
        const newVar: BusinessLineVar = {
          varCode: 'ph_' + result.varCode,
          varName: result.varName,
          varPath: `policyHolder.additionalProperties.${result.varCode}`,
          varType: 'IN',
          varDataType: 'STRING',
          varValue: '',
          varNr: maxVarNr + 1,
          varCdm: 'policyHolder.additionalProperties.' + result.varCode
        };

        this.businessLine.mpVars = [...this.businessLine.mpVars, newVar];
        this.updateChanges();
        this.snack.open('Variable added successfully', 'Close', { duration: 2000 });
      }
    });
  }

  addInsObjectVar(): void {
    const ref = this.dialog.open(AddInsObjectVarDialog, {
      width: '500px'
    });

    ref.afterClosed().subscribe(result => {
      if (result && result.varCode && result.varName) {
        // Calculate next varNr
        const maxVarNr = this.businessLine.mpVars.reduce((max, v) => {
          return Math.max(max, v.varNr ?? 0);
        }, 0);

        // Create new variable with insuredObject prefix
        const newVar: BusinessLineVar = {
          varCode: 'io_' + result.varCode,
          varName: result.varName,
          varPath: `insuredObject.additionalProperties.${result.varCode}`,
          varType: 'IN',
          varDataType: 'STRING',
          varValue: '',
          varNr: maxVarNr + 1,
          varCdm: ''
        };

        this.businessLine.mpVars = [...this.businessLine.mpVars, newVar];
        this.updateChanges();
        this.snack.open('Variable added successfully', 'Close', { duration: 2000 });
      }
    });
  }

  showInsObjectJson(): void {
    const lobCode = this.businessLine.mpCode;

    if (!lobCode) {
      this.snack.open('Please set mpCode first', 'Close', { duration: 2000 });
      return;
    }

    this.svc.getExampleJson(lobCode).subscribe(exampleData => {
      this.dialog.open(JsonViewDialog, {
        data: { title: 'Insured Object JSON', object: exampleData },
        width: '800px',
        maxHeight: '80vh'
      });
    });
  }

  get files(): BusinessLineFile[] {
    return this.businessLine.mpFiles || [];
  }

  get textVars(): BusinessLineVar[] {
    return (this.businessLine.mpVars || [])
      .filter(v => v.varType === 'TEXT')
      .sort((a, b) => (a.varCdm || '').localeCompare(b.varCdm || ''));
  }

  addFile(): void {
    this.openFileDialog({ fileCode: '', fileName: '' }, (res) => {
      if (!res) return;
      
      // Ensure fileCode is lowercase
      res.fileCode = res.fileCode.toLowerCase();
      
      // Validate fileCode uniqueness
      const existingFileCodes = this.files.map(f => f.fileCode);
      if (existingFileCodes.includes(res.fileCode)) {
        this.snack.open('fileCode должен быть уникальным', 'OK', { duration: 2500 });
        return;
      }
      
      const model: BusinessLineFile = { fileCode: res.fileCode, fileName: res.fileName };
      this.businessLine.mpFiles = [...(this.businessLine.mpFiles || []), model];
      this.updateChanges();
    });
  }

  editFile(f: BusinessLineFile): void {
    this.openFileDialog({ ...f, isEdit: true }, (res) => {
      if (!res) return;
      
      // Ensure fileCode is lowercase (though it shouldn't change in edit mode)
      res.fileCode = res.fileCode.toLowerCase();
      
      // Validate fileCode uniqueness (excluding current file)
      const existingFileCodes = this.files
        .filter(file => file.fileCode !== f.fileCode)
        .map(file => file.fileCode);
      if (existingFileCodes.includes(res.fileCode)) {
        this.snack.open('fileCode должен быть уникальным', 'OK', { duration: 2500 });
        return;
      }
      
      const index = this.files.findIndex(x => x.fileCode === f.fileCode);
      if (index !== -1) {
        this.businessLine.mpFiles = [
          ...this.files.slice(0, index),
          { ...f, fileName: res.fileName },
          ...this.files.slice(index + 1)
        ];
        this.updateChanges();
      }
    });
  }

  deleteFile(f: BusinessLineFile): void {
    this.openConfirm('Удалить файл?', () => {
      this.businessLine.mpFiles = this.files.filter(x => x.fileCode !== f.fileCode);
      this.updateChanges();
    });
  }

  openFileDialog(data: any, cb: (res?: any)=>void) {
    const ref = this.dialog.open(FileEditDialog, { data });
    ref.afterClosed().subscribe(cb);
  }

  addTextVar(): void {
    this.openTextVarDialog({ code: '', name: '', value: '' }, (res) => {
      if (!res) return;
      
      // Check unique varCode
      const existingCodes = this.businessLine.mpVars.map(v => v.varCode);
      if (existingCodes.includes(res.code)) {
        this.snack.open('varCode должен быть уникальным', 'OK', { duration: 2500 });
        return;
      }
      
      const newVar: BusinessLineVar = {
        varDataType: 'STRING',
        varCode: res.code,
        varName: res.name,
        varPath: '',
        varType: 'TEXT',
        varValue: res.value,
        varCdm: 'strings.' + res.code,
        varNr: (this.businessLine.mpVars.length || 0) + 1000
      };
      
      this.businessLine.mpVars = [...this.businessLine.mpVars, newVar];
      this.updateChanges();
    });
  }

  editTextVar(v: BusinessLineVar): void {
    this.openTextVarDialog({ 
      code: v.varCode, 
      name: v.varName, 
      value: v.varValue || '',
      isEdit: true,
      originalVarCode: v.varCode
    }, (res) => {
      if (!res) return;
      
      // Check unique varCode (excluding current)
      if (res.code !== res.originalVarCode) {
        const existingCodes = this.businessLine.mpVars
          .filter(v => v.varCode !== res.originalVarCode)
          .map(v => v.varCode);
        if (existingCodes.includes(res.code)) {
          this.snack.open('varCode должен быть уникальным', 'OK', { duration: 2500 });
          return;
        }
      }
      
      const index = this.businessLine.mpVars.findIndex(x => x.varCode === res.originalVarCode);
      if (index !== -1) {
        this.businessLine.mpVars = [
          ...this.businessLine.mpVars.slice(0, index),
          {
            ...v,
            varCode: res.code,
            varName: res.name,
            varValue: res.value,
            varCdm: 'strings.' + res.code
          },
          ...this.businessLine.mpVars.slice(index + 1)
        ];
        this.updateChanges();
      }
    });
  }

  deleteTextVar(v: BusinessLineVar): void {
    this.openConfirm('Удалить строку?', () => {
      this.businessLine.mpVars = this.businessLine.mpVars.filter(x => x.varCode !== v.varCode);
      this.updateChanges();
    });
  }

  openTextVarDialog(data: any, cb: (res?: any)=>void) {
    const ref = this.dialog.open(TextVarEditDialog, { data });
    ref.afterClosed().subscribe(cb);
  }

  /** Гарантирует {@link BusinessLineEdit.mpCoefficients} и вложенные {@link BusinessLineCoefficient.columns}. */
  private normalizeMpCoefficients(): void {
    if (!this.businessLine.mpCoefficients) {
      this.businessLine.mpCoefficients = [];
    }
    for (const c of this.businessLine.mpCoefficients) {
      if (!c.columns) {
        c.columns = [];
      }
    }
  }

  private ensureMpCoefficients(): void {
    this.normalizeMpCoefficients();
  }

  lobColumnKey(col: BusinessLineCoefficientColumn): string {
    return `${col.nr}|${col.varCode}|${col.conditionOperator}|${col.sortOrder}`;
  }

  get selectedCoefficientColumns(): BusinessLineCoefficientColumn[] {
    if (this.selectedCoefficientIndex < 0) {
      return [];
    }
    this.ensureMpCoefficients();
    const c = this.businessLine.mpCoefficients![this.selectedCoefficientIndex];
    return c?.columns ?? [];
  }

  addCoefficient(): void {
    this.ensureMpCoefficients();
    const ref = this.dialog.open(BusinessLineCoefficientDialogComponent, {
      width: '600px',
      data: {
        isNew: true,
        vars: this.businessLine.mpVars,
      },
    });
    ref.afterClosed().subscribe((result: BusinessLineCoefficient | undefined) => {
      if (!result) {
        return;
      }
      if (!result.columns) {
        result.columns = [];
      }
      this.ensureMpCoefficients();
      this.businessLine.mpCoefficients = [...this.businessLine.mpCoefficients!, result];
      this.updateChanges();
    });
  }

  editCoefficient(row: BusinessLineCoefficient, ev?: Event): void {
    ev?.stopPropagation();
    this.ensureMpCoefficients();
    const idx = this.businessLine.mpCoefficients!.findIndex((c) => c.varCode === row.varCode);
    if (idx < 0) {
      return;
    }
    const ref = this.dialog.open(BusinessLineCoefficientDialogComponent, {
      width: '600px',
      data: {
        isNew: false,
        coefficient: this.businessLine.mpCoefficients![idx],
        vars: this.businessLine.mpVars,
      },
    });
    ref.afterClosed().subscribe((result: BusinessLineCoefficient | undefined) => {
      if (!result) {
        return;
      }
      if (!result.columns) {
        result.columns = [];
      }
      this.businessLine.mpCoefficients![idx] = result;
      if (this.selectedCoefficientVarCode === row.varCode) {
        this.selectedCoefficientVarCode = result.varCode;
      }
      this.updateChanges();
    });
  }

  deleteCoefficient(row: BusinessLineCoefficient, ev?: Event): void {
    ev?.stopPropagation();
    this.ensureMpCoefficients();
    const idx = this.businessLine.mpCoefficients!.findIndex((c) => c.varCode === row.varCode);
    if (idx < 0 || !confirm('Удалить коэффициент?')) {
      return;
    }
    this.businessLine.mpCoefficients!.splice(idx, 1);
    if (this.selectedCoefficientIndex === idx) {
      this.selectedCoefficientIndex = -1;
      this.selectedCoefficientVarCode = null;
      this.selectedLobColumnKey = null;
    } else if (this.selectedCoefficientIndex > idx) {
      this.selectedCoefficientIndex--;
    }
    this.updateChanges();
  }

  onCoefficientRowClick(row: BusinessLineCoefficient): void {
    this.ensureMpCoefficients();
    const idx = this.businessLine.mpCoefficients!.findIndex((c) => c.varCode === row.varCode);
    if (idx < 0) {
      return;
    }
    this.selectedCoefficientIndex = idx;
    this.selectedCoefficientVarCode = row.varCode;
    this.selectedLobColumnKey = null;
  }

  isCoefficientRowSelected(row: BusinessLineCoefficient): boolean {
    return this.selectedCoefficientVarCode === row.varCode;
  }

  showLobCoefficientSql(): void {
    if (this.selectedCoefficientIndex < 0) {
      this.snack.open('Выберите коэффициент', 'Закрыть', { duration: 3000 });
      return;
    }
    this.ensureMpCoefficients();
    const coef = this.businessLine.mpCoefficients![this.selectedCoefficientIndex];
    const sql = buildCoefficientDataSqlTemplate(0, coef.varCode, coef.columns ?? []);
    if (sql == null) {
      this.snack.open(
        'Не удалось построить SQL: проверьте колонки (операторы и сортировку).',
        'Закрыть',
        { duration: 4500 },
      );
      return;
    }
    this.dialog.open(SqlDialogComponent, {
      width: '700px',
      minWidth: '500px',
      data: { sql },
    });
  }

  addColumn(): void {
    if (this.selectedCoefficientIndex < 0) {
      return;
    }
    this.ensureMpCoefficients();
    const coef = this.businessLine.mpCoefficients![this.selectedCoefficientIndex];
    const ref = this.dialog.open(BusinessLineColumnDialogComponent, {
      width: '900px',
      minWidth: '900px',
      data: {
        isNew: true,
        vars: this.businessLine.mpVars,
        conditionOperatorOptions: this.conditionOperatorOptions,
        sortOrderOptions: this.sortOrderOptions,
      },
    });
    ref.afterClosed().subscribe((result: BusinessLineCoefficientColumn | undefined) => {
      if (!result) {
        return;
      }
      const prev = coef.columns ?? [];
      coef.columns = [...prev, result];
      this.updateChanges();
    });
  }

  editColumn(column: BusinessLineCoefficientColumn, ev?: Event): void {
    ev?.stopPropagation();
    if (this.selectedCoefficientIndex < 0) {
      return;
    }
    this.ensureMpCoefficients();
    const coef = this.businessLine.mpCoefficients![this.selectedCoefficientIndex];
    const index = coef.columns.indexOf(column);
    if (index < 0) {
      return;
    }
    this.selectedLobColumnKey = this.lobColumnKey(column);
    const ref = this.dialog.open(BusinessLineColumnDialogComponent, {
      width: '900px',
      minWidth: '900px',
      data: {
        isNew: false,
        column: { ...column },
        vars: this.businessLine.mpVars,
        conditionOperatorOptions: this.conditionOperatorOptions,
        sortOrderOptions: this.sortOrderOptions,
      },
    });
    ref.afterClosed().subscribe((result: BusinessLineCoefficientColumn | undefined) => {
      if (!result) {
        return;
      }
      const cols = coef.columns ?? [];
      const target = cols[index];
      Object.assign(target, result);
      coef.columns = [...cols];
      this.selectedLobColumnKey = this.lobColumnKey(target);
      this.updateChanges();
    });
  }

  isLobColumnRowSelected(column: BusinessLineCoefficientColumn): boolean {
    return this.selectedLobColumnKey === this.lobColumnKey(column);
  }

  deleteColumn(column: BusinessLineCoefficientColumn, ev?: Event): void {
    ev?.stopPropagation();
    if (this.selectedCoefficientIndex < 0) {
      return;
    }
    const coef = this.businessLine.mpCoefficients![this.selectedCoefficientIndex];
    const cols = coef.columns ?? [];
    const index = cols.indexOf(column);
    if (index < 0 || !confirm('Удалить колонку?')) {
      return;
    }
    coef.columns = cols.filter((_, i) => i !== index);
    if (this.selectedLobColumnKey === this.lobColumnKey(column)) {
      this.selectedLobColumnKey = null;
    }
    this.updateChanges();
  }

}

@Component({
    selector: 'app-var-edit-dialog',
    imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
    template: `
  <h2 mat-dialog-title style="color: #495057; font-size: 18px; font-weight: 600;">Переменная</h2>
  <div mat-dialog-content style="padding-top: 20px;">
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>varCode</mat-label>
      <input matInput [(ngModel)]="model.varCode">
    </mat-form-field>
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>varType</mat-label>
      <mat-select [(ngModel)]="model.varType">
        <mat-option value="IN">IN</mat-option>
        <mat-option value="VAR">VAR</mat-option>
        <mat-option value="CONST">OUT</mat-option>
        <mat-option value="MAGIC">MAGIC</mat-option>
      </mat-select>
    </mat-form-field>
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>varPath</mat-label>
      <input matInput [(ngModel)]="model.varPath">
    </mat-form-field>
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>varName</mat-label>
      <input matInput [(ngModel)]="model.varName">
    </mat-form-field>
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>varDataType</mat-label>
      <mat-select [(ngModel)]="model.varDataType">
        <mat-option value="STRING">STRING</mat-option>
        <mat-option value="NUMBER">NUMBER</mat-option>
        <mat-option value="DATE">DATE</mat-option>
        <mat-option value="PERIOD">PERIOD</mat-option>
      </mat-select>
    </mat-form-field>
  </div>
  <div mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Отмена</button>
    <button mat-raised-button color="primary" [mat-dialog-close]="model">Сохранить</button>
  </div>
  `
})
export class VarEditDialog {
  model: any;
  constructor(@Inject(MAT_DIALOG_DATA) data: any) { this.model = { ...data }; }
}

@Component({
    selector: 'app-cover-edit-dialog',
    imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
    template: `
  <h2 mat-dialog-title style="color: #495057; font-size: 18px; font-weight: 600;">Покрытие</h2>
  <div mat-dialog-content style="padding-top: 20px;">
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>Код покрытия</mat-label>
      <input matInput [(ngModel)]="model.coverCode">
    </mat-form-field>
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>Название покрытия</mat-label>
      <input matInput [(ngModel)]="model.coverName">
    </mat-form-field>
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>Список рисков</mat-label>
      <input matInput [(ngModel)]="model.risks">
    </mat-form-field>
  </div>
  <div mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Отмена</button>
    <button mat-raised-button color="primary" [mat-dialog-close]="model">Сохранить</button>
  </div>
  `
})
export class CoverEditDialog {
  model: any;
  constructor(@Inject(MAT_DIALOG_DATA) data: any) { this.model = { ...data }; }
}

@Component({
    selector: 'app-confirm-dialog',
    imports: [MatDialogModule, MatButtonModule],
    template: `
  <h2 mat-dialog-title style="color: #495057; font-size: 18px; font-weight: 600;">Подтверждение</h2>
  <div mat-dialog-content style="padding-top: 20px;">{{data.message}}</div>
  <div mat-dialog-actions align="end">
    <button mat-button [mat-dialog-close]="false">Нет</button>
    <button mat-raised-button color="warn" [mat-dialog-close]="true">Да</button>
  </div>
  `
})
export class ConfirmDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {}
}

@Component({
    selector: 'app-json-view-dialog',
    imports: [MatDialogModule, MatButtonModule, MatIconModule, JsonPipe],
    template: `
    <h2 mat-dialog-title style="color: #495057; font-size: 18px; font-weight: 600;">{{ data.title }}</h2>
    <div mat-dialog-content style="padding-top: 20px; max-height: 60vh; overflow: auto;">
      <pre style="background-color: #f5f5f5; padding: 16px; border-radius: 4px; overflow-x: auto;">{{ data.object | json }}</pre>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button (click)="copyToClipboard()">
        <mat-icon>content_copy</mat-icon>
        Копировать
      </button>
      <button mat-raised-button color="primary" mat-dialog-close>Закрыть</button>
    </div>
  `,
    styles: [`
    pre {
      margin: 0;
      font-family: 'Courier New', Courier, monospace;
      font-size: 13px;
      line-height: 1.5;
      white-space: pre-wrap;
      word-wrap: break-word;
    }
  `]
})
export class JsonViewDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public data: { title: string; object: any }) {}

  copyToClipboard(): void {
    const jsonString = JSON.stringify(this.data.object, null, 2);
    navigator.clipboard.writeText(jsonString).then(() => {
      // Could show a snackbar notification here if needed
    });
  }
}

@Component({
    selector: 'app-add-policy-holder-var-dialog',
    imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
    template: `
    <h2 mat-dialog-title style="color: #495057; font-size: 18px; font-weight: 600;">Добавить переменную страхователя</h2>
    <div mat-dialog-content style="padding-top: 20px;">
      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
        <mat-label>varCode</mat-label>
        <input matInput [(ngModel)]="varCode" placeholder="e.g., person.firstName">
      </mat-form-field>

      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
        <mat-label>varName</mat-label>
        <input matInput [(ngModel)]="varName" placeholder="e.g., First Name">
      </mat-form-field>

      <p style="font-size: 12px; color: #666; margin: 0;">
        Variable path will be: <strong>policyHolder.additionalProperties.{{ varCode || '...' }}</strong>
      </p>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button
              color="primary"
              [mat-dialog-close]="{varCode: varCode, varName: varName}"
              [disabled]="!varCode || !varName">
        Сохранить
      </button>
    </div>
  `
})
export class AddPolicyHolderVarDialog {
  varCode = '';
  varName = '';
}

@Component({
    selector: 'app-add-ins-object-var-dialog',
    imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
    template: `
    <h2 mat-dialog-title style="color: #495057; font-size: 18px; font-weight: 600;">Добавить переменную объекта страхования</h2>
    <div mat-dialog-content style="padding-top: 20px;">
      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
        <mat-label>varCode</mat-label>
        <input matInput [(ngModel)]="varCode" placeholder="e.g., device.serialNumber">
      </mat-form-field>

      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
        <mat-label>varName</mat-label>
        <input matInput [(ngModel)]="varName" placeholder="e.g., Serial Number">
      </mat-form-field>

      <p style="font-size: 12px; color: #666; margin: 0;">
        Variable path will be: <strong>insuredObject.additionalProperties.{{ varCode || '...' }}</strong>
      </p>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button
              color="primary"
              [mat-dialog-close]="{varCode: varCode, varName: varName}"
              [disabled]="!varCode || !varName">
        Сохранить
      </button>
    </div>
  `
})
export class AddInsObjectVarDialog {
  varCode = '';
  varName = '';
}

@Component({
    selector: 'app-file-edit-dialog',
    imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
    template: `
  <h2 mat-dialog-title style="color: #495057; font-size: 18px; font-weight: 600;">{{ isEdit ? 'Редактировать файл' : 'Добавить файл' }}</h2>
  <div mat-dialog-content style="padding-top: 20px;">
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>Код типа файлв</mat-label>
      <input matInput [(ngModel)]="model.fileCode" [readonly]="isEdit" (ngModelChange)="onFileCodeChange($event)">
      @if (hasUpperCase) {
        <mat-hint style="color: #f44336;">fileCode должен содержать только строчные буквы</mat-hint>
      }
    </mat-form-field>
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>Название типа файла</mat-label>
      <input matInput [(ngModel)]="model.fileName">
    </mat-form-field>
  </div>
  <div mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Отмена</button>
    <button mat-raised-button color="primary" [mat-dialog-close]="model" [disabled]="!isValid()">Сохранить</button>
  </div>
  `
})
export class FileEditDialog {
  model: any;
  isEdit: boolean = false;
  hasUpperCase: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) data: any) { 
    this.isEdit = data.isEdit || false;
    this.model = { ...data };
    delete this.model.isEdit;
    // Convert existing fileCode to lowercase if present
    if (this.model.fileCode) {
      this.model.fileCode = this.model.fileCode.toLowerCase();
    }
  }

  onFileCodeChange(value: string): void {
    if (value) {
      // Convert to lowercase
      const lowerValue = value.toLowerCase();
      this.model.fileCode = lowerValue;
      // Check if original had uppercase
      this.hasUpperCase = value !== lowerValue;
    } else {
      this.hasUpperCase = false;
    }
  }

  isValid(): boolean {
    if (!this.model.fileCode || !this.model.fileName) {
      return false;
    }
    // Check if fileCode contains only lowercase letters, numbers, and underscores
    const lowerCasePattern = /^[a-z0-9_]+$/;
    return lowerCasePattern.test(this.model.fileCode);
  }
}

@Component({
    selector: 'app-text-var-edit-dialog',
    imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
    template: `
  <h2 mat-dialog-title style="color: #495057; font-size: 18px; font-weight: 600;">{{ isEdit ? 'Редактировать строковый шаблон' : 'Добавить строковый шаблон' }}</h2>
  <div mat-dialog-content style="padding-top: 20px;">
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>Код шаблона</mat-label>
      <input matInput [(ngModel)]="model.code" [readonly]="isEdit">
    </mat-form-field>
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>Наименование шаблона</mat-label>
      <input matInput [(ngModel)]="model.name">
    </mat-form-field>
    <mat-form-field appearance="outline" style="width: 100%;">
      <mat-label>Шаблон текста</mat-label>
      <textarea matInput [(ngModel)]="model.value" rows="4"></textarea>
    </mat-form-field>
  </div>
  <div mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Отмена</button>
    <button mat-raised-button color="primary" [mat-dialog-close]="model" [disabled]="!isValid()">Сохранить</button>
  </div>
  `
})
export class TextVarEditDialog {
  model: any;
  isEdit: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) data: any) { 
    this.isEdit = data.isEdit || false;
    this.model = { ...data };
  }

  isValid(): boolean {
    return !!(this.model.code && this.model.name);
  }
}
