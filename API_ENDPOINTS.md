# PoliTech API Endpoints Documentation

This document contains all REST API endpoints from the backend project.

**Base URL Pattern:** Most endpoints follow `/api/v1/{tenantCode}/...` or `/api/v1/{tenantId}/...` pattern  
**Tenant Codes:** pt, vsk, msg

---

## Authentication & Authorization (`/api/auth`)

**Base Path:** `/api/auth`

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/auth/me` | Get current user information | Yes |
| GET | `/api/auth/context` | Get current user context | Yes |
| GET | `/api/auth/check-product-access` | Check product access permissions | Yes |
| POST | `/api/auth/login` | Login with credentials | No |
| POST | `/api/auth/token` | Generate JWT token | No |
| POST | `/api/auth/refresh-token` | Generate refresh token | No |
| POST | `/api/auth/set-password` | Set/update password (SYS_ADMIN) | SYS_ADMIN |

---

## Test Endpoints (`/api/v1/{tenantCode}/test`)

**Base Path:** `/api/v1/{tenantCode}/test`

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/{tenantCode}/test/create-client` | Create client (test) | Yes |
| POST | `/api/v1/{tenantCode}/test/create-group` | Create group (test) | Yes |
| POST | `/api/v1/{tenantCode}/test/create-account` | Create account (test) | Yes |
| POST | `/api/v1/{tenantCode}/test/create-subaccount` | Create subaccount (test) | Yes |
| GET | `/api/v1/{tenantCode}/test/get-product-roles/{accountId}` | Get product roles for account | Yes |
| GET | `/api/v1/{tenantCode}/test/get-account-login` | Get account login info | Yes |
| POST | `/api/v1/{tenantCode}/test/quote/validator` | Validate quote request | Yes |
| POST | `/api/v1/{tenantCode}/test/policy/validator` | Validate policy request | Yes |
| POST | `/api/v1/{tenantCode}/test/policy/printpf/{policy-nr}/{pf-type}` | Print policy form | Yes |

---

## Admin - Tenant Management (`/api/v1/{tenantId}/admin/tenants`)

**Base Path:** `/api/v1/{tenantId}/admin/tenants`  
**Required Role:** SYS_ADMIN

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/{tenantId}/admin/tenants` | List all tenants |
| POST | `/api/v1/{tenantId}/admin/tenants` | Create new tenant |
| DELETE | `/api/v1/{tenantId}/admin/tenants/{tenantResourceId}` | Delete tenant (soft delete) |

---

## Admin - Client Management (`/api/v1/{tenantId}/admin/clients`)

**Base Path:** `/api/v1/{tenantId}/admin/clients`  
**Required Role:** TNT_ADMIN

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/{tenantId}/admin/clients` | List all clients |
| POST | `/api/v1/{tenantId}/admin/clients` | Create new client |

---

## Admin - Account Management (`/api/v1/{tenantCode}/admin/accounts`)

**Base Path:** `/api/v1/{tenantCode}/admin/accounts`  
**Required Role:** GROUP_ADMIN, PRODUCT_ADMIN

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/{tenantCode}/admin/accounts` | Create account |
| GET | `/api/v1/{tenantCode}/admin/accounts/hierarchy` | Get accounts hierarchy |

---

## Admin - User Management (`/api/v1/{tenantId}/admin/users`)

**Base Path:** `/api/v1/{tenantId}/admin/users`  
**Required Role:** PRODUCT_ADMIN

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/{tenantId}/admin/users` | Create user |
| PUT | `/api/v1/{tenantId}/admin/users/{userId}` | Update user |
| DELETE | `/api/v1/{tenantId}/admin/users/{userId}` | Delete user |

---

## Admin - Admin Management (`/api/v1/{tenantId}/admin/admins`)

**Base Path:** `/api/v1/{tenantId}/admin/admins`

