import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EnvService } from '../env.service';

export abstract class BaseApiService<T> {
  protected constructor(
    protected http: HttpClient,
    protected env: EnvService,
    protected resourcePath: string // например "users"
  ) {}

  getUrl(id?: (number | string)): string {
    let url = this.resourcePath;
    
    if (id) {
      url += '/' + id;
    }
    return this.env.BASE_URL + '/' + url;
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
