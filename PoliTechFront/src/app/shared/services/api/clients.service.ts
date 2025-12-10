import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap, catchError, delay, map } from 'rxjs/operators';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';

export interface Client {
    id?: number;
    tid: number;
    clientId: string;
    name: string;
    description: string;
    trusted_email: string;
    createdAt?: Date | string;
    updateAt?: Date | string;
    status: 'ACTIVE' | 'DELETED' | 'SUSPENDED';
    accountId?: number;
  }

@Injectable({
  providedIn: 'root'
})

export class ClientsService extends BaseApiService<Client> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/clients', authService);
  }


}
