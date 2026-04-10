import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import {
  BusinessLineCoefficient,
  BusinessLineVar,
} from '../../../shared/services/business-line-edit.service';

@Component({
  selector: 'app-business-line-coefficient-dialog',
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatAutocompleteModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить коэффициент' : 'Редактировать коэффициент' }}</h2>
    <mat-dialog-content>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Код переменной</mat-label>
          <input matInput [(ngModel)]="coefficient.varCode" required placeholder="K_Age" />
        </mat-form-field>
      </div>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Название</mat-label>
          <input matInput [(ngModel)]="coefficient.varName" required placeholder="Возрастной коэффициент" />
        </mat-form-field>
      </div>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Alt переменная / значение</mat-label>
          <input
            matInput
            [(ngModel)]="altInputValue"
            (ngModelChange)="onAltInputChange($event)"
            [matAutocomplete]="altAuto"
            name="altInput"
            placeholder="Выберите из списка или введите число"
          />
          <mat-autocomplete
            #altAuto="matAutocomplete"
            [displayWith]="displayAltOption"
            (optionSelected)="onAltOptionSelected($event)"
          >
            @for (v of filteredVars; track v.varCode) {
              <mat-option [value]="v">{{ v.varCode }} — {{ v.varName }}</mat-option>
            }
          </mat-autocomplete>
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
    `,
  ],
})
export class BusinessLineCoefficientDialogComponent {
  coefficient: BusinessLineCoefficient;
  altInputValue: string | BusinessLineVar = '';
  filteredVars: BusinessLineVar[] = [];

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      coefficient?: BusinessLineCoefficient;
      isNew: boolean;
      vars: BusinessLineVar[];
    },
    public dialogRef: MatDialogRef<BusinessLineCoefficientDialogComponent>,
  ) {
    this.coefficient = data.coefficient
      ? { ...data.coefficient, columns: [...(data.coefficient.columns ?? [])] }
      : { varCode: '', varName: '', altVarCode: '', altVarValue: '', columns: [] };
    this.initAltInput();
    this.filteredVars = [...(data.vars ?? [])];
  }

  private initAltInput(): void {
    if (this.coefficient.altVarCode) {
      const v = this.data.vars?.find((x) => x.varCode === this.coefficient.altVarCode);
      this.altInputValue = v ? `${v.varCode} — ${v.varName}` : this.coefficient.altVarCode;
    } else if (this.coefficient.altVarValue != null && String(this.coefficient.altVarValue).trim() !== '') {
      this.altInputValue = String(this.coefficient.altVarValue);
    }
  }

  displayAltOption(v: BusinessLineVar | null): string {
    return v ? `${v.varCode} — ${v.varName}` : '';
  }

  onAltOptionSelected(event: MatAutocompleteSelectedEvent): void {
    const v = event.option.value as BusinessLineVar;
    this.coefficient.altVarCode = v.varCode;
    this.coefficient.altVarValue = '';
  }

  onAltInputChange(value: string | BusinessLineVar): void {
    if (typeof value !== 'string') {
      if (value) {
        this.coefficient.altVarCode = value.varCode;
        this.coefficient.altVarValue = '';
        this.altInputValue = `${value.varCode} — ${value.varName}`;
      }
      return;
    }
    this.altInputValue = value;
    const vars = this.data.vars || [];
    const lower = value.trim().toLowerCase();
    this.filteredVars = lower
      ? vars.filter(
          (v) =>
            v.varCode.toLowerCase().includes(lower) || v.varName.toLowerCase().includes(lower),
        )
      : [...vars];

    const exact = vars.find(
      (v) => v.varCode === value.trim() || `${v.varCode} — ${v.varName}` === value.trim(),
    );
    if (exact) {
      this.coefficient.altVarCode = exact.varCode;
      this.coefficient.altVarValue = '';
    } else {
      const num = parseFloat(value.trim());
      if (value.trim() !== '' && !isNaN(num)) {
        this.coefficient.altVarValue = value.trim();
        this.coefficient.altVarCode = '';
      } else if (value.trim() === '') {
        this.coefficient.altVarCode = '';
        this.coefficient.altVarValue = '';
      }
    }
  }

  isValid(): boolean {
    return !!(this.coefficient.varCode?.trim() && this.coefficient.varName?.trim());
  }

  /** Явное закрытие: [mat-dialog-close] с объектом в MDC-диалогах иногда не передаёт результат в afterClosed. */
  submit(): void {
    if (!this.isValid()) {
      return;
    }
    this.dialogRef.close({
      ...this.coefficient,
      varCode: this.coefficient.varCode.trim(),
      varName: this.coefficient.varName.trim(),
      columns: [...(this.coefficient.columns ?? [])],
    });
  }
}
