import { Component, OnInit, inject, Inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChip } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TenantsService, Tenant } from '../../../shared/services/api/tenants.service';
import { LoginService, Login } from '../../../shared/services/api/logins.service';
import { TenantAdminService, TenantAdmin } from '../../../shared/services/api/tenant-admins.service';
import { AuthService as RestAuthService, User } from '../../../shared/services/auth.service';
import { LoginDialogComponent } from '../components/login-dialog/login-dialog.component';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-tenants-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDialogModule,
    MatSnackBarModule,
    MatSelectModule,
    MatOptionModule,
    MatPaginatorModule,
    MatChip,
    DatePipe
  ],
  templateUrl: './tenants-page.component.html',
  styleUrls: ['./tenants-page.component.scss']
})
export class TenantsPageComponent implements OnInit {
  private tenantsService = inject(TenantsService);
  private loginService = inject(LoginService);
  private tenantAdminService = inject(TenantAdminService);
  private restAuth = inject(RestAuthService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);

  // Current user tenant info
  currentTenantCode: string | null = null;
  currentTenantName: string | null = null;
  isSysAdmin: boolean = false;
  selectedTenantCode: string | null = null;

  currentTenantData: Tenant = {
    name: '',
    code: '',
    isDeleted: false,
    authType: ''
  };

  // Tenants tab
  tenants: Tenant[] = [];
  tenantsDisplayedColumns: string[] = ['id', 'name', 'code', 'status', 'createdAt', 'actions'];

  // Tenant list for dropdown
  tenantListItems: Tenant[] = [];

  // Sys Admins tab
  sysAdmins: TenantAdmin[] = [];
  sysAdminsDisplayedColumns: string[] = ['id', 'userLogin', 'fullName', 'position', 'actions'];

  // TNT Admins tab
  tntAdmins: TenantAdmin[] = [];
  tntAdminsDisplayedColumns: string[] = ['id', 'userLogin', 'fullName', 'position', 'actions'];

  // Product Admins tab
  productAdmins: TenantAdmin[] = [];
  productAdminsDisplayedColumns: string[] = ['id', 'userLogin', 'fullName', 'position', 'actions'];

  // Logins/Users tab
  logins: Login[] = [];
  loginsDisplayedColumns: string[] = ['id', 'userLogin', 'fullName', 'position', 'tenantCode', 'actions'];
  loginsSearchText = '';
  loginsPageIndex = 0;
  loginsPageSize = 10;
  loginsPageSizeOptions = [5, 10, 25, 50];

  loading = false;

  ngOnInit() {
    // Get current user and tenant info
    this.restAuth.currentUser$.pipe(take(1)).subscribe((user: User | null) => {
      if (user) {
        this.currentTenantCode = user.tenantCode;
        this.isSysAdmin = user.tenantCode === 'sys';
        // If SYS admin, load all tenants for dropdown
        if (this.isSysAdmin) {
          this.loadTenantListItems().then(() => {
            // Set default selected tenant to current user's tenant or first one
            if (this.tenantListItems.length > 0) {
              this.selectedTenantCode = this.currentTenantCode || this.tenantListItems[0].code;
              // Update auth service tenant for API calls
              if (this.selectedTenantCode) {
                this.restAuth.tenant = this.selectedTenantCode;
              }
            }
            this.loadCurrentTenantData();
            this.loadAllData();
          });
        } else {
          // For non-SYS users, get tenant name and set auth service tenant
          if (this.currentTenantCode) {
            this.restAuth.tenant = this.currentTenantCode;
          }
          this.loadTenantListItems().then(() => {
            const tenant = this.tenantListItems.find(t => t.code === this.currentTenantCode);
            this.currentTenantName = tenant?.name || this.currentTenantCode || '';
            this.loadCurrentTenantData();
            this.loadAllData();
          });
        }
      } else {
        this.loadAllData();
      }
    });
  }

  private loadAllData() {
    this.loadTenants();
    this.loadLogins();
    if (this.selectedTenantCode === 'sys') {
      this.loadSysAdmins();
    } else {
      const tenantCode = this.selectedTenantCode || this.currentTenantCode;
      if (tenantCode && tenantCode !== 'sys') {
        this.loadTntAdmins();
        this.loadProductAdmins();
      }
    }
  }

