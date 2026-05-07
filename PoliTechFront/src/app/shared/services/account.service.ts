import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, delay } from 'rxjs/operators';
import { AuthService } from './auth.service';

/*
export interface Tenant {
  id?: number;
  code: string;
  name: string;
  description: string;
  trusted_email: string;
  createdAt?: Date | string;
  updateAt?: Date | string;
  status: 'ACTIVE' | 'DELETED' | 'SUSPENDED';
}

export interface Client {
  id?: number;
  tid: number;
  code: string;
  name: string;
  description: string;
  trusted_email: string;
  createdAt?: Date | string;
  updateAt?: Date | string;
  status: 'ACTIVE' | 'DELETED' | 'SUSPENDED';
}


export interface TenantProduct {
  id: number;
  name: string;
}

export interface ClientProduct {
  id: number;
  name: string;
  isSendSms: boolean;
  isSendEmail: boolean;
}
*/

export interface Account {
  id: number;
  parentId?: number | null;
  tid: number;
  clientId: number;
  nodeType: string
  name: string;
 // path: string;
  //path?: BreadcrumbItem[];
  //logins?: string[];
  //admins?: LoginAccount[];
  //tokens?: string[];
  //products?: Product[];
}

export interface AccountGroup {
  id: number;
  parentId?: number | null;
  tid: number;
  clientId: number;
  nodeType: string
  name: string;
 // path: string;
  //path?: BreadcrumbItem[];
  //logins?: string[];
  //admins?: LoginAccount[];
  //tokens?: string[];
  //products?: Product[];
}

export interface SubAccount {
  id: number;
  parentId?: number | null;
  tid: number;
  clientId: number;
  nodeType: string
  name: string;
 // path: string;
  //path?: BreadcrumbItem[];
  //logins?: string[];
  //admins?: LoginAccount[];
  //tokens?: string[];
  //products?: Product[];
}

export interface BreadcrumbItem {
  id: string;
  name: string;
}

export interface Product {
  id?: number;
  accountId: number;
  roleProductId: number;
  roleProductName: string;
  roleAccountId: number;
  isDeleted: boolean;
  canRead: boolean;
  canQuote: boolean;
  canPolicy: boolean;
  canAddendum: boolean;
  canCancel: boolean;
  canProlongate: boolean;

}

export interface LoginAccount {
  id?: number;
  tid?: number;
  clientId?: number;
  accountId?: number;
  userLogin: string;
  userRole?: string;
  isDefault?: boolean;
}

export interface AccountToken {
  id?: number;
  tid?: number;
  clientId?: number;
  accountId?: number;
  token: string;
}

export interface AccountMemberRequest {
  accountId: number;
  role: string;
  userLogin: string;
  fullName: string;
  position: string;
  password: string;
}

export interface AccountMember {
  id?: number;
  role?: string;
  userLogin: string;
  fullName?: string;
  position?: string;
}


@Injectable({ providedIn: 'root' })
export class AccountService {
  

  constructor(private http: HttpClient, private authService: AuthService) {}

  getBaseUrl(): string {
    let url2 = this.authService.baseApiUrl.toString(); // http://localhost:8080/api/v1/VSK
    return url2;
  }  

  getAccount(id: number): Observable<Account> {
    return this.http.get<Account>(this.getBaseUrl() + `/admin/accounts/${id}`);
  }

  getAccounts(id: number): Observable<Account[]> {
    return this.http.get<Account[]>(this.getBaseUrl() + `/admin/accounts/${id}/children?nodeType=ACCOUNT`);
  }

  createAccount(account: Account): Observable<Account> {
    return this.http.post<Account>(this.getBaseUrl() + `/admin/accounts/${account.parentId}/accounts`, account);
  }

  updateAccount(account: Account): Observable<Account> {
    return this.http.put<Account>(this.getBaseUrl() + `/admin/accounts/${account.id}`, account);
  }

  deleteAccount(id: number): Observable<void> {
    return this.http.delete<void>(this.getBaseUrl() + `/admin/accounts/${id}`);
  }

  // groups

  getGroups(parentId: number): Observable<AccountGroup[]> {
    return this.http.get<AccountGroup[]>(this.getBaseUrl() + `/admin/accounts/${parentId}/children?nodeType=GROUP`);
  }

