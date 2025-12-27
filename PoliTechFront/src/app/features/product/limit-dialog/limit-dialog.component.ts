import { Component, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Limit } from '../../../shared';

@Component({
    selector: 'app-limit-dialog',
    imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
],
    template: `
  <h2 mat-dialog-title>{{ data.isNew ? 'Добавить лимит' : 'Редактировать лимит' }}</h2>
  <div mat-dialog-content>
    <mat-form-field appearance="outline" style="width: 100%; display: block; margin-bottom: 16px;">
      <mat-label>Страховая сумма</mat-label>
      <input matInput type="number" [(ngModel)]="model.sumInsured" placeholder="0" required>
    </mat-form-field>
    <mat-form-field appearance="outline" style="width: 100%; display: block;">
      <mat-label>Премия</mat-label>
      <input matInput type="number" [(ngModel)]="model.premium" placeholder="0" required>
    </mat-form-field>
  </div>
  <div mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Отмена</button>
    <button mat-raised-button color="primary" [mat-dialog-close]="model" [disabled]="!isValid()">Добавить</button>
  </div>
  `
})
export class LimitDialogComponent {
  model: Limit;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { limit?: Limit; isNew: boolean },
    public dialogRef: MatDialogRef<LimitDialogComponent>
  ) {
    this.model = data.limit ? { ...data.limit } : { sumInsured: undefined as any, premium: undefined as any };
  }

  isValid(): boolean {
    return !!(this.model.sumInsured && this.model.sumInsured > 0 && this.model.premium && this.model.premium > 0);
  }
}
