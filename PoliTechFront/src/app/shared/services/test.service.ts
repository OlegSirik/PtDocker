import { Injectable, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { BASE_URL } from '../tokens';
import { catchError } from 'rxjs/operators';

export interface TestContext {
  varCode: string;
  varValue: string;
}

export interface TestError {
  errorText: string;
  validator: string;
}

export interface TestResponse {
  product: string;
  context: TestContext[];
  errorText: TestError[];
  policy: string;
}

export interface PfType {
  code: string;
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class TestService {
  private mockPfTypes: PfType[] = [
    { code: 'policy', name: 'Полис' },
    { code: 'kid', name: 'КИД' }
  ];

  constructor(private http: HttpClient, @Inject(BASE_URL) private baseUrl: string) {}

  getPfTypes(): Observable<PfType[]> {
    return this.http.get<PfType[]>(`${this.baseUrl}test/pf-types`).pipe(
      catchError(() => of(this.mockPfTypes))
    );
  }

  validateQuote(requestJson: string): Observable<TestResponse> {
    return this.http.post<TestResponse>(`${this.baseUrl}test/quote/validator`, requestJson, {
      headers: { 'Content-Type': 'application/json' }
    }).pipe(
      catchError(() => {
        // Mock response
        return of({
          product: 'LIFETIME_PLUS',
          context: [
            {
              varCode: 'ph_firstName',
              varValue: 'Акакий'
            },
            {
              varCode: 'ph_lastName',
              varValue: 'Петров'
            }
          ],
          errorText: [
            {
              errorText: 'умад',
              validator: 'sumInsuredIn 0.01-300000 RANGE NUMBER'
            }
          ],
          policy: 'POLICY_001'
        });
      })
    );
  }

  validatePolicy(requestJson: string): Observable<TestResponse> {
    return this.http.post<TestResponse>(`${this.baseUrl}test/policy/validator`, requestJson, {
      headers: { 'Content-Type': 'application/json' }
    }).pipe(
      catchError(() => {
        // Mock response
        return of({
          product: 'LIFETIME_PLUS',
          context: [
            {
              varCode: 'ph_firstName',
              varValue: 'Акакий'
            }
          ],
          errorText: [
            {
              errorText: 'умад',
              validator: 'sumInsuredIn 0.01-300000 RANGE NUMBER'
            }
          ],
          policy: 'POLICY_001'
        });
      })
    );
  }

  calculateQuote(requestJson: string): Observable<string> {
    return this.http.post(`${this.baseUrl}test/quote/calculator`, requestJson, {
      headers: { 'Content-Type': 'application/json' },
      responseType: 'text'
    }).pipe(
      catchError(() => {
        // Mock response
        const mockResult = {
          calculationId: 'CALC_001',
          premium: 15000,
          currency: 'RUB',
          status: 'success',
          timestamp: new Date().toISOString()
        };
        return of(JSON.stringify(mockResult, null, 2));
      })
    );
  }

  calculatePolicy(requestJson: string): Observable<string> {
    return this.http.post(`${this.baseUrl}test/policy/calculator`, requestJson, {
      headers: { 'Content-Type': 'application/json' },
      responseType: 'text'
    }).pipe(
      catchError(() => {
        // Mock response
        const mockResult = {
          policyId: 'POL_001',
          premium: 15000,
          currency: 'RUB',
          status: 'success',
          timestamp: new Date().toISOString()
        };
        return of(JSON.stringify(mockResult, null, 2));
      })
    );
  }

  printPf(requestJson: string, pfType: string): Observable<Blob> {
    return this.http.post(`${this.baseUrl}test/policy/printpf/${pfType}`, requestJson, {
      headers: { 'Content-Type': 'application/json' },
      responseType: 'blob'
    }).pipe(
      catchError(() => {
        // Mock response - create a PDF-like blob
        const mockContent = `Mock Print Form for ${pfType}\n\nRequest Data:\n${requestJson}\n\nGenerated at: ${new Date().toISOString()}`;
        const blob = new Blob([mockContent], { type: 'application/pdf' });
        return of(blob);
      })
    );
  }
}
