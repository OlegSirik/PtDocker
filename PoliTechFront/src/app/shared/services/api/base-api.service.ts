import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';

@Injectable()
export abstract class BaseApiService<T> {
  protected constructor(
    protected http: HttpClient,
    protected env: EnvService,
    protected resourcePath: string,
    protected authService: AuthService
  ) {}

  getUrl(id?: (number | string)): string {

    let url = this.resourcePath;
    //let tenantId = this.env.TENANT_HEADER;
    if (id) {
      url += '/' + id;
    }

    let url2 = this.authService.baseApiUrl.toString();
    console.log('getUrl::::::::: ' + url2 + '/' + url);
    return url2 + '/' + url;
  }
 
  // КОРРЕКТНО: только 2 перегрузки на метод
  // 1. Без headers (для обратной совместимости)
  // 2. С headers
  
  /** Получить все записи */
  getAll(): Observable<T[]>;
  getAll(headers: { [key: string]: string }): Observable<T[]>;
  getAll(headers?: { [key: string]: string }): Observable<T[]> {
    const options = {
      headers: this.createHeaders(headers)
    };
    return this.http.get<T[]>(this.getUrl(), options);
  }

  /** Получить запись по ID */
  getById(id: number | string): Observable<T>;
  getById(id: number | string, headers: { [key: string]: string }): Observable<T>;
  getById(id: number | string, headers?: { [key: string]: string }): Observable<T> {
    const options = {
      headers: this.createHeaders(headers)
    };
    return this.http.get<T>(this.getUrl(id), options);
  }

  /** Создать новую запись */
  create(item: T): Observable<T>;
  create(item: T, headers: { [key: string]: string }): Observable<T>;
  create(item: T, headers?: { [key: string]: string }): Observable<T> {
    const options = {
      headers: this.createHeaders(headers)
    };
    console.log('create: ' + this.getUrl() + ' ' + JSON.stringify(item) + ' ' + JSON.stringify(options));
    return this.http.post<T>(this.getUrl(), item, options);
  }

  /** Обновить запись */
  update(id: string | number, item: Partial<T>): Observable<T>;
  update(id: string | number, item: Partial<T>, headers: { [key: string]: string }): Observable<T>;
  update(id: string | number, item: Partial<T>, headers?: { [key: string]: string }): Observable<T> {
    const options = {
      headers: this.createHeaders(headers)
    };
    return this.http.put<T>(this.getUrl(id), item, options);
  }

  /** Удалить запись */
  delete(id: string | number): Observable<void>;
  delete(id: string | number, headers: { [key: string]: string }): Observable<void>;
  delete(id: string | number, headers?: { [key: string]: string }): Observable<void> {
    const options = {
      headers: this.createHeaders(headers)
    };
    return this.http.delete<void>(this.getUrl(id), options);
  }

  protected createHeaders(customHeaders?: { [key: string]: string }): HttpHeaders {
    let headers = new HttpHeaders();
        
    // Кастомные headers
    if (customHeaders) {
      Object.keys(customHeaders).forEach(key => {
        headers = headers.set(key, customHeaders[key]);
      });
    }
    
    return headers;
  }
}


/*

// Новый код с headers
this.userService.getAll({
  'X-Custom-Header': 'value',
  'Cache-Control': 'no-cache'
}).subscribe(users => {
  console.log(users);
});

*/