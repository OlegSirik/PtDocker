import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap, catchError, delay, map, switchMap } from 'rxjs/operators';
import { EnvService } from '../env.service';
import { AuthService, LoginData, User } from '../auth.service';
import { BaseApiService } from './base-api.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';

export interface Login {
    id?: number;
    tenantCode: string;
    userLogin: string;
    password: string;
    fullName: string;
    position: string;
  }

@Injectable({
  providedIn: 'root'
})

export class LoginService extends BaseApiService<Login> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/logins', authService);
  }
    
  /*
  getAll2(tenant: string): Observable<Login[]> {
    const headers = new HttpHeaders().set('X-Imp-Tenant', tenant);
    return this.http.get<Login[]>(this.getUrl(), { headers });
  }

  create2(tenant: string,login: Login): Observable<Login> {
    const headers = new HttpHeaders().set('X-Imp-Tenant', tenant);
    return this.http.post<Login>(this.getUrl(), login, { headers });
  }

  update2(tenant: string,login: Login): Observable<Login> {
    const headers = new HttpHeaders().set('X-Imp-Tenant', tenant);
    return this.http.put<Login>(this.getUrl(), login, { headers });
  }

  delete2(tenant: string, login: Login): Observable<Login> {  
    const headers = new HttpHeaders().set('X-Imp-Tenant', tenant);
    return this.http.delete<Login>(this.getUrl(), { headers });
  }
*/
/** Обновить пароль */
updatePassword(login: string, tenantCode: string, password: string): Observable<LoginData> {
  return this.authService.getCurrentUser().pipe(
    map((user: User) => {
      const clientId = user?.clientId;
      if (clientId) {
        const loginData: LoginData = { userLogin: login, password: password, clientId: clientId, tenantCode: tenantCode };
        return loginData;
      }
      throw new Error('Client ID not found');
    }),
    switchMap((loginData: LoginData) => {
      return this.authService.changePassword(loginData);
    })
  );
}


}



