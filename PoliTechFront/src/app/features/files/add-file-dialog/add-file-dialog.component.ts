import { Component, Inject } from '@angular/core';

import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FilesService } from '../../../shared/services/files.service';
import { ProductList } from '../../../shared/services/products.service';

@Component({
    selector: 'app-add-file-dialog',
    imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    FormsModule,
    ReactiveFormsModule
],
    template: `
    <h2 mat-dialog-title>Добавить новый файл</h2>
    <form [formGroup]="fileForm" (ngSubmit)="onSubmit()">
      <mat-dialog-content>
        <div class="form-row">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Код продукта</mat-label>
            <mat-select formControlName="productCode" required>
              @for (product of data.products; track product) {
                <mat-option [value]="product.code">
                  {{product.code}} - {{product.name}}
                </mat-option>
              }
            </mat-select>
            @if (fileForm.get('productCode')?.hasError('required')) {
              <mat-error>
                Код продукта обязателен
              </mat-error>
            }
          </mat-form-field>
        </div>
    
        <div class="form-row">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Тип файла</mat-label>
            <mat-select formControlName="fileType" required>
              <mat-option value="Policy">Policy</mat-option>
              <mat-option value="Kid">KID</mat-option>
            </mat-select>
            @if (fileForm.get('fileType')?.hasError('required')) {
              <mat-error>
                Тип файла обязателен
              </mat-error>
            }
          </mat-form-field>
        </div>
    
    
        <div class="form-row">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Код пакета</mat-label>
            <input matInput formControlName="packageCode" required>
            @if (fileForm.get('packageCode')?.hasError('required')) {
              <mat-error>
                Код пакета обязателен
              </mat-error>
            }
          </mat-form-field>
        </div>
    
        <div class="form-row">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Описание файла</mat-label>
            <input matInput formControlName="fileDescription" required>
            @if (fileForm.get('fileDescription')?.hasError('required')) {
              <mat-error>
                Описание файла обязательно
              </mat-error>
            }
          </mat-form-field>
        </div>
    
        <div class="form-row">
          <label class="file-input-label">
            <span>Выберите файл</span>
            <input type="file" (change)="onFileSelected($event)" required>
          </label>
          @if (selectedFile) {
            <div class="selected-file">
              Выбран файл: {{selectedFile.name}}
            </div>
          }
        </div>
      </mat-dialog-content>
    
      <mat-dialog-actions align="end">
        <button mat-button mat-dialog-close>Отмена</button>
        <button mat-raised-button color="primary" type="submit" [disabled]="!fileForm.valid || !selectedFile">
          Добавить
        </button>
      </mat-dialog-actions>
    </form>
    `,
    styles: [`
    .form-row {
      margin-bottom: 16px;
    }
    
    .full-width {
      width: 100%;
    }
    
    mat-dialog-content {
      min-width: 400px;
    }
    
    .file-input-label {
      display: block;
      margin-bottom: 8px;
      font-weight: 500;
      color: rgba(0, 0, 0, 0.87);
    }
    
    .file-input-label input[type="file"] {
      margin-top: 8px;
      width: 100%;
    }
    
    .selected-file {
      margin-top: 8px;
      padding: 8px;
      background-color: #f5f5f5;
      border-radius: 4px;
      font-size: 14px;
      color: #666;
    }
  `]
})
export class AddFileDialogComponent {
  fileForm: FormGroup;
  selectedFile: File | null = null;

  constructor(
    private fb: FormBuilder,
    private filesService: FilesService,
    private dialogRef: MatDialogRef<AddFileDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { products: ProductList[] }
  ) {
    this.fileForm = this.fb.group({
      productCode: ['', Validators.required],
      fileType: ['', Validators.required],
      fileDescription: ['', Validators.required],
      packageCode: ['', Validators.required]
    });
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];
  }

  onSubmit(): void {
    if (this.fileForm.valid && this.selectedFile) {
      const fileData = this.fileForm.value;
      
      this.filesService.createFile(fileData).subscribe(response => {
        if (response.id) {
          this.filesService.uploadFile(response.id, this.selectedFile!).subscribe(() => {
            this.dialogRef.close(true);
          });
        }
      });
    }
  }
}
