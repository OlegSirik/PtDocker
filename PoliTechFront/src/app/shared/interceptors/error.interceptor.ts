import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ErrorHandlerService } from '../services/error-handler.service';

/**
 * HTTP Interceptor to handle errors globally
 * Automatically displays error messages using ErrorHandlerService
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const errorHandler = inject(ErrorHandlerService);
  const skipGlobalError = req.headers.get('X-Skip-Global-Error') === 'true';

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (skipGlobalError) {
        return throwError(() => error);
      }

      // Skip error handling for specific status codes that are handled elsewhere
      // (e.g., 401/403 are handled by auth.interceptor)
      if (error.status === 401 || error.status === 403) {
        return throwError(() => error);
      }

      // Handle all other errors
      errorHandler.handleError(error);

      return throwError(() => error);
    })
  );
};
