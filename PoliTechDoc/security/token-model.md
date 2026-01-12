# Модель токена

## Тип токена
- JWT (JSON Web Token)
- Подписан (RS256)
- Короткое время жизни

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