  getCurrentTenantCode(): string | null {
    return this.selectedTenantCode || this.currentTenantCode;
  }

  onTenantFilterChange(tenantCode: string | null): void {
    this.selectedTenantCode = tenantCode;
    // Update auth service tenant for API calls
    if (tenantCode) {
      this.restAuth.tenant = tenantCode;
    }
    // Load current tenant data for editing
    this.loadCurrentTenantData();
    // Refresh all data when tenant filter changes
    this.loadAllData();
  }

  // ========== CURRENT TENANT METHODS ==========

  loadCurrentTenantData() {
    const tenantCodeToLoad = this.selectedTenantCode || this.currentTenantCode;
    if (!tenantCodeToLoad) {
      return;
    }

    const tenant = this.tenantListItems.find(t => t.code === tenantCodeToLoad);
    if (tenant) {
      this.currentTenantData = {
        id: tenant.id,
        name: tenant.name,
        code: tenant.code,
        isDeleted: tenant.isDeleted || false,
        createdAt: tenant.createdAt,
        updatedAt: tenant.updatedAt,
        authType: tenant.authType || ''
      };
    } else {
      // If tenant not found in list, try to load it
      this.tenantsService.getAll().subscribe({
        next: (tenants: Tenant[]) => {
          const foundTenant = tenants.find(t => t.code === tenantCodeToLoad);
          if (foundTenant) {
            this.currentTenantData = {
              id: foundTenant.id,
              name: foundTenant.name,
              code: foundTenant.code,
              isDeleted: foundTenant.isDeleted || false,
              createdAt: foundTenant.createdAt,
              updatedAt: foundTenant.updatedAt,
              authType: foundTenant.authType || ''
            };
          }
        },
        error: (error: unknown) => {
          console.error('Error loading current tenant data:', error);
        }
      });
    }
  }

  isTenantDataValid(): boolean {
    return !!(this.currentTenantData.name && this.currentTenantData.code);
  }

  saveCurrentTenant() {
    if (!this.isTenantDataValid() || !this.currentTenantData.id) {
      this.snack.open('Не все поля заполнены', 'OK', { duration: 2000 });
      return;
    }

    this.loading = true;
    this.tenantsService.update(this.currentTenantData.id, {
      name: this.currentTenantData.name,
      code: this.currentTenantData.code,
      isDeleted: this.currentTenantData.isDeleted,
      authType: this.currentTenantData.authType
    }).subscribe({
      next: () => {
        this.loading = false;
        this.snack.open('Tenant сохранен', 'OK', { duration: 2000 });
        // Reload tenant list and current tenant data
        this.loadTenantListItems().then(() => {
          this.loadCurrentTenantData();
          this.loadTenants();
        });
      },
      error: (error: unknown) => {
        console.error('Error saving tenant:', error);
        this.loading = false;
        this.snack.open('Ошибка при сохранении tenant', 'OK', { duration: 2000 });
      }
    });
  }

  // ========== TENANTS METHODS ==========

  loadTenants() {
    this.loading = true;
    this.tenantsService.getAll().subscribe({
      next: (tenants: Tenant[]) => {
        this.tenants = tenants;
        this.loading = false;
      },
      error: (error: unknown) => {
        this.loading = false;
        this.snack.open('Ошибка при загрузке tenants', 'OK', { duration: 2000 });
      }
    });
  }

