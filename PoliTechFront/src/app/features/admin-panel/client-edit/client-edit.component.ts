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
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDialog, MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { ClientsService, Client, ClientConfiguration, ClientProduct } from '../../../shared/services/api/clients.service';
import { TenantsService, Tenant } from '../../../shared/services/api/tenants.service';
import { AuthService } from '../../../shared/services/auth.service';
import { ProductService } from '../../../shared/services/product.service';
import { ProductList } from '../../../shared/services/products.service';

@Component({
  selector: 'app-client-edit',
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
    MatCheckboxModule,
    MatTableModule,
    MatTabsModule,
    MatDialogModule,
    MatChipsModule
  ],
  templateUrl: './client-edit.component.html',
  styleUrls: ['./client-edit.component.scss']
})
export class ClientEditComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private clientsService = inject(ClientsService);
  private tenantsService = inject(TenantsService);
  private snack = inject(MatSnackBar);
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  private productService = inject(ProductService);

  client: Client = {
    tid: 0,
    clientId: '',
    name: '',
    isDeleted: false,
    authType: 'CLIENT',
    clientConfiguration: {
      paymentGate: '',
      sendEmailAfterBuy: false,
      sendSmsAfterBuy: false
    }
  };

  originalClient: Client | null = null;
  isNewRecord = true;
  hasChanges = false;
  loading = false;
  loadingProducts = false;
  loadingProductList = false;

  clientProducts: ClientProduct[] = [];
  productsList: ProductList[] = [];
  productColumns: string[] = ['id', 'productName', 'status', 'actions'];

  ngOnInit(): void {

    const clientId = this.route.snapshot.paramMap.get('client-id') || this.route.snapshot.paramMap.get('id');
    if (clientId) {
      this.isNewRecord = false;
      this.loading = true;
      this.clientsService.getById(clientId).subscribe({
        next: (client) => {
          this.client = { 
            ...client,
            clientConfiguration: client.clientConfiguration || {
              paymentGate: '',
              sendEmailAfterBuy: false,
              sendSmsAfterBuy: false
            }
          };
          this.originalClient = { ...this.client };
          this.loading = false;
          this.updateChanges();
          this.loadProductsList();
          this.loadClientProducts();
        },
        error: (error) => {
          console.error('Error loading client:', error);
          this.snack.open('Ошибка при загрузке client', 'OK', { duration: 2000 });
          this.loading = false;
          this.router.navigate(['/', this.authService.tenant, 'admin', 'clients']);
        }
      });
    } else {
      this.isNewRecord = true;
      if (!this.client.clientConfiguration) {
        this.client.clientConfiguration = {
          paymentGate: '',
          sendEmailAfterBuy: false,
          sendSmsAfterBuy: false
        };
      }
      this.updateChanges();
      this.loadProductsList();
    }
  }

  updateChanges(): void {
    if (this.isNewRecord) {
      // For new records, check if any required fields are filled
      this.hasChanges = !!(this.client.clientId || this.client.name || this.client.tid);
    } else {
      // For existing records, compare with original
      this.hasChanges = !this.originalClient || 
        JSON.stringify(this.client) !== JSON.stringify(this.originalClient);
    }
  }

  onFieldChange(): void {
    this.updateChanges();
  }

  save(): void {
    this.client.tid = this.authService.tenantId;
    if (!this.client.clientId || !this.client.name || this.client.tid < 0 ) {
      this.snack.open('Заполните обязательные поля (Code, Name)', 'OK', { duration: 2500 });
      return;
    }

    this.loading = true;
    if (this.isNewRecord) {
      

      this.clientsService.create(this.client).subscribe({
        next: (saved) => {
          this.client = { ...saved };
          this.originalClient = { ...saved };
          this.isNewRecord = false;
          this.loading = false;
          this.updateChanges();
          this.snack.open('Сохранено', 'OK', { duration: 2000 });
          // Navigate to the edit page with the new ID
          if (saved.id) {
            this.router.navigate(['/', this.authService.tenant, 'admin', 'clients', saved.id.toString()]);
          }
          this.loadClientProducts();
        },
        error: (error) => {
          console.error('Error creating client:', error);
          this.snack.open('Ошибка при создании client', 'OK', { duration: 2000 });
          this.loading = false;
        }
      });
    } else {
      if (!this.client.id) {
        this.snack.open('ID клиента не найден', 'OK', { duration: 2000 });
        this.loading = false;
        return;
      }
      this.clientsService.update(this.client.id, this.client).subscribe({
        next: (saved) => {
          this.client = { ...saved };
          this.originalClient = { ...saved };
          this.loading = false;
          this.updateChanges();
          this.snack.open('Сохранено', 'OK', { duration: 2000 });
          this.loadClientProducts();
        },
        error: (error) => {
          console.error('Error updating client:', error);
          this.snack.open('Ошибка при обновлении client', 'OK', { duration: 2000 });
          this.loading = false;
        }
      });
    }
  }

  gotoAccount(client: Client) {
    if (client.clientAccountId) {
      this.router.navigate(['/', this.authService.tenant, 'admin', 'accounts', client.clientAccountId.toString()]);
    }
  }

  loadProductsList(): void {
    this.loadingProductList = true;
    this.productService.getProductsList().subscribe({
      next: (products) => {
        this.productsList = products;
        this.loadingProductList = false;
      },
      error: () => {
        this.snack.open('Ошибка при загрузке продуктов', 'OK', { duration: 2000 });
        this.loadingProductList = false;
      }
    });
  }

  loadClientProducts(): void {
    if (!this.client.id) {
      this.clientProducts = [];
      return;
    }

    this.loadingProducts = true;
    this.clientsService.getClientProducts(this.client.id).subscribe({
      next: (products) => {
        this.clientProducts = products;
        this.loadingProducts = false;
      },
      error: () => {
        this.snack.open('Ошибка при загрузке продуктов клиента', 'OK', { duration: 2000 });
        this.loadingProducts = false;
      }
    });
  }

  openAddProductDialog(): void {
    if (!this.client.id) {
      return;
    }

    const dialogRef = this.dialog.open(AddProductDialogComponent, {
      width: '420px',
      data: {
        products: this.productsList,
        loading: this.loadingProductList
      }
    });

    dialogRef.afterClosed().subscribe((productId: number | null) => {
      if (productId) {
        this.grantProduct(productId);
      }
    });
  }

  private grantProduct(productId: number): void {
    if (!this.client.id) {
      return;
    }

    const product = this.productsList.find(item => item.id === productId);
    if (!product) {
      return;
    }

    const payload: ClientProduct = {
      lobCode: product.lob,
      roleProductId: productId,
      productCode: product.code,
      productName: product.name,
      isDeleted: false
    };

    this.loadingProducts = true;
    this.clientsService.grantProduct(this.client.id, payload).subscribe({
      next: () => {
        this.snack.open('Продукт добавлен', 'OK', { duration: 2000 });
        this.loadClientProducts();
      },
      error: () => {
        this.snack.open('Ошибка при добавлении продукта', 'OK', { duration: 2000 });
        this.loadingProducts = false;
      }
    });
  }

  confirmDeleteProduct(product: ClientProduct): void {
    if (!this.client.id || !product.id) {
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { message: `Удалить продукт "${product.productName}"?` }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadingProducts = true;
        this.clientsService.revokeProduct(this.client.id as number, product.id as number).subscribe({
          next: () => {
            this.snack.open('Продукт удален', 'OK', { duration: 2000 });
            this.loadClientProducts();
          },
          error: () => {
            this.snack.open('Ошибка при удалении продукта', 'OK', { duration: 2000 });
            this.loadingProducts = false;
          }
        });
      }
    });
  }

  deleteProduct(product: ClientProduct): void {
    if (!this.client.id || !product.id) return;
    
    if (confirm(`Удалить продукт "${product.productName}"?`)) {
      this.loadingProducts = true;
      this.clientsService.revokeProduct(this.client.id, product.id).subscribe({
        next: () => {
          this.clientProducts = this.clientProducts.filter(p => p.id !== product.id);
          this.snack.open('Продукт удалён', 'OK', { duration: 2000 });
          this.loadingProducts = false;
        },
        error: () => {
          this.snack.open('Ошибка при удалении продукта', 'OK', { duration: 2000 });
          this.loadingProducts = false;
        }
      });
    }
  }

  getProductStatusLabel(product: ClientProduct): string {
    return product.isDeleted ? 'Suspend' : 'active';
  }
}

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

@Component({
  selector: 'app-add-product-dialog',
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
export class AddProductDialogComponent {
  selectedProductId: number | null = null;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: { products: ProductList[]; loading: boolean },
    private dialogRef: MatDialogRef<AddProductDialogComponent>
  ) {}

  get products(): ProductList[] {
    return this.data.products || [];
  }

  get loading(): boolean {
    return this.data.loading;
  }

  onSave(): void {
    if (!this.selectedProductId) {
      return;
    }
    this.dialogRef.close(this.selectedProductId);
  }
}

