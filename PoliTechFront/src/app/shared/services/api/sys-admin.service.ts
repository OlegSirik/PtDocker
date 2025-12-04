import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap, catchError, delay, map } from 'rxjs/operators';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';

export interface SysAdmin {
    id?: number;
    tenantId: number;
    userLogin: string;
    userName: string;
    fullName: string;
  }

@Injectable({
  providedIn: 'root'
})

export class SysAdminService extends BaseApiService<SysAdmin> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/admins/sys-admins', authService);
  }


}
