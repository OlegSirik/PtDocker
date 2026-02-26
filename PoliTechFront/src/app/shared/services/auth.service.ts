// rest auth - temporary file
import {Injectable, inject} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {BehaviorSubject, Observable, of, tap} from 'rxjs';
import {catchError, switchMap} from 'rxjs/operators';
import {throwError} from 'rxjs';
import {BASE_URL} from '../tokens';

export interface LoginData {
  userLogin: string;
  password: string;
  clientId: number;
  tenantCode: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string | null;
  tokenType: string;
  expiresIn: number;
  refreshExpiresIn: number | null;
  message: string | null;
}

export interface Authority {
  authority: string;
}

export interface User {
  accountId: number;
  productRoles: any[];
  isDefault: boolean;
  clientId: number;
  accountName: string;
  clientName: string;
  tenantId: number;
  tenantCode: string;
  id: number;
  userRole: string;
  authorities: Authority[];
  username: string;
  accounts?: Account[];
}
export interface Account {
  id: number;
  name: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private baseUrl: string = inject(BASE_URL);
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  private readonly TENANT_CODE_KEY = 'tenant_code';
  private readonly ACCOUNT_ID_KEY = 'account_id';

  public get baseApiUrl() {
    return `${this.baseUrl}/api/v1/${this.tenant}`;
  }
  get currentClientId() {
    return 'SYS';
  };

  public currentUser$ = this.currentUserSubject.asObservable();
  public isAuthenticated = new BehaviorSubject<Boolean | null>(null);
  public tenant = localStorage.getItem(this.TENANT_CODE_KEY) || '';
  public tenantId = -1;
  private accountId: number | null = null;

  login(credentials: LoginData): Observable<AuthResponse | User> {
    this.accountId = null;
    const tenantCode = credentials.tenantCode ?? 'demo';
    const url = `${this.baseUrl}/api/v1/${tenantCode}/auth/login`;
    return this.http.post<AuthResponse>(url, credentials).pipe(
      switchMap(response => {
        if (!response.accessToken) {
          return of(response);
        }
        localStorage.setItem('accessToken', response.accessToken);
        localStorage.removeItem(this.ACCOUNT_ID_KEY);
        this.setTenantCode(tenantCode);
        return this.getCurrentUser();
      })
    );
  }

  changePassword(credentials: LoginData): Observable<LoginData> {
    return this.http.post<LoginData>( `${this.baseApiUrl}/auth/set-password`, credentials
      , { headers: { 'X-Imp-Tenant': credentials.tenantCode } });
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.baseApiUrl}/auth/me`)
      .pipe(
        tap(user => {
          this.currentUserSubject.next(user);
          this.setTenantCode(user.tenantCode);
          this.tenantId = user.tenantId;
          this.isAuthenticated.next(true);
          const savedAccountId = this.getStoredAccountId();
          const accountId = this.resolveAccountId(user, savedAccountId);
          this.setAccountId(accountId);
        }),
        catchError(err => {
          this.logout();
          return throwError(() => err);
        })
      );
  }

  private getStoredAccountId(): number | null {
    const raw = localStorage.getItem(this.ACCOUNT_ID_KEY);
    if (raw == null) return null;
    const id = parseInt(raw, 10);
    return isNaN(id) ? null : id;
  }

  private resolveAccountId(user: User, savedId: number | null): number {
    if (savedId != null && user.accounts?.some(a => a.id === savedId)) {
      return savedId;
    }
    return user.accountId;
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem(this.ACCOUNT_ID_KEY);
    this.clearTenantCode();
    this.currentUserSubject.next(null);
    this.isAuthenticated.next(false);
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  hasRole(role: string): boolean {
    const user = this.currentUserSubject.value;
    if (!user) return false;

    // Проверяем userRole
    if (user.userRole === role) {
      return true;
    }

    // Проверяем authorities
    if (user.authorities && user.authorities.some(auth => auth.authority === role || auth.authority === `ROLE_${role}`)) {
      return true;
    }

    return false;
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  initializeAuthState(): Observable<User> | null {
    const token = this.getToken();
    if (token) {
      this.tenant = localStorage.getItem(this.TENANT_CODE_KEY) || this.tenant;
      return this.getCurrentUser().pipe(
        catchError(() => {
          this.isAuthenticated.next(false);
          return of(null as unknown as User);
        })
      );
    }
    this.isAuthenticated.next(false);
    return null;
  }

  setAccountId(accountId: number) {
    this.accountId = accountId;
    localStorage.setItem(this.ACCOUNT_ID_KEY, accountId.toString());
  }

  getAccountId(): number | null {
    if (this.accountId != null) return this.accountId;
    return this.getStoredAccountId();
  }

  getTenantCode(): string {
    return this.tenant || localStorage.getItem(this.TENANT_CODE_KEY) || '';
  }

  private setTenantCode(tenantCode: string) {
    this.tenant = tenantCode;
    if (tenantCode) {
      localStorage.setItem(this.TENANT_CODE_KEY, tenantCode);
    } else {
      this.clearTenantCode();
    }
  }

  private clearTenantCode() {
    this.tenant = '';
    localStorage.removeItem(this.TENANT_CODE_KEY);
  }
}
