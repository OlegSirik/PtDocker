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
    defaultAccountId?: number;
    createdAt?: Date | string;
    updateAt?: Date | string;
    isDeleted: boolean;
    clientAccountId?: number;
    clientConfiguration?: ClientConfiguration;
  }

  export interface ClientConfiguration {
    paymentGate: string;
    sendEmailAfterBuy: boolean;
    sendSmsAfterBuy: boolean;
    paymentGateAgentNumber?: string;
    paymentGateLogin?: string;
    paymentGatePassword?: string;
    employeeEmail?: string;
  }


@Injectable({
  providedIn: 'root'
})

export class ClientsService extends BaseApiService<Client> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/clients', authService);
  }

}
