import { Component, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Package } from '../../../shared';

@Component({
    selector: 'app-package-dialog',
    imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
],
    template: `
    <h2 mat-dialog-title style="color: #495057; font-size: 18px; font-weight: 600;">{{ data.isNew ? 'Добавить пакет' : 'Редактировать пакет' }}</h2>
    <mat-dialog-content style="padding-top: 20px;">
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Код</mat-label>
          <input matInput [(ngModel)]="package.code" required placeholder="1">
        </mat-form-field>
      </div>
      <div class="form-row">  
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Название</mat-label>
          <input matInput [(ngModel)]="package.name" required placeholder="1">
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="package" [disabled]="!isValid()">
        {{ data.isNew ? 'Добавить' : 'Сохранить' }}
      </button>
    </mat-dialog-actions>
  `,
    styles: [`
    .form-row {
      display: grid;
      //grid-template-columns: 1fr 1fr;
      gap: 16px;
      margin-bottom: 16px;
    }
    .form-field {
      width: 100%;
    }
    mat-dialog-content {
      min-width: 400px;
    }
  `]
})
export class PackageDialogComponent {
  package: Package;

  constructor(
    public dialogRef: MatDialogRef<PackageDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      package?: Package;
      isNew: boolean;
    }
  ) {
    this.package = data.package ? { ...data.package } : {
      name: '',
      code: '',
      covers: [],
      files: []
    };
  }

  isValid(): boolean {
    return !!(this.package.name);
  }
}
