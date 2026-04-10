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


}
