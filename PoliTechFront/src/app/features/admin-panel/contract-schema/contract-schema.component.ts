import { Component, Inject, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule, JsonPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogModule,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { concatMap, from, of } from 'rxjs';
import { catchError, map, toArray } from 'rxjs/operators';
import {
  TreeTableComponent,
  type TreeTableChildCreatePayload,
} from '../../../shared/components/tree-table';
import { isObjectVarTypeRow, type TreeTableSourceRow } from '../../../shared/models/tree-table.models';
import {
  VarsService,
  type ContractModel,
  type LobVar,
} from '../../../shared/services/vars.service';
import { AuthService } from '../../../shared/services/auth.service';
import { TOOLBAR_ROLE } from '../../../shared/components/toolbar/toolbar.component';
import {
  LlmService,
  type LlmAssistResponse,
  type LlmSchemaAttributeDraft,
  type LlmSchemaDraft,
} from '../../../shared/services/api/llm.service';
import { TenantLlmConfigService } from '../../../shared/services/api/tenant-llm-config.service';
import { resolveLlmErrorMessage } from '../../../shared/utils/llm-error.util';

@Component({
  selector: 'app-contract-schema',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatSnackBarModule,
    MatTooltipModule,
    TreeTableComponent,
  ],
  templateUrl: './contract-schema.component.html',
  styleUrl: './contract-schema.component.scss',
})
export class ContractSchemaComponent implements OnInit {
  private readonly varsService = inject(VarsService);
  private readonly authService = inject(AuthService);
  private readonly snack = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  private readonly llmService = inject(LlmService);
  private readonly tenantLlmConfigService = inject(TenantLlmConfigService);

  @ViewChild('schemaTreeTable') schemaTreeTable?: TreeTableComponent;

  readonly treeTableDisplayedColumns = ['name', 'code', 'actions'];

  models: ContractModel[] = [];
  selectedModelCode: string | null = null;
  treeTableData: TreeTableSourceRow[] = [];
  showDeletedSchemaTree = false;
  loadingModels = false;
  loadingTree = false;
  generatingExample = false;

  llmReady = true;
  llmText = '';
  askingLlm = false;
  applyingLlm = false;
  llmDraft: LlmSchemaAttributeDraft[] = [];
  llmWarnings: string[] = [];

  ngOnInit(): void {
    if (!this.authService.hasRole(TOOLBAR_ROLE.TNT_ADMIN)) {
      this.router.navigate(['/', this.authService.tenant, 'forbidden']);
      return;
    }
    this.loadLlmStatus();
    this.loadModels();
  }

  private loadLlmStatus(): void {
    this.tenantLlmConfigService.getConfig().subscribe({
      next: (view) => {
        this.llmReady = view.configured && view.enabled;
      },
      error: () => {
        this.llmReady = false;
      },
    });
  }

  loadModels(): void {
    this.loadingModels = true;
    this.varsService.listContractModels().subscribe({
      next: (list) => {
        this.models = list ?? [];
        this.loadingModels = false;
        if (this.models.length > 0) {
          this.selectedModelCode = this.models[0].code;
          this.loadTree();
        } else {
          this.selectedModelCode = null;
          this.treeTableData = [];
        }
      },
      error: (err) => {
        this.loadingModels = false;
        this.snack.open(err?.error?.message ?? 'Не удалось загрузить модели договора', 'OK', {
          duration: 4000,
        });
      },
    });
  }

  onModelChange(): void {
    this.clearLlmDraft();
    this.loadTree();
  }

  askLlm(): void {
    const text = (this.llmText || '').trim();
    if (!text || !this.selectedModelCode || this.askingLlm) {
      return;
    }
    this.askingLlm = true;
    this.llmDraft = [];
    this.llmWarnings = [];
    this.llmService.assistSchema(this.selectedModelCode, text).subscribe({
      next: (response) => this.onLlmAssistResponse(response),
      error: (err) => {
        this.askingLlm = false;
        this.snack.open(resolveLlmErrorMessage(err), 'OK', { duration: 5000 });
      },
    });
  }

  clearLlmDraft(): void {
    this.llmDraft = [];
    this.llmWarnings = [];
  }

