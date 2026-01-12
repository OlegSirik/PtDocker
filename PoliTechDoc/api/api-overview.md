# API — Обзор

## Назначение
API предоставляет программный доступ к бизнес-возможностям системы
и используется:
- UI-приложениями
- Внешними интеграциями
- Внутренними сервисами

API является:
- RESTful
- Stateless
- JSON-based
- Защищённым OAuth2

## Базовые принципы
- Явные контракты (OpenAPI)
- Предсказуемые HTTP-коды
- Строгое разделение AuthN / AuthZ / Domain
- Tenant-aware обработка запросов

## Базовый URL
~~~
/api/v1/{tenantCode}/
~~~

## Связанные разделы
- api/rest-conventions.md
- api/openapi-guidelines.md
- api/error-model.md
- security/oauth2.md