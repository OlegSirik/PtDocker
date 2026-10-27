import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { BusinessLineFile } from '../../../shared/services/business-line-edit.service';

export interface PackageFileDialogResult {
  fileCode: string;
  fileName: string;
  fileNr: number;
  isStatic: boolean;
  file: File;
}

@Component({
  selector: 'app-package-file-dialog',
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
    <h2 mat-dialog-title style="color: #495057; font-size: 18px; font-weight: 600;">Добавить файл ПФ</h2>
    <mat-dialog-content style="padding-top: 20px;">
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Тип ПФ (из LOB)</mat-label>
          <mat-select [(ngModel)]="fileCode" required (selectionChange)="onTypeChange()">
            @for (t of data.lobFiles; track t.fileCode) {
              <mat-option [value]="t.fileCode">{{ t.fileCode }} — {{ t.fileName }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
      </div>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Порядковый номер</mat-label>
          <input matInput type="number" min="1" [(ngModel)]="fileNr" required>
        </mat-form-field>
      </div>
      <div class="form-row checkbox-row">
        <mat-checkbox [(ngModel)]="isStatic">Статический</mat-checkbox>
      </div>
      <div class="form-row">
        <label class="file-label">Файл PDF</label>
        <input type="file" accept=".pdf,application/pdf" (change)="onFileSelected($event)" />
        @if (selectedFile) {
          <div class="file-name">{{ selectedFile.name }}</div>
        }
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button mat-dialog-close>Отмена</button>
      <button mat-flat-button color="primary" [disabled]="!isValid()" (click)="submit()">Добавить</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .form-row {
      display: grid;
      gap: 8px;
      margin-bottom: 16px;
    }
    .form-field { width: 100%; }
    .checkbox-row { margin-bottom: 8px; }
    .file-label { font-size: 12px; color: rgba(0,0,0,.6); }
    .file-name { font-size: 13px; margin-top: 4px; }
    mat-dialog-content { min-width: 420px; }
  `]
})
export class PackageFileDialogComponent {
  fileCode = '';
  fileName = '';
  fileNr = 1;
  isStatic = false;
  selectedFile: File | null = null;

  constructor(
    public dialogRef: MatDialogRef<PackageFileDialogComponent, PackageFileDialogResult>,
    @Inject(MAT_DIALOG_DATA) public data: {
      lobFiles: BusinessLineFile[];
      existingKeys: Array<{ fileCode: string; fileNr: number }>;
    }
  ) {
    if (data.lobFiles?.length === 1) {
      this.fileCode = data.lobFiles[0].fileCode;
      this.fileName = data.lobFiles[0].fileName || data.lobFiles[0].fileCode;
    }
  }

  onTypeChange(): void {
    const t = this.data.lobFiles.find(f => f.fileCode === this.fileCode);
    this.fileName = t?.fileName || this.fileCode;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  isValid(): boolean {
    if (!this.fileCode || !this.selectedFile || !this.fileNr || this.fileNr < 1) {
      return false;
    }
    const duplicate = (this.data.existingKeys || []).some(
      k => k.fileCode === this.fileCode && k.fileNr === this.fileNr
    );
    return !duplicate;
  }

  submit(): void {
    if (!this.isValid() || !this.selectedFile) {
      return;
    }
    this.dialogRef.close({
      fileCode: this.fileCode,
      fileName: this.fileName || this.fileCode,
      fileNr: this.fileNr,
      isStatic: this.isStatic,
      file: this.selectedFile
    });
  }
}
