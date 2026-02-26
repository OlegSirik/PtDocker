# API URL Analysis: Account Management & Account Admins Management

## Current Controllers Overview

### 1. Account Management

| Controller | Base URL | Purpose |
|------------|----------|---------|
| **AccountManagementController** | `/api/v1/{tenantCode}/admin/accounts` | Account hierarchy, tokens, logins, product roles |

**Current endpoints:**

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/{accountId}` | Get account by ID |
| GET | `/{accountId}/accounts` | Get child accounts |
| POST | `/{accountId}/accounts` | Create account |

| GET | `/{accountCode}/groups` | Get groups under account |
| POST | `/{accountId}/groups` | Create group |

| GET | `/{accountId}/subaccounts` | Get subaccounts |
| POST | `/{accountId}/subaccounts` | Create subaccount |

| GET | `/{accountId}/tokens` | Get account tokens |
| POST | `/{accountId}/tokens` | Create token |
| DELETE | `/{accountId}/tokens/{token}` | Delete token |

| GET | `/{accountId}/logins` | Get logins bound to account |
| POST | `/{accountId}/logins` | Create login binding |
| DELETE | `/{accountId}/logins/{userLogin}` | Delete login binding |

| GET | `/{accountId}/path` | Get path from acting account to specified account |
| GET | `/{accountId}/products` | Get product roles for account |
| GET | `/{accountId}/products/{productId}` | Get specific product role |
| POST | `/{accountId}/products` | Grant/update product role |
| PUT | `/{accountId}/products` | Grant/update product role |
| DELETE | `/{accountId}/products/{productId}` | Revoke product role |

**Service:** AccountService, AccountTokenService, AccountLoginService, AdminManagementService (injected but **not used**)

---

### 2. Admin Management (System/Tenant/Product Admins)

| Controller | Base URL | Purpose |
|------------|----------|---------|
| **AdminManagementController** | `/api/v1/{tenantCode}/admins/roles` | SYS_ADMIN, TNT_ADMIN, PRODUCT_ADMIN |

**Current endpoints:**

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/{roleName}` | Get admins by role (sys-admin, tnt-admin, product-admin) |
| POST | (root) | Create admin |
| DELETE | `/{roleId}` | Delete admin by account-login ID |

**Service:** AdminManagementService

**Note:** URL path inconsistency: `admins/roles` vs other controllers using `admin/` prefix.

---

### 3. Client Admins (CLIENT_ADMIN)

**Status:** No dedicated controller. AdminManagementService has:
- `getClientAdmins(tenantCode, clientId)`
- `createClientAdmin(tenantCode, authClientId, userLogin)`
- `deleteClientAdmin(accountLoginId)`

**Gap:** These methods are not exposed via REST API.

---

### 4. Group Admins (GROUP_ADMIN)

**Status:** AdminManagementService had `createGroupAdmin`, `deleteGroupAdmin` in the merged service, but they were removed during refactoring. **Gap:** No API for GROUP_ADMIN management.

---

### 5. Logins (User CRUD)

| Controller | Base URL | Purpose |
|------------|----------|---------|
| **LoginManagementController** | `/api/v1/{tenantCode}/admin/logins` | Login CRUD, passwords |

| Method | URL | Description |
|--------|-----|-------------|
| POST | (root) | Create login |
| PUT | `/{loginId}` | Update login |
| GET | (root) | Get logins by tenant |
| DELETE | `/{loginId}` | Delete (soft) login |

---

### 6. Product Roles (standalone)

| Controller | Base URL | Purpose |
|------------|----------|---------|
| **ProductRoleManagementController** | `/api/admin/product-roles` | Assign/revoke product roles (TNT_ADMIN, GROUP_ADMIN) |

**Note:** No `{tenantCode}` in path — inconsistent with other admin APIs.

---

## URL Pattern Inconsistencies

| Controller | Pattern | Issue |
|------------|---------|-------|
| AccountManagementController | `/api/v1/{tenantCode}/admin/accounts` | ✅ Consistent |
| AdminManagementController | `/api/v1/{tenantCode}/admins/roles` | Uses `admins` not `admin` |
| LoginManagementController | `/api/v1/{tenantCode}/admin/logins` | ✅ Consistent |
| ClientManagementController | `/api/v1/{tenantCode}/admin/clients` | ✅ Consistent |
| ProductRoleManagementController | `/api/admin/product-roles` | Missing `{tenantCode}` |

---

## Proposed Unified API Structure

### Account Management
**Base:** `/api/v1/{tenantCode}/admin/accounts`

Keep current structure. Optional cleanup:
- Consider merging POST/PUT for products into single endpoint

### Account Admins Management (Unified)
**Base:** `/api/v1/{tenantCode}/admin/admins`

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/{roleName}` | List admins by role | SYS_ADMIN, TNT_ADMIN |
| POST | (root) | Create admin | SYS_ADMIN, TNT_ADMIN |
| DELETE | `/{accountLoginId}` | Delete admin | SYS_ADMIN, TNT_ADMIN |

**Role names:** `sys-admin`, `tnt-admin`, `product-admin`, `client-admin`, `group-admin`

### Client Admins (under clients)
**Base:** `/api/v1/{tenantCode}/admin/clients/{clientId}/admins`

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | (root) | List CLIENT_ADMIN for client | TNT_ADMIN |
| POST | (root) | Create CLIENT_ADMIN | TNT_ADMIN |
| DELETE | `/{accountLoginId}` | Delete CLIENT_ADMIN | TNT_ADMIN |

### Group Admins (under accounts)
**Base:** `/api/v1/{tenantCode}/admin/accounts/{accountId}/admins`

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/group-admins` | List GROUP_ADMIN for group | TNT_ADMIN |
| POST | `/group-admins` | Create GROUP_ADMIN | TNT_ADMIN |
| DELETE | `/group-admins/{accountLoginId}` | Delete GROUP_ADMIN | TNT_ADMIN |

### Product Roles (fix consistency)
**Proposed:** `/api/v1/{tenantCode}/admin/product-roles`

Add `{tenantCode}` for consistency.

---

## Summary: Recommended Changes

1. **Rename AdminManagementController base:**  
   `/api/v1/{tenantCode}/admins/roles` → `/api/v1/{tenantCode}/admin/admins`

2. **Add Client Admins API:**  
   Under ClientManagementController or new sub-resource:  
   `/api/v1/{tenantCode}/admin/clients/{clientId}/admins`

3. **Add Group Admins API:**  
   Under AccountManagementController:  
   `/api/v1/{tenantCode}/admin/accounts/{accountId}/group-admins`

4. **Fix ProductRoleManagementController:**  
   Add `{tenantCode}`: `/api/v1/{tenantCode}/admin/product-roles`

5. **Restore AdminManagementService methods** (if removed):  
   - `createGroupAdmin`, `deleteGroupAdmin`  
   - `getClientAdmins` (expose via new endpoint)
