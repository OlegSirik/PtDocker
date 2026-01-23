import { Component, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { CalculatorVar } from '../../../shared/services/calculator.service';

@Component({
    selector: 'app-var-dialog',
    imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
],
    template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить переменную' : 'Редактировать переменную' }}</h2>
    <mat-dialog-content>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Код переменной</mat-label>
          <input matInput [(ngModel)]="varItem.varCode" required placeholder="ph_firstname">
        </mat-form-field>
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Название переменной</mat-label>
          <input matInput [(ngModel)]="varItem.varName" required placeholder="Имя страхователя">
        </mat-form-field>
      </div>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Тип переменной</mat-label>
          <mat-select [(ngModel)]="varItem.varType" required>
 <!--
          <mat-option *ngFor="let option of data.varTypeOptions" [value]="option">
              {{ option }}
            </mat-option>
-->       <mat-option value="VAR">VAR</mat-option>
          <mat-option value="CONST">CONST</mat-option>
          
          
          </mat-select>
        </mat-form-field>
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Тип данных</mat-label>
          <mat-select [(ngModel)]="varItem.varDataType" required>
            <mat-option value="NUMBER">Number</mat-option>
            <mat-option value="STRING">String</mat-option>   
          </mat-select>
        </mat-form-field>
      </div>
      <div class="form-row">
        <mat-form-field class="form-field" appearance="outline">
          <mat-label>Значение переменной</mat-label>
          <input matInput [(ngModel)]="varItem.varValue" placeholder="string">
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="varItem" [disabled]="!isValid()">
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
      min-width: 600px;
    }
  `]
})
export class VarDialogComponent {
  varItem: CalculatorVar;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { 
      varItem?: CalculatorVar; 
      isNew: boolean;
      varTypeOptions: string[];
    },
    public dialogRef: MatDialogRef<VarDialogComponent>
  ) {
    this.varItem = data.varItem ? { ...data.varItem } : {
      varCode: '',
      varName: '',
      varPath: '',
      varType: 'VAR',
      varValue: '',
      varDataType: 'STRING',
      varCdm: '',
      varNr: 10000
    };
  }

  isValid(): boolean {
    return !!(this.varItem.varCode && this.varItem.varName && this.varItem.varType && this.varItem.varDataType);
  }
}
