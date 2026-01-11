# NotFoundException Usage Audit - Final Report

This document lists all places in the project where a "not found" scenario throws an exception other than `NotFoundException`. These should be refactored to use `NotFoundException` for consistency and proper HTTP status codes (404).

## Summary

**Total Issues Found: 5** (Additional issues discovered)

**Status**: Previous 6 issues have been fixed. 5 new issues discovered.

---

## ✅ Previously Fixed Issues (Verified)

1. ✅ **PreProcessServiceImpl.java** - Line 69: Fixed `IllegalArgumentException` → `NotFoundException`
2. ✅ **CalculatorServiceImpl.java** - Lines 261, 267: Fixed `IllegalArgumentException` → `NotFoundException`
3. ✅ **ProductServiceImpl.java** - Line 188: Fixed `IllegalArgumentException` → `NotFoundException`
4. ✅ **TenantService.java** - Line 223: Fixed `IllegalArgumentException` → `NotFoundException`
5. ✅ **JwtAuthenticationFilter.java** - Line 150: Fixed `BadRequestException` → `NotFoundException`
6. ✅ **NoAuthenticationStrategy.java** - Line 39: Fixed `BadCredentialsException` → `NotFoundException`

---

## 🔴 Remaining Issues

### 1. `pt-calculator/src/main/java/ru/pt/calculator/service/CalculatorServiceImpl.java`

**Line 219**: `IllegalArgumentException` → Should be `NotFoundException`
```java
CalculatorEntity entity = calculatorRepository.findByKeys(getCurrentTenantId(), productId, versionNo, packageNo)
        .orElseThrow(() -> new IllegalArgumentException("Calculator not found for productId=" + productId + ", versionNo=" + versionNo + ", packageNo=" + packageNo));
```
**Context**: Calculator entity lookup fails.
**Reason**: Calculator entity not found - should return 404, not 400.

**Line 248**: `IllegalArgumentException` → Should be `NotFoundException`
```java
CalculatorEntity entity = calculatorRepository.findById(calculatorId)
        .orElseThrow(() -> new IllegalArgumentException("Calculator not found for id=" + calculatorId));
```
**Context**: Calculator entity lookup by ID fails.
**Reason**: Calculator entity not found - should return 404.

---

### 2. `pt-auth/src/main/java/ru/pt/auth/service/ClientService.java`

**Line 64**: `IllegalArgumentException` → Should be `NotFoundException`
```java
return clientRepository.findByTenantCodeAndAuthClientId(tenantCode, authClientId)
        .map(this::mapToDomain)
        .orElseThrow(() -> new IllegalArgumentException("Client not found: " + tenantCode + " " + authClientId));
```
**Context**: Client entity lookup fails in `getConfig()` method.
**Reason**: Client entity not found - should return 404.

**Note**: This is similar to the issue fixed in `AccountResolverService.java` line 49, but this is a different method (`getConfig` vs `resolveAccounts`).

---

### 3. `pt-launcher/src/main/java/ru/pt/api/sales/FileController.java`

**Line 73**: `IllegalArgumentException` → Should be `NotFoundException`
```java
TenantEntity tenant = tenantService.findByCode(tenantCode)
        .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
```
**Context**: Tenant entity lookup fails in file upload endpoint.
**Reason**: Tenant entity not found - should return 404.

---

### 4. `pt-calculator/src/main/java/ru/pt/calculator/service/CoefficientServiceImpl.java`

**Line 57**: `IllegalArgumentException` → Should be `NotFoundException`
```java
CoefficientDataEntity entity = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Coefficient row not found: " + id));
```
**Context**: Coefficient entity lookup fails in `update()` method.
**Reason**: Coefficient entity not found - should return 404.

---

## Exceptions That Are Correctly Used

### `UsernameNotFoundException` (Spring Security)
- `pt-auth/src/main/java/ru/pt/auth/service/AccountResolverService.java:78` - User login not found
- `pt-auth/src/main/java/ru/pt/auth/service/AccountResolverService.java:85` - No accounts found for user
- `pt-auth/src/main/java/ru/pt/auth/security/UserDetailsServiceImpl.java:92, 95, 110` - User/account not found in authentication context

