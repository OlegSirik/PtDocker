import { Component, Inject } from '@angular/core';

import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-process-file-dialog',
    imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    FormsModule
],
    template: `
    <h2 mat-dialog-title>
      <mat-icon>play_arrow</mat-icon>
      Обработка файла
    </h2>
         <mat-dialog-content>
       <p>Введите key-value данные для обработки файла:</p>
       <p class="note">Поддерживаются форматы JSON или обычный текст</p>
       <mat-form-field appearance="outline" class="full-width">
         <mat-label>Key-Value данные</mat-label>
         <textarea 
           matInput 
           [(ngModel)]="keyValueData" 
           rows="8" 
           placeholder='[{"key1": "value1", "key2": "value2"}]'
           required>
         </textarea>
       </mat-form-field>
     </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" (click)="processFile()" [disabled]="!keyValueData.trim()">
        Обработать
      </button>
    </mat-dialog-actions>
  `,
    styles: [`
    mat-dialog-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    mat-dialog-content {
      min-width: 400px;
    }
    
    .full-width {
      width: 100%;
    }
    
         textarea {
       font-family: monospace;
     }
     
     .note {
       font-size: 12px;
       color: #666;
       font-style: italic;
       margin-bottom: 16px;
     }
  `]
})
export class ProcessFileDialogComponent {
  keyValueData: string = '';

  constructor(
    private dialogRef: MatDialogRef<ProcessFileDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { fileId: number }
  ) {}

  processFile(): void {
    if (this.keyValueData.trim()) {
      this.dialogRef.close(this.keyValueData);
    }
  }
}
