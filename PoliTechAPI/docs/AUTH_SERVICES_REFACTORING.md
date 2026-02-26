# Auth Services Refactoring Analysis

## Current Structure Overview

### Services in pt-auth (by responsibility)

| Service | Responsibility | Dependencies | Issues |
|---------|----------------|--------------|--------|
| **AccountDataService** | Pure data access (AccountProductRoles, AccountHierarchyProvider) | Repos only | ✅ Clean - single responsibility |
| **AccountServiceImpl** | Account CRUD + auth checks | AccountDataService, AuthorizationService | Delegates to AccountDataService |
| **AccountLoginServiceImpl** | Account↔Login bindings | Repos, AuthorizationService | Focused |
| **AccountTokenServiceImpl** | API tokens for accounts | Repos, AuthorizationService | Focused |
| **ClientService** | Client CRUD + security config + product roles | TenantService, AccountService, ProductService, AuthorizationService | **Too many responsibilities** |
| **TenantService** | Tenant CRUD + security config | Repos | Also implements TenantSecurityConfigService |
| **AdminUserManagementService** | SYS/TNT/PRODUCT admins | 7 repos directly | **Bypasses interfaces, duplicates logic** |
| **ClientAdminsManagementService** | CLIENT/GROUP admins + users | 6 repos directly | **Overlaps with AdminUserManagementService** |
| **LoginManagementService** | Login CRUD, passwords | TenantService, repos | Duplicates permission checks |
| **AccountResolverService** | Auth flow - resolve account from identity | ClientService | Used in filters |
| **UserDetailsServiceImpl** | Spring Security - load user by accountId | Repos | Auth-specific |
| **ClientConfigurationService** | Client config (auth type, etc.) | ClientService | Thin wrapper |
| **AuthorizationServiceImpl** | Permission checks | AccountDataService | ✅ Clean |

### Controllers → Services mapping

| Controller | Services Used | Notes |
|------------|---------------|-------|
| AccountManagementController | AccountService, AccountTokenService, AccountLoginService, **AdminUserManagementService** | AdminUserManagementService injected but **not used** |
| ClientManagementController | ClientService, ClientAdminsManagementService | ClientAdminsManagementService injected but constructor doesn't store it |
| LoginManagementController | LoginManagementService | OK |
| AdminManagementController | AdminUserManagementService | SYS/TNT/PRODUCT admins |
| ProductRoleManagementController | ClientService | Product roles via ClientService |
| ClientConfigurationController | ClientConfigurationService | OK |

---

## Identified Problems

### 1. **ClientService is a "God Service"**
- Client CRUD (TNT_ADMIN)
- ClientSecurityConfig (auth flow - no auth check)
- Product roles for accounts (getProductRolesByAccountId, grantProduct, revokeProduct)
- List clients, get client by ID
- **Recommendation**: Split into `ClientQueryService`, `ClientCommandService`, `ClientSecurityConfigProvider`

### 2. **AdminUserManagementService vs ClientAdminsManagementService**
- **AdminUserManagementService**: SYS_ADMIN, TNT_ADMIN, PRODUCT_ADMIN
- **ClientAdminsManagementService**: CLIENT_ADMIN, GROUP_ADMIN, + group users
- Both: direct repo access, duplicate `getAdmins()`, duplicate `checkPermitionAndGetTenantCode()`, duplicate `getCurrentUser()`
- **Recommendation**: Merge into `AdminManagementService` with clear role-based methods, or extract shared `AdminPermissionHelper`

### 3. **Product role assignment scattered**
- `AccountService.grantProduct()`, `revokeProduct()` → uses AccountDataService
- `AdminUserManagementService.assignProductRole()`, `revokeProductRole()` → uses ProductRoleRepository directly
- `ClientService.getProductRolesByAccountId()`, `grantProduct()`, `revokeProduct()` → delegates to AccountService
- **Recommendation**: Centralize in `AccountService` (or `ProductRoleService`), remove from AdminUserManagementService

