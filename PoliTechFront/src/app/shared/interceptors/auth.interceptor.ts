// rest auth - temporary file
import {
  HttpErrorResponse,
  HttpInterceptorFn,
  HttpStatusCode
} from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { ErrorHandlerService } from '../services/error-handler.service';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const errorHandler = inject(ErrorHandlerService);
  const router = inject(Router);
  const token = authService.getToken();
  const accountId = authService.getAccountId();
  let clonedReq = req;

  // Add Authorization header if token exists
  if (token) {
    clonedReq = clonedReq.clone({
      headers: clonedReq.headers.set('Authorization', `Bearer ${token}`)
    });
  }

  // Add X-Account-Id header if accountId exists
  if (accountId !== null) {
    clonedReq = clonedReq.clone({
      headers: clonedReq.headers.set('X-Account-Id', accountId.toString())
    });
  }

  const result = next(clonedReq);

  return result.pipe(
    catchError((error: HttpErrorResponse) => {
      console.log('error', error);
      const tenantCode = authService.tenant || '';
      
      if (error.status === HttpStatusCode.Unauthorized) {
        // 401: User is not authenticated → redirect to login
        const loginTenant = tenantCode || 'demo';
        router.navigate(['/', loginTenant, 'login']);
      } else if (error.status === HttpStatusCode.Forbidden) {
        // 403: User is authenticated but lacks permission
        const method = req.method.toUpperCase();
        
        if (method === 'GET') {
          // For GET requests: don't redirect, let component handle empty state
          // Just pass error through
        } else {
          // For POST, PUT, DELETE: show error message from API response
          errorHandler.handleError(error, 'Доступ запрещён');
        }
        // Don't redirect to forbidden page anymore
      }
      
      return throwError(() => error);
    })
  );
};
