import { Component, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { CalculatorCoefficient, CalculatorVar } from '../../../shared/services/calculator.service';

@Component({
    selector: 'app-coefficient-dialog',
    imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
],
    template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить коэффициент' : 'Редактировать коэффициент' }}</h2>
    <mat-dialog-content>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Код переменной</mat-label>
          <input matInput [(ngModel)]="coefficient.varCode" required placeholder="K_Age">
        </mat-form-field>
      </div>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Название</mat-label>
          <input matInput [(ngModel)]="coefficient.varName" required placeholder="Возрастной коэффициент">
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="coefficient" [disabled]="!isValid()">
        {{ data.isNew ? 'Добавить' : 'Сохранить' }}
      </button>
    </mat-dialog-actions>
  `,
    styles: [`
    .form-row {
      display: grid;
      grid-template-columns: 1fr;
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
export class CoefficientDialogComponent {
  coefficient: CalculatorCoefficient;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { 
      coefficient?: CalculatorCoefficient; 
      isNew: boolean;
      vars: CalculatorVar[];
    },
    public dialogRef: MatDialogRef<CoefficientDialogComponent>
  ) {
    this.coefficient = data.coefficient ? { ...data.coefficient } : {
      varCode: '',
      varName: '',
      columns: []
    };
  }

  isValid(): boolean {
    return !!(this.coefficient.varCode && this.coefficient.varName);
  }
}
