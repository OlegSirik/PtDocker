import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-parse-errors-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Ошибки разбора</h2>
    <mat-dialog-content>
      <pre class="error-text">{{ data.message }}</pre>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-raised-button color="primary" mat-dialog-close>OK</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .error-text {
      white-space: pre-wrap;
      word-wrap: break-word;
      margin: 0;
      font-family: inherit;
      font-size: 14px;
      max-height: 60vh;
      overflow-y: auto;
    }
    mat-dialog-content {
      min-width: 400px;
    }
  `]
})
export class ParseErrorsDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ParseErrorsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { message: string }
  ) {}
}
