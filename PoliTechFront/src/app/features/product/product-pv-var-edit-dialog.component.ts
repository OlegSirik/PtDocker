import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import type { PolicyVar } from '../../shared/services/product.service';

export interface ProductPvVarEditDialogData {
  variable: PolicyVar;
}

function parseOptionalLong(text: string): number | undefined {
  const t = text.trim();
  if (t === '') {
    return undefined;
  }
  const n = Number(t);
  return Number.isFinite(n) ? n : undefined;
}

function parseOptionalNullableLong(text: string): number | null | undefined {
  const t = text.trim();
  if (t === '') {
    return null;
  }
  const n = Number(t);
  return Number.isFinite(n) ? n : null;
}

@Component({
  selector: 'app-product-pv-var-edit-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
  ],
  template: `
    <h2 mat-dialog-title>Редактирование переменной (PvVar)</h2>
    <mat-dialog-content class="dialog-body" style="padding-top: 30px;">
   <!--   
        <mat-form-field appearance="outline" class="full">
          <mat-label>id</mat-label>
          <input matInput name="id" [(ngModel)]="idText" />
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>parent_id</mat-label>
          <input matInput name="parent_id" [(ngModel)]="parentIdText" />
        </mat-form-field>
-->
        <mat-form-field appearance="outline" class="full">
          <mat-label>varCode</mat-label>
          <input matInput name="varCode" [(ngModel)]="model.varCode" readonly />
        </mat-form-field>
        <mat-form-field appearance="outline" class="full">
          <mat-label>varName</mat-label>
          <input matInput name="varName" [(ngModel)]="model.varName" readonly />
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>varType</mat-label>
          <input matInput name="varType" [(ngModel)]="model.varType" readonly />
        </mat-form-field>
        <mat-form-field appearance="outline" class="full">
          <mat-label>varDataType</mat-label>
          <input matInput name="varDataType" [(ngModel)]="model.varDataType" readonly />
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>varList</mat-label>
          <input matInput name="varList" [(ngModel)]="model.varList" readonly />
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>varValue</mat-label>
          <input matInput name="varValue" [(ngModel)]="model.varValue" />
        </mat-form-field>

        <mat-checkbox [(ngModel)]="model.isTarifFactor" name="isTarifFactor">isTarifFactor</mat-checkbox>
      
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
        padding-top: 30px;
      }
      .full {
        width: 100%;
      }
    `,
  ],
})
export class ProductPvVarEditDialogComponent {
  model: PolicyVar;

  idText = '';
  parentIdText = '';
  varNrText = '';

  constructor(
    private dialogRef: MatDialogRef<ProductPvVarEditDialogComponent, PolicyVar | undefined>,
    @Inject(MAT_DIALOG_DATA) public data: ProductPvVarEditDialogData,
  ) {
    this.model = {
      ...data.variable,
      varPath: data.variable.varPath ?? '',
      varName: data.variable.varName ?? '',
      varCode: data.variable.varCode ?? '',
      varDataType: data.variable.varDataType ?? 'STRING',
      varValue: data.variable.varValue ?? '',
      varType: data.variable.varType ?? 'STRING',
      varCdm: data.variable.varCdm ?? '',
      varNr: data.variable.varNr ?? 0,
      varList: data.variable.varList ?? '',
      
      isSystem: data.variable.isSystem ?? false,
      isDeleted: data.variable.isDeleted ?? false,
      isTarifFactor: data.variable.isTarifFactor ?? false,
    };
    this.idText = data.variable.id != null ? String(data.variable.id) : '';
    this.parentIdText = data.variable.parent_id != null ? String(data.variable.parent_id) : '';
    this.varNrText = data.variable.varNr != null ? String(data.variable.varNr) : '0';
  }

  isValid(): boolean {
    return !!(this.model.varCode?.trim() && this.model.varName?.trim());
  }

  save(): void {
    if (!this.isValid()) {
      return;
    }
    this.model.varCode = this.model.varCode.trim();
    this.model.varName = this.model.varName.trim();

    const idParsed = parseOptionalLong(this.idText);
    this.model.id = idParsed;

    const parentParsed = parseOptionalNullableLong(this.parentIdText);
    if (this.parentIdText.trim() === '') {
      this.model.parent_id = null;
    } else {
      this.model.parent_id = parentParsed ?? null;
    }

    const nr = Number(String(this.varNrText).trim());
    this.model.varNr = Number.isFinite(nr) ? nr : 0;

    this.dialogRef.close(this.model);
  }
}
