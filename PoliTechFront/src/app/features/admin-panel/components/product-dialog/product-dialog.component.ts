import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { Product } from '../../../../shared/services/tenant.service';

@Component({
  selector: 'app-product-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.product ? 'Редактировать продукт' : 'Добавить продукт' }}</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>ID продукта</mat-label>
        <input matInput [(ngModel)]="productId" type="number" required>
      </mat-form-field>
      <mat-checkbox [(ngModel)]="canRead">Может читать</mat-checkbox>
      <mat-checkbox [(ngModel)]="canQuote">Может создавать котировки</mat-checkbox>
      <mat-checkbox [(ngModel)]="canPolicy">Может создавать полисы</mat-checkbox>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="getResult()" [disabled]="!productId">
        {{ data.product ? 'Сохранить' : 'Добавить' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width {
      width: 100%;
      min-width: 300px;
      margin-bottom: 16px;
    }
    mat-dialog-content {
      margin-bottom: 16px;
    }
    mat-checkbox {
      display: block;
      margin-bottom: 8px;
    }
  `]
})
export class ProductDialogComponent {
  productId: number | null = null;
  canRead: boolean = false;
  canQuote: boolean = false;
  canPolicy: boolean = false;

  constructor(
    public dialogRef: MatDialogRef<ProductDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { product: Product | null; tid: number }
  ) {
    if (data.product) {
      this.productId = data.product.id;
      this.canRead = data.product.can_read || false;
      this.canQuote = data.product.can_quote || false;
      this.canPolicy = data.product.can_policy || false;
    }
  }

  getResult(): Partial<Product> {
    return {
      id: this.productId!,
      can_read: this.canRead,
      can_quote: this.canQuote,
      can_policy: this.canPolicy
    };
  }
}