  applyLlmDraft(): void {
    if (!this.selectedModelCode || this.llmDraft.length === 0 || this.applyingLlm) {
      return;
    }
    const contractCode = this.selectedModelCode;
    const ordered = this.orderSchemaDraft(this.llmDraft);
    const idByCode = new Map<string, number>();
    for (const row of this.treeTableData) {
      if (row.varCode) {
        idByCode.set(row.varCode, row.id);
      }
    }

    this.applyingLlm = true;
    from(ordered)
      .pipe(
        concatMap((attr) => {
          const body = this.draftToLobVar(attr, idByCode);
          return this.varsService.createAttribute(contractCode, body).pipe(
            map((saved) => {
              if (saved?.varCode && saved.id != null) {
                idByCode.set(saved.varCode, saved.id);
              }
              return { ok: true as const, code: attr.varCode };
            }),
            catchError((err) =>
              of({
                ok: false as const,
                code: attr.varCode,
                message: err?.error?.message ?? 'Ошибка создания',
              }),
            ),
          );
        }),
        toArray(),
      )
      .subscribe({
        next: (results) => {
          this.applyingLlm = false;
          const failed = results.filter((r) => !r.ok);
          const okCount = results.length - failed.length;
          if (failed.length === 0) {
            this.snack.open(`Добавлено атрибутов: ${okCount}`, 'OK', { duration: 3500 });
            this.clearLlmDraft();
            this.llmText = '';
          } else {
            const codes = failed.map((f) => f.code).join(', ');
            this.snack.open(
              `Применено ${okCount}, ошибки: ${codes}`,
              'OK',
              { duration: 6000 },
            );
          }
          this.loadTree();
        },
        error: () => {
          this.applyingLlm = false;
          this.snack.open('Ошибка применения черновика', 'OK', { duration: 4000 });
          this.loadTree();
        },
      });
  }

  private onLlmAssistResponse(response: LlmAssistResponse): void {
    this.askingLlm = false;
    if (!response.success) {
      const msg =
        response.errors?.filter(Boolean).join('; ') || 'LLM не вернул черновик атрибутов';
      this.snack.open(msg, 'OK', { duration: 5000 });
      return;
    }
    const draft = response.result as LlmSchemaDraft | undefined;
    const attrs = draft?.attributes ?? [];
    if (!attrs.length) {
      this.snack.open('Пустой черновик атрибутов', 'OK', { duration: 3500 });
      return;
    }
    this.llmDraft = attrs;
    this.llmWarnings = response.warnings ?? [];
    if (this.llmWarnings.length) {
      this.snack.open(this.llmWarnings.join('; '), 'OK', { duration: 5000 });
    }
  }

  /** OBJECT parents first, then leaves; stable among siblings. */
  private orderSchemaDraft(attrs: LlmSchemaAttributeDraft[]): LlmSchemaAttributeDraft[] {
    return [...attrs].sort((a, b) => {
      const ao = (a.varType || '').toUpperCase() === 'OBJECT' ? 0 : 1;
      const bo = (b.varType || '').toUpperCase() === 'OBJECT' ? 0 : 1;
      return ao - bo;
    });
  }

  private draftToLobVar(
    attr: LlmSchemaAttributeDraft,
    idByCode: Map<string, number>,
  ): LobVar {
    let parentId: number | null =
      attr.parent_id != null && attr.parent_id > 0 ? attr.parent_id : null;
    const parentCode = (attr.parentVarCode || '').trim();
    if (parentCode && idByCode.has(parentCode)) {
      parentId = idByCode.get(parentCode)!;
    }
    const varType = (attr.varType || 'IN').trim() || 'IN';
    const varDataType =
      (attr.varDataType || '').trim() ||
      (varType.toUpperCase() === 'OBJECT' ? 'OBJECT' : 'STRING');
    return {
      varCode: (attr.varCode || '').trim(),
      varName: (attr.varName || '').trim(),
      varPath: (attr.varPath || '').trim() || (attr.varCode || '').trim(),
      varType,
      varDataType,
      varValue: attr.varValue ?? '',
      varNr: '0',
      id: 0,
      parent_id: parentId,
      varList: attr.varList ?? null,
      isSystem: false,
      isDeleted: false,
      name: (attr.varName || '').trim(),
    };
  }

