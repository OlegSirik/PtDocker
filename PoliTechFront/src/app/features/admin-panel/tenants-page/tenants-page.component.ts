import { Component, OnInit, inject, Inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChip } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TenantsService, Tenant } from '../../../shared/services/api/tenants.service';
import { SysAdminService, SysAdmin } from '../../../shared/services/api/sys-admin.service';
import { TntAdminService, TntAdmin } from '../../../shared/services/api/tnt-admin.service';
import { AuthService } from '../../../shared/services/auth/auth.service';

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
    DatePipe
  ],
  templateUrl: './tenants-page.component.html',
  styleUrls: ['./tenants-page.component.scss']
})
export class TenantsPageComponent implements OnInit {
  private router = inject(Router);
  private tenantsService = inject(TenantsService);
  private sysAdminService = inject(SysAdminService);
  private tntAdminService = inject(TntAdminService);
  private auth = inject(AuthService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);

  // Tenants tab
  tenants: Tenant[] = [];
  tenantsDisplayedColumns: string[] = ['id', 'name', 'code', 'createdAt', 'actions'];
  tenantsSearchText = '';

  // Sys Admins tab
  sysAdmins: SysAdmin[] = [];
  sysAdminsDisplayedColumns: string[] = ['id', 'userLogin', 'userName', 'fullName', 'actions'];
  sysAdminsSearchText = '';

  // TNT Admins tab
  tntAdmins: TntAdmin[] = [];
  tntAdminsDisplayedColumns: string[] = ['id', 'userLogin', 'userName', 'fullName', 'actions'];
  tntAdminsSearchText = '';
  tntAdminsFilterTenantCode: string | null = null;
  tenantListItems: Tenant[] = [];

  loading = false;

  ngOnInit() {
    this.loadTenants();
    this.loadSysAdmins();
    this.loadTenantListItems();
  }

  // ========== TENANTS METHODS ==========

  get filteredTenants(): Tenant[] {
    const s = this.tenantsSearchText.trim().toLowerCase();
    if (!s) return this.tenants;
    return this.tenants.filter(t =>
      t.name.toLowerCase().includes(s) ||
      (t.code && t.code.toLowerCase().includes(s))
    );
  }

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

  onTenantsSearch(searchValue: string): void {
    this.tenantsSearchText = searchValue;
  }

  // ========== SYS ADMINS METHODS ==========

  get filteredSysAdmins(): SysAdmin[] {
    const s = this.sysAdminsSearchText.trim().toLowerCase();
    if (!s) return this.sysAdmins;
    return this.sysAdmins.filter(a =>
      a.userLogin.toLowerCase().includes(s) ||
      (a.userName && a.userName.toLowerCase().includes(s)) ||
      (a.fullName && a.fullName.toLowerCase().includes(s))
    );
  }

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
    const dialogRef = this.dialog.open(SysAdminDialogComponent, {
      width: '500px',
      data: { admin: null, isNew: true }
    });

