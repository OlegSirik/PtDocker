import { Component, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { CoefficientColumn, CalculatorVar } from '../../../shared/services/calculator.service';

@Component({
    selector: 'app-column-dialog',
    imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
],
    template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить колонку' : 'Редактировать колонку' }}</h2>
    <mat-dialog-content>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Колонка №</mat-label>
          <input matInput [(ngModel)]="column.nr" required placeholder="1">
        </mat-form-field>
    
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Оператор условия</mat-label>
          <mat-select [(ngModel)]="column.conditionOperator" required>
            @for (option of data.conditionOperatorOptions; track option) {
              <mat-option [value]="option">
                {{ option }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>
    
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Код переменной</mat-label>
          <mat-select [(ngModel)]="column.varCode" required>
            @for (varItem of data.vars; track varItem) {
              <mat-option [value]="varItem.varCode">
                {{ varItem.varCode }} - {{ varItem.varName }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>
    
      </div>
    
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Тип данных</mat-label>
          <mat-select [(ngModel)]="column.varDataType" required>
            <mat-option value="NUMBER">Number</mat-option>
            <mat-option value="STRING">String</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Порядок сортировки</mat-label>
          <mat-select [(ngModel)]="column.sortOrder" required>
            @for (option of data.sortOrderOptions; track option) {
              <mat-option [value]="option">
                {{ option }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="column" [disabled]="!isValid()">
        {{ data.isNew ? 'Добавить' : 'Сохранить' }}
      </button>
    </mat-dialog-actions>
    `,
    styles: [`
    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr 2fr;
      gap: 16px;
      margin-bottom: 16px;
      margin-top: 16px;
    }
    .form-field {
      width: 100%;
    }
    mat-dialog-content {
      min-width: 600px;
    }
  `]
})
export class ColumnDialogComponent {
  column: CoefficientColumn;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { 
      column?: CoefficientColumn; 
      isNew: boolean;
      vars: CalculatorVar[];
      conditionOperatorOptions: string[];
      sortOrderOptions: string[];
    },
    public dialogRef: MatDialogRef<ColumnDialogComponent>
  ) {
    this.column = data.column ? { ...data.column } : {
      varCode: '',
      varDataType: '',
      nr: '',
      conditionOperator: '',
      sortOrder: ''
    };
  }

  isValid(): boolean {
    return !!(this.column.nr && this.column.varCode && this.column.varDataType && 
              this.column.conditionOperator && this.column.sortOrder);
  }
}
