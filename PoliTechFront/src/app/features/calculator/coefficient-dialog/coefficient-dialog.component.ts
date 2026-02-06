import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { CalculatorCoefficient, CalculatorVar } from '../../../shared/services/calculator.service';

@Component({
    selector: 'app-coefficient-dialog',
    imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatAutocompleteModule
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
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Alt переменная / значение</mat-label>
          <input matInput
            [(ngModel)]="altInputValue"
            (ngModelChange)="onAltInputChange($event)"
            [matAutocomplete]="altAuto"
            name="altInput"
            placeholder="Выберите из списка или введите число">
          <mat-autocomplete #altAuto="matAutocomplete"
            [displayWith]="displayAltOption"
            (optionSelected)="onAltOptionSelected($event)">
            @for (v of filteredVars; track v.varCode) {
              <mat-option [value]="v">{{ v.varCode }} - {{ v.varName }}</mat-option>
            }
          </mat-autocomplete>
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
  altInputValue: string | CalculatorVar = '';
  filteredVars: CalculatorVar[] = [];

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
    this.initAltInput();
    this.filteredVars = [...(data.vars || [])];
  }

  private initAltInput(): void {
    if (this.coefficient.altVarName) {
      const v = this.data.vars?.find(x => x.varCode === this.coefficient.altVarName);
      this.altInputValue = v ? `${v.varCode} - ${v.varName}` : this.coefficient.altVarName;
    } else if (this.coefficient.altVarValue != null) {
      this.altInputValue = String(this.coefficient.altVarValue);
    }
  }

  displayAltOption(v: CalculatorVar | null): string {
    return v ? `${v.varCode} - ${v.varName}` : '';
  }

  onAltOptionSelected(event: MatAutocompleteSelectedEvent): void {
    const v = event.option.value as CalculatorVar;
    this.coefficient.altVarName = v.varCode;
    this.coefficient.altVarValue = undefined;
  }

  onAltInputChange(value: string | CalculatorVar): void {
    if (typeof value !== 'string') {
      if (value) {
        this.coefficient.altVarName = value.varCode;
        this.coefficient.altVarValue = undefined;
        this.altInputValue = `${value.varCode} - ${value.varName}`;
      }
      return;
    }
    this.altInputValue = value;
    const vars = this.data.vars || [];
    const lower = value.trim().toLowerCase();
    this.filteredVars = lower
      ? vars.filter(v =>
          v.varCode.toLowerCase().includes(lower) ||
          v.varName.toLowerCase().includes(lower)
        )
      : [...vars];

    const exact = vars.find(v =>
      v.varCode === value.trim() ||
      `${v.varCode} - ${v.varName}` === value.trim()
    );
    if (exact) {
      this.coefficient.altVarName = exact.varCode;
      this.coefficient.altVarValue = undefined;
    } else {
      const num = parseFloat(value.trim());
      if (value.trim() !== '' && !isNaN(num)) {
        this.coefficient.altVarValue = num;
        this.coefficient.altVarName = undefined;
      } else if (value.trim() === '') {
        this.coefficient.altVarName = undefined;
        this.coefficient.altVarValue = undefined;
      }
    }
  }

  isValid(): boolean {
    return !!(this.coefficient.varCode && this.coefficient.varName);
  }
}
