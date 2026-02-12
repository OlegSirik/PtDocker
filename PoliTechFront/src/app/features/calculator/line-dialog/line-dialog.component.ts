import { Component, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
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
    MatButtonModule,
    MatTabsModule
],
    template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить строку формулы' : 'Редактировать строку формулы' }}</h2>
    <mat-dialog-content>
      <div class="row row-nr">
        <mat-form-field class="field" appearance="outline">
          <mat-label>№</mat-label>
          <input matInput [(ngModel)]="line.nr" required placeholder="1">
        </mat-form-field>
      </div>

      <mat-tab-group [(selectedIndex)]="selectedTabIndex">
        <mat-tab label="Формула">
          <div class="tab-content">
            <div class="row row-3">
              <h4>Формула расчета: Результат = Аргумент1 Оператор Аргумент2</h4>
              <mat-form-field class="field" appearance="outline">
                <mat-label>Результат</mat-label>
                <input 
                  matInput 
                  [(ngModel)]="expressionResultInput" 
                  [matAutocomplete]="expressionResultAuto"
                  (input)="filterExpressionResult($event)"
                  required>
                <mat-autocomplete #expressionResultAuto="matAutocomplete" [displayWith]="displayVar" (optionSelected)="onExpressionResultSelected($event)">
                  @for (varItem of filteredExpressionResultVars; track varItem.varCode) {
                    <mat-option [value]="varItem">
                      {{ getVarDisplayLabel(varItem) }}
                    </mat-option>
                  }
                </mat-autocomplete>
              </mat-form-field>
            </div>
            <div class="row row-3">
              <mat-form-field class="field" appearance="outline">
                <mat-label>Аргумент 1</mat-label>
                <input 
                  matInput 
                  [(ngModel)]="expressionLeftInput" 
                  [matAutocomplete]="expressionLeftAuto"
                  (input)="filterExpressionLeft($event)"
                  required>
                <mat-autocomplete #expressionLeftAuto="matAutocomplete" [displayWith]="displayVar" (optionSelected)="onExpressionLeftSelected($event)">
                  @for (varItem of filteredExpressionLeftVars; track varItem.varCode) {
                    <mat-option [value]="varItem">
                      {{ getVarDisplayLabel(varItem) }}
                    </mat-option>
                  }
                </mat-autocomplete>
              </mat-form-field>
            </div>
            <div class="row row-4">
              <mat-form-field class="field" appearance="outline">
                <mat-label>Оператор</mat-label>
                <mat-select [(ngModel)]="line.expressionOperator" >
                  @for (option of data.expressionOperatorOptions; track option) {
                    <mat-option [value]="option">
                      {{ option }}
                    </mat-option>
                  }
                </mat-select>
              </mat-form-field>
              <mat-form-field class="field" appearance="outline">
                <mat-label>Аргумент 2</mat-label>
                <input 
                  matInput 
                  [(ngModel)]="expressionRightInput" 
                  [matAutocomplete]="expressionRightAuto"
                  (input)="filterExpressionRight($event)">
                <mat-autocomplete #expressionRightAuto="matAutocomplete" [displayWith]="displayVar" (optionSelected)="onExpressionRightSelected($event)">
                  @for (varItem of filteredExpressionRightVars; track varItem.varCode) {
                    <mat-option [value]="varItem">
                      {{ getVarDisplayLabel(varItem) }}
                    </mat-option>
                  }
                </mat-autocomplete>
              </mat-form-field>
            </div>
          </div>
        </mat-tab>


        <mat-tab label="Выполнить если">
          <div class="tab-content">
            <div class="row row-1">
              <h4>Выполнить формулу если выполняется условие</h4>
              <mat-form-field class="field" appearance="outline">
                <mat-label>Что сравниваем</mat-label>
                <input 
                  matInput 
                  [(ngModel)]="conditionLeftInput" 
                  [matAutocomplete]="conditionLeftAuto"
                  (input)="filterConditionLeft($event)"
                  (ngModelChange)="syncConditionLeftFromInput($event)">
                <mat-autocomplete #conditionLeftAuto="matAutocomplete" [displayWith]="displayVar" (optionSelected)="onConditionLeftSelected($event)">
                  @for (varItem of filteredConditionLeftVars; track varItem.varCode) {
                    <mat-option [value]="varItem">
                      {{ getVarDisplayLabel(varItem) }}
                    </mat-option>
                  }
                </mat-autocomplete>
              </mat-form-field>
            </div>
            <div class="row row-2">
              <mat-form-field class="field" appearance="outline">
                <mat-label>Как сравниваем</mat-label>
                <input 
                  matInput 
                  [(ngModel)]="conditionOperatorInput" 
                  [matAutocomplete]="conditionOperatorAuto"
                  (input)="filterConditionOperator($event)"
                  (ngModelChange)="syncConditionOperatorFromInput($event)">
                <mat-autocomplete #conditionOperatorAuto="matAutocomplete" [displayWith]="displayOperator" (optionSelected)="onConditionOperatorSelected($event)">
                  @for (option of filteredConditionOperatorOptions; track option) {
                    <mat-option [value]="option">
                      {{ option }}
                    </mat-option>
                  }
                </mat-autocomplete>
              </mat-form-field>
              <mat-form-field class="field" appearance="outline">
                <mat-label>С чем сравниваем</mat-label>
                <input 
                  matInput 
                  [(ngModel)]="conditionRightInput" 
                  [matAutocomplete]="conditionRightAuto"
                  (input)="filterConditionRight($event)"
                  (ngModelChange)="syncConditionRightFromInput($event)">
                <mat-autocomplete #conditionRightAuto="matAutocomplete" [displayWith]="displayVar" (optionSelected)="onConditionRightSelected($event)">
                  @for (varItem of filteredConditionRightVars; track varItem.varCode) {
                    <mat-option [value]="varItem">
                      {{ getVarDisplayLabel(varItem) }}
                    </mat-option>
                  }
                </mat-autocomplete>
              </mat-form-field>
            </div>
          </div>
        </mat-tab>
        <mat-tab label="Округление">
          <div class="tab-content">
          <h4>Округление результата до 2 знаков после '.'</h4>
            <div class="row row-5">
              <mat-form-field class="field" appearance="outline">
                <mat-label>Округление</mat-label>
                <mat-select [(ngModel)]="line.postProcessor">
                  @for (option of data.postProcessorOptions; track option) {
                    <mat-option [value]="option">
                      {{ option }}
                    </mat-option>
                  }
                </mat-select>
              </mat-form-field>
            </div>
          </div>
        </mat-tab>
      </mat-tab-group>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      @if (hasPrecondition && selectedTabIndex === 1) {
        <button mat-button (click)="clearCondition()">Очистить условие</button>
      }
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
    .row-nr { grid-template-columns: 120px; margin-top: 20px; margin-bottom: 8px; }
    .row-1 { grid-template-columns: 1fr; }
    .row-2 { grid-template-columns: 200px 1fr; }
    .row-3 { grid-template-columns: 1fr; }
    .row-4 { grid-template-columns: 200px 1fr; }
    .row-5 { grid-template-columns: 1fr; }
    .field { width: 100%; }
    .tab-content { padding-top: 16px; }
    mat-dialog-content { min-width: 900px; padding-top: 20px; min-height: 500px; }
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
  conditionLeftInput: string = '';
  filteredConditionLeftVars: CalculatorVar[] = [];
  conditionOperatorInput: string = '';
  filteredConditionOperatorOptions: string[] = [];
  conditionRightInput: string = '';
  filteredConditionRightVars: CalculatorVar[] = [];
  selectedTabIndex = 0;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { 
      line?: FormulaLine; 
      isNew: boolean;
      vars: CalculatorVar[];
      conditionOperatorOptions: string[];
      expressionOperatorOptions: string[];
      postProcessorOptions: string[];
      varDisplayMode?: 'text' | 'code';
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
    
    // Initialize filtered vars and input value (exclude invalid vars)
    this.filteredExpressionResultVars = (this.data.vars || []).filter(v => v?.varCode != null && v?.varName != null);
    if (this.line.expressionResult) {
      const selectedVar = this.data.vars.find(v => v.varCode === this.line.expressionResult);
      if (selectedVar) {
        this.expressionResultInput = this.getVarDisplayLabel(selectedVar);
      } else {
        this.expressionResultInput = this.line.expressionResult;
      }
    }
    
    // Initialize expressionLeft
    this.filteredExpressionLeftVars = (this.data.vars || []).filter(v => v?.varCode != null && v?.varName != null);
    if (this.line.expressionLeft) {
      const selectedVar = this.data.vars.find(v => v.varCode === this.line.expressionLeft);
      if (selectedVar) {
        this.expressionLeftInput = this.getVarDisplayLabel(selectedVar);
      } else {
        this.expressionLeftInput = this.line.expressionLeft;
      }
    }
    
    // Initialize expressionRight
    this.filteredExpressionRightVars = (this.data.vars || []).filter(v => v?.varCode != null && v?.varName != null);
    if (this.line.expressionRight) {
      const selectedVar = this.data.vars.find(v => v.varCode === this.line.expressionRight);
      if (selectedVar) {
        this.expressionRightInput = this.getVarDisplayLabel(selectedVar);
      } else {
        this.expressionRightInput = this.line.expressionRight;
      }
    }

    // Initialize condition fields
    this.filteredConditionLeftVars = (this.data.vars || []).filter(v => v?.varCode != null && v?.varName != null);
    if (this.line.conditionLeft) {
      const selectedVar = this.data.vars.find(v => v.varCode === this.line.conditionLeft);
      if (selectedVar) {
        this.conditionLeftInput = this.getVarDisplayLabel(selectedVar);
      } else {
        this.conditionLeftInput = this.line.conditionLeft;
      }
    }

    this.filteredConditionOperatorOptions = [...(this.data.conditionOperatorOptions || [])];
    this.conditionOperatorInput = this.line.conditionOperator ?? '';

    this.filteredConditionRightVars = (this.data.vars || []).filter(v => v?.varCode != null && v?.varName != null);
    if (this.line.conditionRight) {
      const selectedVar = this.data.vars.find(v => v.varCode === this.line.conditionRight);
      if (selectedVar) {
        this.conditionRightInput = this.getVarDisplayLabel(selectedVar);
      } else {
        this.conditionRightInput = this.line.conditionRight;
      }
    }
  }

  filterExpressionResult(event: Event): void {
    const input = (event.target as HTMLInputElement).value;
    const filterValue = input.toLowerCase();
    
    this.filteredExpressionResultVars = this.data.vars.filter(varItem =>
      varItem?.varCode && varItem?.varName &&
      (varItem.varCode.toLowerCase().includes(filterValue) ||
       varItem.varName.toLowerCase().includes(filterValue))
    );
  }

  onExpressionResultSelected(event: any): void {
    const selectedVar = event.option.value as CalculatorVar;
    this.line.expressionResult = selectedVar.varCode;
    this.expressionResultInput = this.getVarDisplayLabel(selectedVar);
  }

  filterExpressionLeft(event: Event): void {
    const input = (event.target as HTMLInputElement).value;
    const filterValue = input.toLowerCase();
    
    this.filteredExpressionLeftVars = (this.data.vars || []).filter(varItem =>
      varItem?.varCode && varItem?.varName &&
      (varItem.varCode.toLowerCase().includes(filterValue) ||
       varItem.varName.toLowerCase().includes(filterValue))
    );
  }

  onExpressionLeftSelected(event: any): void {
    const selectedVar = event.option.value as CalculatorVar;
    this.line.expressionLeft = selectedVar.varCode;
    this.expressionLeftInput = this.getVarDisplayLabel(selectedVar);
  }

  filterExpressionRight(event: Event): void {
    const input = (event.target as HTMLInputElement).value;
    const filterValue = input.toLowerCase();
    
    this.filteredExpressionRightVars = (this.data.vars || []).filter(varItem =>
      varItem?.varCode && varItem?.varName &&
      (varItem.varCode.toLowerCase().includes(filterValue) ||
       varItem.varName.toLowerCase().includes(filterValue))
    );
  }

  onExpressionRightSelected(event: any): void {
    const selectedVar = event.option.value as CalculatorVar;
    this.line.expressionRight = selectedVar.varCode;
    this.expressionRightInput = this.getVarDisplayLabel(selectedVar);
  }

  filterConditionLeft(event: Event): void {
    const input = (event.target as HTMLInputElement).value;
    const filterValue = input.toLowerCase();
    this.filteredConditionLeftVars = (this.data.vars || []).filter(varItem =>
      varItem?.varCode && varItem?.varName &&
      (varItem.varCode.toLowerCase().includes(filterValue) ||
       varItem.varName.toLowerCase().includes(filterValue))
    );
  }

  onConditionLeftSelected(event: any): void {
    const selectedVar = event.option.value as CalculatorVar;
    this.line.conditionLeft = selectedVar.varCode;
    this.conditionLeftInput = this.getVarDisplayLabel(selectedVar);
  }

  syncConditionLeftFromInput(value: unknown): void {
    const str = typeof value === 'string' ? value : '';
    if (!str.trim()) {
      this.line.conditionLeft = '';
      return;
    }
    const trimmed = str.trim();
    const match = (this.data.vars || []).find(v => 
      v.varCode === trimmed || v.varName === trimmed || `${v.varCode} - ${v.varName}` === trimmed);
    this.line.conditionLeft = match ? match.varCode : '';
  }

  filterConditionOperator(event: Event): void {
    const input = (event.target as HTMLInputElement).value;
    const filterValue = input.toLowerCase();
    const options = this.data.conditionOperatorOptions || [];
    this.filteredConditionOperatorOptions = filterValue
      ? options.filter(opt => opt.toLowerCase().includes(filterValue))
      : [...options];
  }

  onConditionOperatorSelected(event: any): void {
    const selected = event.option.value as string;
    this.line.conditionOperator = selected;
    this.conditionOperatorInput = selected;
  }

  syncConditionOperatorFromInput(value: unknown): void {
    const str = typeof value === 'string' ? value : '';
    if (!str.trim()) {
      this.line.conditionOperator = '';
      return;
    }
    const opts = this.data.conditionOperatorOptions || [];
    this.line.conditionOperator = opts.includes(str.trim()) ? str.trim() : '';
  }

  filterConditionRight(event: Event): void {
    const input = (event.target as HTMLInputElement).value;
    const filterValue = input.toLowerCase();
    this.filteredConditionRightVars = (this.data.vars || []).filter(varItem =>
      varItem?.varCode && varItem?.varName &&
      (varItem.varCode.toLowerCase().includes(filterValue) ||
       varItem.varName.toLowerCase().includes(filterValue))
    );
  }

  onConditionRightSelected(event: any): void {
    const selectedVar = event.option.value as CalculatorVar;
    this.line.conditionRight = selectedVar.varCode;
    this.conditionRightInput = this.getVarDisplayLabel(selectedVar);
  }

  syncConditionRightFromInput(value: unknown): void {
    const str = typeof value === 'string' ? value : '';
    if (!str.trim()) {
      this.line.conditionRight = '';
      return;
    }
    const trimmed = str.trim();
    const match = (this.data.vars || []).find(v => 
      v.varCode === trimmed || v.varName === trimmed || `${v.varCode} - ${v.varName}` === trimmed);
    this.line.conditionRight = match ? match.varCode : '';
  }

  get hasPrecondition(): boolean {
    return !!(this.line.conditionLeft || this.line.conditionOperator || this.line.conditionRight);
  }

  clearCondition(): void {
    this.line.conditionLeft = '';
    this.line.conditionOperator = '';
    this.line.conditionRight = '';
    this.conditionLeftInput = '';
    this.conditionOperatorInput = '';
    this.conditionRightInput = '';
  }

  displayOperator(val: string | null): string {
    return val ?? '';
  }

  getVarDisplayLabel(varItem: CalculatorVar): string {
    return (this.data?.varDisplayMode === 'code') ? (varItem.varCode ?? '') : (varItem.varName ?? varItem.varCode ?? '');
  }

  displayVar(val: CalculatorVar | string | null): string {
    if (val == null) return '';
    if (typeof val === 'string') return val;
    return (this.data?.varDisplayMode === 'code') ? (val.varCode ?? '') : (val.varName ?? val.varCode ?? '');
  }

  isValid(): boolean {
    return !!(this.line.nr &&  this.line.expressionResult && 
              this.line.expressionLeft );
  }
}
