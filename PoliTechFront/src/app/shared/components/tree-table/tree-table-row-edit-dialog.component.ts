import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import type { TreeTableSourceRow } from '../../models/tree-table.models';

/** Какие поля доступны для правки в диалоге. */
export type TreeTableRowEditFields = 'all' | 'valueList';

export interface TreeTableRowEditDialogData {
  mode: 'edit' | 'create';
  /** Редактируемая строка (копия) */
  row: TreeTableSourceRow;
  /** Для create: parent_id нового узла */
  parentId: number | null;
  /** Код нельзя менять (системная переменная) */
  codeReadonly?: boolean;
  /**
   * `valueList` — только varList и varValue (LOB / продукт);
   * `all` — полный набор (схема тенанта).
   */
  editableFields?: TreeTableRowEditFields;
}

const VAR_TYPES = ['IN', 'VAR', 'MAGIC', 'CONST', 'OBJECT'] as const;
const VAR_DATA_TYPES = ['STRING', 'NUMBER', 'OBJECT'] as const;
const DEFAULT_STATES = ['ENABLED', 'DISABLED'] as const;

@Component({
  selector: 'app-tree-table-row-edit-dialog',
  standalone: true,
  imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule],
  template: `
    <h2 mat-dialog-title>{{ data.mode === 'create' ? 'Новый узел' : 'Редактирование' }}</h2>
    <mat-dialog-content class="dialog-body">
      <mat-form-field appearance="outline" class="full">
        <mat-label>Код (varCode)</mat-label>
        <input
          matInput
          [(ngModel)]="model.varCode"
          name="varCode"
          [readonly]="metaReadonly || data.codeReadonly === true"
        />
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Название (varName)</mat-label>
        <input matInput [(ngModel)]="model.varName" name="varName" [readonly]="metaReadonly" />
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Тип (varType)</mat-label>
        <mat-select [(ngModel)]="model.varType" name="varType" [disabled]="metaReadonly">
          @for (t of varTypes; track t) {
            <mat-option [value]="t">{{ t }}</mat-option>
          }
        </mat-select>
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Тип данных (varDataType)</mat-label>
        <mat-select [(ngModel)]="model.varDataType" name="varDataType" [disabled]="metaReadonly">
          @for (t of varDataTypes; track t) {
            <mat-option [value]="t">{{ t }}</mat-option>
          }
        </mat-select>
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Список (varList)</mat-label>
        <input matInput [(ngModel)]="model.varList" name="varList" />
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Путь (varPath)</mat-label>
        <input matInput [(ngModel)]="model.varPath" name="varPath" [readonly]="metaReadonly" />
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Значение (varValue)</mat-label>
        <input matInput [(ngModel)]="model.varValue" name="varValue" />
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Порядок (var_ord)</mat-label>
        <input matInput type="number" [(ngModel)]="model.varNr" name="varNr" [readonly]="metaReadonly" />
      </mat-form-field>
      @if (!metaReadonly) {
        <mat-form-field appearance="outline" class="full">
          <mat-label>Статус (default_state)</mat-label>
          <mat-select [(ngModel)]="model.defaultState" name="defaultState">
            @for (s of defaultStates; track s) {
              <mat-option [value]="s">{{ s }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button mat-dialog-close type="button">Отмена</button>
      <button mat-flat-button color="primary" type="button" [disabled]="!isValid()" (click)="save()">
        Сохранить
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .dialog-body {
        display: flex;
        flex-direction: column;
        min-width: 360px;
        padding-top: 8px;
      }
      .full {
        width: 100%;
      }
    `,
  ],
})
export class TreeTableRowEditDialogComponent {
  model: TreeTableSourceRow;
  readonly varTypes = [...VAR_TYPES];
  readonly varDataTypes = [...VAR_DATA_TYPES];
  readonly defaultStates = [...DEFAULT_STATES];
  /** Метаданные схемы недоступны (режим LOB/продукт). */
  readonly metaReadonly: boolean;

  constructor(
    private dialogRef: MatDialogRef<TreeTableRowEditDialogComponent, TreeTableSourceRow>,
    @Inject(MAT_DIALOG_DATA) public data: TreeTableRowEditDialogData,
  ) {
    this.metaReadonly = (data.editableFields ?? 'all') === 'valueList';
    this.model = { ...data.row };
    const vt = (this.model.varType ?? '').trim().toUpperCase();
    this.model.varType = (VAR_TYPES as readonly string[]).includes(vt) ? vt : 'IN';
    const vdt = (this.model.varDataType ?? '').trim().toUpperCase();
    this.model.varDataType = (VAR_DATA_TYPES as readonly string[]).includes(vdt) ? vdt : 'STRING';
    const ds = (this.model.defaultState ?? 'ENABLED').trim().toUpperCase();
    this.model.defaultState = ds === 'DISABLED' ? 'DISABLED' : 'ENABLED';
  }

  isValid(): boolean {
    if (this.metaReadonly) {
      return true;
    }
    return !!(this.model.varCode?.trim() && this.model.varName?.trim());
  }

  save(): void {
    if (!this.isValid()) return;
    if (!this.metaReadonly) {
      this.model.varCode = this.model.varCode.trim();
      this.model.varName = this.model.varName.trim();
      this.model.name = this.model.name?.trim() || this.model.varName;
      const nr = Number(this.model.varNr);
      this.model.varNr = Number.isFinite(nr) ? nr : 0;
    }
    if (this.data.mode === 'create') {
      this.model.parent_id = this.data.parentId;
    }
    this.dialogRef.close(this.model);
  }
}
