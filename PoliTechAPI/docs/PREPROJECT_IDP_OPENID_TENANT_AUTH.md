# Pre-project: IdP OpenID Providers, Tenant/Client Linking and JWT Validation

## 1. Overview

Several authorization types can be applied at **tenant** and **client** level. This pre-project defines:

- A **collection of OpenID Connect IdP providers** that the platform can use.
- **Tenant → IdP linking**: each tenant (and optionally each client) is linked to one IdP.
- **JWT validation** using the linked IdP (issuer, JWKS).
- **Optional IdP admin access**: when admin credentials for an IdP exist, expose **CRUD for IdP clients and users**.
- **Integration** with existing **client creation** and **user/login creation** flows so that IdP entities are created when app entities are created.

---

## 2. Current State (Brief)

- **Tenant**: `TenantEntity` has `authType` (e.g. `JWT`, `KEYCLOAK`, `HEADERS`, `APIKEY`, `NONE`). No explicit “IdP provider” reference yet.
- **Client**: `ClientEntity` has `authType`; can override or inherit from tenant.
- **Auth resolution**: `AccountResolutionFilter` chooses an `IdentitySourceStrategy` by tenant/client `AuthType` (e.g. `JwtAuthenticationStrategy`, `KeycloakIdentityStrategy`).
- **Keycloak**: `KeycloakAdminClient` + `KeycloakServiceImpl` exist for one Keycloak realm; not yet “one of many” IdPs per tenant.

---

## 3. Target Architecture

### 3.1 IdP Provider Registry

- **Concept**: A configurable list of **OpenID Connect IdP providers** (Keycloak, Auth0, custom OIDC, etc.).
- **Storage**: Either:
  - **Option A**: New table `acc_idp_providers` (id, code, issuer, jwks_uri, admin_api_url, config jsonb, …).
  - **Option B**: Configuration (e.g. YAML/properties) keyed by provider code, or a mix (built-in config + DB overrides).
- **Per-IdP data** (at least):
  - `code` (e.g. `KEYCLOAK_VSK`, `AUTH0_TENANT1`)
  - `issuer` (e.g. `https://keycloak.example.com/realms/vsk`)
  - `jwks_uri` (for JWT validation)
  - Optional: `admin_api_base_url`, `admin_client_id`, `admin_client_secret` (or client_credentials flow) for admin operations.
- **Usage**: Resolve “which IdP” for a tenant/client from this registry.

### 3.2 Tenant and Client → IdP Linking

- **Tenant**:
  - Add field to `TenantEntity`: `idp_provider_code` (nullable), referencing the IdP registry (e.g. FK or string code).
  - If present, tenant uses that IdP for JWT validation and (if admin is configured) for IdP admin operations.
- **Client** (optional refinement):
  - Keep or add `idp_provider_code` on `ClientEntity`; if null, fall back to tenant’s IdP.
  - Allows “tenant default IdP” vs “client-specific IdP”.

### 3.3 JWT Validation via Linked IdP

- **Flow**:
  1. Request arrives; tenant (and client) are resolved (e.g. from path or context).
  2. From tenant (and optional client) resolve `idp_provider_code` → get IdP config (issuer, jwks_uri).
  3. Validate JWT:
     - Issuer must match IdP’s `issuer`.
     - Signature verified using JWKS from `jwks_uri` (fetch/cache JWKS).
  4. Extract identity (sub, preferred_username, etc.) and proceed with existing account resolution (e.g. map to `LoginEntity` / accounts).
- **Implementation**:
  - New auth type (e.g. `OPENID`) or reuse/extend `KEYCLOAK` to be “OIDC with tenant-linked IdP”.
  - New or extended strategy (e.g. `OpenIdConnectIdentityStrategy`) that:
    - Takes tenant (and optionally client) from request context.
    - Loads IdP config from registry.
    - Validates JWT with that IdP’s JWKS and issuer.
  - JWKS caching (in-memory or cache) to avoid per-request HTTP calls.

### 3.4 IdP Admin: Clients and Users CRUD (when admin exists)

- **Condition**: For a given IdP, admin credentials (or client_credentials) are configured (e.g. in IdP registry or tenant/client config).
- **Operations** (to be implemented as services + optional REST):
  - **IdP Clients CRUD**: Create/read/update/delete “clients” (OAuth2/OIDC clients) in the IdP realm.
  - **IdP Users CRUD**: Create/read/update/delete users in the IdP.
- **Keycloak**: Reuse/extend `KeycloakAdminClient` so it is instantiated per IdP config (realm + server URL + admin credentials) from registry, not from a single global property set.
- **Generic OIDC**: If other IdPs (e.g. Auth0) are supported, add an adapter interface (e.g. `IdpAdminClient`) with Keycloak and Auth0 implementations; admin CRUD then goes through this interface.

### 3.5 Integration with Client Creation (App)

