import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { FormulaLine, CalculatorVar } from '../../../shared/services/calculator.service';

@Component({
  selector: 'app-line-dialog',
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
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить строку' : 'Редактировать строку' }}</h2>
    <mat-dialog-content>
      <!-- Row 1: nr -->
      <div class="row row-1">
        <mat-form-field class="field" appearance="outline">
          <mat-label>№</mat-label>
          <input matInput [(ngModel)]="line.nr" required placeholder="1">
        </mat-form-field>
      </div>

      <!-- Row 2: conditionLeft, conditionOperator, conditionRight -->
      <div class="row row-2">
        <mat-form-field class="field" appearance="outline">
          <mat-label>Условие слева</mat-label>
          <mat-select [(ngModel)]="line.conditionLeft" >
            <mat-option *ngFor="let varItem of data.vars" [value]="varItem.varCode">
              {{ varItem.varCode }} - {{ varItem.varName }}
            </mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field class="field" appearance="outline">
          <mat-label>Оператор условия</mat-label>
          <mat-select [(ngModel)]="line.conditionOperator" >
            <mat-option *ngFor="let option of data.conditionOperatorOptions" [value]="option">
              {{ option }}
            </mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field class="field" appearance="outline">
          <mat-label>Условие справа</mat-label>
          <mat-select [(ngModel)]="line.conditionRight" >
            <mat-option *ngFor="let varItem of data.vars" [value]="varItem.varCode">
              {{ varItem.varCode }} - {{ varItem.varName }}
            </mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <!-- Row 3: expressionResult -->
      <div class="row row-3">
        <mat-form-field class="field" appearance="outline">
          <mat-label>Результат выражения</mat-label>
          <mat-select [(ngModel)]="line.expressionResult" required>
            <mat-option *ngFor="let varItem of data.vars" [value]="varItem.varCode">
              {{ varItem.varCode }} - {{ varItem.varName }}
            </mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <!-- Row 4: expressionLeft, expressionOperator, expressionRight -->
      <div class="row row-4">
        <mat-form-field class="field" appearance="outline">
          <mat-label>Выражение слева</mat-label>
          <mat-select [(ngModel)]="line.expressionLeft" required>
            <mat-option *ngFor="let varItem of data.vars" [value]="varItem.varCode">
              {{ varItem.varCode }} - {{ varItem.varName }}
            </mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field class="field" appearance="outline">
          <mat-label>Оператор выражения</mat-label>
          <mat-select [(ngModel)]="line.expressionOperator" >
            <mat-option *ngFor="let option of data.expressionOperatorOptions" [value]="option">
              {{ option }}
            </mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field class="field" appearance="outline">
          <mat-label>Выражение справа</mat-label>
          <mat-select [(ngModel)]="line.expressionRight" >
            <mat-option *ngFor="let varItem of data.vars" [value]="varItem.varCode">
              {{ varItem.varCode }} - {{ varItem.varName }}
            </mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <!-- Row 5: postProcessor -->
      <div class="row row-5">
        <mat-form-field class="field" appearance="outline">
          <mat-label>Постпроцессор</mat-label>
          <mat-select [(ngModel)]="line.postProcessor">
            <mat-option *ngFor="let option of data.postProcessorOptions" [value]="option">
              {{ option }}
            </mat-option>
          </mat-select>
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="line" [disabled]="!isValid()">
        {{ data.isNew ? 'Добавить' : 'Сохранить' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .row {
      display: grid;
      gap: 16px;
      margin-bottom: 16px;
      align-items: center;
    }
    .row-1 { grid-template-columns: 1fr; }
    .row-2 { grid-template-columns: 1fr 300px 1fr; }
    .row-3 { grid-template-columns: 1fr; }
    .row-4 { grid-template-columns: 1fr 300px 1fr; }
    .row-5 { grid-template-columns: 1fr; }
    .field { width: 100%; }
    mat-dialog-content { min-width: 900px; }
  `]
})
export class LineDialogComponent {
  line: FormulaLine;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { 
      line?: FormulaLine; 
      isNew: boolean;
      vars: CalculatorVar[];
      conditionOperatorOptions: string[];
      expressionOperatorOptions: string[];
      postProcessorOptions: string[];
    },
    public dialogRef: MatDialogRef<LineDialogComponent>
  ) {
    this.line = data.line ? { ...data.line } : {
      nr: '',
      conditionLeft: '',
      conditionOperator: '',
      conditionRight: '',
      expressionResult: '',
      expressionLeft: '',
      expressionOperator: '',
      expressionRight: '',
      postProcessor: ''
    };
  }

  isValid(): boolean {
    return !!(this.line.nr &&  this.line.expressionResult && 
              this.line.expressionLeft );
  }
}