### TNT_ADMIN Management (SYS_ADMIN)
| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/api/v1/{tenantId}/admin/admins/tnt-admins` | Create TNT_ADMIN | SYS_ADMIN |
| DELETE | `/api/v1/{tenantId}/admin/admins/tnt-admins/{adminId}` | Delete TNT_ADMIN | SYS_ADMIN |

### GROUP_ADMIN Management (TNT_ADMIN)
| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/api/v1/{tenantId}/admin/admins/group-admins` | Create GROUP_ADMIN | TNT_ADMIN |
| DELETE | `/api/v1/{tenantId}/admin/admins/group-admins/{adminId}` | Delete GROUP_ADMIN | TNT_ADMIN |

### PRODUCT_ADMIN Management (GROUP_ADMIN)
| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/api/v1/{tenantId}/admin/admins/product-admins` | Create PRODUCT_ADMIN | GROUP_ADMIN |
| PUT | `/api/v1/{tenantId}/admin/admins/product-admins/{adminId}` | Update PRODUCT_ADMIN | GROUP_ADMIN |

---

## Admin - Login Management (`/api/v1/{tenantCode}/logins`)

**Base Path:** `/api/v1/{tenantCode}/logins`  
**Required Role:** SYS_ADMIN, TNT_ADMIN, GROUP_ADMIN

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/{tenantCode}/logins` | Create login |
| PATCH | `/api/v1/{tenantCode}/logins/{loginId}` | Update login |
| GET | `/api/v1/{tenantCode}/logins` | Get all logins for tenant |
| DELETE | `/api/v1/{tenantCode}/logins/{loginId}` | Delete login (soft delete) |

---

## Admin - Product Role Management (`/api/admin/product-roles`)

**Base Path:** `/api/admin/product-roles`  
**Required Role:** TNT_ADMIN, GROUP_ADMIN

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/product-roles` | Assign product role |
| PUT | `/api/admin/product-roles/{productRoleId}` | Update product role |
| DELETE | `/api/admin/product-roles/{productRoleId}` | Revoke product role |
| GET | `/api/admin/product-roles/account/{accountId}` | Get product roles for account |

---

## Admin - Products (`/api/v1/{tenantCode}/admin/products`)

**Base Path:** `/api/v1/{tenantCode}/admin/products`  
**Required Role:** SYS_ADMIN

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/{tenantCode}/admin/products` | List all products |
| POST | `/api/v1/{tenantCode}/admin/products` | Create product |
| GET | `/api/v1/{tenantCode}/admin/products/{productId}/versions/{versionNo}` | Get product version |
| POST | `/api/v1/{tenantCode}/admin/products/{productId}/versions/{versionNo}/cmd/create` | Create new version from existing |
| PUT | `/api/v1/{tenantCode}/admin/products/{productId}/versions/{versionNo}` | Update product version |
| DELETE | `/api/v1/{tenantCode}/admin/products/{productId}` | Delete product (soft delete) |
| DELETE | `/api/v1/{tenantCode}/admin/products/{productId}/versions/{versionNo}` | Delete product version |
| GET | `/api/v1/{tenantCode}/admin/products/{productId}/versions/{versionNo}/example_quote` | Get quote example JSON |
| GET | `/api/v1/{tenantCode}/admin/products/{productId}/versions/{versionNo}/example_save` | Get save example JSON |

---

## Admin - LOB (Line of Business) (`/api/v1/{tenantCode}/admin/lobs`)

**Base Path:** `/api/v1/{tenantCode}/admin/lobs`  
**Required Role:** SYS_ADMIN

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/{tenantCode}/admin/lobs` | List all LOBs |
| GET | `/api/v1/{tenantCode}/admin/lobs/{code}` | Get LOB by code |
| POST | `/api/v1/{tenantCode}/admin/lobs` | Create LOB |
| PUT | `/api/v1/{tenantCode}/admin/lobs/{code}` | Update LOB |
| DELETE | `/api/v1/{tenantCode}/admin/lobs/{id}` | Delete LOB (soft delete) |
| GET | `/api/v1/{tenantCode}/admin/lobs/{code}/example` | Get JSON example for LOB |

---

## Admin - Files (`/api/v1/{tenantCode}/admin/files`)

**Base Path:** `/api/v1/{tenantCode}/admin/files`  
**Required Role:** SYS_ADMIN

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/{tenantCode}/admin/files` | List files (optional: ?product_code=xxx) |
| GET | `/api/v1/{tenantCode}/admin/files/{fileId}` | Download file |
| POST | `/api/v1/{tenantCode}/admin/files` | Create file metadata |
| POST | `/api/v1/{tenantCode}/admin/files/{fileId}` | Upload file (multipart/form-data) |
| DELETE | `/api/v1/{tenantCode}/admin/files/{fileId}` | Delete file (soft delete) |
| POST | `/api/v1/{tenantCode}/admin/files/{fileId}/cmd/process` | Process file with key-value pairs |

