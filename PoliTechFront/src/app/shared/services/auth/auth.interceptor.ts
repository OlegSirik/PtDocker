import { Injectable } from '@angular/core';
import {
  HttpEvent, HttpInterceptor, HttpHandler, HttpRequest
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { EnvService } from '../env.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private auth: AuthService, private env: EnvService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let headers = req.headers;

    const token =     this.auth.getToken();
    const tenantId =  this.auth.getTenantId();
    const clientId =  this.auth.getClientId();
    const accountId = this.auth.getAccountId();

    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    if (tenantId) {
      headers = headers.set(this.env.TENANT_HEADER, tenantId);
    }
    if (clientId) {
      headers = headers.set(this.env.CLIENT_HEADER, clientId);
    }
    if (accountId) {
      headers = headers.set(this.env.ACCOUNT_HEADER, accountId);
    }

    const cloned = req.clone({ headers });
    return next.handle(cloned);
  }
}
