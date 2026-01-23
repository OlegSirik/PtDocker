import { Component, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { Cover } from '../../../shared/services/product.service';

@Component({
    selector: 'app-cover-dialog',
    imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule
],
    template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить покрытие' : 'Редактировать покрытие' }}</h2>
    <mat-dialog-content>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Код покрытия</mat-label>
          <mat-select [(ngModel)]="cover.code" required>
            @for (option of data.coverCodeOptions; track option) {
              <mat-option [value]="option">
                {{ option }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>
      </div>
      <div class="form-row">
        <mat-checkbox [(ngModel)]="cover.isMandatory" class="checkbox-field">
          Обязательное
        </mat-checkbox>
      </div>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Период ожидания</mat-label>
          <input matInput [(ngModel)]="cover.waitingPeriod">
        </mat-form-field>
      </div>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Срок покрытия</mat-label>
          <input matInput [(ngModel)]="cover.coverageTerm">
        </mat-form-field>
      </div>
      <div class="form-row">
        <mat-checkbox [(ngModel)]="cover.isDeductibleMandatory" class="checkbox-field">
          Франшиза обязательна
        </mat-checkbox>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="cover" [disabled]="!isValid()">
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
      align-items: center;
      width: 100%;
      justify-items: start;
    }
    .checkbox-field {
      width: 100%;
    }
    .form-field {
      width: 100%;
    }
    mat-dialog-content {
      min-width: 500px;
    }
  `]
})
export class CoverDialogComponent {
  cover: Cover;

  constructor(
    public dialogRef: MatDialogRef<CoverDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      cover?: Cover;
      isNew: boolean;
      coverCodeOptions: string[];
    }
  ) {
    this.cover = data.cover ? { ...data.cover } : {
      code: '',
      isMandatory: true,
      waitingPeriod: '',
      coverageTerm: '',
      isDeductibleMandatory: false,
      deductibles: [],
      limits: []
    };
  }

  isValid(): boolean {
    return !!(this.cover.code);
  }
}
