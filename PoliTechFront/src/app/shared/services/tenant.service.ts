import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, delay } from 'rxjs/operators';
import { AuthService } from './auth.service';


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

export interface Account {
  id: number;
  parentId?: number | null;
  tid: number;
  clientId: number;
  nodeType: 'CLIENT' | 'GROUP' | 'ACCOUNT' | 'SUB';
  name: string;
 // path: string;
  path?: BreadcrumbItem[];
  logins?: string[];
  admins?: LoginAccount[];
  tokens?: string[];
  products?: Product[];
}

export interface BreadcrumbItem {
  id: string;
  name: string;
}

export interface Product {
  id: number;
  name: string;
  can_read: boolean;
  can_quote: boolean;
  can_policy: boolean;
}

export interface LoginAccount {
  tid: number;
  clientId: number;
  accountId: number;
  userLogin: string;
  userRole: string;
  isDefault: boolean;
}


@Injectable({ providedIn: 'root' })
export class TenantService {
  

  constructor(private http: HttpClient, private authService: AuthService) {}

  getBaseUrl(): string {
    let url2 = this.authService.baseApiUrl.toString(); // http://localhost:8080/api/v1/VSK
    return url2;
  }  

  getAccount(id: number): Observable<Account> {
    return this.http.get<Account>(this.getBaseUrl() + `/admin/accounts/${id}`);
  }

  getChildAccounts(id: number): Observable<Account[]> {
    return this.http.get<Account[]>(this.getBaseUrl() + `/admin/accounts/${id}/accounts`);
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

  // --- Product Methods --- 

  // Mimic storage of products per account in the mock accounts array
  addProduct(tntId: number, clientId: number, accountId: number, product: Product): Observable<Product> {
    return this.http.post<Product>(this.getBaseUrl() + `/admin/accounts/${accountId}/products`, product);
  }

  updateProduct(accountId: number, product: Product): Observable<Product> {
    return this.http.put<Product>(this.getBaseUrl() + `/admin/accounts/${accountId}/products`, { accountId, product });
  }

  deleteProduct(accountId: number, productId: number): Observable<void> {
    return this.http.delete<void>(this.getBaseUrl() + `/admin/accounts/${accountId}/products/${productId}`);
  }

  getProducts(accountId: number): Observable<Product[]> {
    return this.http.get<Product[]>(this.getBaseUrl() + `/admin/accounts/${accountId}/products`);
  }

  // ACCOUNT LOGINS
  addLogin(accountId: number, login: LoginAccount): Observable<string> {
    return this.http.post<string>(this.getBaseUrl() + `/admin/accounts/${accountId}/logins`, login);
  }
  
  updateLogin(accountId: number, login: string): Observable<string> {
    return this.http.put<string>(this.getBaseUrl() + `/admin/accounts/${accountId}/logins`, login);
  }
  
  deleteLogin(accountId: number, login: string): Observable<void> {
    return this.http.delete<void>(this.getBaseUrl() + `/admin/accounts/${accountId}/logins/${login}`);
  }
  
  getLogins(accountId: number, limit: number = 10, offset: number = 0): Observable<LoginAccount[]> {
    return this.http.get<LoginAccount[]>(this.getBaseUrl() + `/admin/accounts/${accountId}/logins?limit=${limit}&offset=${offset}`);
  }
  
  // ACCOUNT TOKENS
  addToken(accountId: number, token: string): Observable<string> {
    return this.http.post<string>(this.getBaseUrl() + `/admin/accounts/${accountId}/tokens`, token);
  }
  
  updateToken(accountId: number, token: string): Observable<string> {
    return this.http.put<string>(this.getBaseUrl() + `/admin/accounts/${accountId}/tokens`, token);
  }
  
  deleteToken(accountId: number, token: string): Observable<void> {
    return this.http.delete<void>(this.getBaseUrl() + `/admin/accounts/${accountId}/tokens/${token}`);
  }
  
}

