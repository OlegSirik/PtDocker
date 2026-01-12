# Интеграция с Spring Security

## Обзор
Spring Security используется в качестве gatekeeper
перед авторизацией на уровне домена.

## Использование
Разрешения RBAC применяются с использованием @PreAuthorize.

Пример:
```java
@PreAuthorize("hasAuthority('policy:create')")
```

## Разделение ответственности
Компонент | Ответственность
--- | ---
Spring Security | Контроль доступа к API
AuthorizationService | Доменная авторизация

## Правила
- Нет бизнес-логики в SpEL
- Нет доступа к базе данных в @PreAuthorize
- Все доменные правила проходят через AuthorizationService