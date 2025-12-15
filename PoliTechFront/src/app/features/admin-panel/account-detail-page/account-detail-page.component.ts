import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TenantService, Account, Product, LoginAccount } from '../../../shared/services/tenant.service';
import { AuthService } from '../../../shared/services/auth.service';
import { MatChipListbox, MatChipOption } from "@angular/material/chips";
import { MatTabsModule } from '@angular/material/tabs';
import { EMPTY, Subject } from 'rxjs';
import { filter, map, switchMap, takeUntil, tap, catchError } from 'rxjs/operators';
import { AddGroupDialogComponent } from '../components/add-group-dialog/add-group-dialog.component';
import { AddAccountDialogComponent } from '../components/add-account-dialog/add-account-dialog.component';
import { ProductDialogComponent } from '../components/product-dialog/product-dialog.component';
import { TextDialogComponent } from '../components/text-dialog/text-dialog.component';

@Component({
  selector: 'app-account-detail-page',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatChipListbox,
    MatChipOption,
    MatTabsModule
],
  templateUrl: './account-detail-page.component.html',
  styleUrls: ['./account-detail-page.component.scss']
})
export class AccountDetailPageComponent implements OnInit, OnDestroy {
  accountId: number = 0;
  account: Account | null = null;
  groups: Account[] = [];
  accounts: Account[] = [];
  subs: Account[] = [];
  loading = false;
  private destroy$ = new Subject<void>();

  groupsColumns: string[] = ['name', 'path'];
  accountsColumns: string[] = ['name', 'path'];
  subColumns: string[] = ['name', 'actions'];
  productsColumns: string[] = ['name', 'can_read', 'can_quote', 'can_policy', 'actions'];
  loginsColumns: string[] = ['login', 'actions'];
  tokensColumns: string[] = ['token', 'actions'];
  adminsColumns: string[] = ['admin', 'actions'];