- **Current**: App creates `ClientEntity` (tenant, name, authType, …) in DB.
- **New**:
  - After creating `ClientEntity`, if tenant (or client) is linked to an IdP **and** that IdP has admin access:
    - Create corresponding OAuth2/OIDC “client” in the IdP (e.g. Keycloak client with clientId = app client id or derived).
  - Store IdP-side client id in `ClientEntity` or in a separate mapping table if needed (e.g. `client_idp_binding`).
- **Failure handling**: Decide policy (e.g. fail app client creation if IdP create fails, or create in app only and log/sync later).

### 3.6 Integration with User/Login Creation (App)

- **Current**: `LoginManagementService.createLogin` creates `LoginEntity` (tid, userLogin, fullName, position).
- **New**:
  - After creating `LoginEntity`, if tenant is linked to an IdP **and** that IdP has admin access:
    - Create user in IdP (e.g. Keycloak user with username = userLogin, optional temp password or “set password via link”).
    - Optionally link IdP user id to `LoginEntity` (e.g. `login.idp_user_id` or mapping table).
  - **setPassword**: When app sets password for a login, if IdP admin is available, update password in IdP as well (or trigger “set password” flow in IdP).
- **Failure handling**: Same as client (fail entire operation vs. app-only + retry/sync).

---

## 4. Data Model (Proposed)

### 4.1 IdP provider (new)

- `acc_idp_providers` (or equivalent):
  - id, code (unique), name, issuer, jwks_uri,
  - admin_api_base_url (nullable), admin_client_id (nullable), admin_client_secret (nullable) or secret ref,
  - config (jsonb) for provider-specific options,
  - created_at, updated_at.

### 4.2 Tenant

- Add column: `idp_provider_code` (nullable, FK or string to `acc_idp_providers.code`).
- Optional: `idp_realm` or `idp_tenant_id` in config if one IdP instance serves multiple realms/tenants.

### 4.3 Client

- Optional: `idp_provider_code` (nullable); if null, use tenant’s.
- Optional: `idp_client_id` (nullable) to store IdP-side client id after creation.

### 4.4 Login / User

- Optional: `idp_user_id` (nullable) on `LoginEntity` or in a side table to store IdP user id after creation.

---

## 5. Implementation Phases (Suggested)

1. **Phase 1 – IdP registry and tenant link**
   - Add `acc_idp_providers` (migration).
   - Add `idp_provider_code` to tenant (and optional to client); entity + DTO + API.
   - Seed or configure at least one IdP (e.g. existing Keycloak).

2. **Phase 2 – JWT validation with linked IdP**
   - Implement (or extend) strategy that uses tenant’s IdP: load JWKS from `jwks_uri`, validate JWT issuer/signature.
   - Wire into existing auth filter (e.g. new `AuthType.OPENID` or reuse KEYCLOAK with tenant-scoped config).
   - JWKS cache.

3. **Phase 3 – IdP admin API (clients and users CRUD)**
   - Define `IdpAdminClient` interface (createClient, getUserByUsername, createUser, setPassword, …).
   - Keycloak implementation (refactor `KeycloakAdminClient` to be config-driven per IdP).
   - Optional REST endpoints (e.g. internal or admin-only) to create/update/delete IdP clients and users.
   - Use only when `idp_provider` has admin credentials.

4. **Phase 4 – Integration with client creation**
   - In `ClientService` (or equivalent), after DB create: if tenant has IdP with admin, call IdP admin to create client; store `idp_client_id` if needed.

5. **Phase 5 – Integration with login creation**
   - In `LoginManagementService.createLogin`, after DB create: if tenant has IdP with admin, create user in IdP; store `idp_user_id` if needed.
   - In `setPassword`: if IdP admin exists, update password in IdP.

---

## 6. Security and Operational Notes

- **Secrets**: IdP admin client secrets and JWKS endpoints must be stored/accessed securely (e.g. secrets manager, env, or encrypted config).
- **Caching**: JWKS and possibly admin tokens should be cached with TTL to avoid excessive calls and to respect key rotation (short TTL or event-driven refresh).
- **Tenant isolation**: All IdP operations (JWT validation and admin) must be scoped by tenant (and client) so that one tenant cannot use another tenant’s IdP config.

---

## 7. Summary

| Area | Description |
|------|-------------|
| **IdP collection** | Registry of OpenID providers (issuer, jwks_uri, optional admin API + credentials). |
| **Tenant/Client link** | Tenant (and optionally client) linked to one IdP by `idp_provider_code`. |
| **JWT validation** | Use linked IdP’s issuer + JWKS to validate JWT; integrate with existing identity resolution. |
| **IdP admin CRUD** | When admin credentials exist: CRUD for IdP clients and users (Keycloak first; extend to other IdPs via interface). |
| **Client creation** | On app client create, optionally create IdP client and store binding. |
| **Login creation** | On app login create (and setPassword), optionally create/update IdP user. |

This pre-project can be used as the basis for backlog items and technical design of each phase.
