import { Component, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { Deductible } from '../../../shared/services/product.service';

@Component({
    selector: 'app-deductible-dialog',
    imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
],
    template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить франшизу' : 'Редактировать франшизу' }}</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" style="width: 100%; display: block; margin-bottom: 16px;">
        <mat-label>ID</mat-label>
        <input matInput type="number" [(ngModel)]="deductible.id" required placeholder="1">
      </mat-form-field>
      <mat-form-field appearance="outline" style="width: 100%; display: block;">
        <mat-label>Текст</mat-label>
        <input matInput type="text" [(ngModel)]="deductible.text" required placeholder="X рублей">
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="deductible" [disabled]="!isValid()">
        {{ data.isNew ? 'Добавить' : 'Сохранить' }}
      </button>
    </mat-dialog-actions>
    `,
    styles: [`
    mat-dialog-content {
      min-width: 500px;
    }
  `]
})
export class DeductibleDialogComponent {
  deductible: Deductible;

  constructor(
    public dialogRef: MatDialogRef<DeductibleDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      deductible?: Deductible;
      isNew: boolean;
    }
  ) {
    this.deductible = data.deductible ? { ...data.deductible } : {
      id: undefined as any,
      text: ''
    };
  }

  isValid(): boolean {
    return !!(this.deductible.id !== undefined && this.deductible.id !== null && this.deductible.text && this.deductible.text.trim() !== '');
  }
}
