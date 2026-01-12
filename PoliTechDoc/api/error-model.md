# Error Model

## Общий формат ошибки

```json
{
  "errorCode": "POLICY_ALREADY_ISSUED",
  "message": "Договор уже выпущен",
  "details": {
    "policyId": "123"
  },
  "traceId": "abc-123"
}

Категории ошибок
HTTP	Категория
400	Ошибка валидации
401	Не аутентифицирован
403	Доступ запрещён
404	Ресурс не найден
409	Конфликт
422	Нарушение бизнес-правил
500	Внутренняя ошибка


Правила

message предназначен для UI
errorCode стабилен и используется в интеграциях
details опционален

Связь с авторизацией
AuthN → 401
AuthZ → 403
Domain policy → 422

## Ошибки авторизации

### 401 Unauthorized
Возвращается, если:
- Отсутствует токен
- Токен невалиден или истёк

### 403 Forbidden
Возвращается, если:
- AuthorizationService отклонил доступ
- Недостаточно прав
- Нарушена tenant-граница

### Пример ответа
```json
{
  "errorCode": "ACCESS_DENIED",
  "message": "Договор уже выпущен",
  "details": {
    "resource": "policy",
    "action": "update"
  }
}