  loadTree(): void {
    if (!this.selectedModelCode) {
      this.treeTableData = [];
      return;
    }
    this.loadingTree = true;
    this.varsService.loadAttributes(this.selectedModelCode).subscribe({
      next: (rows) => {
        this.treeTableData = this.excludeTextVarTypeRows(rows ?? []);
        this.loadingTree = false;
      },
      error: (err) => {
        this.loadingTree = false;
        this.treeTableData = [];
        this.snack.open(err?.error?.message ?? 'Не удалось загрузить схему', 'OK', {
          duration: 4000,
        });
      },
    });
  }

  /**
   * Не показывать узлы с {@code varType = TEXT} (шаблоны digests/ПФ).
   * OBJECT без оставшихся потомков тоже убираются.
   */
  private excludeTextVarTypeRows(rows: TreeTableSourceRow[]): TreeTableSourceRow[] {
    const withoutText = rows.filter(
      (r) => (r.varType?.trim().toUpperCase() ?? '') !== 'TEXT',
    );
    const byId = new Map(withoutText.map((r) => [r.id, r]));
    const childrenByParent = new Map<number, TreeTableSourceRow[]>();
    for (const r of withoutText) {
      if (r.parent_id == null) continue;
      let list = childrenByParent.get(r.parent_id);
      if (!list) {
        list = [];
        childrenByParent.set(r.parent_id, list);
      }
      list.push(r);
    }

    const keep = new Set<number>();
    const visit = (id: number): boolean => {
      const row = byId.get(id);
      if (!row) return false;
      const kids = childrenByParent.get(id) ?? [];
      if (!isObjectVarTypeRow(row)) {
        keep.add(id);
        for (const c of kids) visit(c.id);
        return true;
      }
      let any = false;
      for (const c of kids) {
        if (visit(c.id)) any = true;
      }
      if (any) keep.add(id);
      return any;
    };

    for (const r of withoutText) {
      if (r.parent_id == null || !byId.has(r.parent_id)) {
        visit(r.id);
      }
    }
    return withoutText.filter((r) => keep.has(r.id));
  }

  onShowDeletedSchemaChange(): void {
    // TreeTable reacts to [showDeleted] binding.
  }

  /** Полный JSON документа по схеме ({@code GET …/attributes/metadata}). */
  generateExampleJson(): void {
    if (!this.selectedModelCode || this.generatingExample) {
      return;
    }
    const code = this.selectedModelCode;
    this.generatingExample = true;
    this.varsService.getAttributesMetadataJson(code).subscribe({
      next: (object) => {
        this.generatingExample = false;
        this.dialog.open(ContractSchemaJsonDialog, {
          width: '800px',
          maxHeight: '80vh',
          data: {
            title: `Пример JSON — ${code}`,
            object,
          },
        });
      },
      error: (err) => {
        this.generatingExample = false;
        this.snack.open(err?.error?.message ?? 'Не удалось сгенерировать пример JSON', 'OK', {
          duration: 4000,
        });
      },
    });
  }

  onTreeTableEdit(node: TreeTableSourceRow): void {
    this.schemaTreeTable?.openEditRowDialog(node);
  }

  onTreeTableCreate(parent: TreeTableSourceRow): void {
    this.schemaTreeTable?.openCreateChildDialog(parent);
  }

  onSchemaTreeRowUpdated(row: TreeTableSourceRow): void {
    if (!this.selectedModelCode) return;
    const body = this.toLobVar(row);
    this.varsService.updateAttribute(this.selectedModelCode, body).subscribe({
      next: (saved) => {
        this.applyLobVarToRow(row, saved);
        this.treeTableData = [...this.treeTableData];
        this.snack.open('Атрибут сохранён', 'OK', { duration: 2500 });
      },
      error: (err) => {
        this.snack.open(err?.error?.message ?? 'Ошибка сохранения атрибута', 'OK', {
          duration: 4000,
        });
        this.loadTree();
      },
    });
  }

