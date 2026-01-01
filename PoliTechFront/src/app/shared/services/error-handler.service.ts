import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { ErrorModel, ErrorDetail } from '../models/error.model';

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlerService {
  private snackBar = inject(MatSnackBar);

  /**
   * Handle error response and display error message to user
   * @param error HttpErrorResponse or ErrorModel
   * @param defaultMessage Default message if error format is not recognized
   */
  handleError(error: HttpErrorResponse | ErrorModel | any, defaultMessage: string = 'Произошла ошибка'): void {
    let errorModel: ErrorModel;

    // Try to extract ErrorModel from HttpErrorResponse
    if (error instanceof HttpErrorResponse) {
      if (error.error && typeof error.error === 'object') {
        // Check if error.error matches ErrorModel structure
        if ('code' in error.error && 'message' in error.error) {
          errorModel = error.error as ErrorModel;
        } else if (error.error.message) {
          // Fallback: create ErrorModel from error.error
          errorModel = {
            code: error.status || 500,
            message: error.error.message || error.message || defaultMessage,
            errors: error.error.errors || []
          };
        } else {
          errorModel = {
            code: error.status || 500,
            message: error.message || defaultMessage
          };
        }
      } else if (error.error && typeof error.error === 'string') {
        errorModel = {
          code: error.status || 500,
          message: error.error || error.message || defaultMessage
        };
      } else {
        errorModel = {
          code: error.status || 500,
          message: error.message || defaultMessage
        };
      }
    } else if (error && typeof error === 'object' && 'code' in error && 'message' in error) {
      // Direct ErrorModel
      errorModel = error as ErrorModel;
    } else {
      // Unknown error format
      errorModel = {
        code: 500,
        message: defaultMessage
      };
    }

    this.showError(errorModel);
  }

  /**
   * Display error message using MatSnackBar
   * @param errorModel ErrorModel to display
   */
  private showError(errorModel: ErrorModel): void {
    let message = `[Код: ${errorModel.code}] ${errorModel.message}`;

    // Add error details if available
    if (errorModel.errors && errorModel.errors.length > 0) {
      const details = errorModel.errors
        .map(err => this.formatErrorDetail(err))
        .filter(msg => msg.length > 0)
        .join('\n');
      
      if (details) {
        message += '\n\nДетали:\n' + details;
      }
    }

    // Show error message
    this.snackBar.open(message, 'Закрыть', {
      duration: 5000,
      panelClass: ['error-snackbar'],
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });
  }

  /**
   * Format ErrorDetail to readable string
   * @param detail ErrorDetail to format
   * @returns Formatted error detail string
   */
  private formatErrorDetail(detail: ErrorDetail): string {
    const parts: string[] = [];

    if (detail.field) {
      parts.push(`Поле: ${detail.field}`);
    }

    if (detail.message) {
      parts.push(detail.message);
    } else if (detail.reason) {
      parts.push(detail.reason);
    }

    if (detail.domain) {
      parts.push(`(Домен: ${detail.domain})`);
    }

    return parts.join(' - ');
  }

  /**
   * Get error message as string (useful for logging or custom display)
   * @param error HttpErrorResponse or ErrorModel
   * @returns Error message string
   */
  getErrorMessage(error: HttpErrorResponse | ErrorModel | any): string {
    let errorModel: ErrorModel | null = null;

    if (error instanceof HttpErrorResponse) {
      if (error.error && typeof error.error === 'object' && 'code' in error.error && 'message' in error.error) {
        errorModel = error.error as ErrorModel;
      } else {
        errorModel = {
          code: error.status || 500,
          message: error.error?.message || error.message || 'Произошла ошибка'
        };
      }
    } else if (error && typeof error === 'object' && 'code' in error && 'message' in error) {
      errorModel = error as ErrorModel;
    } else {
      return 'Произошла неизвестная ошибка';
    }

    return errorModel.message;
  }

  /**
   * Get error code
   * @param error HttpErrorResponse or ErrorModel
   * @returns Error code number
   */
  getErrorCode(error: HttpErrorResponse | ErrorModel | any): number {
    if (error instanceof HttpErrorResponse) {
      if (error.error && typeof error.error === 'object' && 'code' in error.error) {
        return (error.error as ErrorModel).code;
      }
      return error.status || 500;
    } else if (error && typeof error === 'object' && 'code' in error) {
      return (error as ErrorModel).code;
    }
    return 500;
  }
}
