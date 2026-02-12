import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-sql-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>SQL</h2>
    <mat-dialog-content>
      <pre class="sql-text">{{ data.sql }}</pre>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-raised-button color="primary" mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .sql-text {
      white-space: pre-wrap;
      word-wrap: break-word;
      margin: 0;
      font-family: monospace;
      font-size: 13px;
      max-height: 60vh;
      overflow-y: auto;
      background: #f5f5f5;
      padding: 12px;
      border-radius: 4px;
    }
    mat-dialog-content {
      min-width: 500px;
    }
  `]
})
export class SqlDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<SqlDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { sql: string }
  ) {}
}
