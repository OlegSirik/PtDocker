import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from '../auth.service';

/**
 * Service for product test quote/policy JSON examples.
 */
@Injectable({
  providedIn: 'root'
})
export class TestRequestService {

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getUrl(path: string, productId: number, versionNo: number): string {
    const base = this.authService.baseApiUrl + '/admin/text/' + path;
    return `${base}/${productId}/${versionNo}`;
  }

  getTestQuote(productId: number, versionNo: number): Observable<string> {
    return this.http.get(this.getUrl('quote', productId, versionNo), { responseType: 'text' });
  }

  getTestPolicy(productId: number, versionNo: number): Observable<string> {
    return this.http.get(this.getUrl('policy', productId, versionNo), { responseType: 'text' });
  }

  saveTestQuote(productId: number, versionNo: number, json: string): Observable<void> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.http.post(this.getUrl('quote', productId, versionNo), json, {
      headers,
      responseType: 'text'
    }).pipe(map(() => undefined));
  }

  saveTestPolicy(productId: number, versionNo: number, json: string): Observable<void> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.http.post(this.getUrl('policy', productId, versionNo), json, {
      headers,
      responseType: 'text'
    }).pipe(map(() => undefined));
  }

  /** Execute quote (calculate) - POST sales/quotes */
  executeQuote(json: string): Observable<string> {
    const url = this.authService.baseApiUrl + '/sales/quotes';
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.http.post(url, json, { headers, responseType: 'text' });
  }

  /** Execute policy (save) - POST sales/policies */
  executePolicy(json: string): Observable<string> {
    const url = this.authService.baseApiUrl + '/sales/policies';
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.http.post(url, json, { headers, responseType: 'text' });
  }
}