  products: Product[] = [];
  logins: string[] = [];
  tokens: string[] = [];
  admins: LoginAccount[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private tenantService: TenantService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // Subscribe to route parameter changes to reload when ID changes
    this.route.paramMap.pipe(
      takeUntil(this.destroy$)
    ).subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.accountId = +id;
        this.loadAccount();
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadAccount() {
    this.loading = true;
    this.tenantService.getAccount(this.accountId).subscribe({
      next: (account: Account) => {
        this.account = account;        
    
        this.loading = false;
      },
      error: (error: unknown) => {
        console.error('Error loading account:', error);
        this.loading = false;
      }
    });
    this.loadChildAccounts();
  }

  goToAccount(id: string) {
    this.router.navigate(['/', this.authService.tenant, 'admin', 'accounts', id]);
  }
    
  loadChildAccounts() {
    if (!this.accountId) return;
    
    this.tenantService.getChildAccounts(this.accountId).subscribe({
      next: (childAccounts: Account[]) => {
        this.groups = childAccounts.filter((a: Account) => a.nodeType === 'GROUP');
        this.accounts = childAccounts.filter((a: Account) => a.nodeType === 'ACCOUNT');
        this.subs = childAccounts.filter((a: Account) => a.nodeType === 'SUB');
      },
      error: (error: unknown) => {
        console.error('Error loading child accounts:', error);
      }
    });
  }

  goUp() {
    if (this.account?.parentId) {
      this.router.navigate(['/', this.authService.tenant, 'admin', 'accounts', this.account.parentId]);
    }
  }

  addGroup() {
    const dialogRef = this.dialog.open(AddGroupDialogComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.account) {
        const newAccount: Account = {
          id: 0,
          clientId: this.account.clientId,
          tid: this.account.tid,
          parentId: this.accountId,
          nodeType: 'GROUP',
          name: result
          //path: ''
        };

        this.tenantService.createAccount(newAccount).subscribe({
          next: () => {
            this.loadChildAccounts();
          },
          error: (error: unknown) => {
            console.error('Error creating group:', error);
          }
        });
      }
    });
  }

  editGroup(group: Account) {
    if (group.id) {
      this.router.navigate(['/', this.authService.tenant, 'admin', 'accounts', group.id]);
    }
  }

  addAccount() {
    const dialogRef = this.dialog.open(AddAccountDialogComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.account) {
        const newAccount: Account = {
          id: 0,
          tid: this.account.tid,
          clientId: this.account.clientId,
          parentId: this.accountId,
          nodeType: 'ACCOUNT',
          name: result
//          path: ''
        };

        this.tenantService.createAccount(newAccount).subscribe({
          next: () => {
            this.loadChildAccounts();
          },
          error: (error: unknown) => {
            console.error('Error creating account:', error);
          }
        });
      }
    });
  }

  editAccount(account: Account) {
    if (account.id) {
      this.router.navigate(['/', this.authService.tenant, 'admin', 'accounts', account.id]);
    }
  }

  addSub() {
    const dialogRef = this.dialog.open(AddAccountDialogComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.account) {
        const newAccount: Account = {
          id: 0,
          tid: this.account.tid,
          clientId: this.account.clientId,
          parentId: this.accountId,
          nodeType: 'SUB',
          name: result
        };

        this.tenantService.createAccount(newAccount).subscribe({
          next: () => {
            this.loadChildAccounts();
          },
          error: (error: unknown) => {
            console.error('Error creating sub account:', error);
          }
        });
      }
    });
  }

  viewAccount(account: Account) {
    if (account.id) {
      this.router.navigate(['/', this.authService.tenant, 'admin', 'accounts', account.id]);
    }
  }

  // Products
  addProduct() {
    if (!this.account) return;
    const dialogRef = this.dialog.open(ProductDialogComponent, {
      width: '400px',
      data: { product: null, tid: this.account.tid }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (!this.account) return;
        this.tenantService.addProduct(this.account.tid, this.account.clientId, this.account.id, result).subscribe({
          next: (product: Product) => {
            this.products = [...this.products, product];
          },
          error: (error: unknown) => {
            console.error('Error adding product:', error);
          }
        });
        this.persistAccountExtras();
      }
    });
  }

  editProduct(product: Product) {
    if (!this.account) return;
    const dialogRef = this.dialog.open(ProductDialogComponent, {
      width: '400px',
      data: { product, tid: this.account.tid }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.products = this.products.map(p => p.id === product.id ? { ...p, ...result } : p);
        this.persistAccountExtras();
      }
    });
  }

  deleteProduct(product: Product) {
    if (confirm('Delete product?')) {
      this.products = this.products.filter(p => p.id !== product.id);
      this.persistAccountExtras();
    }
  }

  // Logins
  addLogin() {
    const dialogRef = this.dialog.open(TextDialogComponent, {
      width: '400px',
      data: { title: 'Add Login', label: 'Login' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.logins = [...this.logins, result];
        this.persistAccountExtras();
      }
    });
  }

  editLogin(login: string, index: number) {
    const dialogRef = this.dialog.open(TextDialogComponent, {
      width: '400px',
      data: { title: 'Edit Login', label: 'Login', value: login }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.logins[index] = result;
        this.logins = [...this.logins];
        this.persistAccountExtras();
      }
    });
  }

  deleteLogin(index: number) {
    if (confirm('Delete login?')) {
      this.logins.splice(index, 1);
      this.logins = [...this.logins];
      this.persistAccountExtras();
    }
  }

  // Tokens
  addToken() {
    const dialogRef = this.dialog.open(TextDialogComponent, {
      width: '400px',
      data: { title: 'Add Token', label: 'Token' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.tokens = [...this.tokens, result];
        this.persistAccountExtras();
      }
    });
  }

  editToken(token: string, index: number) {
    const dialogRef = this.dialog.open(TextDialogComponent, {
      width: '400px',
      data: { title: 'Edit Token', label: 'Token', value: token }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.tokens[index] = result;
        this.tokens = [...this.tokens];
        this.persistAccountExtras();
      }
    });
  }

  deleteToken(index: number) {
    if (confirm('Delete token?')) {
      this.tokens.splice(index, 1);
      this.tokens = [...this.tokens];
      this.persistAccountExtras();
    }
  }

  // Admins
  addAdmin() {
    const dialogRef = this.dialog.open(TextDialogComponent, {
      width: '400px',
      data: { title: 'Add Admin', label: 'Admin (email/login)' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (!this.account) return;

        const login: LoginAccount = {
          tid: this.account.tid,
          clientId: this.account.clientId,
          accountId: this.account.id,
          userLogin: result.trim(),
          userRole: 'ADMIN',
          isDefault: false
        };

        this.tenantService.addLogin(this.account.id, login).subscribe({
          next: () => {
            if (!this.account) return;
            this.tenantService.getLogins(this.account.id, 10, 0).subscribe({
              next: (logins: LoginAccount[]) => {
                this.admins = logins;
              },
              error: (error: unknown) => {
                console.error('Error getting logins:', error);
              }
            });
          }
        });
      }
    });
  }

  editAdmin(admin: string, index: number) {
    const dialogRef = this.dialog.open(TextDialogComponent, {
      width: '400px',
      data: { title: 'Edit Admin', label: 'Admin', value: admin }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.admins[index] = result;
        this.admins = [...this.admins];
        this.persistAccountExtras();
      }
    });
  }

  deleteAdmin(index: number) {
    if (confirm('Delete admin?')) {
      this.admins.splice(index, 1);
      this.admins = [...this.admins];
      this.persistAccountExtras();
    }
  }

  private persistAccountExtras() {
    if (!this.account) return;
    const updated: Account = {
      ...this.account,
      products: this.products,
      logins: this.logins,
      tokens: this.tokens,
      admins: this.admins
    };
    this.tenantService.updateAccount(updated).subscribe({
      next: (acc: Account) => { this.account = acc; },
      error: (e: unknown) => console.error('Error saving account extras', e)
    });
  }

  private getErrorMessage(error: unknown): string {
    if (!error) {
      return 'Unknown error';
    }

    if (typeof error === 'string') {
      return error;
    }

    if (error instanceof Error) {
      return error.message;
    }

    if (typeof error === 'object') {
      const errObj = error as { error?: any; message?: string };
      if (errObj?.error) {
        if (typeof errObj.error === 'string') {
          return errObj.error;
        }
        if (typeof errObj.error?.message === 'string') {
          return errObj.error.message;
        }
      }
      if (typeof errObj?.message === 'string') {
        return errObj.message;
      }
    }

    try {
      return JSON.stringify(error);
    } catch {
      return 'Unknown error';
    }
  }
}