  createGroup(group: Account): Observable<Account> {
    return this.http.post<Account>(this.getBaseUrl() + `/admin/accounts/${group.parentId}/children`, group);
  }
/*
  updateGroup(group: Account): Observable<Account> {
    return this.http.put<Account>(this.getBaseUrl() + `/admin/accounts/${group.id}`, group);
  }

  deleteGroup(id: number): Observable<void> {
    return this.http.delete<void>(this.getBaseUrl() + `/admin/accounts/${id}`);
  }
*/
  // groups

  getSubs(parentId: number): Observable<SubAccount[]> {
    return this.http.get<SubAccount[]>(this.getBaseUrl() + `/admin/accounts/${parentId}/children?nodeType=SUB`);
  }

  createSub(sub: SubAccount): Observable<SubAccount> {
    return this.http.post<SubAccount>(this.getBaseUrl() + `/admin/accounts/${sub.parentId}/subaccounts`, sub);
  }
/*
  updateGroup(group: Account): Observable<Account> {
    return this.http.put<Account>(this.getBaseUrl() + `/admin/accounts/${group.id}`, group);
  }

  deleteGroup(id: number): Observable<void> {
    return this.http.delete<void>(this.getBaseUrl() + `/admin/accounts/${id}`);
  }
*/

// --- Product Methods --- 

  // Mimic storage of products per account in the mock accounts array
  addProduct(accountId: number, product: Product): Observable<Product> {
    return this.http.post<Product>(this.getBaseUrl() + `/admin/accounts/${accountId}/products`, product);
  }

  updateProduct(accountId: number, product: Product): Observable<Product> {
    return this.http.put<Product>(this.getBaseUrl() + `/admin/accounts/${accountId}/products`, product);
  }

  deleteProduct(accountId: number, productId: number): Observable<void> {
    return this.http.delete<void>(this.getBaseUrl() + `/admin/accounts/${accountId}/products/${productId}`);
  }

  getProducts(accountId: number): Observable<Product[]> {
    return this.http.get<Product[]>(this.getBaseUrl() + `/admin/accounts/${accountId}/products`);
  }

  // ACCOUNT LOGINS
  getLogins(accountId: number): Observable<LoginAccount[]> {
    return this.http.get<LoginAccount[]>(this.getBaseUrl() + `/admin/accounts/${accountId}/logins`);
  }

  addLogin(accountId: number, login: LoginAccount): Observable<LoginAccount> {
    return this.http.post<LoginAccount>(this.getBaseUrl() + `/admin/accounts/${accountId}/logins`, login);
  }
  
  deleteLogin(accountId: number, userLogin: string): Observable<void> {
    return this.http.delete<void>(this.getBaseUrl() + `/admin/accounts/${accountId}/logins/${userLogin}`);
  }
  
  // ACCOUNT TOKENS
  getTokens(accountId: number): Observable<AccountToken[]> {
    return this.http.get<AccountToken[]>(this.getBaseUrl() + `/admin/accounts/${accountId}/children?nodeType=TOKEN`);
  }

  addToken(accountId: number, token: string): Observable<AccountToken> {
    return this.http.post<AccountToken>(this.getBaseUrl() + `/admin/accounts/${accountId}/tokens`, { token });
  }
  
  deleteToken(accountId: number, token: string): Observable<void> {
    return this.http.delete<void>(this.getBaseUrl() + `/admin/accounts/${accountId}/tokens/${token}`);
  }

  // ACCOUNT MEMBERS (admins)
  addMember(accountId: number, role: string, userLogin: string, fullName: string, position: string, password: string): Observable<any> {
    const body: AccountMemberRequest = { accountId, role, userLogin, fullName, position, password };
    return this.http.post<any>(this.getBaseUrl() + `/admin/accounts/${accountId}/members`, body);
  }

  getMembers(accountId: number, role?: string): Observable<AccountMember[]> {
    const query = role ? `?role=${encodeURIComponent(role)}` : '';
    return this.http
      .get<AccountMember[]>(this.getBaseUrl() + `/admin/accounts/${accountId}/members${query}`)
      .pipe(
        catchError((_error: HttpErrorResponse) => of([]))
      );
  }
  
  //
  getPath(accountId: number): Observable<Account[]> {
    return this.http.get<Account[]>(this.getBaseUrl() + `/admin/accounts/${accountId}/path`);
  }
}