  createNewTenant() {
    const dialogRef = this.dialog.open(TenantDialogComponent, {
      width: '500px',
      data: { tenant: null, isNew: true }
    });

    dialogRef.afterClosed().subscribe((result: Tenant | undefined) => {
      if (result) {
        this.tenantsService.create(result).subscribe({
          next: () => {
            this.loadTenants();
            this.snack.open('Tenant создан', 'OK', { duration: 2000 });
          },
          error: () => {
            this.snack.open('Ошибка при создании tenant', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  selectTenant(tenant: Tenant) {
    // Set the selected tenant
    this.selectedTenantCode = tenant.code;
    // Update auth service tenant for API calls
    //if (tenant.code) {
    //  this.restAuth.tenant = tenant.code;
    //}
    // Update current tenant name
    this.currentTenantName = tenant.name;
    // Load current tenant data for editing
    this.loadCurrentTenantData();
    // Reload all data for the selected tenant
    this.loadAllData();
  }

  editTenant(tenant: Tenant) {
    const dialogRef = this.dialog.open(TenantDialogComponent, {
      width: '500px',
      data: { tenant: { ...tenant }, isNew: false }
    });

    dialogRef.afterClosed().subscribe((result: Tenant | undefined) => {
      if (result && tenant.id) {
        this.tenantsService.update(tenant.id, result).subscribe({
          next: () => {
            this.loadTenants();
            this.snack.open('Tenant обновлен', 'OK', { duration: 2000 });
            // If the edited tenant is the current one, reload its data
            if (this.selectedTenantCode === tenant.code || this.currentTenantCode === tenant.code) {
              this.loadCurrentTenantData();
              this.loadAllData();
            }
          },
          error: () => {
            this.snack.open('Ошибка при обновлении tenant', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  deleteTenant(tenant: Tenant) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { message: `Удалить tenant "${tenant.name}"?` }
    });

    dialogRef.afterClosed().subscribe((result: boolean | undefined) => {
      if (result && tenant.id) {
        this.tenantsService.delete(tenant.id).subscribe({
          next: () => {
            this.loadTenants();
            this.snack.open('Tenant удален', 'OK', { duration: 2000 });
          },
          error: () => {
            this.snack.open('Ошибка при удалении', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  loadTenantListItems(): Promise<void> {
    return new Promise((resolve) => {
      this.tenantsService.getAll().subscribe({
        next: (tenants: Tenant[]) => {
          this.tenantListItems = tenants;
          resolve();
        },
        error: (error: unknown) => {
          console.error('Error loading tenant list:', error);
          resolve();
        }
      });
    });
  }

  // ========== SYS ADMINS METHODS ==========

  loadSysAdmins() {
    this.loading = true;
    this.tenantAdminService.getSysADmins(this.selectedTenantCode || this.currentTenantCode || 'NULL').subscribe({
      next: (admins: TenantAdmin[]) => {
        this.sysAdmins = admins;
        this.loading = false;
      },
      error: (error: unknown) => {
        console.error('Error loading sys admins:', error);
        this.loading = false;
        this.snack.open('Ошибка при загрузке sys admins', 'OK', { duration: 2000 });
      }
    });
  }

  createNewSysAdmin() {
    const dialogRef = this.dialog.open(SysAdminAddDialogComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe((result: string | undefined) => {
      if (result) {
        const tenantCode = this.selectedTenantCode || this.currentTenantCode;
        // Find login by userLogin to get full details

              const sysAdminData: TenantAdmin = {
                tenantCode: 'sys',
                userLogin: result,
                role: 'SYS_ADMIN',
                authClientId: 'sys'
              };

              this.tenantAdminService.create(sysAdminData, {'X-Imp-Tenant': tenantCode || 'demo'}).subscribe({
                next: () => {
                  this.loadSysAdmins();
                  this.snack.open('Sys Admin создан', 'OK', { duration: 2000 });
                },
                error: (error: unknown) => {
                  console.error('Error creating sys admin:', error);
                  this.snack.open('Ошибка при создании sys admin', 'OK', { duration: 2000 });
                }
              });
      }
    });
  }

  deleteSysAdmin(admin: TenantAdmin) {
    const tenantCode = this.selectedTenantCode || this.currentTenantCode;
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { message: `Удалить sys admin "${admin.userLogin}"?` }
    });

    dialogRef.afterClosed().subscribe((result: boolean | undefined) => {
      if (result && admin.id) {
        this.tenantAdminService.delete(admin.id, {'X-Imp-Tenant': tenantCode || 'demo'}).subscribe({
          next: () => {
            this.loadSysAdmins();
            this.snack.open('Sys Admin удален', 'OK', { duration: 2000 });
          },
          error: () => {
            this.snack.open('Ошибка при удалении', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  // ========== TNT ADMINS METHODS ==========

  loadTntAdmins() {
    const tenantCode = this.selectedTenantCode || this.currentTenantCode;
    if (!tenantCode || tenantCode === 'SYS') {
      this.tntAdmins = [];
      return;
    }

    this.loading = true;
    this.tenantAdminService.getTntADmins(tenantCode || 'NULL').subscribe({
      next: (admins: TenantAdmin[]) => {
        this.tntAdmins = admins;
        this.loading = false;
      },
      error: (error: unknown) => {
        console.error('Error loading tnt admins:', error);
        this.loading = false;
        this.snack.open('Ошибка при загрузке tenant admins', 'OK', { duration: 2000 });
      }
    });
  }

  createNewTntAdmin() {
    const dialogRef = this.dialog.open(TntAdminAddDialogComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe((result: string | undefined) => {
      if (result) {
        const tenantCode = this.selectedTenantCode || this.currentTenantCode;
        // Find login by userLogin from the list of logins
        //const login = this.logins.find(l => l.userLogin === result);

        //if (login) {
          const tntAdminData: TenantAdmin = {
            tenantCode: tenantCode || '',
            userLogin: result,
            role: 'TNT_ADMIN',
            authClientId: 'sys'
          };

          this.tenantAdminService.create(tntAdminData, {'X-Imp-Tenant': tenantCode || 'demo'}).subscribe({
            next: () => {
              this.loadTntAdmins();
              this.snack.open('Tenant Admin создан', 'OK', { duration: 2000 });
            },
            error: (error: unknown) => {
              console.error('Error creating tnt admin:', error);
              this.snack.open('Ошибка при создании tenant admin', 'OK', { duration: 2000 });
            }
          });
      //}
    }
  });
}

  deleteTntAdmin(admin: TenantAdmin) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { message: `Удалить tenant admin "${admin.userLogin}"?` }
    });

    dialogRef.afterClosed().subscribe((result: boolean | undefined) => {
      if (result && admin.id) {
        const tenantCode = this.selectedTenantCode || this.currentTenantCode;
        this.tenantAdminService.delete(admin.id, {'X-Imp-Tenant': tenantCode || 'demo'}).subscribe({
          next: () => {
            this.loadTntAdmins();
            this.snack.open('Tenant Admin удален', 'OK', { duration: 2000 });
          },
          error: () => {
            this.snack.open('Ошибка при удалении', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  // ========== PRODUCT ADMINS METHODS ==========

  loadProductAdmins() {
    const tenantCode = this.selectedTenantCode || this.currentTenantCode;
    if (!tenantCode || tenantCode === 'sys') {
      this.productAdmins = [];
      return;
    }

    this.loading = true;
    this.tenantAdminService.getProductAdmins(tenantCode || 'NULL').subscribe({
      next: (admins: TenantAdmin[]) => {
        this.productAdmins = admins;
        this.loading = false;
      },
      error: (error: unknown) => {
        console.error('Error loading product admins:', error);
        this.loading = false;
        this.snack.open('Ошибка при загрузке product admins', 'OK', { duration: 2000 });
      }
    });
  }

  createNewProductAdmin() {
    const dialogRef = this.dialog.open(ProductAdminAddDialogComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe((result: string | undefined) => {
      if (result) {
        const tenantCode = this.selectedTenantCode || this.currentTenantCode;
        // Find login by userLogin from the list of logins
        //const login = this.logins.find(l => l.userLogin === result);

        //if (login) {
          const productAdminData: TenantAdmin = {
            tenantCode: tenantCode || '',
            userLogin: result,
            role: 'PRODUCT_ADMIN',
            authClientId: 'sys'
          };

          this.tenantAdminService.create(productAdminData, {'X-Imp-Tenant': tenantCode || 'demo'}).subscribe({
            next: () => {
              this.loadProductAdmins();
              this.snack.open('Product Admin создан', 'OK', { duration: 2000 });
            },
            error: (error: unknown) => {
              console.error('Error creating product admin:', error);
              this.snack.open('Ошибка при создании product admin', 'OK', { duration: 2000 });
            }
          });
        //}
      }
    });
  }

  editProductAdmin(admin: TenantAdmin) {
    const dialogRef = this.dialog.open(ProductAdminAddDialogComponent, {
      width: '400px',
      data: { admin: admin }
    });

    dialogRef.afterClosed().subscribe((result: string | undefined) => {
      if (result && admin.id) {
        const tenantCode = this.selectedTenantCode || this.currentTenantCode;
        const productAdminData: TenantAdmin = {
          tenantCode: tenantCode || '',
          userLogin: result,
          role: 'PRODUCT_ADMIN',
        };

        this.tenantAdminService.update(admin.id, productAdminData).subscribe({
          next: () => {
            this.loadProductAdmins();
            this.snack.open('Product Admin обновлен', 'OK', { duration: 2000 });
          },
          error: (error: unknown) => {
            console.error('Error updating product admin:', error);
            this.snack.open('Ошибка при обновлении product admin', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  deleteProductAdmin(admin: TenantAdmin) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { message: `Удалить product admin "${admin.userLogin}"?` }
    });

    dialogRef.afterClosed().subscribe((result: boolean | undefined) => {
      if (result && admin.id) {
        const tenantCode = this.selectedTenantCode || this.currentTenantCode;
        this.tenantAdminService.delete(admin.id, {'X-Imp-Tenant': tenantCode || ''}).subscribe({
          next: () => {
            this.loadProductAdmins();
            this.snack.open('Product Admin удален', 'OK', { duration: 2000 });
          },
          error: () => {
            this.snack.open('Ошибка при удалении', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  // ========== LOGINS/USERS METHODS ==========

  get filteredLogins(): Login[] {
    const s = this.loginsSearchText.trim().toLowerCase();
    if (!s) return this.logins;
    return this.logins.filter(l =>
      (l.userLogin && l.userLogin.toLowerCase().includes(s)) ||
      (l.fullName && l.fullName.toLowerCase().includes(s)) ||
      (l.position && l.position.toLowerCase().includes(s)) ||
      (l.tenantCode && l.tenantCode.toString().toLowerCase().includes(s))
    );
  }

  get paginatedLogins(): Login[] {
    const startIndex = this.loginsPageIndex * this.loginsPageSize;
    return this.filteredLogins.slice(startIndex, startIndex + this.loginsPageSize);
  }

  loadLogins() {
    this.loading = true;
    this.loginService.getAll( {'X-Imp-Tenant': this.selectedTenantCode || this.currentTenantCode || 'NULL' } ).subscribe({
      next: (logins: Login[]) => {
        this.logins = logins;
        this.loading = false;
      },
      error: (error: unknown) => {
        console.error('Error loading logins:', error);
        this.loading = false;
        this.snack.open('Ошибка при загрузке пользователей', 'OK', { duration: 2000 });
      }
    });
  }

  createNewLogin() {


    const dialogRef = this.dialog.open(LoginDialogComponent, {
      width: '600px',
      data: { login: null, tenantCode: this.selectedTenantCode }}
    );

    dialogRef.afterClosed().subscribe((result: { success: boolean; action: string; login?: Login } | undefined) => {
      if (result && result.success) {
        this.loadLogins();
        if (result.action === 'create') {
          this.snack.open('Пользователь создан', 'OK', { duration: 2000 });
        } else if (result.action === 'update') {
          this.snack.open('Пользователь обновлен', 'OK', { duration: 2000 });
        } else if (result.action === 'delete') {
          this.snack.open('Пользователь удален', 'OK', { duration: 2000 });
        }
      } else if (result && !result.success) {
        this.snack.open('Ошибка при выполнении операции', 'OK', { duration: 2000 });
      }
    });
  }

  editLogin(login: Login) {
    const dialogRef = this.dialog.open(LoginDialogComponent, {
      width: '600px',
      data: { login: login }
    });

    dialogRef.afterClosed().subscribe((result: { success: boolean; action: string } | undefined) => {
      if (result && result.success) {
        this.loadLogins();
        if (result.action === 'update') {
          this.snack.open('Пользователь обновлен', 'OK', { duration: 2000 });
        } else if (result.action === 'delete') {
          this.snack.open('Пользователь удален', 'OK', { duration: 2000 });
        }
      } else if (result && !result.success) {
        this.snack.open('Ошибка при выполнении операции', 'OK', { duration: 2000 });
      }
    });
  }

  deleteLogin(login: Login) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { message: `Удалить пользователя "${login.userLogin}"?` }
    });

    dialogRef.afterClosed().subscribe((result: boolean | undefined) => {
      if (result && login.id) {
        const tenantCode = this.selectedTenantCode || this.currentTenantCode;
        this.loginService.delete(login.id, {'X-Imp-Tenant': tenantCode || 'demo'}).subscribe({
          next: () => {
            this.loadLogins();
            this.snack.open('Пользователь удален', 'OK', { duration: 2000 });
          },
          error: () => {
            this.snack.open('Ошибка при удалении', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  onLoginsSearch(searchValue: string): void {
    this.loginsSearchText = searchValue;
    this.loginsPageIndex = 0; // Reset to first page when searching
  }

  onLoginsPageChange(event: PageEvent): void {
    this.loginsPageIndex = event.pageIndex;
    this.loginsPageSize = event.pageSize;
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

// Tenant Dialog Component
@Component({
  selector: 'app-tenant-dialog',
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDividerModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Новый tenant' : 'Редактировать tenant' }}</h2>
    <mat-divider></mat-divider>
    <mat-dialog-content>
      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 12px;">
        <mat-label>Name</mat-label>
        <input matInput [(ngModel)]="tenant.name" required />
      </mat-form-field>

      <mat-form-field appearance="outline" style="width: 100%;">
        <mat-label>Code</mat-label>
        <input matInput [(ngModel)]="tenant.code" required />
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Отмена</button>
      <button mat-raised-button color="primary" [disabled]="!isValid()" (click)="onSave()">
        {{ data.isNew ? 'Создать' : 'Сохранить' }}
      </button>
    </mat-dialog-actions>
  `
})
export class TenantDialogComponent {
  tenant: Tenant;

  constructor(
    public dialogRef: MatDialogRef<TenantDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { tenant: Tenant | null; isNew: boolean }
  ) {
    this.tenant = data.tenant || {
      name: '',
      code: '',
      isDeleted: false
    };
  }

  isValid(): boolean {
    return !!(this.tenant.name && this.tenant.code);
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.isValid()) {
      this.dialogRef.close(this.tenant);
    }
  }
}

// Sys Admin Add Dialog Component
@Component({
  selector: 'app-sys-admin-add-dialog',
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDividerModule
  ],
  template: `
    <h2 mat-dialog-title>Sys Admin</h2>
    <mat-divider></mat-divider>
    <mat-dialog-content>
      <mat-form-field appearance="outline" style="width: 100%;">
        <mat-label>Login</mat-label>
        <input matInput [(ngModel)]="userLogin" required>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" [disabled]="!userLogin" (click)="onAdd()">Add</button>
    </mat-dialog-actions>
  `
})
export class SysAdminAddDialogComponent {
  userLogin: string = '';

  constructor(
    public dialogRef: MatDialogRef<SysAdminAddDialogComponent>
  ) {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onAdd(): void {
    if (this.userLogin) {
      this.dialogRef.close(this.userLogin);
    }
  }
}

// TNT Admin Add Dialog Component
@Component({
  selector: 'app-tnt-admin-add-dialog',
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDividerModule
  ],
  template: `
    <h2 mat-dialog-title>Tenant Admin</h2>
    <mat-divider></mat-divider>
    <mat-dialog-content>
      <mat-form-field appearance="outline" style="width: 100%;">
        <mat-label>Login</mat-label>
        <input matInput [(ngModel)]="userLogin" required>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" [disabled]="!userLogin" (click)="onAdd()">Add</button>
    </mat-dialog-actions>
  `
})
export class TntAdminAddDialogComponent {
  userLogin: string = '';

  constructor(
    public dialogRef: MatDialogRef<TntAdminAddDialogComponent>
  ) {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onAdd(): void {
    if (this.userLogin) {
      this.dialogRef.close(this.userLogin);
    }
  }
}

// Product Admin Add Dialog Component
@Component({
  selector: 'app-product-admin-add-dialog',
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDividerModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data?.admin ? 'Редактировать Product Admin' : 'Product Admin' }}</h2>
    <mat-divider></mat-divider>
    <mat-dialog-content>
      <mat-form-field appearance="outline" style="width: 100%;">
        <mat-label>Login</mat-label>
        <input matInput [(ngModel)]="userLogin" required>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Отмена</button>
      <button mat-raised-button color="primary" [disabled]="!userLogin" (click)="onSave()">
        {{ data?.admin ? 'Сохранить' : 'Добавить' }}
      </button>
    </mat-dialog-actions>
  `
})
export class ProductAdminAddDialogComponent {
  userLogin: string = '';

  constructor(
    public dialogRef: MatDialogRef<ProductAdminAddDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { admin?: TenantAdmin } | null
  ) {
    if (data?.admin) {
      this.userLogin = data.admin.userLogin || '';
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.userLogin) {
      this.dialogRef.close(this.userLogin);
    }
  }
}

