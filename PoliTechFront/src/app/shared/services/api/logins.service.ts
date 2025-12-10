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

/** Обновить пароль */
updatePassword(login: string, tenantCode: string, password: string): Observable<LoginData> {
  return this.authService.getCurrentUser().pipe(
    map((user: User) => {
      const clientId = user?.clientId;
      if (clientId) {
        const loginData: LoginData = { userLogin: login, password: password, clientId: clientId };
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



