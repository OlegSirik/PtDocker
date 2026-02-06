import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';
import { EnvService } from '../env.service';

export interface Commission {
  id?: number;
  accountId: number;
  productId: number;
  action: string;
  rateValue?: number;
  fixedAmount?: number;
  minAmount?: number;
  maxAmount?: number;
  commissionMinRate?: number;
  commissionMaxRate?: number;
  agdNumber?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CommissionService extends BaseApiService<Commission> {

  constructor(
    http: HttpClient,
    env: EnvService,
    authService: AuthService
  ) {
    super(http, env, 'admin/commissions', authService);
  }

  /** GET /configurations?accountId=&productId=&action= */
  getConfigurations(accountId: number, productId?: number, action?: string): Observable<Commission[]> {
    let params: Record<string, string> = { accountId: String(accountId) };
    if (productId != null) params['productId'] = String(productId);
    if (action != null) params['action'] = action;
    const query = new URLSearchParams(params).toString();
    const url = this.getUrl() + '/configurations' + (query ? '?' + query : '');
    return this.http.get<Commission[]>(url);
  }

  /** POST /configurations */
  createConfiguration(commission: Commission): Observable<Commission> {
    return this.http.post<Commission>(this.getUrl() + '/configurations', commission);
  }

  /** PUT /configurations/{id} */
  updateConfiguration(id: number, commission: Partial<Commission>): Observable<Commission> {
    return this.http.put<Commission>(this.getUrl() + '/configurations/' + id, { ...commission, id });
  }

  /** DELETE /configurations/{id} */
  deleteConfiguration(id: number): Observable<void> {
    return this.http.delete<void>(this.getUrl() + '/configurations/' + id);
  }

  /** GET /configurations/{id} */
  getConfigurationById(id: number): Observable<Commission> {
    return this.http.get<Commission>(this.getUrl() + '/configurations/' + id);
  }
}
