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

@Injectable({
  providedIn: 'root'
})

export class TenantAdminService extends BaseApiService<TenantAdmin> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admins/roles', authService);
  }

  /** Получить TNT admins по tenant code */
  
  getSysADmins(tenantCode: string): Observable<TenantAdmin[]> {
    const headers = new HttpHeaders()
    .set('X-Imp-Tenant', tenantCode);

    return this.http.get<TenantAdmin[]>(this.getUrl() + '/sys_admin', { headers });
  }
  
  getTntADmins(tenantCode: string): Observable<TenantAdmin[]> {
    const headers = new HttpHeaders()
    .set('X-Imp-Tenant', tenantCode);

    return this.http.get<TenantAdmin[]>(this.getUrl() + '/tnt_admin', { headers });
  }

  getProductAdmins(tenantCode: string): Observable<TenantAdmin[]> {
    const headers = new HttpHeaders()
    .set('X-Imp-Tenant', tenantCode);

    return this.http.get<TenantAdmin[]>(this.getUrl() + '/product_admin', { headers });
  }
}
