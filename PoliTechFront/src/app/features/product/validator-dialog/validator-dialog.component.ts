import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { QuoteValidator } from '../../../shared/services/product.service';

@Component({
  selector: 'app-validator-dialog',
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
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить проверку' : 'Редактировать проверку' }}</h2>
    <mat-dialog-content>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Номер строки</mat-label>
          <input matInput type="number" [(ngModel)]="validator.lineNr" placeholder="1">
        </mat-form-field>
      </div>

      <div class="form-row">
      <mat-form-field class="form-field" appearance="outline">
          <mat-label>Ключ слева</mat-label>
          <mat-select [(ngModel)]="validator.keyLeft" required>
            <mat-option *ngFor="let option of data.keyLeftOptions" [value]="option">
              {{ option }}
            </mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Тип правила</mat-label>
          <mat-select [(ngModel)]="validator.ruleType" required>
            <mat-option *ngFor="let option of data.ruleTypeOptions" [value]="option">
              {{ option }}
            </mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Ключ справа</mat-label>
          <mat-select [(ngModel)]="validator.keyRight">
            <mat-option *ngFor="let option of data.keyLeftOptions" [value]="option">
              {{ option }}
            </mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <div class="form-row">
      <div></div>

        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Тип данных</mat-label>
          <mat-select [(ngModel)]="validator.dataType" required>
            <mat-option value="NUMBER">Number</mat-option>
            <mat-option value="STRING">String</mat-option>
            <mat-option value="DATE">Date</mat-option>
            <mat-option value="DURATION">Duration</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Значение справа</mat-label>
          <input matInput [(ngModel)]="validator.valueRight" placeholder="10000-200000">
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
    .row-2 { grid-template-columns: 1fr 300px 1fr; }
    .row-3 { grid-template-columns: 1fr; }
    .row-4 { grid-template-columns: 1fr 300px 1fr; }
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
}