**Reason**: These are Spring Security-specific exceptions for authentication scenarios, which is appropriate.

### `IllegalStateException` (Context/State Issues)
- `pt-auth/src/main/java/ru/pt/auth/service/AccountResolverService.java:38` - TenantContext not set
- `pt-auth/src/main/java/ru/pt/auth/service/AccountResolverService.java:41` - ClientContext not set
- `pt-auth/src/main/java/ru/pt/auth/service/AccountResolverService.java:57` - Default account not set
- `pt-auth/src/main/java/ru/pt/auth/service/AccountResolverService.java:61` - Account in context is invalid
- `pt-auth/src/main/java/ru/pt/auth/security/UserDetailsServiceImpl.java:62, 67` - Context not set

**Reason**: These are state/configuration issues, not "not found" scenarios. `IllegalStateException` is appropriate.

### `BadRequestException` (Validation/Input Issues)
- Various validation errors (empty fields, invalid formats, etc.)
- Duplicate entity creation attempts
- Invalid enum values

**Reason**: These are input validation errors (400), not "not found" scenarios (404).

### `IllegalArgumentException` (Invalid Arguments)
- Account type validation errors (e.g., "Account must be of type CLIENT")
- Invalid parent account types
- Unknown variables in VariableContext

**Reason**: These are argument validation errors, not "not found" scenarios.

---

## Recommendations

1. **Replace all `IllegalArgumentException` with "not found" messages** → Use `NotFoundException`
2. **Ensure proper HTTP status codes**: `NotFoundException` returns 404, which is semantically correct for "resource not found" scenarios
3. **Maintain consistency**: All entity lookup failures should use `NotFoundException`

---

## Files to Update

1. `pt-calculator/src/main/java/ru/pt/calculator/service/CalculatorServiceImpl.java` (2 issues)
2. `pt-auth/src/main/java/ru/pt/auth/service/ClientService.java` (1 issue)
3. `pt-launcher/src/main/java/ru/pt/api/sales/FileController.java` (1 issue)
4. `pt-calculator/src/main/java/ru/pt/calculator/service/CoefficientServiceImpl.java` (1 issue)

---

## Quick Fix Summary

```java
// Before:
.orElseThrow(() -> new IllegalArgumentException("Calculator not found for productId=" + productId + ", versionNo=" + versionNo + ", packageNo=" + packageNo));
.orElseThrow(() -> new IllegalArgumentException("Calculator not found for id=" + calculatorId));
.orElseThrow(() -> new IllegalArgumentException("Client not found: " + tenantCode + " " + authClientId));
.orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
.orElseThrow(() -> new IllegalArgumentException("Coefficient row not found: " + id));

// After:
.orElseThrow(() -> new NotFoundException("Calculator not found for productId=" + productId + ", versionNo=" + versionNo + ", packageNo=" + packageNo));
.orElseThrow(() -> new NotFoundException("Calculator not found for id=" + calculatorId));
.orElseThrow(() -> new NotFoundException("Client not found: " + tenantCode + " " + authClientId));
.orElseThrow(() -> new NotFoundException("Tenant not found"));
.orElseThrow(() -> new NotFoundException("Coefficient row not found: " + id));
```

---

## Summary Statistics

- **Total Issues Found**: 11 (6 fixed + 5 remaining)
- **Fixed**: 6 ✅
- **Remaining**: 5 🔴
- **Correctly Used**: Multiple (Spring Security exceptions, state exceptions, validation exceptions)

---

## Notes

- Some exceptions in authentication/security contexts may intentionally use different exception types for security reasons (to avoid revealing whether a user exists).
- Spring Security exceptions (`UsernameNotFoundException`, `BadCredentialsException`) are appropriate for authentication scenarios.
- `IllegalStateException` is appropriate for context/state issues, not "not found" scenarios.
- All entity lookup failures should consistently use `NotFoundException` to return proper HTTP 404 status codes.
