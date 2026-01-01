# Error Handler Service

Общий обработчик ошибок для отображения сообщений об ошибках в формате `ErrorModel` из backend API.

## Структура ErrorModel

```typescript
interface ErrorModel {
  code: number;        // HTTP код ошибки
  message: string;    // Основное сообщение об ошибке
  errors?: ErrorDetail[];  // Детали ошибок (опционально)
}

interface ErrorDetail {
  domain?: string;    // Домен ошибки
  reason?: string;    // Причина ошибки
  message?: string;   // Сообщение об ошибке
  field?: string;     // Поле, связанное с ошибкой
}
```

## Автоматическая обработка

HTTP interceptor `errorInterceptor` автоматически обрабатывает все HTTP ошибки (кроме 401/403, которые обрабатываются `authInterceptor`).

Ошибки автоматически отображаются в виде snackbar с:
- Кодом ошибки
- Основным сообщением
- Деталями ошибок (если доступны)

## Ручное использование

Если нужно обработать ошибку вручную в компоненте:

```typescript
import { ErrorHandlerService } from '@shared/services/error-handler.service';
import { inject } from '@angular/core';

export class MyComponent {
  private errorHandler = inject(ErrorHandlerService);

  someMethod() {
    this.httpService.getData().subscribe({
      next: (data) => {
        // Handle success
      },
      error: (error) => {
        // Manual error handling
        this.errorHandler.handleError(error, 'Не удалось загрузить данные');
      }
    });
  }
}
```

## Методы сервиса

### `handleError(error, defaultMessage?)`
Обрабатывает ошибку и отображает сообщение пользователю.

### `getErrorMessage(error)`
Возвращает текст сообщения об ошибке как строку (полезно для логирования).

### `getErrorCode(error)`
Возвращает код ошибки как число.

## Примеры ответов от backend

### Простая ошибка:
```json
{
  "code": 400,
  "message": "Неверный запрос"
}
```

### Ошибка с деталями:
```json
{
  "code": 400,
  "message": "Ошибка валидации",
  "errors": [
    {
      "field": "email",
      "message": "Email обязателен",
      "domain": "validation"
    }
  ]
}
```
