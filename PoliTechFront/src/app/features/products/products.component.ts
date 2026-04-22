import { Component, DestroyRef, OnInit, inject, Inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';

import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
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
import { AuthService } from '../../shared/services/auth.service';
import { InsCompanyService, InsuranceCompanyDto } from '../../shared/services/api/ins-company.service';

@Component({
    selector: 'app-products',
    imports: [
    CommonModule,
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
  private route = inject(ActivatedRoute);
  private destroyRef = inject(DestroyRef);
  private svc = inject(ProductsService);
  private snack = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private authService = inject(AuthService);
  private insCompanyService = inject(InsCompanyService);


  products: ProductList[] = [];
  displayedColumns: string[] = ['id', 'lob', 'code', 'name', 'prodVersionNo', 'devVersionNo', 'actions'];
  searchText = '';
  pageSize = 25;
  pageIndex = 0;

  /** Страховые компании */
  insCompanies: InsuranceCompanyDto[] = [];
  insCompaniesDisplayedColumns: string[] = ['id', 'name', 'status', 'actions'];
  loadingInsCompanies = false;

  /** Выбранная СК из query ?insComp= — фильтр списка продуктов */
  selectedInsCompId: number | null = null;

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
    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
        const raw = params.get('insComp');
        if (raw == null || raw === '') {
          this.selectedInsCompId = null;
        } else {
          const n = Number(raw);
          this.selectedInsCompId = Number.isFinite(n) ? n : null;
        }
        this.loadProducts();
      });
    this.loadInsuranceCompanies();
  }

  loadProducts(): void {
    this.svc.getProducts(this.selectedInsCompId).subscribe((products) => {
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
        this.router.navigate(['/', this.authService.tenant, 'product', createdProduct.id, 'version', versionNo]);
      }
    });
  }

  editProduct(product: ProductList, event?: Event): void {
    event?.stopPropagation();
    if (!product?.id) return;

    // Use devVersionNo if available, otherwise use prodVersionNo, fallback to 0
    const versionNo = product.devVersionNo !== null ? product.devVersionNo :
                     product.prodVersionNo !== null ? product.prodVersionNo : 0;

    this.router.navigate(['/', this.authService.tenant, 'product', product.id, 'version', versionNo]);
  }

  deleteProduct(product: ProductList, event?: Event): void {
    event?.stopPropagation();
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

  loadInsuranceCompanies(): void {
    this.loadingInsCompanies = true;
    this.insCompanyService.list().subscribe({
      next: (list) => {
        this.insCompanies = list;
        this.loadingInsCompanies = false;
      },
      error: () => {
        this.loadingInsCompanies = false;
        this.snack.open('Не удалось загрузить страховые компании', 'OK', { duration: 3000 });
      },
    });
  }

  addInsuranceCompany(): void {
    this.router.navigate(['/', this.authService.tenant, 'admin', 'ins-company', 'edit']);
  }

  editInsuranceCompany(row: InsuranceCompanyDto, event?: Event): void {
    event?.stopPropagation();
    if (row.id == null) return;
    this.router.navigate(['/', this.authService.tenant, 'admin', 'ins-company', String(row.id)]);
  }

  /** Клик по строке СК: переключить фильтр продуктов (?insComp=id); повторный клик по той же — сброс. */
  onInsuranceCompanyRowClick(row: InsuranceCompanyDto): void {
    if (row.id == null) return;
    const id = Number(row.id);
    const next =
      this.selectedInsCompId != null && this.selectedInsCompId === id ? null : id;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { insComp: next },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }

  clearInsCompFilter(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { insComp: null },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }

  selectedInsCompanyName(): string | null {
    if (this.selectedInsCompId == null) return null;
    const c = this.insCompanies.find((x) => x.id != null && Number(x.id) === this.selectedInsCompId);
    return c?.name ?? null;
  }

  deleteInsuranceCompany(row: InsuranceCompanyDto, event?: Event): void {
    event?.stopPropagation();
    if (row.id == null) return;
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { message: `Удалить страховую компанию «${row.name}»?` },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.insCompanyService.delete(row.id!).subscribe({
          next: () => {
            this.loadInsuranceCompanies();
            this.snack.open('Удалено', 'OK', { duration: 2000 });
          },
          error: () => {
            this.snack.open('Ошибка при удалении', 'OK', { duration: 2500 });
          },
        });
      }
    });
  }

  getInsStatusClass(status: string | undefined): string {
    const s = (status ?? '').toLowerCase();
    if (s === 'suspended') return 'pt-status-suspended';
    return 'pt-status-active';
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
    vars: [],
    rules: { insuredEqualsPolicyHolder: false }
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
