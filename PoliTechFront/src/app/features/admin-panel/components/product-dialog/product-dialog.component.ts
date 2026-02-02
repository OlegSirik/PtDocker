import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { Product } from '../../../../shared/services/account.service';

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
        <mat-label>Продукт  {{product.roleProductId}} </mat-label>
        <input matInput [(ngModel)]="product.roleProductName" type="string" required>
      </mat-form-field>
      <mat-checkbox [(ngModel)]="product.isDeleted">Продукт недоступен для account</mat-checkbox>

      <mat-checkbox [(ngModel)]="product.canRead">Может читать</mat-checkbox>
      <mat-checkbox [(ngModel)]="product.canQuote">Может создавать котировки</mat-checkbox>
      <mat-checkbox [(ngModel)]="product.canPolicy">Может создавать полисы</mat-checkbox>

    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="getResult()">
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
  product: Product;

  constructor(
    public dialogRef: MatDialogRef<ProductDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { product: Product | null; accountId?: number }
  ) {
    if (data.product) {
      // Edit mode - use existing product
      this.product = { ...data.product };
    } else {
      // Create mode - initialize new product
      this.product = {
        accountId: data.accountId || 0,
        roleProductId: 0,
        roleProductName: '',
        roleAccountId: data.accountId || 0,
        isDeleted: false,
        canRead: false,
        canQuote: false,
        canPolicy: false,
        canAddendum: false,
        canCancel: false,
        canProlongate: false
      };
    }
  }

  getResult(): Product {
    return this.product;
  }
}

