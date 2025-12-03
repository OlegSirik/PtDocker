// rest auth - temporary file
import {Injectable, inject, Inject} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {BASE_URL} from '../tokens';

export interface LoginData {
  userLogin: string;
  password: string;
  clientId: number;
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
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private baseUrl: string = inject(BASE_URL);
  private currentUserSubject = new BehaviorSubject<User | null>(null);

  public get baseApiUrl() {
    return `${this.baseUrl}/api/v1/${this.tenant}`;
  }

  public currentUser$ = this.currentUserSubject.asObservable();
  public isAuthenticated = new BehaviorSubject<Boolean | null>(null);
  public tenant = '';

  login(credentials: LoginData): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/api/auth/login`, credentials)
      .pipe(
        tap(response => {
          if (response.accessToken) {
            localStorage.setItem('accessToken', response.accessToken);
            this.getCurrentUser().subscribe();
          }
        })
      );
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/api/auth/me`)
      .pipe(
        tap(user=> {
          this.currentUserSubject.next(user);
          this.tenant = user.tenantCode;
          console.log('this.tenant', this.tenant);
          this.isAuthenticated.next(true);
        })
      );
  }

  logout(): void {
    localStorage.removeItem('accessToken');
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
      return this.getCurrentUser();
    }
    return null;
  }
}
