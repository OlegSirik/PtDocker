// auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

export interface UserAccountSummary {
  id: number;
  tid: number;
  clientId: number;
  type: string;
  name: string;
}

export interface UserProfile {
  user_id: number;
  login: string;
  current_account: UserAccountSummary;
  available_accounts: UserAccountSummary[];
  effective_role: string;
  product_roles: string[];
  token?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'auth.jwt';
  private readonly PROFILE_KEY = 'auth.profile';
  private readonly BASE_URL = 'http://localhost:8080';
  private readonly TENANT_KEY = 'tenant_id';
  private readonly CLIENT_KEY = 'client_id';
  private readonly ACCOUNT_KEY = 'account_id';

  private readonly profileSubject = new BehaviorSubject<UserProfile | null>(this.loadProfile());
  readonly profile$ = this.profileSubject.asObservable();

  private readonly tokenSubject = new BehaviorSubject<string | null>(this.loadToken());
  readonly token$ = this.tokenSubject.asObservable();

  constructor(private http: HttpClient) {}

  get token(): string | null {
    return this.tokenSubject.value;
  }

  set token(value: string | null) {
    if (value) {
      localStorage.setItem(this.TOKEN_KEY, value);
    } else {
      localStorage.removeItem(this.TOKEN_KEY);
    }
    this.tokenSubject.next(value);
  }

  get profile(): UserProfile | null {
    return this.profileSubject.value;
  }

  set profile(value: UserProfile | null) {
    if (value) {
      localStorage.setItem(this.PROFILE_KEY, JSON.stringify(value));
    } else {
      localStorage.removeItem(this.PROFILE_KEY);
    }
    this.profileSubject.next(value);
  }

  isAuthenticated(): boolean {
    const token = this.token;
    return !!token && !this.isTokenExpired(token);
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }

  login(username: string, password: string): Observable<UserProfile> {
    const headers = new HttpHeaders({
      Authorization: 'Basic ' + btoa(`${username}:${password}`)
    });

    return this.http.get<UserProfile>(`${this.BASE_URL}/acc/me/profile`, { headers }).pipe(
      catchError((_error: HttpErrorResponse) => {
        return of(this.createMockProfile(username));
      }),
      map(profile => this.enrichProfile(profile)),
      tap(profile => this.persistSession(profile))
    );
  }

  logout(): void {
    this.token = null;
    this.profile = null;
  }

  getAuthorizationHeader(): string | null {
    return this.token ? `Bearer ${this.token}` : null;
  }

  hasEffectiveRole(role: string): boolean {
    const profile = this.profile;
    return profile?.effective_role === role;
  }

  hasAnyRole(roles: string[]): boolean {
    const profile = this.profile;
    if (!profile) return false;
    return roles.includes(profile.effective_role) || profile.product_roles.some(role => roles.includes(role));
  }

  hasAccountType(type: string): boolean {
    const profile = this.profile;
    if (!profile) return false;
    return profile.available_accounts.some(account => account.type === type);
  }

  switchAccount(accountId: number): void {
    const profile = this.profile;
    if (!profile) return;
    const target = profile.available_accounts.find(a => a.id === accountId);
    if (!target) return;

    const updated: UserProfile = {
      ...profile,
      current_account: target,
      effective_role: target.type ?? profile.effective_role
    };
    this.persistSession(updated);
  }

  private persistSession(profile: UserProfile): void {
    const token = profile.token ?? this.generateMockToken(profile.login);
    this.token = token;
    this.profile = { ...profile, token };
  }

  autoLogin(): void {
    const existingProfile = this.loadProfile();
    const existingToken = this.loadToken();

    this.profileSubject.next(existingProfile);
    this.tokenSubject.next(existingToken);
  }

  private enrichProfile(profile: UserProfile): UserProfile {
    if (profile.current_account) {
      return profile;
    }

    const fallbackAccount = profile.available_accounts?.[0];
    if (fallbackAccount) {
      return {
        ...profile,
        current_account: fallbackAccount,
        effective_role: fallbackAccount.type ?? profile.effective_role
      };
    }

    return profile;
  }

  private createMockProfile(login: string): UserProfile {
    return {
      user_id: Math.floor(Math.random() * 1000) + 1,
      login,
      current_account: {
        id: 12345,
        tid: 2,
        clientId: 52,
        type: 'SALES',
        name: 'Sales Portfolio ОСAGO'
      },
      available_accounts: [
        { id: 12345, tid: 2, clientId: 51, type: 'SALES', name: 'Sales Portfolio' },
        { id: 0, tid: 3, clientId: 54, type: 'SYS_ADMIN', name: 'System Administration' }
      ],
      effective_role: 'SALES',
      product_roles: ['OSAGO_SALE', 'NS15_QUOTE', 'TELE2_SALE']
    };
  }

  private generateMockToken(login: string): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({
      sub: login,
      exp: Math.floor(Date.now() / 1000) + 60 * 60,
      iat: Math.floor(Date.now() / 1000)
    }));
    const signature = btoa('mock-signature');
    return `${header}.${payload}.${signature}`;
  }

  private loadProfile(): UserProfile | null {
    try {
      const raw = localStorage.getItem(this.PROFILE_KEY);
      return raw ? JSON.parse(raw) as UserProfile : null;
    } catch {
      return null;
    }
  }

  private loadToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
  getTenantId(): string | null {
    return localStorage.getItem(this.TENANT_KEY);
  }

  getClientId(): string | null {
    return localStorage.getItem(this.CLIENT_KEY);
  }

  getAccountId(): string | null {
    return localStorage.getItem(this.ACCOUNT_KEY);
  }

  clear() {
    localStorage.clear();
  }
}
