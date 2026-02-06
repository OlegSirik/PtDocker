import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  MatDialogModule,
  MatDialogRef,
  MAT_DIALOG_DATA
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { Commission } from '../../../../shared/services/api/commission.service';

export const ACTION_OPTIONS = [
  { value: 'sale', label: 'Продажа' },
  { value: 'prolongation', label: 'Пролонгация' }
];

export interface CommissionEditData {
  productName: string;
  accountId: number;
  productId: number;
  commission?: Commission | null;
}

@Component({
  selector: 'app-commission-edit',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatOptionModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.commission?.id ? 'Редактировать' : 'Добавить' }}</h2>
    <mat-dialog-content class="commission-edit-content">
      <h3 class="product-title">{{ data.productName }}</h3>

      <div class="form-row line2">
        <span class="label">Agent dogovor</span>
        <mat-form-field appearance="outline" class="adr-field">
          <mat-label>agdNumber</mat-label>
          <input matInput [(ngModel)]="model.agdNumber" name="agdNumber">
        </mat-form-field>
        <mat-form-field appearance="outline" class="action-field">
          <mat-label>Action</mat-label>
          <mat-select [(ngModel)]="model.action" name="action">
            <mat-option *ngFor="let opt of actionOptions" [value]="opt.value">{{ opt.label }}</mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <div class="form-row line3">
        <span class="label">Если %</span>
        <mat-form-field appearance="outline" class="number-field align-right">
          <mat-label>rateValue</mat-label>
          <input matInput type="number" [(ngModel)]="model.rateValue" name="rateValue"
                 min="0" max="100" step="0.01">
        </mat-form-field>
      </div>

      <div class="form-row line4">
        <span class="label">Диапазон изменения % Кв от</span>
        <mat-form-field appearance="outline" class="number-field">
          <input matInput type="number" [(ngModel)]="model.commissionMinRate" name="commissionMinRate"
                 min="0" [max]="maxRate" step="0.01" placeholder="от">
        </mat-form-field>
        <span class="label">до</span>
        <mat-form-field appearance="outline" class="number-field">
          <input matInput type="number" [(ngModel)]="model.commissionMaxRate" name="commissionMaxRate"
                 min="0" [max]="maxRate" step="0.01" placeholder="до">
        </mat-form-field>
      </div>

      <div class="form-row line5">
        <span class="label">Минимальный размер Кв в рублях</span>
        <mat-form-field appearance="outline" class="number-field">
          <input matInput type="number" [(ngModel)]="model.minAmount" name="minAmount"
                 min="0" step="0.01">
        </mat-form-field>
        </div>

        <div class="form-row line5">
        <span class="label">Максимальный размер Кв в рублях</span>
        <mat-form-field appearance="outline" class="number-field">
          <input matInput type="number" [(ngModel)]="model.maxAmount" name="maxAmount"
                 min="0" step="0.01">
        </mat-form-field>
      </div>

      <div class="form-row line6">
        <span class="label">Фиксированный размер Кв в рублях</span>
        <mat-form-field appearance="outline" class="number-field">
          <input matInput type="number" [(ngModel)]="model.fixedAmount" name="fixedAmount"
                 min="0" step="0.01">
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отменить</button>
      <button mat-raised-button color="primary" (click)="onSave()">Сохранить</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .commission-edit-content {
      min-width: 800px;
      padding-top: 8px;
    }
    .product-title {
      margin: 0 0 20px 0;
      font-size: 18px;
    }
    .form-row {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      justify-content: flex-end;
      gap: 12px;
      margin-bottom: 16px;
    }
    .form-row .label {
      flex-shrink: 0;
      min-width: 100px;
    }
    .commission-edit-content mat-form-field {
      min-width: 150px;
    }
    .line2 .adr-field { flex: 1; min-width: 150px; }
    .line2 .action-field { min-width: 150px; }
    .line3 .number-field { min-width: 150px; }
    .align-right input { text-align: right; }
    .number-field { min-width: 150px; }
  `]
})
export class CommissionEditComponent {
  readonly actionOptions = ACTION_OPTIONS;

  model: Partial<Commission> & { accountId: number; productId: number } = {
    accountId: 0,
    productId: 0,
    action: 'sale',
    agdNumber: '',
    rateValue: undefined,
    commissionMinRate: undefined,
    commissionMaxRate: undefined,
    minAmount: undefined,
    maxAmount: undefined,
    fixedAmount: undefined
  };

  constructor(
    public dialogRef: MatDialogRef<CommissionEditComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CommissionEditData
  ) {
    this.model.accountId = data.accountId;
    this.model.productId = data.productId;
    if (data.commission) {
      this.model = {
        ...data.commission,
        accountId: data.accountId,
        productId: data.productId
      };
    } else {
      this.model.action = 'sale';
    }
  }

  get maxRate(): number {
    const v = this.model.rateValue;
    return v != null && !isNaN(Number(v)) ? Number(v) : 100;
  }

  onSave(): void {
    const result: Commission = {
      id: this.model.id,
      accountId: this.model.accountId,
      productId: this.model.productId,
      action: this.model.action || 'sale',
      agdNumber: this.model.agdNumber || undefined,
      rateValue: this.model.rateValue != null ? Number(this.model.rateValue) : undefined,
      commissionMinRate: this.model.commissionMinRate != null ? Number(this.model.commissionMinRate) : undefined,
      commissionMaxRate: this.model.commissionMaxRate != null ? Number(this.model.commissionMaxRate) : undefined,
      minAmount: this.model.minAmount != null ? Number(this.model.minAmount) : undefined,
      maxAmount: this.model.maxAmount != null ? Number(this.model.maxAmount) : undefined,
      fixedAmount: this.model.fixedAmount != null ? Number(this.model.fixedAmount) : undefined
    };
    this.dialogRef.close(result);
  }
}
