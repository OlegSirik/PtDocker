# ADR-004: Архитектура авторизации

## Статус
Принято

## Контекст
Система требует гибкой модели авторизации, поддерживающей:
- Мультитенантность
- Богатые доменные правила
- Интеграцию с OAuth2 / Keycloak
- Четкое разделение ответственностей

## Решение
Система использует гибридную модель авторизации:

1. RBAC (Role-Based Access Control) для крупнозернистого доступа
2. Политико-ориентированную авторизацию для детальных доменных правил

Разрешения RBAC сопоставляются с областями OAuth2 и
применяются с использованием Spring Security.

Детальная авторизация реализуется через
AuthorizationService и AuthorizationPolicy.

## Рассмотренные альтернативы

### Службы авторизации Keycloak
Отклонено из-за:
- Логика домена вне кодовой базы
- Сложность тестирования
- Привязка к поставщику

### Чистый RBAC
Отклонено из-за:
- Невозможности выражения доменных ограничений
- Взрывного роста количества разрешений

### Чистый ABAC
Отклонено из-за:
- Избыточной сложности
- Снижения читаемости

## Последствия
- Четкое разделение между аутентификацией и авторизацией
- Доменные правила остаются в коде
- Логика авторизации тестируема и аудируема






# ADR: Hierarchical Authorization with Privileges

## Status
Accepted

## Context
The system operates in a multi-tenant environment with
hierarchical accounts (Tenant → Client → Group → Portfolio).

Pure role-based access control is insufficient due to:
- hierarchical data ownership
- administrator impersonation
- partner-specific access scopes

At the same time, hierarchy alone does not restrict
what actions may be performed.

## Decision
The authorization model combines two independent checks:

1. **Hierarchical account access**
   Access is allowed only if the acting account
   is equal to or above the resource account
   in the account hierarchy.

2. **Privilege-based action control**
   Each action requires an explicit privilege
   in the form `<resource>:<action>`.

Privileges are grouped into roles,
but authorization checks are performed
against privileges, not roles.

## Roles and Privileges
Roles are static definitions in code
mapping to a predefined set of privileges.

Privileges are part of the system contract
and versioned together with the codebase.

## Consequences
- Fine-grained and explicit access control
- Clear separation of data scope and action scope
- Predictable and auditable authorization
- Easy extension for new resources and actions

## Example
To edit a policy:

- actingAccount must have hierarchical access to the policy account
- role must include `POLICY:EDIT`

Both conditions are mandatory.

## Audit
All actions can be audited as:

principalAccount → actingAccount → resource → action
