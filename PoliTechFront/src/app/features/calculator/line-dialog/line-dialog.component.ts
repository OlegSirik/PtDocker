import { Component, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { FormulaLine, CalculatorVar } from '../../../shared/services/calculator.service';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-line-dialog',
    imports: [
    FormsModule,
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatAutocompleteModule,
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
      <div class="row row-1">
        <mat-form-field class="field" appearance="outline">
          <mat-label>Условие слева</mat-label>
          <mat-select [(ngModel)]="line.conditionLeft" >
            @for (varItem of data.vars; track varItem) {
              <mat-option [value]="varItem.varCode">
                {{ varItem.varCode }} - {{ varItem.varName }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>
          </div>
<div class="row row-2">
        <mat-form-field class="field" appearance="outline">
          <mat-label>Оператор условия</mat-label>
          <mat-select [(ngModel)]="line.conditionOperator" >
            @for (option of data.conditionOperatorOptions; track option) {
              <mat-option [value]="option">
                {{ option }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field class="field" appearance="outline">
          <mat-label>Условие справа</mat-label>
          <mat-select [(ngModel)]="line.conditionRight" >
            @for (varItem of data.vars; track varItem) {
              <mat-option [value]="varItem.varCode">
                {{ varItem.varCode }} - {{ varItem.varName }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>
      </div>
    
      <!-- Row 3: expressionResult -->
      <div class="row row-3">
        <mat-form-field class="field" appearance="outline">
          <mat-label>Результат выражения</mat-label>
          <input 
            matInput 
            [(ngModel)]="expressionResultInput" 
            [matAutocomplete]="expressionResultAuto"
            (input)="filterExpressionResult($event)"
            required>
          <mat-autocomplete #expressionResultAuto="matAutocomplete" [displayWith]="displayVar" (optionSelected)="onExpressionResultSelected($event)">
            @for (varItem of filteredExpressionResultVars; track varItem.varCode) {
              <mat-option [value]="varItem">
                {{ varItem.varCode }} - {{ varItem.varName }}
              </mat-option>
            }
          </mat-autocomplete>
        </mat-form-field>
      </div>
    
      <!-- Row 3: expressionLeft -->
      <div class="row row-3">
        <mat-form-field class="field" appearance="outline">
          <mat-label>Выражение слева</mat-label>
          <input 
            matInput 
            [(ngModel)]="expressionLeftInput" 
            [matAutocomplete]="expressionLeftAuto"
            (input)="filterExpressionLeft($event)"
            required>
          <mat-autocomplete #expressionLeftAuto="matAutocomplete" [displayWith]="displayVar" (optionSelected)="onExpressionLeftSelected($event)">
            @for (varItem of filteredExpressionLeftVars; track varItem.varCode) {
              <mat-option [value]="varItem">
                {{ varItem.varCode }} - {{ varItem.varName }}
              </mat-option>
            }
          </mat-autocomplete>
        </mat-form-field>
      </div>

      <!-- Row 4: expressionOperator, expressionRight -->
      <div class="row row-4">
        <mat-form-field class="field" appearance="outline">
          <mat-label>Оператор выражения</mat-label>
          <mat-select [(ngModel)]="line.expressionOperator" >
            @for (option of data.expressionOperatorOptions; track option) {
              <mat-option [value]="option">
                {{ option }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field class="field" appearance="outline">
          <mat-label>Выражение справа</mat-label>
          <input 
            matInput 
            [(ngModel)]="expressionRightInput" 
            [matAutocomplete]="expressionRightAuto"
            (input)="filterExpressionRight($event)">
          <mat-autocomplete #expressionRightAuto="matAutocomplete" [displayWith]="displayVar" (optionSelected)="onExpressionRightSelected($event)">
            @for (varItem of filteredExpressionRightVars; track varItem.varCode) {
              <mat-option [value]="varItem">
                {{ varItem.varCode }} - {{ varItem.varName }}
              </mat-option>
            }
          </mat-autocomplete>
        </mat-form-field>
      </div>
    
      <!-- Row 5: postProcessor -->
      <div class="row row-5">
        <mat-form-field class="field" appearance="outline">
          <mat-label>Постпроцессор</mat-label>
          <mat-select [(ngModel)]="line.postProcessor">
            @for (option of data.postProcessorOptions; track option) {
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
    .row-2 { grid-template-columns: 200px 1fr; }
    .row-3 { grid-template-columns: 1fr; }
    .row-4 { grid-template-columns: 200px 1fr; }
    .row-5 { grid-template-columns: 1fr; }
    .field { width: 100%; }
    mat-dialog-content { min-width: 900px; }
  `]
})
export class LineDialogComponent {
  line: FormulaLine;
  expressionResultInput: string = '';
  filteredExpressionResultVars: CalculatorVar[] = [];
  expressionLeftInput: string = '';
  filteredExpressionLeftVars: CalculatorVar[] = [];
  expressionRightInput: string = '';
  filteredExpressionRightVars: CalculatorVar[] = [];

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
    
    // Initialize filtered vars and input value
    this.filteredExpressionResultVars = [...this.data.vars];
    if (this.line.expressionResult) {
      const selectedVar = this.data.vars.find(v => v.varCode === this.line.expressionResult);
      if (selectedVar) {
        this.expressionResultInput = `${selectedVar.varCode} - ${selectedVar.varName}`;
      } else {
        this.expressionResultInput = this.line.expressionResult;
      }
    }
    
    // Initialize expressionLeft
    this.filteredExpressionLeftVars = [...this.data.vars];
    if (this.line.expressionLeft) {
      const selectedVar = this.data.vars.find(v => v.varCode === this.line.expressionLeft);
      if (selectedVar) {
        this.expressionLeftInput = `${selectedVar.varCode} - ${selectedVar.varName}`;
      } else {
        this.expressionLeftInput = this.line.expressionLeft;
      }
    }
    
    // Initialize expressionRight
    this.filteredExpressionRightVars = [...this.data.vars];
    if (this.line.expressionRight) {
      const selectedVar = this.data.vars.find(v => v.varCode === this.line.expressionRight);
      if (selectedVar) {
        this.expressionRightInput = `${selectedVar.varCode} - ${selectedVar.varName}`;
      } else {
        this.expressionRightInput = this.line.expressionRight;
      }
    }
  }

  filterExpressionResult(event: Event): void {
    const input = (event.target as HTMLInputElement).value;
    const filterValue = input.toLowerCase();
    
    this.filteredExpressionResultVars = this.data.vars.filter(varItem =>
      varItem.varCode.toLowerCase().includes(filterValue) ||
      varItem.varName.toLowerCase().includes(filterValue)
    );
  }

  onExpressionResultSelected(event: any): void {
    const selectedVar = event.option.value as CalculatorVar;
    this.line.expressionResult = selectedVar.varCode;
    this.expressionResultInput = `${selectedVar.varCode} - ${selectedVar.varName}`;
  }

  filterExpressionLeft(event: Event): void {
    const input = (event.target as HTMLInputElement).value;
    const filterValue = input.toLowerCase();
    
    this.filteredExpressionLeftVars = this.data.vars.filter(varItem =>
      varItem.varCode.toLowerCase().includes(filterValue) ||
      varItem.varName.toLowerCase().includes(filterValue)
    );
  }

  onExpressionLeftSelected(event: any): void {
    const selectedVar = event.option.value as CalculatorVar;
    this.line.expressionLeft = selectedVar.varCode;
    this.expressionLeftInput = `${selectedVar.varCode} - ${selectedVar.varName}`;
  }

  filterExpressionRight(event: Event): void {
    const input = (event.target as HTMLInputElement).value;
    const filterValue = input.toLowerCase();
    
    this.filteredExpressionRightVars = this.data.vars.filter(varItem =>
      varItem.varCode.toLowerCase().includes(filterValue) ||
      varItem.varName.toLowerCase().includes(filterValue)
    );
  }

  onExpressionRightSelected(event: any): void {
    const selectedVar = event.option.value as CalculatorVar;
    this.line.expressionRight = selectedVar.varCode;
    this.expressionRightInput = `${selectedVar.varCode} - ${selectedVar.varName}`;
  }

  displayVar(varItem: CalculatorVar | null): string {
    if (!varItem) return '';
    return `${varItem.varCode} - ${varItem.varName}`;
  }

  isValid(): boolean {
    return !!(this.line.nr &&  this.line.expressionResult && 
              this.line.expressionLeft );
  }
}
