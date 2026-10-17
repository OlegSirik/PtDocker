import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../auth.service';

export interface PolicyFormDto {
  id?: number;
  pageId: string;
  title: string;
  ioType?: string;
}

export interface ProductForQuoteDto {
  productId: number;
  productCode: string;
  productName: string;
  lob: string;
  versionNo: number;
  pageId: string;
  title: string;
  ioType?: string;
}

@Injectable({ providedIn: 'root' })
export class PolicyFormService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);

  /** Full global catalog (tid=1), same for every tenant/LOB. */
  listForms(): Observable<PolicyFormDto[]> {
    return this.http.get<PolicyFormDto[]>(`${this.auth.baseApiUrl}/admin/policy-forms`);
  }

  /** @deprecated Use listForms — forms are not filtered by LOB. */
  listFormsByLob(_lobCode: string): Observable<PolicyFormDto[]> {
    return this.listForms();
  }

  getByPageId(pageId: string): Observable<PolicyFormDto> {
    return this.http.get<PolicyFormDto>(
      `${this.auth.baseApiUrl}/admin/policy-forms/${encodeURIComponent(pageId)}`,
    );
  }

  listProductsForQuote(): Observable<ProductForQuoteDto[]> {
    return this.http.get<ProductForQuoteDto[]>(`${this.auth.baseApiUrl}/lk/products-for-quote`);
  }

  create(payload: PolicyFormDto): Observable<PolicyFormDto> {
    return this.http.post<PolicyFormDto>(`${this.auth.baseApiUrl}/admin/policy-forms`, payload);
  }
}
