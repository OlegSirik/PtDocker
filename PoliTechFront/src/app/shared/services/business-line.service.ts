import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap, catchError, delay, map } from 'rxjs/operators';
import { BASE_URL } from '../tokens';

export interface BusinessLine {
  id: number;
  mpCode: string;
  mpName: string;
}

@Injectable({
  providedIn: 'root'
})
export class BusinessLineService {
  constructor(private http: HttpClient, @Inject(BASE_URL) private baseUrl: string) {};

  private mockData: BusinessLine[] = [
    { id: 1, mpCode: 'MORTGAGE', mpName: 'Ипотека' },
    { id: 2, mpCode: 'AUTO', mpName: 'Автострахование' },
    { id: 3, mpCode: 'PROPERTY', mpName: 'Имущество' },
    { id: 4, mpCode: 'HEALTH', mpName: 'Здоровье' },
    { id: 5, mpCode: 'LIFE', mpName: 'Жизнь' }
  ];


  getBusinessLines(): Observable<BusinessLine[]> {
    if (!this.http) {
      throw new Error('HttpClient is not initialized');
    }

    return this.http.get<BusinessLine[]>(`${this.baseUrl}admin/lobs`).pipe(
      tap(data => {
        if (Array.isArray(data) && data.length !== 0) {
          this.mockData = data;
        }
      }),
      catchError(error => {
        console.error('Error fetching business lines:', error);
        return of(this.mockData);
      })
    );
  }

  addBusinessLine(businessLine: Omit<BusinessLine, 'id'>): Observable<BusinessLine> {
    const newId = Math.max(...this.mockData.map(item => item.id)) + 1;
    const newBusinessLine: BusinessLine = {
      ...businessLine,
      id: newId
    };
    this.mockData.push(newBusinessLine);
    return of(newBusinessLine);
  }

  updateBusinessLine(id: number, businessLine: Partial<BusinessLine>): Observable<BusinessLine | null> {
    const index = this.mockData.findIndex(item => item.id === id);
    if (index !== -1) {
      this.mockData[index] = { ...this.mockData[index], ...businessLine };
      return of(this.mockData[index]);
    }
    return of(null);
  }

  deleteBusinessLine(id: number): Observable<boolean> {
    if (this.http) {
      const url = `${this.baseUrl}admin/lobs/${id}`;
      return this.http.delete(url).pipe(
        map(() => true),
        catchError(error => {
          console.error('Error deleting business line:', error);
          return of(false);
        })
      );
    }
    return of(false);
  }


  getLobCodes(): Observable<string[]> {
    return this.getBusinessLines().pipe(
      map((businessLines: BusinessLine[]) => businessLines.map(item => item.mpCode))
    );

  }

}
