import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../auth.service';

export interface RefDict {
  code: string;
  name: string;
}

export interface RefDataItem {
  code: string;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class RefDictsService {
  constructor(
    private http: HttpClient,
    private auth: AuthService
  ) {}

  private baseUrl(): string {
    return `${this.auth.baseApiUrl}/admin/refdicts`;
  }

  listDicts(): Observable<RefDict[]> {
    return this.http.get<RefDict[]>(this.baseUrl());
  }

  createDict(dto: RefDict): Observable<RefDict> {
    return this.http.post<RefDict>(this.baseUrl(), dto);
  }

  updateDict(code: string, dto: RefDict): Observable<RefDict> {
    return this.http.put<RefDict>(`${this.baseUrl()}/${encodeURIComponent(code)}`, dto);
  }

  deleteDict(code: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl()}/${encodeURIComponent(code)}`);
  }

  listItems(dictCode: string): Observable<RefDataItem[]> {
    return this.http.get<RefDataItem[]>(`${this.baseUrl()}/${encodeURIComponent(dictCode)}/items`);
  }

  createItem(dictCode: string, dto: RefDataItem): Observable<RefDataItem> {
    return this.http.post<RefDataItem>(`${this.baseUrl()}/${encodeURIComponent(dictCode)}/items`, dto);
  }

  updateItem(dictCode: string, itemCode: string, dto: RefDataItem): Observable<RefDataItem> {
    return this.http.put<RefDataItem>(
      `${this.baseUrl()}/${encodeURIComponent(dictCode)}/items/${encodeURIComponent(itemCode)}`,
      dto
    );
  }

  deleteItem(dictCode: string, itemCode: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl()}/${encodeURIComponent(dictCode)}/items/${encodeURIComponent(itemCode)}`
    );
  }
}
