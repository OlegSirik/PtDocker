import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { QuoteValidator } from '../../../shared';

@Component({
    selector: 'app-validator-dialog',
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
    <h2 mat-dialog-title style="color: #495057; font-size: 18px; font-weight: 600;">{{ data.isNew ? 'Добавить проверку' : 'Редактировать проверку' }}</h2>
    <mat-dialog-content style="padding-top: 20px;">
      <div class="row-1" >
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Порядок выполнения ( если важно )</mat-label>
          <input matInput type="number" [(ngModel)]="validator.lineNr" placeholder="1">
        </mat-form-field>
      </div>

      <div class="row-2">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Что проверяем</mat-label>
          <input matInput 
                 [(ngModel)]="validator.keyLeft" 
                 [matAutocomplete]="keyLeftAuto"
                 (ngModelChange)="onKeyLeftInput($event)"
                 required
                 placeholder="Выберите переменную">
          <mat-autocomplete #keyLeftAuto="matAutocomplete" 
                            (optionSelected)="onKeyLeftSelected($event)"
                            [displayWith]="displayKeyLeft.bind(this)">
            @for (option of filteredKeyLeftOptions; track option) {
              <mat-option [value]="getKeyOptionValue(option)">
                {{ getKeyOptionLabel(option) }}
              </mat-option>
            }
          </mat-autocomplete>
        </mat-form-field>

        </div>

      <div class="row-2">

        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Тип правила</mat-label>
          <mat-select [(ngModel)]="validator.ruleType" required (selectionChange)="onValidatorFieldChange()">
            @for (option of data.ruleTypeOptions; track option) {
              <mat-option [value]="option">
                {{ getRuleTypeLabel(option) }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>
      </div>

      <div class="row-2">  
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Значение условия</mat-label>
          <input matInput 
                 [(ngModel)]="validator.keyRight" 
                 [matAutocomplete]="keyRightAuto"
                 (ngModelChange)="onKeyRightInput($event)"
                 placeholder="Выберите или введите значение">
          <mat-autocomplete #keyRightAuto="matAutocomplete" 
                            (optionSelected)="onKeyRightSelected($event)"
                            [displayWith]="displayKeyRight.bind(this)">
            @for (option of filteredKeyRightOptions; track option) {
              <mat-option [value]="getKeyOptionValue(option)">
                {{ getKeyOptionLabel(option) }}
              </mat-option>
            }
          </mat-autocomplete>
        </mat-form-field>
      </div>

      @if (false) {
      <div class="row-4">
<div></div>
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Значение справа</mat-label>
          <input matInput [(ngModel)]="validator.valueRight" (ngModelChange)="onValidatorFieldChange()">
        </mat-form-field>
      </div>
      }

      <div class="form-row">
        <mat-form-field class="form-field full-width" appearance="outline">
          <mat-label>Текст ошибки</mat-label>
          <input matInput [(ngModel)]="validator.errorText" required placeholder="Ошибка в страховой сумме">
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="validator" [disabled]="!isValid()">
        {{ data.isNew ? 'Добавить' : 'Сохранить' }}
      </button>
    </mat-dialog-actions>
    `,
    styles: [`
    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr 1fr;
      gap: 16px;
      margin-bottom: 16px;
    }
    .row-1 { grid-template-columns: 1fr; margin-top: 25px;}
    .row-2 { grid-template-columns: 1fr; }
    .row-3 { display: grid; gap: 16px; grid-template-columns: 1fr 4fr; }
    .row-4 { display: grid; gap: 16px; grid-template-columns: 1fr 4fr; }
    .row-5 { grid-template-columns: 1fr; }
    .full-width {
      grid-column: 1 / -1;
    }
    .form-field {
      width: 100%;
    }
    mat-dialog-content {
      min-width: 900px;
    }
  `]
})
export class ValidatorDialogComponent {
  validator: QuoteValidator;
  filteredKeyLeftOptions: any[] = [];
  filteredKeyRightOptions: any[] = [];

  // Rule type labels mapping
  ruleTypeLabels: { [key: string]: string } = {
    'NOT_NULL': 'Должно быть заполнено',
    'RANGE': 'Должно находиться в диапазоне',
    '=': 'Равно',
    '!=': 'Не равно',
    '>': 'Больше',
    '<': 'Меньше',
    '>=': 'Больше или равно',
    '<=': 'Меньше или равно',
    'IN_LIST': 'Входит в список'
  };

  constructor(
    public dialogRef: MatDialogRef<ValidatorDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      validator?: QuoteValidator;
      isNew: boolean;
      keyLeftOptions: string[];
      ruleTypeOptions: string[];
    }
  ) {
    this.validator = data.validator ? { ...data.validator } : {
      keyLeft: '',
      ruleType: '',
      dataType: 'NUMBER',
      errorText: '',
      isKeyRightCustomValue: false
    };
    // Initialize filtered options sorted alphabetically by label
    this.filteredKeyLeftOptions = this.sortOptionsByLabel([...(data.keyLeftOptions || [])]);
    this.filteredKeyRightOptions = this.sortOptionsByLabel([...(data.keyLeftOptions || [])]);
  }

  /** Sort options alphabetically by label */
  private sortOptionsByLabel(options: any[]): any[] {
    return options.sort((a, b) => {
      const labelA = this.getKeyOptionLabel(a).toLowerCase();
      const labelB = this.getKeyOptionLabel(b).toLowerCase();
      return labelA.localeCompare(labelB);
    });
  }

  /** Get label for rule type */
  getRuleTypeLabel(code: string): string {
    return this.ruleTypeLabels[code] || code;
  }

  /** Filter keyLeft options based on input */
  onKeyLeftInput(value: string): void {
    const filterValue = (value || '').toLowerCase();
    const filtered = (this.data.keyLeftOptions || []).filter(option => {
      const label = this.getKeyOptionLabel(option).toLowerCase();
      const code = this.getKeyOptionValue(option).toLowerCase();
      return label.includes(filterValue) || code.includes(filterValue);
    });
    this.filteredKeyLeftOptions = this.sortOptionsByLabel(filtered);
    this.onValidatorFieldChange();
  }

  /** Handle keyLeft option selection from autocomplete */
  onKeyLeftSelected(event: any): void {
    this.validator.keyLeft = event.option.value;
    this.onValidatorFieldChange();
  }

  /** Display function for keyLeft autocomplete */
  displayKeyLeft(value: string): string {
    if (!value) return '';
    const option = (this.data.keyLeftOptions || []).find(opt => 
      this.getKeyOptionValue(opt) === value
    );
    return option ? this.getKeyOptionLabel(option) : value;
  }

  /** Filter keyRight options based on input */
  onKeyRightInput(value: string): void {
    const filterValue = (value || '').toLowerCase();
    const filtered = (this.data.keyLeftOptions || []).filter(option => {
      const label = this.getKeyOptionLabel(option).toLowerCase();
      const code = this.getKeyOptionValue(option).toLowerCase();
      return label.includes(filterValue) || code.includes(filterValue);
    });
    this.filteredKeyRightOptions = this.sortOptionsByLabel(filtered);
    // Check if value is custom (not in list)
    this.validator.isKeyRightCustomValue = !this.isValueInList(value);
    this.onValidatorFieldChange();
  }

  /** Handle option selection from autocomplete */
  onKeyRightSelected(event: any): void {
    this.validator.keyRight = event.option.value;
    this.validator.isKeyRightCustomValue = false; // Selected from list
    this.onValidatorFieldChange();
  }

  /** Check if value exists in the options list */
  isValueInList(value: string): boolean {
    if (!value) return true;
    return (this.data.keyLeftOptions || []).some(option => 
      this.getKeyOptionValue(option) === value
    );
  }

  /** Display function for autocomplete - shows label for selected value */
  displayKeyRight(value: string): string {
    if (!value) return '';
    const option = (this.data.keyLeftOptions || []).find(opt => 
      this.getKeyOptionValue(opt) === value
    );
    return option ? this.getKeyOptionLabel(option) : value;
  }

  isValid(): boolean {
    return !!(this.validator.keyLeft && this.validator.ruleType && this.validator.errorText);
  }

  /**
   * Возвращает значение, которое будет сохраняться в валидаторе
   * (всегда код переменной)
   */
  getKeyOptionValue(option: any): string {
    if (!option) {
      return '';
    }
    if (typeof option === 'string') {
      return option;
    }
    if ('varCode' in option && typeof option.varCode === 'string') {
      return option.varCode;
    }
    return String(option);
  }

  /**
   * Возвращает текст для отображения в выпадающем списке:
   *   varCode - varName  (если есть имя)
   *   varCode           (если имени нет)
   */
  getKeyOptionLabel(option: any): string {
    if (!option) {
      return '';
    }
    if (typeof option === 'string') {
      return option;
    }
    const code = 'varCode' in option ? option.varCode : '';
    const name = 'varName' in option ? option.varName : '';
    if (name) {
      return `${name}`;
    }
    return String(option);
  }

  /**
   * Обработчик изменений ключей/типа правила.
   * Если текст ошибки пустой, заполняем его сгенерированным описанием.
   */
  onValidatorFieldChange(): void {
    //if (!this.validator.errorText || !this.validator.errorText.trim()) {
      this.validator.errorText = this.buildErrorText();
    //}
  }

  /** Строим текст ошибки в зависимости от ruleType */
  private buildErrorText(): string {
    const leftLabel = this.getKeyLabel(this.validator.keyLeft);
    const rightLabel = this.getKeyLabel(this.validator.keyRight);
    const valueRight = this.validator.valueRight || '';

    switch (this.validator.ruleType) {
      case 'NOT_NULL':
        return `Поле "${leftLabel}" должно быть заполнено`;
      case 'RANGE':
        return `Значение "${leftLabel}" должно находится в диапазоне ${rightLabel}`;
      case '=':
        return `"${leftLabel}" должно быть равно ${rightLabel }`;
      case '!=':
        return `"${leftLabel}" не должно быть равно ${rightLabel }`;
      case '>':
        return `"${leftLabel}" должно быть больше ${rightLabel }`;
      case '<':
        return `"${leftLabel}" должно быть меньше ${rightLabel }`;
      case 'IN_LIST':
        return `Значение "${leftLabel}" должно быть в списке ${rightLabel}`;
      default:
        // Базовый текст по умолчанию
        return `Неверное значение в поле "${leftLabel}"`;
    }
  }

  /** Находим человекочитаемое имя переменной по её коду */
  private getKeyLabel(code?: string): string {
    if (!code) {
      return '';
    }
    const options = this.data.keyLeftOptions || [];
    const found = options.find((opt: any) => {
      if (!opt || typeof opt === 'string') {
        return opt === code;
      }
      return opt.varCode === code;
    }) as any;

    if (found && typeof found !== 'string' && found.varCode) {
      if (found.varName) {
        return `${found.varName}`;
      }
      return found.varCode;
    }

    return code;
  }
}
