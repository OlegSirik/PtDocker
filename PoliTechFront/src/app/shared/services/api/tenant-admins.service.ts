import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap, catchError, delay, map } from 'rxjs/operators';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';

export interface TenantAdmin {
    id?: number;
    tid?: number;
    clientId?: number;
    authClientId?: string;
    accountId?: number;
    tenantCode?: string;
    userLogin: string;
    fullName?: string;
    position?: string;
    role?: string;
  }

export interface AddAdminMemberRequest {
  role: string;
  userLogin: string;
  fullName?: string;
  position?: string;
  password?: string;
}

export const ADMIN_ROLES = {
  SYS_ADMIN: 'SYS_ADMIN',
  TNT_ADMIN: 'TNT_ADMIN',
  PRODUCT_ADMIN: 'PRODUCT_ADMIN',
  GROUP_ADMIN: 'GROUP_ADMIN',
} as const;

@Injectable({
  providedIn: 'root'
})

export class TenantAdminService extends BaseApiService<TenantAdmin> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/admins/members', authService);
  }

  /** Получить TNT admins по tenant code */
  
  getSysADmins(tenantCode: string): Observable<TenantAdmin[]> {
    const headers = new HttpHeaders()
    .set('X-Imp-Tenant', tenantCode);

    return this.http.get<TenantAdmin[]>(this.getUrl() + '?role=SYS_ADMIN', { headers });
  }
  
  getTntADmins(tenantCode: string): Observable<TenantAdmin[]> {
    const headers = new HttpHeaders()
    .set('X-Imp-Tenant', tenantCode);

    return this.http.get<TenantAdmin[]>(this.getUrl() + '?role=TNT_ADMIN', { headers });
  }

  getProductAdmins(tenantCode: string): Observable<TenantAdmin[]> {
    const headers = new HttpHeaders()
    .set('X-Imp-Tenant', tenantCode);

    return this.http.get<TenantAdmin[]>(this.getUrl() + '?role=PRODUCT_ADMIN', { headers });
  }

  addMember(body: AddAdminMemberRequest, headers?: { [key: string]: string }): Observable<TenantAdmin> {
    const options = { headers: this.createHeaders(headers) };
    return this.http.post<TenantAdmin>(this.getUrl(), {
      role: body.role,
      userLogin: body.userLogin,
      fullName: body.fullName || undefined,
      position: body.position || undefined,
      password: body.password || undefined,
    }, options);
  }
}
