import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-delete-confirm-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon color="warn">warning</mat-icon>
      Подтверждение удаления
    </h2>
    <mat-dialog-content>
      <p>Вы уверены, что хотите удалить файл "{{data.fileName}}"?</p>
      <p>Это действие нельзя отменить.</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="warn" (click)="confirmDelete()">
        Удалить
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
      min-width: 300px;
    }
  `]
})
export class DeleteConfirmDialogComponent {
  constructor(
    private dialogRef: MatDialogRef<DeleteConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { fileName: string }
  ) {}

  confirmDelete(): void {
    this.dialogRef.close(true);
  }
}
