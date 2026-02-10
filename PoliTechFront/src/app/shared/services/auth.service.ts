// rest auth - temporary file
import {Injectable, inject, Inject} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {BehaviorSubject, Observable, tap} from 'rxjs';
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

  login(credentials: LoginData): Observable<AuthResponse> {
    console.log(credentials);
    
    const tenantCode = credentials.tenantCode ?? 'demo';
    let url = `${this.baseUrl}/api/v1/${tenantCode}/auth/login`;
    return this.http.post<AuthResponse>( url, credentials)
      .pipe(
        tap(response => {
          if (response.accessToken) {
            localStorage.setItem('accessToken', response.accessToken);
            this.setTenantCode(tenantCode);
            this.getCurrentUser().subscribe();
          }
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
        tap(user=> {
          this.currentUserSubject.next(user);
          this.setTenantCode(user.tenantCode);
          this.tenantId = user.tenantId;
          this.isAuthenticated.next(true);
          this.setAccountId(user.accountId);
        })
      );
  }

  logout(): void {
    localStorage.removeItem('accessToken');
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
      return this.getCurrentUser();
    }
    return null;
  }

  setAccountId(accountId: number) {
    this.accountId = accountId;
  }

  getAccountId(): number | null {
    return this.accountId ?? null;
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
