import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

type TemplateNameDialogData = {
  templateName: string;
};

@Component({
  selector: 'app-template-name-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Редактировать имя шаблона</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" style="width: 100%;">
        <mat-label>Имя калькулятора</mat-label>
        <input matInput [(ngModel)]="templateName" maxlength="255" />
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="cancel()">Отмена</button>
      <button mat-flat-button color="primary" (click)="save()" [disabled]="!templateName.trim()">Сохранить</button>
    </mat-dialog-actions>
  `
})
export class TemplateNameDialogComponent {
  templateName: string;

  constructor(
    private readonly dialogRef: MatDialogRef<TemplateNameDialogComponent>,
    @Inject(MAT_DIALOG_DATA) data: TemplateNameDialogData
  ) {
    this.templateName = data?.templateName ?? '';
  }

  cancel(): void {
    this.dialogRef.close();
  }

  save(): void {
    this.dialogRef.close(this.templateName.trim());
  }
}
