# Authentication Process (AuthN)

This document describes the runtime authentication flow implemented in the
Spring Security filter chain, the identity resolution strategies, and the
UserDetails factory used by the platform.

Scope: request entry → SecurityContext population. Authorization (AuthZ) is
documented separately in `authorization/*`.

## High-level flow

```
HTTP request
  |
  v
TenantResolutionFilter
  |
  v
AccountResolutionFilter
  |
  v
IdentityResolutionFilter
  |
  v
TenantImpersonationFilter
  |
  v
ContextCleanupFilter
  |
  v
Controller
```

## Filter chain (actual order)

Configured in `ru.pt.auth.configuration.SecurityConfig`.

```
UsernamePasswordAuthenticationFilter
  ^ (TenantResolutionFilter is inserted before this)
TenantResolutionFilter
  -> AccountResolutionFilter
    -> IdentityResolutionFilter
      -> TenantImpersonationFilter
        -> ContextCleanupFilter
```

### TenantResolutionFilter
Source: `ru.pt.auth.security.filter.TenantResolutionFilter`

Responsibilities:
- Extract tenant code from URL `/api/v{n}/{tenantCode}/...`
- Store tenant in `RequestContext`
- Skip if request matches `security.publicUrls`

### AccountResolutionFilter
Source: `ru.pt.auth.security.filter.AccountResolutionFilter`

Responsibilities:
- Fetch `TenantSecurityConfig` and select an `IdentitySourceStrategy`
- Resolve identity (JWT, headers, API key, etc.)
- Call `AccountResolverService.resolveAccounts()` to decide the account id
- Store client/login/account into `RequestContext`

### IdentityResolutionFilter
Source: `ru.pt.auth.security.filter.IdentityResolutionFilter`

Responsibilities:
- If not already authenticated, build `UserDetails` using account id
- Set Spring `SecurityContextHolder` authentication
- On error, return `401`

### TenantImpersonationFilter
Source: `ru.pt.auth.security.filter.TenantImpersonationFilter`

Responsibilities:
- Allow SYS_ADMIN to set `X-Imp-Tenant` header
- Stores impersonated tenant into `UserDetailsImpl`

### ContextCleanupFilter
Source: `ru.pt.auth.security.filter.ContextCleanupFilter`

Responsibilities:
- Always clears `RequestContext` after request

## RequestContext (ThreadLocal)

`RequestContext` is implemented by `ThreadLocalContext`.

```
RequestContext:
  tenant  -> current tenant code
  client  -> auth client id (client_id)
  login   -> user login (user_login)
  account -> resolved account id
```

These values are populated by the selected `IdentitySourceStrategy` and
refined in `AccountResolverService`.

## Identity source strategies

`AccountResolutionFilter` selects a strategy based on `TenantSecurityConfig.authType`.

```
AuthType.JWT      -> JwtAuthenticationStrategy
AuthType.HEADERS  -> HeaderAuthenticationStrategy
AuthType.API_KEY  -> ApiKeyIdentityStrategy
AuthType.KEYCLOAK -> KeycloakIdentityStrategy
AuthType.NONE     -> NoAuthenticationStrategy
```

### JwtAuthenticationStrategy
Source: `ru.pt.auth.security.strategy.JwtAuthenticationStrategy`

Inputs:
- `Authorization: Bearer <jwt>`
- optional `X-Account-Id`

Actions:
- Validate token and extract `username` and `client_id`
- Store `client_id` + `username` in `RequestContext`
- Store `X-Account-Id` if present (validated as numeric)

### HeaderAuthenticationStrategy
Source: `ru.pt.auth.security.strategy.HeaderAuthenticationStrategy`

Inputs:
- `X-Client-Id`, `X-User-Id`, optional `X-Account-Id`

Notes:
- Currently validates presence of required headers, but does not
  store them in `RequestContext` yet.

### ApiKeyIdentityStrategy
Source: `ru.pt.auth.security.strategy.ApiKeyIdentityStrategy`

Inputs:
- `X-API-Key`

Notes:
- Placeholder implementation; request context is not populated yet.

### KeycloakIdentityStrategy
Source: `ru.pt.auth.security.strategy.KeycloakIdentityStrategy`

Notes:
- Placeholder implementation; request context is not populated yet.

### NoAuthenticationStrategy
Source: `ru.pt.auth.security.strategy.NoAuthenticationStrategy`

Inputs:
- `X-Account-Id`

Actions:
- Fetch account by id, set `tenant/client/login/account`

## Account resolution

`AccountResolverService.resolveAccounts()` refines account selection:

```
If ClientAuthType == CLIENT:
  account := defaultAccountId (enforced)
Else (USER):
  if X-Account-Id provided -> keep
  else -> select default account for user
```

## UserDetails factory (UserFactory)

User creation happens in `UserDetailsServiceImpl` and the factory method
`UserDetailsImpl.build(...)`.

```
RequestContext
  -> tenantCode + clientId + login + accountId
  -> AccountLoginRepository.findByAll4Fields(...)
  -> ProductRoleRepository.findAllProductRolesByAccountId(...)
  -> AccountRepository.findByTenantCodeAndId(...)
  -> UserDetailsImpl.build(...)
  -> SecurityContextHolder.setAuthentication(...)
```

`UserDetailsImpl.build()` is the canonical "UserFactory" because it takes
domain entities and produces the `UserDetails` used throughout the system.

### Acting account resolution

`UserDetailsServiceImpl` computes `actingAccountId` based on account node type:

```
SUB           -> parent account
ACCOUNT       -> same account
SYS_ADMIN     -> tenant root account
TNT_ADMIN     -> tenant root account
PRODUCT_ADMIN -> tenant root account
GROUP_ADMIN   -> parent account
GROUP/CLIENT/TENANT -> forbidden
```

## Pseudographic schemas

### Filter pipeline with context

```
Request
  |
  v
TenantResolutionFilter
  - ctx.tenant = {tenantCode}
  |
  v
AccountResolutionFilter
  - strategy.resolveIdentity()
  - ctx.client/login/account set
  - AccountResolverService.resolveAccounts()
  |
  v
IdentityResolutionFilter
  - userDetails = UserDetailsServiceImpl.loadUserByUsername(ctx.account)
  - SecurityContextHolder.setAuthentication(userDetails)
  |
  v
TenantImpersonationFilter
  - if SYS_ADMIN and X-Imp-Tenant -> user.impersonatedTenantCode
  |
  v
ContextCleanupFilter
  - ctx.clear()
```

### JWT path (AuthType.JWT)

```
Authorization: Bearer <jwt>
        |
        v
JwtAuthenticationStrategy
  - validate token
  - ctx.client = client_id
  - ctx.login  = username
  - ctx.account = X-Account-Id (optional)
        |
        v
AccountResolverService
  - choose default account if needed
        |
        v
UserDetailsImpl.build(...)
```

### Client vs User selection

```
ClientAuthType = CLIENT  -> account := defaultAccountId
ClientAuthType = USER    -> account := X-Account-Id or default account
```

## Warnings / gaps

- `JwtAuthenticationFilter` exists but is not wired into the filter chain.
  It contains an alternative flow with `Partner-Client-Id` and
  `Partner-User-Id` headers.
- `HeaderAuthenticationStrategy`, `ApiKeyIdentityStrategy`,
  `KeycloakIdentityStrategy` are incomplete placeholders.
- `SecurityConfigurationProperties.publicUrls` controls filter skipping.
