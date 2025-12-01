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
      if ([HttpStatusCode.Forbidden, HttpStatusCode.Unauthorized].includes(error.status)) {
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
