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
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TenantsService, Tenant } from '../../../shared/services/api/tenants.service';
import { LoginService, Login } from '../../../shared/services/api/logins.service';
import { SysAdminService, SysAdmin } from '../../../shared/services/api/sys-admin.service';
import { TntAdminService, TntAdmin } from '../../../shared/services/api/tnt-admin.service';
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
    DatePipe
  ],
  templateUrl: './tenants-page.component.html',
  styleUrls: ['./tenants-page.component.scss']
})
export class TenantsPageComponent implements OnInit {
  private tenantsService = inject(TenantsService);
  private loginService = inject(LoginService);
  private sysAdminService = inject(SysAdminService);
  private tntAdminService = inject(TntAdminService);
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
    isDeleted: false
  };

  // Tenants tab
  tenants: Tenant[] = [];
  tenantsDisplayedColumns: string[] = ['id', 'name', 'code', 'createdAt', 'actions'];

  // Tenant list for dropdown
  tenantListItems: Tenant[] = [];

  // Sys Admins tab
  sysAdmins: SysAdmin[] = [];
  sysAdminsDisplayedColumns: string[] = ['id', 'userLogin', 'fullName', 'position', 'actions'];

  // TNT Admins tab
  tntAdmins: TntAdmin[] = [];
  tntAdminsDisplayedColumns: string[] = ['id', 'userLogin', 'fullName', 'position', 'actions'];

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
        this.isSysAdmin = user.tenantCode === 'SYS';
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
    if (this.selectedTenantCode === 'SYS') {
      this.loadSysAdmins();
    } else {
      const tenantCode = this.selectedTenantCode || this.currentTenantCode;
      if (tenantCode && tenantCode !== 'SYS') {
        this.loadTntAdmins();
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
        updatedAt: tenant.updatedAt
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
              updatedAt: foundTenant.updatedAt
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
      isDeleted: this.currentTenantData.isDeleted
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
        console.error('Error loading tenants:', error);
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
    this.sysAdminService.getAll().subscribe({
      next: (admins: SysAdmin[]) => {
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
        // Find login by userLogin to get full details
        this.loginService.getAll().subscribe({
          next: (logins: Login[]) => {
            const login = logins.find(l => l.userLogin === result);
            if (login) {
              const sysAdminData: SysAdmin = {
                tenantCode: 'SYS',
                userLogin: login.userLogin,
                fullName: login.fullName || '',
                position: login.position || ''
              };

              this.sysAdminService.create(sysAdminData).subscribe({
                next: () => {
                  this.loadSysAdmins();
                  this.snack.open('Sys Admin создан', 'OK', { duration: 2000 });
                },
                error: (error: unknown) => {
                  console.error('Error creating sys admin:', error);
                  this.snack.open('Ошибка при создании sys admin', 'OK', { duration: 2000 });
                }
              });
            } else {
              this.snack.open('Пользователь с таким логином не найден', 'OK', { duration: 2000 });
            }
          },
          error: (error: unknown) => {
            console.error('Error loading logins:', error);
            this.snack.open('Ошибка при загрузке пользователей', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  deleteSysAdmin(admin: SysAdmin) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { message: `Удалить sys admin "${admin.userLogin}"?` }
    });

    dialogRef.afterClosed().subscribe((result: boolean | undefined) => {
      if (result && admin.id) {
        this.sysAdminService.delete(admin.id).subscribe({
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
    this.tntAdminService.getByTenantCode(tenantCode).subscribe({
      next: (admins: TntAdmin[]) => {
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
        // Find login by userLogin to get full details
        this.loginService.getAll().subscribe({
          next: (logins: Login[]) => {
            const login = logins.find(l => l.userLogin === result);
            if (login) {
              const tenantCode = this.selectedTenantCode || this.currentTenantCode;
              if (!tenantCode || tenantCode === 'SYS') {
                this.snack.open('Не выбран tenant', 'OK', { duration: 2000 });
                return;
              }

              const tntAdminData: TntAdmin = {
                id: 0,
                tid: 0,
                clientId: 0,
                accountId: 0,
                tenantCode: tenantCode,
                userLogin: login.userLogin,
                fullName: login.fullName || '',
                position: login.position || ''
              };

              this.tntAdminService.create(tntAdminData).subscribe({
                next: () => {
                  this.loadTntAdmins();
                  this.snack.open('Tenant Admin создан', 'OK', { duration: 2000 });
                },
                error: (error: unknown) => {
                  console.error('Error creating tnt admin:', error);
                  this.snack.open('Ошибка при создании tenant admin', 'OK', { duration: 2000 });
                }
              });
            } else {
              this.snack.open('Пользователь с таким логином не найден', 'OK', { duration: 2000 });
            }
          },
          error: (error: unknown) => {
            console.error('Error loading logins:', error);
            this.snack.open('Ошибка при загрузке пользователей', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  deleteTntAdmin(admin: TntAdmin) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { message: `Удалить tenant admin "${admin.userLogin}"?` }
    });

    dialogRef.afterClosed().subscribe((result: boolean | undefined) => {
      if (result && admin.id) {
        this.tntAdminService.delete(admin.id).subscribe({
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
    this.loginService.getAll().subscribe({
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
      data: { login: null }
    });

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
        this.loginService.delete(login.id).subscribe({
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
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Новый tenant' : 'Редактировать tenant' }}</h2>
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
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Sys Admin</h2>
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
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Tenant Admin</h2>
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

