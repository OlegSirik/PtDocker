import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

export interface RefDictEntryDialogData {
  title: string;
  code: string;
  name: string;
  isNew: boolean;
}

export interface RefDictEntryDialogResult {
  code: string;
  name: string;
}

@Component({
  selector: 'app-refdict-entry-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  template: `
    <h2 mat-dialog-title class="dialog-title">{{ data.title }}</h2>
    <div mat-dialog-content class="dialog-content">
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Код</mat-label>
        <input matInput [(ngModel)]="model.code" [readonly]="!data.isNew" required />
      </mat-form-field>
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Название</mat-label>
        <input matInput [(ngModel)]="model.name" required />
      </mat-form-field>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button type="button" (click)="cancel()">Отменить</button>
      <button mat-flat-button color="primary" type="button" [disabled]="!isValid()" (click)="save()">
        Сохранить
      </button>
    </div>
  `,
  styles: [
    `
      .dialog-title {
        color: #495057;
        font-size: 18px;
        font-weight: 600;
      }

      .dialog-content {
        padding-top: 20px;
        min-width: 360px;
      }

      .full-width {
        width: 100%;
        margin-bottom: 16px;
      }
    `,
  ],
})
export class RefDictEntryDialogComponent {
  model: RefDictEntryDialogResult;

  constructor(
    private dialogRef: MatDialogRef<RefDictEntryDialogComponent, RefDictEntryDialogResult | undefined>,
    @Inject(MAT_DIALOG_DATA) public data: RefDictEntryDialogData
  ) {
    this.model = {
      code: data.code ?? '',
      name: data.name ?? '',
    };
  }

  isValid(): boolean {
    return !!this.model.code.trim() && !!this.model.name.trim();
  }

  save(): void {
    if (!this.isValid()) {
      return;
    }
    this.dialogRef.close({
      code: this.model.code.trim(),
      name: this.model.name.trim(),
    });
  }

  cancel(): void {
    this.dialogRef.close(undefined);
  }
}
