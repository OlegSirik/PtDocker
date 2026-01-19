import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';

@Injectable()
export abstract class BaseApiService<T> {
  protected constructor(
    protected http: HttpClient,
    protected env: EnvService,
    protected resourcePath: string, // например "users"
    protected authService: AuthService
  ) {}

  getUrl(id?: (number | string)): string {

    let url = this.resourcePath;
    let tenantId = this.env.TENANT_HEADER;
    if (id) {
      url += '/' + id;
    }
    //console.log('getUrl: ' + this.env.BASE_URL + '/api/v1/' + tenantId + '/' + url);

    let url2 = this.authService.baseApiUrl.toString();
    //console.log('url2', url2);
    return url2 + '/' + url;
    //return this.env.BASE_URL + '/api/v1/' + tenantId + '/' + url;
  }
  
  /** Получить все записи */
  getAll(): Observable<T[]> {
    return this.http.get<T[]>(this.getUrl());
  }

  /** Получить запись по ID */
  getById(id: (number | string)): Observable<T> {
    return this.http.get<T>(this.getUrl(id));
  }

  /** Создать новую запись */
  create(item: T): Observable<T> {
    return this.http.post<T>(this.getUrl(), item);
  }

  /** Обновить запись */
  update(id: string | number, item: Partial<T>): Observable<T> {
    return this.http.put<T>(this.getUrl(id), item);
  }

  /** Удалить запись */
  delete(id: string | number): Observable<void> {
    return this.http.delete<void>(this.getUrl(id));
  }
}