### 4. **Duplicate permission logic**
- `checkPermitionAndGetTenantCode()`, `userIsSysAdmin()`, `userIsTntAdmin()` repeated in AdminUserManagementService, ClientAdminsManagementService, LoginManagementService
- **Recommendation**: Extract `AdminPermissionService` or use `AuthorizationService` consistently

### 5. **TenantService dual role**
- Implements `TenantSecurityConfigService` (auth config)
- Also tenant CRUD
- **Recommendation**: Split or rename for clarity

### 6. **AccountResolverService → ClientService**
- AccountResolverService calls `ClientService.getConfig()` during auth (before user exists)
- ClientService mixes auth-flow methods with admin methods
- **Recommendation**: Extract `ClientSecurityConfigService` (interface exists) - separate impl for auth flow

---

## Recommended Refactoring Structure

### Phase 1: Extract shared permission logic

```
ru.pt.auth.service.admin/
├── AdminPermissionHelper.java    // checkPermitionAndGetTenantCode, userIsSysAdmin, userIsTntAdmin
└── (used by AdminUserManagementService, ClientAdminsManagementService, LoginManagementService)
```

### Phase 2: Consolidate admin services

```
Option A: Single AdminManagementService
├── getAdmins(tenantCode, role)
├── createSysAdmin(), createTntAdmin(), createProductAdmin()
├── createClientAdmin(), createGroupAdmin()
├── deleteAdmin()
├── getGroupAdmins(), getGroupUsers(), createGroupUser(), etc.
└── assignProductRole(), revokeProductRole()  // move from AdminUserManagementService

Option B: Keep split but extract shared
├── AdminUserManagementService (SYS, TNT, PRODUCT) → uses AdminPermissionHelper
├── ClientAdminsManagementService (CLIENT, GROUP) → uses AdminPermissionHelper
└── ProductRoleAdminService (assign/revoke) → used by both
```

### Phase 3: Split ClientService

```
ClientService (facade - optional)
├── ClientQueryService      // getClientById, listClients (read-only + auth)
├── ClientCommandService    // createClient, updateClient (write + auth)
└── ClientSecurityConfigProvider  // getConfig(tenant, clientId) - NO auth, for AccountResolverService
```

### Phase 4: Clarify interfaces

| Current | Proposed |
|---------|----------|
| ClientService implements ClientSecurityConfigService | ClientSecurityConfigProvider (new) implements ClientSecurityConfigService |
| TenantService implements TenantSecurityConfigService | Keep or split TenantSecurityConfigProvider |
| AccountService | Keep - already clean |
| AccountLoginService, AccountTokenService | Keep - focused |

---

## Dependency Diagram (Current → Proposed)

### Current (simplified)
```
AccountManagementController
  ├── AccountService
  ├── AccountLoginService  
  ├── AccountTokenService
  └── AdminUserManagementService (unused)

ClientManagementController
  └── ClientService (does too much)

AdminManagementController
  └── AdminUserManagementService

LoginManagementController
  └── LoginManagementService

ProductRoleManagementController
  └── ClientService → AccountService (product roles)
```

### Proposed
```
AccountManagementController
  ├── AccountService
  ├── AccountLoginService
  └── AccountTokenService

ClientManagementController
  ├── ClientQueryService
  └── ClientCommandService

AdminManagementController
  └── AdminManagementService (merged)

LoginManagementController
  └── LoginManagementService (+ AdminPermissionHelper)

ProductRoleManagementController
  └── AccountService (product roles stay here)

Auth flow (filters):
  AccountResolverService → ClientSecurityConfigProvider (lightweight, no auth)
```

---

## Implementation Order

1. **Low risk**: Extract `AdminPermissionHelper`, refactor 3 services to use it ✅
2. **Medium risk**: Merge AdminUserManagementService + ClientAdminsManagementService → AdminManagementService ✅
3. **Medium risk**: Move product role assign/revoke from AdminUserManagementService to AccountService ✅
4. **Higher risk**: Split ClientService into Query/Command/SecurityConfig
5. **Fix**: Remove unused AdminUserManagementService from AccountManagementController constructor ✅
6. **Fix**: ClientManagementController - use or remove ClientAdminsManagementService parameter ✅
