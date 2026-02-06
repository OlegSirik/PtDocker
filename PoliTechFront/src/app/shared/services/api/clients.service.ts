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
    authType: string;
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

  export interface ClientProduct {
    id?: number;
    accountId?: number;
    lobCode?: string;
    roleProductId: number;
    roleProductName?: string;
    productCode?: string;
    productName?: string;
    isDeleted: boolean;
}

@Injectable({
  providedIn: 'root'
})

export class ClientsService extends BaseApiService<Client> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/clients', authService);
  }

  /** Получить все записи */
  getProductsAll(): Observable<ClientProduct[]> {
    return this.http.get<ClientProduct[]>(this.getUrl() + "/products");
  }

  /** Получить продукты клиента */
  getClientProducts(clientId: string | number): Observable<ClientProduct[]> {
    return this.http.get<ClientProduct[]>(this.getUrl(clientId) + "/products");
  }

  /** Создать новую запись */
  grantProduct(clientid: string | number, item: ClientProduct): Observable<ClientProduct> {
    return this.http.post<ClientProduct>(this.getUrl(clientid) + "/products", item);
  }

  /** Удалить запись */
  revokeProduct(id: string | number, grantId: string | number): Observable<void> {
    return this.http.delete<void>(this.getUrl(id) + "/products/" + grantId);
  }  
}
