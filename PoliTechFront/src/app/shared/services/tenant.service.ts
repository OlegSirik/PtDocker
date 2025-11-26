import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, delay } from 'rxjs/operators';

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

const BASE_URL = 'http://localhost:8080/acc';
const TENANT_ID = 38;
const CLIENT_ID = 52;
// Mock data
const MOCK_TENANTS: Tenant[] = [
  {
    id: 1,
    code: 'TENANT001',
    name: 'Acme Corporation',
    description: 'Main tenant for Acme Corporation',
    trusted_email: 'admin@acme.com',
    createdAt: new Date('2024-01-15'),
    updateAt: new Date('2024-01-20'),
    status: 'ACTIVE'
  },
  {
    id: 2,
    code: 'TENANT002',
    name: 'TechStart Inc',
    description: 'Technology startup company',
    trusted_email: 'info@techstart.com',
    createdAt: new Date('2024-02-10'),
    updateAt: new Date('2024-02-15'),
    status: 'ACTIVE'
  },
  {
    id: 3,
    code: 'TENANT003',
    name: 'Global Solutions',
    description: 'International business solutions provider',
    trusted_email: 'contact@globalsolutions.com',
    createdAt: new Date('2024-03-05'),
    updateAt: new Date('2024-03-10'),
    status: 'SUSPENDED'
  },
  {
    id: 4,
    code: 'TENANT004',
    name: 'Local Business',
    description: 'Small local business',
    trusted_email: 'owner@localbiz.com',
    createdAt: new Date('2024-04-01'),
    updateAt: new Date('2024-04-01'),
    status: 'ACTIVE'
  }
];

@Injectable({ providedIn: 'root' })
export class TenantService {
  private mockData: Tenant[] = [...MOCK_TENANTS];

  constructor(private http: HttpClient) {}

  getTenant(id: number): Observable<Tenant> {
    return this.http.get<Tenant>(`${BASE_URL}/tnts/${id}`).pipe(
      catchError((error: HttpErrorResponse) => {
        // If API doesn't exist, return mock data
        if (error.status === 404 || error.status === 0) {
          const tenant = this.mockData.find(t => t.id === id);
          if (tenant) {
            return of({ ...tenant }).pipe(delay(300));
          }
          return throwError(() => new Error(`Tenant with id ${id} not found`));
        }
        return throwError(() => error);
      })
    );
  }

  getTenants(): Observable<Tenant[]> {
    //return of([...this.mockData]).pipe(delay(300));
    return this.http.get<Tenant[]>(`${BASE_URL}/tnts`).pipe(
      catchError((error: HttpErrorResponse) => {
        // If API doesn't exist, return mock data
        if (error.status === 404 || error.status === 0) {
          return of([...this.mockData]).pipe(delay(300));
        }
        return throwError(() => error);
      })
    );
  }

  createTenant(tenant: Tenant): Observable<Tenant> {
    return this.http.post<Tenant>(`${BASE_URL}/tnts`, tenant).pipe(
      catchError((error: HttpErrorResponse) => {
        // If API doesn't exist, create mock data
        if (error.status === 404 || error.status === 0) {
          const newTenant: Tenant = {
            ...tenant,
            id: Math.max(...this.mockData.map(t => t.id || 0)) + 1,
            createdAt: new Date(),
            updateAt: new Date()
          };
          this.mockData.push(newTenant);
          return of(newTenant).pipe(delay(300));
        }
        return throwError(() => error);
      })
    );
  }

  updateTenant(tenant: Tenant): Observable<Tenant> {
    return this.http.put<Tenant>(`${BASE_URL}/tnts`, tenant).pipe(
      catchError((error: HttpErrorResponse) => {
        // If API doesn't exist, update mock data
        if (error.status === 404 || error.status === 0) {
          const index = this.mockData.findIndex(t => t.id === tenant.id);
          if (index !== -1) {
            const updatedTenant: Tenant = {
              ...tenant,
              updateAt: new Date()
            };
            this.mockData[index] = updatedTenant;
            return of(updatedTenant).pipe(delay(300));
          }
          return throwError(() => new Error(`Tenant with id ${tenant.id} not found`));
        }
        return throwError(() => error);
      })
    );
  }

