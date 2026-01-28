import { Component, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { QuoteValidator } from '../../../shared';

@Component({
    selector: 'app-validator-dialog',
    imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
],
    template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить проверку' : 'Редактировать проверку' }}</h2>
    <mat-dialog-content>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Номер строки</mat-label>
          <input matInput type="number" [(ngModel)]="validator.lineNr" placeholder="1">
        </mat-form-field>
      </div>

      <div class="row-2">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Что проверяем</mat-label>
          <mat-select [(ngModel)]="validator.keyLeft" required (selectionChange)="onValidatorFieldChange()">
            @for (option of data.keyLeftOptions; track option) {
              <mat-option [value]="getKeyOptionValue(option)">
                {{ getKeyOptionLabel(option) }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>

        </div>

      <div class="row-3">

        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Тип правила</mat-label>
          <mat-select [(ngModel)]="validator.ruleType" required (selectionChange)="onValidatorFieldChange()">
            @for (option of data.ruleTypeOptions; track option) {
              <mat-option [value]="option">
                {{ option }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Ключ справа</mat-label>
          <mat-select [(ngModel)]="validator.keyRight" (selectionChange)="onValidatorFieldChange()">
            @for (option of data.keyLeftOptions; track option) {
              <mat-option [value]="getKeyOptionValue(option)">
                {{ getKeyOptionLabel(option) }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>
      </div>

      <div class="row-4">
<div></div>
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Значение справа</mat-label>
          <input matInput [(ngModel)]="validator.valueRight" (ngModelChange)="onValidatorFieldChange()">
        </mat-form-field>
      </div>

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
    .row-1 { grid-template-columns: 1fr; }
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
      errorText: ''
    };
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
    if (code && name) {
      return `${code} - ${name}`;
    }
    return code || String(option);
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
        return `Поле "${leftLabel}" не должно быть пустым`;
      case 'RANGE':
        return `Допустимый диапазон для "${leftLabel}" = ${valueRight}`;
      case '=':
        return `"${leftLabel}" должно быть равно ${rightLabel || valueRight}`;
      case '!=':
        return `"${leftLabel}" не должно быть равно ${rightLabel || valueRight}`;
      case '>':
        return `"${leftLabel}" должно быть больше ${rightLabel || valueRight}`;
      case '<':
        return `"${leftLabel}" должно быть меньше ${rightLabel || valueRight}`;
      case 'IN_LIST':
        return `Допустимые значения для "${leftLabel}" = ${valueRight}`;
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
        return `${found.varCode} - ${found.varName}`;
      }
      return found.varCode;
    }

    return code;
  }
}
