import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';

export interface SysAdmin {
    id?: number;
    tid?: number;
    clientId?: number;
    accountId?: number;
    tenantCode: string;
    userLogin: string;
    fullName: string;
    position?: string;
    createdAt?: string;
    updatedAt?: string;
  }

@Injectable({
  providedIn: 'root'
})

export class SysAdminService extends BaseApiService<SysAdmin> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/admins/sys-admins', authService);
  }


}
