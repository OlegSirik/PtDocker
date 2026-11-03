import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap, catchError, delay, map, switchMap } from 'rxjs/operators';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';
import { ClientsService, Client } from './clients.service';

/** Matches backend `RecordStatus` JSON values. */
export enum RecordStatus {
  ACTIVE = 'ACTIVE',
  DELETED = 'DELETED',
}

export interface Tenant {
    id?: number;
    name: string;
    code: string;
    recordStatus: string;
    createdAt?: Date | string;
    updatedAt?: Date | string;
    authType?: string ;
  }

@Injectable({
  providedIn: 'root'
})

export class TenantsService extends BaseApiService<Tenant> {

  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/tenants', authService);
  }

  /**
   * Tenant CRUD always goes through {@code sys} path so DELETED tenants
   * can still be listed / reactivated (path tenant must be ACTIVE for getConfig).
   */
  override getUrl(id?: number | string): string {
    let url = this.resourcePath;
    if (id !== undefined && id !== null && id !== '') {
      url += '/' + id;
    }
    const root = this.authService.baseApiUrl.replace(/\/api\/v1\/[^/]+$/, '/api/v1/sys');
    return `${root}/${url}`;
  }
}
