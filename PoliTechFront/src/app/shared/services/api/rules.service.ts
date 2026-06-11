import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';

export type RuleScopeType = 'PRODUCT' | 'LOB' | 'TENANT' | 'CLIENT';

export type RuleType =
  | 'PRE_QUOTE_VALIDATION'
  | 'POST_QUOTE_VALIDATION'
  | 'PRE_SAVE_VALIDATION'
  | 'POST_SAVE_VALIDATION'
  | 'QUOTE_CALCULATION'
  | 'UNDERWRITING'
  | 'WORKFLOW'
  | 'CROSS_SELL'
  | 'FRAUD_CHECK'
  | 'ISSUANCE'
  | 'RENEWAL';

export interface Rule {
  id?: number;
  code: string;
  name: string;
  scopeType: RuleScopeType;
  scopeCode: string;
  ruleType: RuleType;
  priority?: number;
  recordStatus?: string;
  expressionLanguage?: string;
  expression: string;
  message: string;
  llmText?: string;
}

export interface RuleListFilter {
  ruleType?: RuleType;
  scopeType?: RuleScopeType;
  scopeCode?: string;
  recordStatus?: string;
}

@Injectable({ providedIn: 'root' })
export class RulesService extends BaseApiService<Rule> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/rules', authService);
  }

  list(filter: RuleListFilter = {}): Observable<Rule[]> {
    let params = new HttpParams();
    if (filter.ruleType) {
      params = params.set('ruleType', filter.ruleType);
    }
    if (filter.scopeType) {
      params = params.set('scopeType', filter.scopeType);
    }
    if (filter.scopeCode) {
      params = params.set('scopeCode', filter.scopeCode);
    }
    if (filter.recordStatus) {
      params = params.set('recordStatus', filter.recordStatus);
    }
    return this.http.get<Rule[]>(this.getUrl(), { params });
  }

  reloadCache(): Observable<{ status: string }> {
    return this.http.post<{ status: string }>(this.getUrl() + '/cmd/reload', {});
  }
}
