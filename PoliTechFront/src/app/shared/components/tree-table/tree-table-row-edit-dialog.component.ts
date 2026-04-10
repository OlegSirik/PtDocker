import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import type { TreeTableSourceRow } from '../../models/tree-table.models';

export interface TreeTableRowEditDialogData {
  mode: 'edit' | 'create';
  /** Редактируемая строка (копия) */
  row: TreeTableSourceRow;
  /** Для create: parent_id нового узла */
  parentId: number | null;
  /** Код нельзя менять (системная переменная) */
  codeReadonly?: boolean;
}

const VAR_DATA_TYPES = ['STRING', 'NUMBER', 'DATE', 'TIME', 'OBJECT'] as const;

@Component({
  selector: 'app-tree-table-row-edit-dialog',
  standalone: true,
  imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule],
  template: `
    <h2 mat-dialog-title>{{ data.mode === 'create' ? 'Новый узел' : 'Редактирование' }}</h2>
    <mat-dialog-content class="dialog-body">
      <mat-form-field appearance="outline" class="full">
        <mat-label>Код (varCode)</mat-label>
        <input matInput [(ngModel)]="model.varCode" name="varCode" [readonly]="data.codeReadonly === true" />
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Название (varName)</mat-label>
        <input matInput [(ngModel)]="model.varName" name="varName" />
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Тип (varType)</mat-label>
        <input matInput [(ngModel)]="model.varType" name="varType" />
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Тип данных (varDataType)</mat-label>
        <mat-select [(ngModel)]="model.varDataType" name="varDataType">
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
        <input matInput [(ngModel)]="model.varPath" name="varPath" />
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Значение (varValue)</mat-label>
        <input matInput [(ngModel)]="model.varValue" name="varValue" />
      </mat-form-field>
      @if (data.mode === 'create') {
        <mat-form-field appearance="outline" class="full">
          <mat-label>Порядок (varNr)</mat-label>
          <input matInput type="number" [(ngModel)]="model.varNr" name="varNr" />
        </mat-form-field>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close type="button">Отмена</button>
      <button mat-raised-button color="primary" type="button" [disabled]="!isValid()" (click)="save()">
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
  readonly varDataTypes = [...VAR_DATA_TYPES];

  constructor(
    private dialogRef: MatDialogRef<TreeTableRowEditDialogComponent, TreeTableSourceRow>,
    @Inject(MAT_DIALOG_DATA) public data: TreeTableRowEditDialogData,
  ) {
    this.model = { ...data.row };
    this.model.varDataType = this.model.varDataType ?? 'STRING';
    this.model.varType = this.model.varType ?? 'STRING';
  }

  isValid(): boolean {
    return !!(this.model.varCode?.trim() && this.model.varName?.trim());
  }

  save(): void {
    if (!this.isValid()) return;
    this.model.varCode = this.model.varCode.trim();
    this.model.varName = this.model.varName.trim();
    this.model.name = this.model.name?.trim() || this.model.varName;
    if (this.data.mode === 'create') {
      this.model.parent_id = this.data.parentId;
    }
    this.dialogRef.close(this.model);
  }
}
