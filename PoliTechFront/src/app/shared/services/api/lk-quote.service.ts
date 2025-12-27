import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';

export interface Quote {
  id?: string; // uuid
  draftId?: string;
  policyNr?: string;
  productCode?: string;
  insCompany?: string;
  createDate?: Date | string;
  issueDate?: Date | string;
  issueTimezone?: string;
  paymentDate?: Date | string;
  startDate?: Date | string;
  endDate?: Date | string;
  policyStatus?: string;
  phDigest?: string;
  ioDigest?: string;
  premium?: number;
  agentDigest?: string;
  agentKvPrecent?: number;
  agentKvAmount?: number;
  comand1?: boolean;
  comand2?: boolean;
  comand3?: boolean;
  comand4?: boolean;
  comand5?: boolean;
  comand6?: boolean;
  comand7?: boolean;
  comand8?: boolean;
  comand9?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class LkQuoteService extends BaseApiService<Quote> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'sales/quotes', authService);
  }


   getAccountQuotes(qstr: string): Observable<Quote[]> {
    console.log('getAccountQuotes', qstr);
    return this.http.get<Quote[]>(`${this.getUrl()}?qstr=${encodeURIComponent(qstr)}`);
  }
}
