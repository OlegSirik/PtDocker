import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { Deductible } from '../../../shared/services/product.service';

@Component({
  selector: 'app-deductible-dialog',
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
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить франшизу' : 'Редактировать франшизу' }}</h2>
    <mat-dialog-content>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Номер</mat-label>
          <input matInput type="number" [(ngModel)]="deductible.nr" placeholder="1">
        </mat-form-field>
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Тип франшизы</mat-label>
          <mat-select [(ngModel)]="deductible.deductibleType" required>
            <mat-option *ngFor="let option of data.deductibleTypeOptions" [value]="option">
              {{ option }}
            </mat-option>
          </mat-select>
        </mat-form-field>
      </div>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Франшиза</mat-label>
          <input matInput type="number" [(ngModel)]="deductible.deductible" required placeholder="100">
        </mat-form-field>
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Единица</mat-label>
          <mat-select [(ngModel)]="deductible.deductibleUnit" required>
            <mat-option *ngFor="let option of data.deductibleUnitOptions" [value]="option">
              {{ option }}
            </mat-option>
          </mat-select>
        </mat-form-field>
      </div>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Специфика</mat-label>
          <mat-select [(ngModel)]="deductible.deductibleSpecific" required>
            <mat-option *ngFor="let option of data.deductibleSpecificOptions" [value]="option">
              {{ option }}
            </mat-option>
          </mat-select>
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="deductible" [disabled]="!isValid()">
        {{ data.isNew ? 'Добавить' : 'Сохранить' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
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
export class DeductibleDialogComponent {
  deductible: Deductible;

  constructor(
    public dialogRef: MatDialogRef<DeductibleDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      deductible?: Deductible;
      isNew: boolean;
      deductibleTypeOptions: string[];
      deductibleUnitOptions: string[];
      deductibleSpecificOptions: string[];
    }
  ) {
    this.deductible = data.deductible ? { ...data.deductible } : {
      deductibleType: 'MANDATORY',
      deductible: 0,
      deductibleUnit: 'RUB',
      deductibleSpecific: 'EVERY'
    };
  }

  isValid(): boolean {
    return !!(this.deductible.deductibleType && this.deductible.deductibleUnit && this.deductible.deductibleSpecific);
  }
}
