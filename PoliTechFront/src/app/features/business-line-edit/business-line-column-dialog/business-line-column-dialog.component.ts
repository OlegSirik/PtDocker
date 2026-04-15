import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import {
  BusinessLineCoefficientColumn,
  BusinessLineVar,
} from '../../../shared/services/business-line-edit.service';

@Component({
  selector: 'app-business-line-column-dialog',
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatButtonModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить колонку' : 'Редактировать колонку' }}</h2>
    <mat-dialog-content>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Колонка №</mat-label>
          <input matInput [(ngModel)]="column.nr" required placeholder="1" />
        </mat-form-field>

        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Оператор условия</mat-label>
          <mat-select [(ngModel)]="column.conditionOperator" required>
            @for (option of data.conditionOperatorOptions; track option) {
              <mat-option [value]="option">{{ option }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Код переменной</mat-label>
          <input
            matInput
            [(ngModel)]="column.varCode"
            [matAutocomplete]="varCodeAutocomplete"
            required
          />
          <mat-autocomplete #varCodeAutocomplete="matAutocomplete">
            @for (varItem of filteredVarOptions; track varItem.varCode) {
              <mat-option [value]="varItem.varCode">
                {{ varItem.varCode }} — {{ varItem.varName }}
              </mat-option>
            }
          </mat-autocomplete>
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
              <mat-option [value]="option">{{ option }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" type="button" [disabled]="!isValid()" (click)="submit()">
        {{ data.isNew ? 'Добавить' : 'Сохранить' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
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
    `,
  ],
})
export class BusinessLineColumnDialogComponent {
  column: BusinessLineCoefficientColumn;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      column?: BusinessLineCoefficientColumn;
      isNew: boolean;
      vars: BusinessLineVar[];
      conditionOperatorOptions: string[];
      sortOrderOptions: string[];
    },
    public dialogRef: MatDialogRef<BusinessLineColumnDialogComponent>,
  ) {
    this.column = data.column
      ? { ...data.column }
      : {
          varCode: '',
          varDataType: '',
          nr: '',
          conditionOperator: '',
          sortOrder: '',
        };
  }

  get filteredVarOptions(): BusinessLineVar[] {
    const query = (this.column.varCode ?? '').toLowerCase().trim();
    if (!query) {
      return this.data.vars;
    }
    return this.data.vars.filter((v) =>
      v.varCode.toLowerCase().includes(query) ||
      (v.varName ?? '').toLowerCase().includes(query),
    );
  }

  isValid(): boolean {
    return !!(
      this.column.nr &&
      this.column.varCode &&
      this.column.varDataType &&
      this.column.conditionOperator &&
      this.column.sortOrder
    );
  }

  submit(): void {
    if (!this.isValid()) {
      return;
    }
    this.dialogRef.close({ ...this.column });
  }
}
