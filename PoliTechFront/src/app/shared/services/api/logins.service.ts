import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap, catchError, delay, map, switchMap } from 'rxjs/operators';
import { EnvService } from '../env.service';
import { AuthService, LoginData, User } from '../auth.service';
import { BaseApiService } from './base-api.service';

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
    super(http, env, 'logins', authService);
  }

 getUrl2(tenant: string, id?: (number | string)): string {

    let url = this.resourcePath;
    
    if (id) {
      url += '/' + id;
    }
    return this.env.BASE_URL + '/api/v1/' + tenant + '/' + url;

    //let url2 = this.authService.baseApiUrl.toString();
    
    //return url2 + '/' + url;
  }
  
  getAll2(tenant: string): Observable<Login[]> {
    return this.http.get<Login[]>(this.getUrl2(tenant));
  }
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



