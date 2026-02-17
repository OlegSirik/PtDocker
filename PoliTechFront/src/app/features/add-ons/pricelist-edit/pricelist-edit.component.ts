import { Component, OnInit, inject, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDialog, MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { AuthService } from '../../../shared/services/auth.service';
import { AddonPricelistService, PricelistDto, AddonProductRef } from '../../../shared/services/api/addon-pricelist.service';
import { AddonProvidersService, ProviderDto } from '../../../shared/services/api/addon-providers.service';
import { ProductService } from '../../../shared/services/product.service';
import { ProductList } from '../../../shared/services/products.service';

@Component({
  selector: 'app-pricelist-edit',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSelectModule,
    MatOptionModule,
    MatSnackBarModule,
    MatTableModule,
    MatTabsModule,
    MatDialogModule
  ],
  templateUrl: './pricelist-edit.component.html',
  styleUrls: ['./pricelist-edit.component.scss']
})
export class PricelistEditComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private pricelistService = inject(AddonPricelistService);
  private providersService = inject(AddonProvidersService);
  private productService = inject(ProductService);
  private snack = inject(MatSnackBar);
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);

  pricelist: PricelistDto = {
    providerId: 0,
    code: '',
    name: '',
    status: 'ACTIVE'
  };
  provider: ProviderDto | null = null;
  productsList: ProductList[] = [];
  isNewRecord = true;
  loading = false;
  loadingProducts = false;
  loadingProductList = false;
  productColumns = ['productId', 'productName', 'preconditions', 'actions'];

  get addonProducts(): AddonProductRef[] {
    return this.pricelist.addonProducts || [];
  }

  get headerTitle(): string {
    const pName = this.pricelist.name || '';
    const provName = this.provider?.name || '';
    return [pName, provName].filter(Boolean).join(' / ') || 'Pricelist';
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    const spId = this.route.snapshot.queryParamMap.get('spId');

    if (id && id !== 'edit') {
      this.isNewRecord = false;
      this.loading = true;
      this.pricelistService.getPricelist(+id).subscribe({
        next: (pl) => {
          this.pricelist = { ...pl };
          this.loadProvider();
          this.loadProductsList();
          this.loading = false;
        },
        error: () => {
          this.snack.open('Ошибка при загрузке', 'OK', { duration: 2000 });
          this.loading = false;
          this.goBack();
        }
      });
    } else {
      if (spId) {
        this.pricelist.providerId = +spId;
        this.loadProvider();
      }
      this.loadProductsList();
    }
  }

  loadProvider(): void {
    if (!this.pricelist.providerId) return;
    this.providersService.getById(this.pricelist.providerId).subscribe({
      next: (p) => this.provider = p
    });
  }

  loadProductsList(): void {
    this.loadingProductList = true;
    this.productService.getProductsList().subscribe({
      next: (products) => {
        this.productsList = products || [];
        this.loadingProductList = false;
      },
      error: () => this.loadingProductList = false
    });
  }

  save(): void {
    if (!this.pricelist.name?.trim() || !this.pricelist.code?.trim()) {
      this.snack.open('Заполните название и код', 'OK', { duration: 2000 });
      return;
    }
    if (!this.pricelist.providerId) {
      this.snack.open('Выберите провайдера', 'OK', { duration: 2000 });
      return;
    }

    this.loading = true;
    if (this.isNewRecord) {
      this.pricelistService.createPricelist(this.pricelist).subscribe({
        next: (saved) => {
          this.snack.open('Сохранено', 'OK', { duration: 2000 });
          this.router.navigate(['/', this.authService.tenant, 'admin', 'addon', 'sp-list']);
        },
        error: () => {
          this.snack.open('Ошибка при создании', 'OK', { duration: 2000 });
          this.loading = false;
        }
      });
    } else {
      if (!this.pricelist.id) {
        this.snack.open('ID не найден', 'OK', { duration: 2000 });
        this.loading = false;
        return;
      }
      this.pricelistService.updatePricelist(this.pricelist.id, this.pricelist).subscribe({
        next: () => {
          this.snack.open('Сохранено', 'OK', { duration: 2000 });
          this.goBack();
        },
        error: () => {
          this.snack.open('Ошибка при сохранении', 'OK', { duration: 2000 });
          this.loading = false;
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/', this.authService.tenant, 'admin', 'addon', 'sp-list']);
  }

  openAddProductDialog(): void {
    const openWithProducts = (products: ProductList[]) => {
      const existingIds = new Set((this.pricelist.addonProducts || []).map(ap => ap.productId));
      const available = products.filter(p => p.id && !existingIds.has(p.id));

      const dialogRef = this.dialog.open(AddonProductDialogComponent, {
        width: '420px',
        data: { products: available, loading: false }
      });

      dialogRef.afterClosed().subscribe((productId: number | null) => {
        if (productId) {
          this.addProduct(productId);
        }
      });
    };

    if (this.productsList.length > 0) {
      openWithProducts(this.productsList);
    } else {
      this.productService.getProductsList().subscribe({
        next: (list) => {
          this.productsList = list || [];
          openWithProducts(this.productsList);
        }
      });
    }
  }

  addProduct(productId: number): void {
    const refs = [...(this.pricelist.addonProducts || []), { productId, preconditions: '' }];
    this.pricelist = { ...this.pricelist, addonProducts: refs };
    if (!this.isNewRecord && this.pricelist.id) {
      this.loadingProducts = true;
      this.pricelistService.updatePricelist(this.pricelist.id, this.pricelist).subscribe({
        next: (updated) => {
          this.pricelist = updated;
          this.loadingProducts = false;
          this.snack.open('Продукт добавлен', 'OK', { duration: 2000 });
        },
        error: () => {
          this.loadingProducts = false;
        }
      });
    }
  }

  removeProduct(productId: number): void {
    const refs = (this.pricelist.addonProducts || []).filter(ap => ap.productId !== productId);
    this.pricelist = { ...this.pricelist, addonProducts: refs };
    if (!this.isNewRecord && this.pricelist.id) {
      this.loadingProducts = true;
      this.pricelistService.updatePricelist(this.pricelist.id, this.pricelist).subscribe({
        next: (updated) => {
          this.pricelist = updated;
          this.loadingProducts = false;
          this.snack.open('Продукт удалён', 'OK', { duration: 2000 });
        },
        error: () => {
          this.loadingProducts = false;
        }
      });
    }
  }

  getProductName(productId: number): string {
    const p = this.productsList.find(pl => pl.id === productId);
    return p ? (p.name || p.code || String(productId)) : String(productId);
  }
}

@Component({
  selector: 'app-addon-product-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatOptionModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Добавить продукт</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" style="width: 100%;">
        <mat-label>Продукт</mat-label>
        <mat-select [(ngModel)]="selectedProductId" [disabled]="loading">
          <mat-option *ngFor="let product of products" [value]="product.id">
            {{ product.name }} ({{ product.code }})
          </mat-option>
        </mat-select>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Отмена</button>
      <button mat-raised-button color="primary" [disabled]="!selectedProductId" (click)="onSave()">
        Сохранить
      </button>
    </mat-dialog-actions>
  `
})
export class AddonProductDialogComponent {
  selectedProductId: number | null = null;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: { products: ProductList[]; loading: boolean },
    private dialogRef: MatDialogRef<AddonProductDialogComponent>
  ) {}

  get products(): ProductList[] {
    return this.data.products || [];
  }

  get loading(): boolean {
    return this.data.loading;
  }

  onSave(): void {
    if (!this.selectedProductId) return;
    this.dialogRef.close(this.selectedProductId);
  }
}
