import { Component, OnInit, inject, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { ProductsService, ProductList } from '../../shared/services/products.service';
import { MatSelectModule } from '@angular/material/select';
import { ProductService, Product, BusinessLineService } from '../../shared';
import { Observable } from 'rxjs';
import {AsyncPipe} from '@angular/common';

@Component({
    selector: 'app-products',
    imports: [
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatTableModule,
    MatSnackBarModule,
    MatDialogModule,
    MatPaginatorModule
],
    templateUrl: './products.component.html',
    styleUrls: ['./products.component.scss']
})
export class ProductsComponent implements OnInit {
  private router = inject(Router);
  private svc = inject(ProductsService);
  private snack = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  products: ProductList[] = [];
  displayedColumns: string[] = ['id', 'lob', 'name', 'code', 'prodVersionNo', 'devVersionNo', 'actions'];
  searchText = '';
  pageSize = 5;
  pageIndex = 0;

  get filteredProducts(): ProductList[] {
    const s = this.searchText.trim().toLowerCase();
    if (!s) return this.products;
    return this.products.filter(p =>
      p.name.toLowerCase().includes(s) ||
      p.code.toLowerCase().includes(s) ||
      p.lob.toLowerCase().includes(s)
    );
  }

  get paginatedProducts(): ProductList[] {
    const startIndex = this.pageIndex * this.pageSize;
    return this.filteredProducts.slice(startIndex, startIndex + this.pageSize);
  }

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.svc.getProducts().subscribe(products => {
      this.products = products;
    });
  }

  createNewProduct(): void {
    const dialogRef = this.dialog.open(CreateProductDialogComponent, {
      width: '500px'
    });

    dialogRef.afterClosed().subscribe((createdProduct: import('../../shared/services/product.service').Product | undefined) => {
      if (createdProduct && createdProduct.id !== undefined) {
        const versionNo = createdProduct.versionNo ?? 0;
        this.router.navigate(['/product', createdProduct.id, 'version', versionNo]);
      }
    });
  }

  editProduct(product: ProductList): void {
    if (!product?.id) return;

    // Use devVersionNo if available, otherwise use prodVersionNo, fallback to 0
    const versionNo = product.devVersionNo !== undefined ? product.devVersionNo :
                     product.prodVersionNo !== undefined ? product.prodVersionNo : 0;

    this.router.navigate(['/product', product.id, 'version', versionNo]);
  }

  deleteProduct(product: ProductList): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { message: `Удалить продукт "${product.name}"?` }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && product.id) {
        this.svc.deleteProduct(product.id).subscribe({
          next: () => {
            this.loadProducts();
            this.snack.open('Продукт удален', 'OK', { duration: 2000 });
          },
          error: () => {
            this.snack.open('Ошибка при удалении', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  onSearch(searchValue: string): void {
    this.searchText = searchValue;
    this.pageIndex = 0; // Reset to first page when searching
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
  }
}

// Confirm Dialog Component
@Component({
    selector: 'app-confirm-dialog',
    imports: [MatDialogModule, MatButtonModule],
    template: `
    <h2 mat-dialog-title>Подтверждение</h2>
    <mat-dialog-content>{{ data.message }}</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="warn" [mat-dialog-close]="true">Удалить</button>
    </mat-dialog-actions>
  `
})
export class ConfirmDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: { message: string }) {}
}

// Create Product Dialog Component

@Component({
    selector: 'app-create-product-dialog',
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    AsyncPipe
  ],
    template: `
    <h2 mat-dialog-title>Новый продукт</h2>
    <mat-dialog-content>
      <div>
        <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 12px;">
          <mat-label>Линия бизнеса (lob)</mat-label>
          <mat-select [(ngModel)]="product.lob">
            @for (c of lobCodes$ | async; track c) {
              <mat-option [value]="c">{{ c }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 12px;">
          <mat-label>Код</mat-label>
          <input matInput [(ngModel)]="product.code" />
        </mat-form-field>

        <mat-form-field appearance="outline" style="width: 100%;">
          <mat-label>Название</mat-label>
          <input matInput [(ngModel)]="product.name" />
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Отмена</button>
      <button mat-raised-button color="primary" [disabled]="!canCreate() || creating" (click)="onCreate()">
        Создать
      </button>
    </mat-dialog-actions>
    `
})
export class CreateProductDialogComponent {
  lobCodes$: Observable<string[]>;
  creating = false;
  product: Product = {
    lob: '',
    code: '',
    name: '',
    versionNo: 1,
    waitingPeriod: {},
    policyTerm: {},
    numberGenerator: { mask: '', maxValue: 0, resetPolicy: 'MONTHLY', xorMask: '' },
    quoteValidator: [],
    saveValidator: [],
    packages: [],
    vars: []
  };

  constructor(
    private dialogRef: MatDialogRef<CreateProductDialogComponent>,
    private productService: ProductService,
    private businessLineService: BusinessLineService,
    private snackBar: MatSnackBar
  ) {
    this.lobCodes$ = this.businessLineService.getLobCodes();
  }

  canCreate(): boolean {
    return !!this.product.lob && !!this.product.code && !!this.product.name;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onCreate(): void {
    if (!this.canCreate() || this.creating) return;
    this.creating = true;
    this.productService.createProduct(this.product).subscribe({
      next: (created: Product) => {
        this.creating = false;
        this.dialogRef.close(created);
      },
      error: (err) => {
        this.creating = false;
        const msg = this.productService.handleHttpError(err);
        this.snackBar.open(msg, 'OK', { duration: 2500 });
      }
    });
  }
}
