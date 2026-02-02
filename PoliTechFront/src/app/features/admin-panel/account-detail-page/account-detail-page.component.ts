import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AccountService, Account, SubAccount, AccountGroup, Product, LoginAccount, AccountToken } from '../../../shared/services/account.service';
import { AuthService } from '../../../shared/services/auth.service';
import { MatChipListbox, MatChipOption } from "@angular/material/chips";
import { MatTabsModule } from '@angular/material/tabs';
import { EMPTY, Subject, forkJoin } from 'rxjs';
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
  path: Account[] = [];
  groups: AccountGroup[] = [];
  accounts: Account[] = [];
  subs: SubAccount[] = [];
  loading = false;
  private destroy$ = new Subject<void>();

  groupsColumns: string[] = ['name', 'actions'];
  accountsColumns: string[] = ['name', 'actions'];
  subColumns: string[] = ['name', 'actions'];
  productsColumns: string[] = ['roleProductId', 'roleProductName', 'canRead', 'canQuote', 'canPolicy', 'actions'];
  loginsColumns: string[] = ['login', 'actions'];
  tokensColumns: string[] = ['token', 'actions'];
  adminsColumns: string[] = ['admin', 'actions'];

  products: Product[] = [];
  logins: LoginAccount[] = [];
  tokens: AccountToken[] = [];
  admins: LoginAccount[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private accountService: AccountService,
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
    this.accountService.getAccount(this.accountId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (account: Account) => {
        this.account = account;
        this.loadChildAccounts();
      },
      error: (error: unknown) => {
        console.error('Error loading account:', error);
        this.loading = false;
      }
    });
  }

  loadChildAccounts() {
    if (!this.accountId) return;

    forkJoin({
      path: this.accountService.getPath(this.accountId),
      groups: this.accountService.getGroups(this.accountId),
      accounts: this.accountService.getAccounts(this.accountId),
      subs: this.accountService.getSubs(this.accountId),
      products: this.accountService.getProducts(this.accountId),
      logins: this.accountService.getLogins(this.accountId),
      tokens: this.accountService.getTokens(this.accountId)
    }).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: ({ path, groups, accounts, subs, products, logins, tokens }) => {
        this.path = path;
        this.groups = groups;
        this.accounts = accounts;
        this.subs = subs;
        this.products = products;
        this.logins = logins;
        this.tokens = tokens;
        this.loading = false;
      },
      error: (error: unknown) => {
        console.error('Error loading account data:', error);
        this.loading = false;
      }
    });
  }

  goToAccount(id: number) {
    this.router.navigate(['/', this.authService.tenant, 'admin', 'accounts', id]);
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

        this.accountService.createAccount(newAccount).subscribe({
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

  editGroup(group: AccountGroup) {
    if (group.id) {
      this.router.navigate(['/', this.authService.tenant, 'admin', 'accounts', group.id]);
    }
  }

  deleteGroup(group: AccountGroup) {
    if (confirm(`Удалить группу "${group.name}"?`)) {
      this.accountService.deleteAccount(group.id).subscribe({
        next: () => {
          this.groups = this.groups.filter(g => g.id !== group.id);
          this.snackBar.open('Группа удалена', 'OK', { duration: 2000 });
        },
        error: (error: unknown) => {
          console.error('Error deleting group:', error);
          this.snackBar.open('Ошибка при удалении группы', 'OK', { duration: 3000 });
        }
      });
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

        this.accountService.createAccount(newAccount).subscribe({
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

  deleteAccount(account: Account) {
    if (confirm(`Удалить аккаунт "${account.name}"?`)) {
      this.accountService.deleteAccount(account.id).subscribe({
        next: () => {
          this.accounts = this.accounts.filter(a => a.id !== account.id);
          this.snackBar.open('Аккаунт удалён', 'OK', { duration: 2000 });
        },
        error: (error: unknown) => {
          console.error('Error deleting account:', error);
          this.snackBar.open('Ошибка при удалении аккаунта', 'OK', { duration: 3000 });
        }
      });
    }
  }

  addSub() {
    const dialogRef = this.dialog.open(AddAccountDialogComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.account) {
        const newSub: SubAccount = {
          id: 0,
          tid: this.account.tid,
          clientId: this.account.clientId,
          parentId: this.accountId,
          nodeType: 'SUB',
          name: result
        };

        this.accountService.createSub(newSub).subscribe({
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

  viewAccount(account: Account | SubAccount) {
    if (account.id) {
      this.router.navigate(['/', this.authService.tenant, 'admin', 'accounts', account.id]);
    }
  }

  deleteSub(sub: SubAccount) {
    if (confirm(`Удалить субаккаунт "${sub.name}"?`)) {
      this.accountService.deleteAccount(sub.id).subscribe({
        next: () => {
          this.subs = this.subs.filter(s => s.id !== sub.id);
          this.snackBar.open('Субаккаунт удалён', 'OK', { duration: 2000 });
        },
        error: (error: unknown) => {
          console.error('Error deleting sub account:', error);
          this.snackBar.open('Ошибка при удалении субаккаунта', 'OK', { duration: 3000 });
        }
      });
    }
  }

  // Products
  addProduct() {
    if (!this.account) return;
    const dialogRef = this.dialog.open(ProductDialogComponent, {
      width: '400px',
      data: { product: null, accountId: this.accountId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.account) {
        this.accountService.addProduct(this.accountId, result).subscribe({
          next: (product: Product) => {
            this.products = [...this.products, product];
            this.snackBar.open('Продукт добавлен', 'OK', { duration: 2000 });
          },
          error: (error: unknown) => {
            console.error('Error adding product:', error);
            this.snackBar.open('Ошибка при добавлении продукта', 'OK', { duration: 3000 });
          }
        });
      }
    });
  }

  editProduct(product: Product) {
    if (!this.account) return;
    const dialogRef = this.dialog.open(ProductDialogComponent, {
      width: '400px',
      data: { product, accountId: this.accountId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.accountService.updateProduct(this.accountId, result).subscribe({
          next: (saved: Product) => {
            this.products = this.products.map(p => p.id === product.id ? saved : p);
            this.snackBar.open('Продукт обновлён', 'OK', { duration: 2000 });
          },
          error: (error: unknown) => {
            console.error('Error updating product:', error);
            this.snackBar.open('Ошибка при обновлении продукта', 'OK', { duration: 3000 });
          }
        });
      }
    });
  }

  deleteProduct(product: Product) {
    if (!product.id) return;
    if (confirm('Удалить продукт?')) {
      this.accountService.deleteProduct(this.accountId, product.id).subscribe({
        next: () => {
          this.products = this.products.filter(p => p.id !== product.id);
          this.snackBar.open('Продукт удалён', 'OK', { duration: 2000 });
        },
        error: (error: unknown) => {
          console.error('Error deleting product:', error);
          this.snackBar.open('Ошибка при удалении продукта', 'OK', { duration: 3000 });
        }
      });
    }
  }

  // Logins
  addLogin() {
    if (!this.account) return;
    const dialogRef = this.dialog.open(TextDialogComponent, {
      width: '400px',
      data: { title: 'Add Login', label: 'Login' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.account) {
        const login: LoginAccount = { userLogin: result.trim() };
        this.accountService.addLogin(this.accountId, login).subscribe({
          next: (created: LoginAccount) => {
            this.logins = [...this.logins, created];
            this.snackBar.open('Login added', 'OK', { duration: 2000 });
          },
          error: (error: unknown) => {
            console.error('Error adding login:', error);
            this.snackBar.open('Error adding login', 'OK', { duration: 3000 });
          }
        });
      }
    });
  }

  editLogin(login: LoginAccount, index: number) {
    // Logins are not editable - navigate or just view
  }

  deleteLogin(login: LoginAccount) {
    if (!login.userLogin) return;
    if (confirm(`Delete login "${login.userLogin}"?`)) {
      this.accountService.deleteLogin(this.accountId, login.userLogin).subscribe({
        next: () => {
          this.logins = this.logins.filter(l => l.userLogin !== login.userLogin);
          this.snackBar.open('Login deleted', 'OK', { duration: 2000 });
        },
        error: (error: unknown) => {
          console.error('Error deleting login:', error);
          this.snackBar.open('Error deleting login', 'OK', { duration: 3000 });
        }
      });
    }
  }

  // Tokens
  addToken() {
    if (!this.account) return;
    const dialogRef = this.dialog.open(TextDialogComponent, {
      width: '400px',
      data: { title: 'Add Token', label: 'Token' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.account) {
        this.accountService.addToken(this.accountId, result.trim()).subscribe({
          next: (created: AccountToken) => {
            this.tokens = [...this.tokens, created];
            this.snackBar.open('Token added', 'OK', { duration: 2000 });
          },
          error: (error: unknown) => {
            console.error('Error adding token:', error);
            this.snackBar.open('Error adding token', 'OK', { duration: 3000 });
          }
        });
      }
    });
  }

  editToken(token: AccountToken, index: number) {
    // Tokens are not editable - just view
  }

  deleteToken(token: AccountToken) {
    if (!token.token) return;
    if (confirm(`Delete token "${token.token}"?`)) {
      this.accountService.deleteToken(this.accountId, token.token).subscribe({
        next: () => {
          this.tokens = this.tokens.filter(t => t.token !== token.token);
          this.snackBar.open('Token deleted', 'OK', { duration: 2000 });
        },
        error: (error: unknown) => {
          console.error('Error deleting token:', error);
          this.snackBar.open('Error deleting token', 'OK', { duration: 3000 });
        }
      });
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

        this.accountService.addLogin(this.account.id, login).subscribe({
          next: () => {
            if (!this.account) return;
            this.accountService.getLogins(this.account.id).subscribe({
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
      //products: this.products,
      //logins: this.logins,
      //tokens: this.tokens,
      //admins: this.admins
    };
    this.accountService.updateAccount(updated).subscribe({
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

