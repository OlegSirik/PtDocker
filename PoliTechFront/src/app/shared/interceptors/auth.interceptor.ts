// rest auth - temporary file
import {
  HttpErrorResponse,
  HttpInterceptorFn,
  HttpStatusCode
} from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();
  let result;

  if (token) {
    const cloned = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    result = next(cloned);
  } else {
    result = next(req)
  }

  return result.pipe(
    catchError((error: HttpErrorResponse) => {
      console.log('error', error);
      const tenantCode = authService.tenant || '';
      
      if (error.status === HttpStatusCode.Unauthorized) {
        // 401: User is not authenticated → redirect to login
        if (tenantCode) {
          router.navigate(['/', tenantCode, 'login']);
        } else {
          router.navigate(['/login']);
        }
      } else if (error.status === HttpStatusCode.Forbidden) {
        // 403: User is authenticated but lacks permission → redirect to forbidden page
        if (tenantCode) {
          router.navigate(['/', tenantCode, 'forbidden']);
        } else {
          router.navigate(['/forbidden']);
        }
      }
      
      return throwError(() => error);
    })
  );
};
