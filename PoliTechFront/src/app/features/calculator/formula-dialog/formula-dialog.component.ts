import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { CalculatorFormula, CalculatorVar } from '../../../shared/services/calculator.service';

@Component({
  selector: 'app-formula-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить формулу' : 'Редактировать формулу' }}</h2>
    <mat-dialog-content>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Код переменной</mat-label>
          <input matInput [value]="formula.varCode" readonly>
        </mat-form-field>
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Название переменной</mat-label>
          <input matInput [(ngModel)]="formula.varName" required placeholder="Премия">
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="formula" [disabled]="!isValid()">
        {{ data.isNew ? 'Добавить' : 'Сохранить' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
      margin-bottom: 16px;
    }
    .form-field {
      width: 100%;
    }
    mat-dialog-content {
      min-width: 500px;
    }
  `]
})
export class FormulaDialogComponent {
  formula: CalculatorFormula;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { 
      formula?: CalculatorFormula; 
      isNew: boolean;
      vars: CalculatorVar[];
    },
    public dialogRef: MatDialogRef<FormulaDialogComponent>
  ) {
    this.formula = data.formula ? { ...data.formula } : {
      varCode: '',
      varName: '',
      lines: []
    };
  }

  isValid(): boolean {
    return !!(this.formula.varCode && this.formula.varName);
  }
}
