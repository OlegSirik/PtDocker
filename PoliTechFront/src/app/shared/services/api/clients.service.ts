import { HttpClient, HttpParams } from '@angular/common/http';
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
    authType?: string;
    authLevel: string;
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

/** Ответ GET .../clients/{id}/members (как AdminResponse на бэкенде) */
export interface ClientMember {
  id: number;
  tid?: number;
  tenantCode?: string;
  clientId?: number;
  accountId: number;
  userLogin: string;
  userRole?: string;
  fullName?: string;
  position?: string;
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

  /** Участники аккаунта клиента (например GROUP_ADMIN): GET .../members?role=group_admin */
  getClientMembers(clientId: string | number, role?: string): Observable<ClientMember[]> {
    let params = new HttpParams();
    if (role != null && role !== '') {
      params = params.set('role', role);
    }
    return this.http.get<ClientMember[]>(this.getUrl(clientId) + '/members', { params });
  }

  /** Добавить участника: POST body { role, userLogin } */
  addClientMember(
    clientId: string | number,
    body: { role: string; userLogin: string }
  ): Observable<ClientMember> {
    return this.http.post<ClientMember>(this.getUrl(clientId) + '/members', body);
  }

  /** Удалить привязку участника (бэкенд сам находит ролевой аккаунт GROUP_ADMIN). */
  deleteClientMember(clientId: string | number, memberId: string | number): Observable<void> {
    return this.http.delete<void>(this.getUrl(clientId) + '/members/' + memberId);
  }
}