    dialogRef.afterClosed().subscribe((result: SysAdmin | undefined) => {
      if (result) {
        this.sysAdminService.create(result).subscribe({
          next: () => {
            this.loadSysAdmins();
            this.snack.open('Sys Admin создан', 'OK', { duration: 2000 });
          },
          error: () => {
            this.snack.open('Ошибка при создании sys admin', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  editSysAdmin(admin: SysAdmin) {
    const dialogRef = this.dialog.open(SysAdminDialogComponent, {
      width: '500px',
      data: { admin: { ...admin }, isNew: false }
    });

    dialogRef.afterClosed().subscribe((result: SysAdmin | undefined) => {
      if (result && admin.id) {
        this.sysAdminService.update(admin.id, result).subscribe({
          next: () => {
            this.loadSysAdmins();
            this.snack.open('Sys Admin обновлен', 'OK', { duration: 2000 });
          },
          error: () => {
            this.snack.open('Ошибка при обновлении sys admin', 'OK', { duration: 2000 });
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

  onSysAdminsSearch(searchValue: string): void {
    this.sysAdminsSearchText = searchValue;
  }

  // ========== TNT ADMINS METHODS ==========

  get filteredTntAdmins(): TntAdmin[] {
    const s = this.tntAdminsSearchText.trim().toLowerCase();
    if (!s) return this.tntAdmins;
    return this.tntAdmins.filter(a =>
      a.userLogin.toLowerCase().includes(s) ||
      (a.userName && a.userName.toLowerCase().includes(s)) ||
      (a.fullName && a.fullName.toLowerCase().includes(s))
    );
  }

  loadTntAdmins(tenantCode?: string | null) {
    this.loading = true;
    const codeToUse = tenantCode || this.tntAdminsFilterTenantCode;
    
    if (!codeToUse) {
      // If no tenant code selected, load all (or empty)
      this.tntAdmins = [];
      this.loading = false;
      return;
    }

    this.tntAdminService.getByTenantCode(codeToUse).subscribe({
      next: (admins: TntAdmin[]) => {
        this.tntAdmins = admins;
        this.loading = false;
      },
      error: (error: unknown) => {
        console.error('Error loading tnt admins:', error);
        this.loading = false;
        this.snack.open('Ошибка при загрузке tnt admins', 'OK', { duration: 2000 });
      }
    });
  }

  loadTenantListItems() {
    this.tenantsService.getAll().subscribe({
      next: (tenants: Tenant[]) => {
        this.tenantListItems = tenants;
      },
      error: (error: unknown) => {
        console.error('Error loading tenant list:', error);
      }
    });
  }

  createNewTntAdmin() {
    const dialogRef = this.dialog.open(TntAdminDialogComponent, {
      width: '500px',
      data: { 
        admin: null, 
        isNew: true,
        tenants: this.tenantListItems
      }
    });

    dialogRef.afterClosed().subscribe((result: TntAdmin | undefined) => {
      if (result) {
        this.tntAdminService.create(result).subscribe({
          next: () => {
            this.loadTntAdmins();
            this.snack.open('TNT Admin создан', 'OK', { duration: 2000 });
          },
          error: () => {
            this.snack.open('Ошибка при создании tnt admin', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  editTntAdmin(admin: TntAdmin) {
    const dialogRef = this.dialog.open(TntAdminDialogComponent, {
      width: '500px',
      data: { 
        admin: { ...admin }, 
        isNew: false,
        tenants: this.tenantListItems
      }
    });

    dialogRef.afterClosed().subscribe((result: TntAdmin | undefined) => {
      if (result && admin.id) {
        this.tntAdminService.update(admin.id, result).subscribe({
          next: () => {
            this.loadTntAdmins();
            this.snack.open('TNT Admin обновлен', 'OK', { duration: 2000 });
          },
          error: () => {
            this.snack.open('Ошибка при обновлении tnt admin', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  deleteTntAdmin(admin: TntAdmin) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { message: `Удалить tnt admin "${admin.userLogin}"?` }
    });

    dialogRef.afterClosed().subscribe((result: boolean | undefined) => {
      if (result && admin.id) {
        this.tntAdminService.delete(admin.id).subscribe({
          next: () => {
            this.loadTntAdmins();
            this.snack.open('TNT Admin удален', 'OK', { duration: 2000 });
          },
          error: () => {
            this.snack.open('Ошибка при удалении', 'OK', { duration: 2000 });
          }
        });
      }
    });
  }

  onTntAdminsSearch(searchValue: string): void {
    this.tntAdminsSearchText = searchValue;
  }

  onTntAdminsFilterChange(tenantCode: string | null): void {
    this.tntAdminsFilterTenantCode = tenantCode;
    this.loadTntAdmins(tenantCode);
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

// Sys Admin Dialog Component
@Component({
  selector: 'app-sys-admin-dialog',
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Новый sys admin' : 'Редактировать sys admin' }}</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 12px;">
        <mat-label>User Login</mat-label>
        <input matInput [(ngModel)]="admin.userLogin" required [readonly]="!data.isNew" />
      </mat-form-field>

      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 12px;">
        <mat-label>User Name</mat-label>
        <input matInput [(ngModel)]="admin.userName" required />
      </mat-form-field>

      <mat-form-field appearance="outline" style="width: 100%;">
        <mat-label>Full Name</mat-label>
        <input matInput [(ngModel)]="admin.fullName" required />
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
export class SysAdminDialogComponent {
  admin: SysAdmin;

  constructor(
    public dialogRef: MatDialogRef<SysAdminDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { admin: SysAdmin | null; isNew: boolean }
  ) {
    this.admin = data.admin || {
      tenantId: 0,
      userLogin: '',
      userName: '',
      fullName: ''
    };
  }

  isValid(): boolean {
    return !!(this.admin.userLogin && this.admin.userName && this.admin.fullName);
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.isValid()) {
      this.dialogRef.close(this.admin);
    }
  }
}

// TNT Admin Dialog Component
@Component({
  selector: 'app-tnt-admin-dialog',
  imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatOptionModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Новый tnt admin' : 'Редактировать tnt admin' }}</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 12px;">
        <mat-label>Tenant</mat-label>
        <mat-select [(ngModel)]="selectedTenantCode" (ngModelChange)="onTenantChange($event)" required [disabled]="!data.isNew">
          <mat-option [value]="">Выберите tenant</mat-option>
          @for (tenant of data.tenants; track tenant.id) {
            <mat-option [value]="tenant.code">{{ tenant.name }}</mat-option>
          }
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 12px;">
        <mat-label>User Login</mat-label>
        <input matInput [(ngModel)]="admin.userLogin" required [readonly]="!data.isNew" />
      </mat-form-field>

      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 12px;">
        <mat-label>User Name</mat-label>
        <input matInput [(ngModel)]="admin.userName" required />
      </mat-form-field>

      <mat-form-field appearance="outline" style="width: 100%;">
        <mat-label>Full Name</mat-label>
        <input matInput [(ngModel)]="admin.fullName" required />
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
export class TntAdminDialogComponent {
  admin: TntAdmin;
  selectedTenantCode: string = '';

  constructor(
    public dialogRef: MatDialogRef<TntAdminDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { 
      admin: TntAdmin | null; 
      isNew: boolean;
      tenants: Tenant[];
    }
  ) {
    if (data.admin) {
      this.admin = { ...data.admin };
      this.selectedTenantCode = this.admin.tenantCode || '';
    } else {
      this.admin = {
        tenantId: 0,
        tenantCode: '',
        userLogin: '',
        userName: '',
        fullName: ''
      };
      this.selectedTenantCode = '';
    }
  }

  onTenantChange(tenantCode: string): void {
    this.selectedTenantCode = tenantCode;
    this.admin.tenantCode = tenantCode;
    const tenant = this.data.tenants.find(t => t.code === tenantCode);
    if (tenant && tenant.id) {
      this.admin.tenantId = tenant.id;
    }
  }

  isValid(): boolean {
    return !!(this.admin.tenantCode && this.admin.userLogin && this.admin.userName && this.admin.fullName);
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.isValid()) {
      this.dialogRef.close(this.admin);
    }
  }
}