  deleteTenant(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/deleteTenant?id=${id}`).pipe(
      catchError((error: HttpErrorResponse) => {
        // If API doesn't exist, delete from mock data
        if (error.status === 404 || error.status === 0) {
          const index = this.mockData.findIndex(t => t.id === id);
          if (index !== -1) {
            this.mockData.splice(index, 1);
            return of(void 0).pipe(delay(300));
          }
          return throwError(() => new Error(`Tenant with id ${id} not found`));
        }
        return throwError(() => error);
      })
    );
  }

  getClient(id: number): Observable<Client> {
    return this.http.get<Client>(`${BASE_URL}/getClient?id=${id}`).pipe(
      catchError((error) => {
        // Show a message via snackbar
        if (typeof window !== 'undefined' && (window as any).snackBar) {
          (window as any).snackBar.open(
            error?.message || 'An error occurred while fetching client',
            'Close',
            { duration: 3000 }
          );
        } else if (typeof console !== 'undefined') {
          console.error('SnackBar:', error?.message || 'An error occurred while fetching client');
        }
        return throwError(() => error);
      })
    );
  }

  getClients(): Observable<Client[]> {
    //return of([...this.mockData]).pipe(delay(300));
    return this.http.get<Client[]>(`${BASE_URL}/getClients`).pipe(
      catchError((error) => {
        // Show a message via snackbar
        if (typeof window !== 'undefined' && (window as any).snackBar) {
          (window as any).snackBar.open(
            error?.message || 'An error occurred while fetching client',
            'Close',
            { duration: 3000 }
          );
        } else if (typeof console !== 'undefined') {
          console.error('SnackBar:', error?.message || 'An error occurred while fetching client');
        }
        return throwError(() => error);
      })
    );
  }

  createClient(client: Client): Observable<Client> {
    return this.http.post<Client>(`${BASE_URL}/createClient`, client).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404 || error.status === 0) {
          const newClient: Client = {
            ...client,
            id: Math.max(...this.mockData.map(c => c.id || 0)) + 1,
            createdAt: new Date(),
            updateAt: new Date()
          };
          this.mockData.push(newClient);
          return of(newClient).pipe(delay(300));
        }
        return throwError(() => error);
      })
    );
  }

  updateClient(client: Client): Observable<Client> {
    return this.http.put<Client>(`${BASE_URL}/updateClient`, client).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404 || error.status === 0) {
          const index = this.mockData.findIndex(c => c.id === client.id);
          if (index !== -1) {
            const updated: Client = { ...client, updateAt: new Date() };
            this.mockData[index] = updated;
            return of(updated).pipe(delay(300));
          }
          return throwError(() => new Error(`Client with id ${client.id} not found`));
        }
        return throwError(() => error);
      })
    );
  }

  deleteClient(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/deleteClient?id=${id}`).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404 || error.status === 0) {
          const index = this.mockData.findIndex(c => c.id === id);
          if (index !== -1) {
            this.mockData.splice(index, 1);
            return of(void 0).pipe(delay(300));
          }
          return throwError(() => new Error(`Client with id ${id} not found`));
        }
        return throwError(() => error);
      })
    );
  }
  private mockTenantProducts: TenantProduct[] = [
    { id: 1, name: 'Product 1' },
    { id: 2, name: 'Product 2' }
  ];
  
  getTenantProducts(tid: number): Observable<TenantProduct[]> {
    return this.http.get<TenantProduct[]>(`${BASE_URL}/getTenantProducts?tid=${tid}`).pipe(
      catchError((_error: any) => {
        return of([...this.mockTenantProducts]).pipe(delay(300));
      })
    );
  }
  
  private mockClientProducts: ClientProduct[] = [
    { id: 1, name: 'Product 1', isSendSms: true, isSendEmail: true },
    { id: 2, name: 'Product 2', isSendSms: false, isSendEmail: true }
  ];
  
  getClientProducts(tid: number): Observable<ClientProduct[]> {
    return this.http.get<ClientProduct[]>(`${BASE_URL}/getClientProducts?tid=${tid}`).pipe(
      catchError((_error: any) => {
        return of([...this.mockClientProducts]).pipe(delay(300));
      })
    );
  }
  
  // Account methods
  private mockAccounts: Account[] = [
    { id: 1, parentId: null, tid: 1, clientId: 52, nodeType: 'CLIENT', name: 'Acme Client', 
      path: [{ id: '1', name: 'Acme Client' }] },
    { id: 2, parentId: 1, tid: 1, clientId: 52, nodeType: 'GROUP', name: 'Sales Group', 
      path: [{ id: '1', name: 'Acme Client' }, { id: '2', name: 'Sales Group' }]
     },
    { id: 3, parentId: 1, tid: 1, clientId: 52, nodeType: 'GROUP', name: 'Support Group', 
      path: [{ id: '1', name: 'Acme Client' }, { id: '3', name: 'Support Group' }]
     },
    { id: 4, parentId: 2, tid: 1, clientId: 52, nodeType: 'ACCOUNT', name: 'Account A', 
      path: [{ id: '1', name: 'Acme Client' }, { id: '2', name: 'Sales Group' }, { id: '4', name: 'Account A' }],
      products: [
        { id: 1, name: 'Product 1', can_read: true, can_quote: true, can_policy: false },
        { id: 2, name: 'Product 2', can_read: true, can_quote: false, can_policy: true }
      ],
      logins: ['login1@example.com', 'login2@example.com'],
      tokens: ['token123', 'token456']
     },
    { id: 5, parentId: 2, tid: 1, clientId: 52, nodeType: 'ACCOUNT', name: 'Account B', 
      path: [{ id: '1', name: 'Acme Client' }, { id: '2', name: 'Sales Group' }, { id: '5', name: 'Account B' }]
     },
    { id: 6, parentId: 3, tid: 1, clientId: 52, nodeType: 'ACCOUNT', name: 'Account C', 
      path: [{ id: '1', name: 'Acme Client' }, { id: '3', name: 'Support Group' }, { id: '6', name: 'Account C' }]
     }
  ];

  getAccount(id: number): Observable<Account> {
    return this.http.get<Account>(`${BASE_URL}/acc/tnts/${TENANT_ID}/clients/${CLIENT_ID}/accounts/${id}`).pipe(
      catchError(() => {
        const account = this.mockAccounts.find(a => a.id === id);
        if (account) {
          return of({ ...account }).pipe(delay(300));
        }
        return throwError(() => new Error(`Account with id ${id} not found`));
      })
    );
  }

  getChildAccounts(parentId: number): Observable<Account[]> {
    return this.http.get<Account[]>(`${BASE_URL}/acc/tnts/38/clients/52/accounts/${parentId}/accounts`).pipe(
      catchError((_error: any) => {
        const children = this.mockAccounts.filter(a => a.parentId === parentId);
        return of([...children]).pipe(delay(300));
      })
    );
  }

  createAccount(account: Account): Observable<Account> {
    return this.http.post<Account>(`${BASE_URL}/acc/tnts/38/clients/52/accounts/${account.parentId}/accounts`, account).pipe(
      catchError(() => {
        
          const parentAccount = account.parentId 
            ? this.mockAccounts.find(a => a.id === account.parentId)
            : null;
          const newId = Math.max(...this.mockAccounts.map(a => a.id || 0)) + 1;
          const newAccount: Account = {
            ...account,
            id: newId,
            
            path: parentAccount && Array.isArray(parentAccount.path)
              ? [...parentAccount.path, { id: newId.toString(), name: account.name }]
              : [{ id: newId.toString(), name: account.name }]
          };
          this.mockAccounts.push(newAccount);
          return of(newAccount).pipe(delay(300));
        
      })
    );
  }

  updateAccount(account: Account): Observable<Account> {
    return this.http.put<Account>(`${BASE_URL}/updateAccount`, account).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404 || error.status === 0) {
          const index = this.mockAccounts.findIndex(a => a.id === account.id);
          if (index !== -1) {
            this.mockAccounts[index] = { ...account };
            return of({ ...account }).pipe(delay(300));
          }
          return throwError(() => new Error(`Account with id ${account.id} not found`));
        }
        return throwError(() => error);
      })
    );
  }

  deleteAccount(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/deleteAccount?id=${id}`).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404 || error.status === 0) {
          const index = this.mockAccounts.findIndex(a => a.id === id);
          if (index !== -1) {
            // Also remove all children
            this.mockAccounts = this.mockAccounts.filter(a => {
              let current: Account | undefined = a;
              while (current?.parentId) {
                if (current.parentId === id) return false;
                current = this.mockAccounts.find(acc => acc.id === current?.parentId);
              }
              return a.id !== id;
            });
            return of(void 0).pipe(delay(300));
          }
          return throwError(() => new Error(`Account with id ${id} not found`));
        }
        return throwError(() => error);
      })
    );
  }

  // --- Product Methods --- 

  // Mimic storage of products per account in the mock accounts array
  addProduct(tntId: number, clientId: number, accountId: number, product: Product): Observable<Product> {
    return this.http.post<Product>(`${BASE_URL}/acc/tnts/${tntId}/clients/${clientId}/accounts/${accountId}/products`, product).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404 || error.status === 0) {
          const acc = this.mockAccounts.find(a => a.id === accountId);
          if (acc) {
            const maxId = acc.products && acc.products.length > 0
              ? Math.max(...acc.products.map(p => p.id)) : 0;
            const newProduct: Product = { ...product, id: maxId + 1 };
            acc.products = acc.products ? [...acc.products, newProduct] : [newProduct];
            return of(newProduct).pipe(delay(300));
          }
          return throwError(() => new Error(`Account with id ${accountId} not found`));
        }
        return throwError(() => error);
      })
    );
  }

  updateProduct(accountId: number, product: Product): Observable<Product> {
    return this.http.put<Product>(`${BASE_URL}/acc/tnts/${TENANT_ID}/clients/${CLIENT_ID}/accounts/${accountId}/products`, { accountId, product }).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404 || error.status === 0) {
          const acc = this.mockAccounts.find(a => a.id === accountId);
          if (acc && acc.products) {
            const idx = acc.products.findIndex(p => p.id === product.id);
            if (idx !== -1) {
              acc.products[idx] = { ...product };
              return of({ ...product }).pipe(delay(300));
            }
            return throwError(() => new Error(`Product with id ${product.id} not found in account ${accountId}`));
          }
          return throwError(() => new Error(`Account with id ${accountId} not found`));
        }
        return throwError(() => error);
      })
    );
  }

  deleteProduct(accountId: number, productId: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/acc/tnts/${TENANT_ID}/clients/${CLIENT_ID}/accounts/${accountId}/products/${productId}`).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404 || error.status === 0) {
          const acc = this.mockAccounts.find(a => a.id === accountId);
          if (acc && acc.products) {
            acc.products = acc.products.filter(p => p.id !== productId);
            return of(void 0).pipe(delay(300));
          }
          return throwError(() => new Error(`Account with id ${accountId} not found`));
        }
        return throwError(() => error);
      })
    );
  }

  getProducts(accountId: number): Observable<Product[]> {
    return this.http.get<Product[]>(`${BASE_URL}/acc/tnts/${TENANT_ID}/clients/${CLIENT_ID}/accounts/${accountId}/products`).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404 || error.status === 0) {
          const acc = this.mockAccounts.find(a => a.id === accountId);
          if (acc && acc.products) {
            return of([...acc.products]).pipe(delay(300));
          }
          return of([]).pipe(delay(300));
        }
        return throwError(() => error);
      })
    );
  }

  // ACCOUNT LOGINS
  addLogin(accountId: number, login: LoginAccount): Observable<string> {
    return this.http.post<string>(`${BASE_URL}/acc/tnts/${TENANT_ID}/clients/${CLIENT_ID}/accounts/${accountId}/logins`, login).pipe(
      catchError((error) => {
        return throwError(() => error);
      })
    );
  }
  
  updateLogin(accountId: number, login: string): Observable<string> {
    return this.http.put<string>(`${BASE_URL}/acc/tnts/${TENANT_ID}/clients/${CLIENT_ID}/accounts/${accountId}/logins`, login).pipe(
      catchError((error) => {
        return throwError(() => error);
      })
    );
  }
  
  deleteLogin(accountId: number, login: string): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/acc/tnts/${TENANT_ID}/clients/${CLIENT_ID}/accounts/${accountId}/logins/${login}`).pipe(
      catchError((error) => {
        return throwError(() => error);
      })
    );
  }
  
  getLogins(accountId: number, limit: number = 10, offset: number = 0): Observable<LoginAccount[]> {
    return this.http.get<LoginAccount[]>(`${BASE_URL}/acc/tnts/${TENANT_ID}/clients/${CLIENT_ID}/accounts/${accountId}/logins?limit=${limit}&offset=${offset}`).pipe(
      catchError((error) => {
        return throwError(() => error);
      })
    );
  }
  
  // ACCOUNT TOKENS
  addToken(accountId: number, token: string): Observable<string> {
    return this.http.post<string>(`${BASE_URL}/acc/tnts/${TENANT_ID}/clients/${CLIENT_ID}/accounts/${accountId}/tokens`, token).pipe(
      catchError((error) => {
        return throwError(() => error);
      })
    );
  }
  
  updateToken(accountId: number, token: string): Observable<string> {
    return this.http.put<string>(`${BASE_URL}/acc/tnts/${TENANT_ID}/clients/${CLIENT_ID}/accounts/${accountId}/tokens`, token).pipe(
      catchError((error) => {
        return throwError(() => error);
      })
    );
  }
  
  deleteToken(accountId: number, token: string): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/acc/tnts/${TENANT_ID}/clients/${CLIENT_ID}/accounts/${accountId}/tokens/${token}`).pipe(
      catchError((error) => {
        return throwError(() => error);
      })
    );
  }
  
}

