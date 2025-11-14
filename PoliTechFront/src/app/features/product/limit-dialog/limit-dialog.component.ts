import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Limit } from '../../../shared/services/product.service';

@Component({
  selector: 'app-limit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
  <h2 mat-dialog-title>{{ data.isNew ? 'Добавить лимит' : 'Редактировать лимит' }}</h2>
  <div mat-dialog-content>
    <mat-form-field appearance="outline" style="min-width: 300px;">
      <mat-label>№</mat-label>
      <input matInput type="number" [(ngModel)]="model.nr" placeholder="1">
    </mat-form-field>
    <mat-form-field appearance="outline" style="min-width: 300px;">
      <mat-label>Страховая сумма</mat-label>
      <input matInput type="number" [(ngModel)]="model.sumInsured" placeholder="0">
    </mat-form-field>
    <mat-form-field appearance="outline" style="min-width: 300px;">
      <mat-label>Премия</mat-label>
      <input matInput type="number" [(ngModel)]="model.premium" placeholder="0">
    </mat-form-field>
  </div>
  <div mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Отмена</button>
    <button mat-raised-button color="primary" [mat-dialog-close]="model" [disabled]="!isValid()">OK</button>
  </div>
  `
})
export class LimitDialogComponent {
  model: Limit;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { limit?: Limit; isNew: boolean },
    public dialogRef: MatDialogRef<LimitDialogComponent>
  ) {
    this.model = data.limit ? { ...data.limit } : { sumInsured: 0, premium: 0 };
  }

  isValid(): boolean {
    return this.model.sumInsured >= 0 && this.model.premium >= 0;
  }
}
