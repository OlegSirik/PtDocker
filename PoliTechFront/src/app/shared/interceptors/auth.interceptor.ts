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
import { BASE_URL } from '../tokens';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const errorHandler = inject(ErrorHandlerService);
  const router = inject(Router);
  const baseUrl = inject(BASE_URL);
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
      const tenantCode = authService.getTenantCode();
      
      if (error.status === HttpStatusCode.Unauthorized) {
        // 401: User is not authenticated → redirect to login
        const loginTenant = tenantCode || 'demo';
        window.location.href = `${baseUrl}/${loginTenant}/login`;
      } else if (error.status === HttpStatusCode.Forbidden) {
        // 403: User is authenticated but lacks permission
        const method = req.method.toUpperCase();
        
        if (method === 'GET') {
          // For GET requests: don't redirect, let component handle empty state
          // Just pass error through
        } else {
          // For POST, PUT, DELETE: show error message from API response
          // Combine Russian text with API message
          const apiMessage = error.error?.message || '';
          const combinedMessage = apiMessage 
            ? `Доступ запрещён: ${apiMessage}` 
            : 'Доступ запрещён';
          
          // Create modified error with combined message for display
          const displayError = {
            ...error,
            error: {
              ...error.error,
              code: error.error?.code || 403,
              message: combinedMessage
            }
          };
          errorHandler.handleError(displayError, combinedMessage);
        }
        // Don't redirect to forbidden page anymore
      }
      
      return throwError(() => error);
    })
  );
};