  onSchemaTreeChildCreate(payload: TreeTableChildCreatePayload<TreeTableSourceRow>): void {
    if (!this.selectedModelCode) return;
    const d = payload.draft;
    const body: LobVar = {
      varCode: d.varCode.trim(),
      varName: d.varName.trim(),
      varPath: (d.varPath ?? '').trim() || d.varCode.trim(),
      varType: d.varType ?? 'IN',
      varDataType: d.varDataType ?? 'STRING',
      varValue: d.varValue ?? '',
      varNr: String(d.varNr ?? 0),
      id: 0,
      parent_id: payload.parentId,
      varList: d.varList ?? null,
      isSystem: false,
      isDeleted: false,
      name: d.name?.trim() || d.varName.trim() || '',
    };
    this.varsService.createAttribute(this.selectedModelCode, body).subscribe({
      next: () => {
        this.snack.open('Атрибут добавлен', 'OK', { duration: 2500 });
        this.loadTree();
      },
      error: (err) => {
        this.snack.open(err?.error?.message ?? 'Ошибка создания атрибута', 'OK', {
          duration: 4000,
        });
      },
    });
  }

  onTreeTableDelete(node: TreeTableSourceRow): void {
    if (!this.selectedModelCode) return;
    if (node.isSystem) {
      this.snack.open('Системный атрибут нельзя удалить', 'OK', { duration: 3000 });
      this.loadTree();
      return;
    }
    const body = this.toLobVar(node);
    this.varsService.deleteAttribute(this.selectedModelCode, body).subscribe({
      next: () => {
        this.snack.open(`Удалено: ${node.varName} (${node.varCode})`, 'OK', { duration: 3500 });
        this.loadTree();
      },
      error: (err) => {
        this.snack.open(err?.error?.message ?? 'Ошибка удаления', 'OK', { duration: 4000 });
        this.loadTree();
      },
    });
  }

  private toLobVar(row: TreeTableSourceRow): LobVar {
    return {
      varCode: row.varCode,
      varName: row.varName,
      varPath: row.varPath ?? '',
      varType: row.varType ?? 'IN',
      varDataType: row.varDataType ?? 'STRING',
      varValue: row.varValue ?? '',
      varNr: String(row.varNr ?? 0),
      id: row.id,
      parent_id: row.parent_id,
      varList: row.varList ?? null,
      isSystem: !!row.isSystem,
      isDeleted: !!row.isDeleted,
      name: row.name ?? row.varName ?? '',
    };
  }

  private applyLobVarToRow(target: TreeTableSourceRow, lob: LobVar): void {
    target.id = lob.id;
    target.parent_id = lob.parent_id;
    target.varCode = lob.varCode;
    target.varName = lob.varName;
    target.varType = lob.varType;
    target.varPath = lob.varPath ?? '';
    target.varDataType =
      typeof lob.varDataType === 'string' ? lob.varDataType : target.varDataType ?? 'STRING';
    target.varValue = lob.varValue ?? '';
    const nr = Number(String(lob.varNr ?? '').trim());
    target.varNr = Number.isFinite(nr) ? nr : target.varNr ?? 0;
    target.varList = lob.varList ?? undefined;
    target.isSystem = lob.isSystem;
    target.name = lob.name ?? lob.varName ?? '';
  }
}

@Component({
  selector: 'app-contract-schema-json-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule, JsonPipe],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <div mat-dialog-content class="json-dialog-content">
      <pre>{{ data.object | json }}</pre>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button type="button" (click)="copyToClipboard()">
        <mat-icon>content_copy</mat-icon>
        Копировать
      </button>
      <button mat-flat-button color="primary" mat-dialog-close type="button">Закрыть</button>
    </div>
  `,
  styles: [
    `
      .json-dialog-content {
        max-height: 60vh;
        overflow: auto;
      }
      pre {
        margin: 0;
        padding: 16px;
        background: #f5f5f5;
        border-radius: 4px;
        font-family: 'Courier New', Courier, monospace;
        font-size: 13px;
        line-height: 1.5;
        white-space: pre-wrap;
        word-wrap: break-word;
      }
    `,
  ],
})
export class ContractSchemaJsonDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public data: { title: string; object: unknown }) {}

  copyToClipboard(): void {
    const jsonString = JSON.stringify(this.data.object, null, 2);
    void navigator.clipboard.writeText(jsonString);
  }
}
