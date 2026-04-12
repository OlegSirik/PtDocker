import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';

/** Соответствует InsuranceCompanyDto на бэкенде */
export interface InsuranceCompanyDto {
  id?: number;
  tid?: number;
  code?: string;
  name?: string;
  status?: string;
  shortName?: string;
  fullName?: string;
  egr?: string;
  postalAddress?: string;
  legalAddress?: string;
  phone?: string;
  mail?: string;
  inn?: string;
  kpp?: string;
  okpo?: string;
  ogrn?: string;
  account?: string;
  bank?: string;
  bic?: string;
  corrAccount?: string;
  displayName?: string;
  representativeString?: string;
}

@Injectable({
  providedIn: 'root',
})
export class InsCompanyService extends BaseApiService<InsuranceCompanyDto> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/insCompanies', authService);
  }

  list(): Observable<InsuranceCompanyDto[]> {
    return this.getAll();
  }
}
