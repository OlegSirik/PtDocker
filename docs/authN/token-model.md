# Модель токена

## Тип токена
- JWT (JSON Web Token)
- Короткое время жизни

## Получение токена (login/password)

Источник: `POST /api/v1/{tenantCode}/auth/login`

Выбор способа происходит по `tenant.authType` через `List<ExternalJwtAuthenticator>`:
- `LOCAL_JWT` -> `LocalJwtPasswordAuthenticator`
  - локальная проверка логин/пароль (`SimpleAuthService`)
  - выдача внутреннего JWT
- `KEYCLOAK` (и совместимо `JWT`) -> `KeycloakPasswordGrantAuthenticator`
  - password grant во внешний Keycloak
  - возврат `access_token` от IdP

Если подходящий аутентификатор не найден, API возвращает `400`.

## Общие claims

| Claim | Описание |
|------|----------|
| sub | Идентификатор субъекта |
| scope | Список scopes |
| exp | Время истечения |
| iat | Время выпуска |

## USER-токен
- preferred_username
- email
- realm_access / resource_access

## CLIENT-токен
- sub = client_id
- Отсутствуют пользовательские claims

## Валидация токена
Выполняется Spring Security:
- Проверка подписи
- Проверка срока действия
- Проверка issuer
- Проверка audience

Примечание:
- Для `AuthType.LOCAL_JWT` используется локальная валидация (`LocalJwtAuthenticationStrategy`)
- Для `AuthType.KEYCLOAK` используется `KeycloakIdentityStrategy` с проверками claim'ов по `auth_config`
- Для `AuthType.JWT` используется `JwtAuthenticationStrategy` с per-tenant `auth_config`
