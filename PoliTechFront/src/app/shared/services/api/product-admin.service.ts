import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap, catchError, delay, map } from 'rxjs/operators';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';

export interface ProductAdmin {
    id?: number;
    tid?: number;
    clientId?: number;
    accountId?: number;
    tenantCode: string;
    userLogin: string;
    fullName?: string;
    position?: string;
  }

@Injectable({
  providedIn: 'root'
})

export class ProductAdminService extends BaseApiService<ProductAdmin> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/admins/product-admins', authService);
  }
}