---

## Admin - Calculators (`/api/v1/{tenantCode}/admin/calculators`)

**Base Path:** `/api/v1/{tenantCode}/admin/calculators`  
**Required Role:** SYS_ADMIN

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/{tenantCode}/admin/calculators/products/{productId}/versions/{versionNo}/packages/{packageNo}` | Get calculator |
| POST | `/api/v1/{tenantCode}/admin/calculators/products/{productId}/versions/{versionNo}/packages/{packageNo}` | Create calculator |
| PUT | `/api/v1/{tenantCode}/admin/calculators/products/{productId}/versions/{versionNo}/packages/{packageNo}` | Replace calculator |
| GET | `/api/v1/{tenantCode}/admin/calculators/{calculatorId}/coefficients/{code}` | Get coefficients table |
| POST | `/api/v1/{tenantCode}/admin/calculators/{calculatorId}/coefficients/{code}` | Create coefficients table |
| PUT | `/api/v1/{tenantCode}/admin/calculators/{calculatorId}/coefficients/{code}` | Replace coefficients table |
| POST | `/api/v1/{tenantCode}/admin/calculators/{calculatorId}/prc/syncvars` | Sync calculator variables |

---

## Sales - Policies (`/api/v1/{tenantCode}/sales/policies`)

**Base Path:** `/api/v1/{tenantCode}/sales/policies`  
**Required:** Authentication

| Method | Endpoint | Description | Required Permission |
|--------|----------|-------------|---------------------|
| POST | `/api/v1/{tenantCode}/sales/policies` | Create policy | POLICY |
| PUT | `/api/v1/{tenantCode}/sales/policies/{policyNumber}` | Update policy | ADDENDUM |
| GET | `/api/v1/{tenantCode}/sales/policies/{policyId}` | Get policy by ID | READ |
| GET | `/api/v1/{tenantCode}/sales/policies/by-number/{policyNumber}` | Get policy by number | READ |
| GET | `/api/v1/{tenantCode}/sales/policies/by-account/{userAccountId}` | Get policies by account | READ |
| POST | `/api/v1/{tenantCode}/sales/policies/{policyNumber}/paid` | Mark policy as paid | - |

---

## Numbers (`/api/v1/numbers`)

**Base Path:** `/api/v1/numbers`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/numbers/next` | Get next number |
| POST | `/api/v1/numbers` | Create number generator |
| PUT | `/api/v1/numbers` | Update number generator |

---

## Notes

- **Path Variables:**
  - `{tenantCode}` or `{tenantId}`: Tenant identifier (pt, vsk, msg)
  - `{productId}`: Product ID (integer)
  - `{versionNo}`: Version number (integer)
  - `{packageNo}`: Package number (integer)
  - `{calculatorId}`: Calculator ID (integer)
  - `{fileId}`: File ID (long)
  - `{loginId}`: Login ID (long)
  - `{userId}`: User ID (long)
  - `{accountId}`: Account ID (long)
  - `{policyId}`: Policy ID (UUID)
  - `{policyNumber}`: Policy number (string)
  - `{code}`: Code (string)

- **Authentication:** Most endpoints require JWT token in Authorization header: `Bearer <token>`

- **Roles Hierarchy:**
  - SYS_ADMIN (highest)
  - TNT_ADMIN
  - GROUP_ADMIN
  - PRODUCT_ADMIN
  - USER (lowest)
