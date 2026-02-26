# PoliTechFront Security Analysis

## Executive Summary

The frontend security layer has evolved through multiple approaches (Keycloak, REST auth, alternate auth service) and shows signs of incomplete migration and inconsistent design. Several critical issues can cause navigation hangs, lost session state, and weak route protection.

---

## 1. Architecture Overview

### 1.1 Two Auth Service Implementations (Dead Code)

| Location | Status | Used By |
|----------|--------|---------|
| `shared/services/auth.service.ts` | **Active** | authGuard, authInterceptor, login, toolbar, app.component, most services |
| `shared/services/auth/auth.service.ts` | **Removed** | Was unused; deleted along with `auth/auth.guard.ts` and `auth/auth.interceptor.ts` |

The `auth/auth.service.ts` uses different models (`UserProfile`, `UserAccountSummary`), different storage keys (`auth.jwt`, `auth.profile`), and different endpoints (`/acc/me/profile`). It is effectively dead code and should be removed or consolidated.

### 1.2 Auth Flow (Active)

```
LoginComponent
    → POST /api/v1/{tenant}/auth/login
    → Store accessToken in localStorage
    → Call getCurrentUser() → GET /api/v1/{tenant}/auth/me
    → Store User in BehaviorSubject, isAuthenticated = true
    → Redirect to returnUrl

App init (AppComponent.ngOnInit)
    → initializeAuthState() if token exists
    → getCurrentUser() → update isAuthenticated
```

---

## 2. Session Storage

### 2.1 What Is Stored

| Key | Location | Persists on Refresh |
|-----|----------|---------------------|
| `accessToken` | localStorage | ✅ Yes |
| `tenant_code` | localStorage | ✅ Yes |
| `accountId` | **In-memory only** (`AuthService.accountId`) | ❌ **No** |

### 2.2 Critical Issue: Account Selection Lost on Refresh

When the user switches account in the toolbar (`setAccountId`), the choice is stored only in memory. On page refresh:

1. `initializeAuthState()` runs
2. `getCurrentUser()` returns user with default `accountId`
3. `setAccountId(user.accountId)` overwrites with default
4. User's account switch is lost

**Impact:** API calls after refresh use the wrong account until the user manually switches again.

**Fix:** Persist `accountId` in localStorage (e.g. `account_id`) and restore it in `initializeAuthState()`.

---

## 3. Permissions / Roles

### 3.1 Data Model

From `/auth/me` response:

```typescript
interface User {
  userRole: string;           // e.g. "SYS_ADMIN"
  authorities: Authority[];   // e.g. [{ authority: "ROLE_SYS_ADMIN" }]
  productRoles: any[];
  accounts?: Account[];
}
```

### 3.2 Permission Checks

- `hasRole(role)` – checks `userRole` and `authorities`
- `hasAnyRole(roles)` – checks if user has any of the given roles

### 3.3 Route-Level Role Checks: Disabled

In `auth.guard.ts`, role-based route protection is commented out:

```typescript
// Lines 60–86: role check is commented out
// if (requiredRole) {
//   if (!authService.hasAnyRole(roles)) {
//     router.navigate(['/', tenantCode, 'forbidden']);
//     ...
```

**Impact:** All authenticated users can access all protected routes. No route-level permission enforcement.

### 3.4 Component-Level Permission Checks

Components use `authService.hasRole()` / `hasAnyRole()` for UI (e.g. hiding buttons), but routes do not enforce the same roles. A user can still reach a route and see 403 from the API.

---

## 4. Auth Guard Issues

### 4.1 Guard Logic Bug: Unauthenticated Users Block Forever

```typescript
return authService.isAuthenticated.pipe(
  filter(Boolean),   // ← Filters out null AND false
  map(isAuthenticated => { ... })
);
```

- `isAuthenticated` starts as `null`
- When user has no token: `initializeAuthState()` returns `null`, `isAuthenticated` stays `null`
- `filter(Boolean)` never emits for `null` or `false`
- Guard never resolves → navigation hangs indefinitely

**Fix:** Handle all states explicitly:

```typescript
return authService.isAuthenticated.pipe(
  filter(v => v !== null),  // Wait only for loading; allow false through
  take(1),
  map(isAuthenticated => {
    if (isAuthenticated) return true;
    const tenantCode = route.params['tenantId'] || authService.getTenantCode() || '';
    window.location.href = tenantCode 
      ? `${baseUrl}/${tenantCode}/login` 
      : `${baseUrl}/login`;
    return false;
  })
);
```

Also ensure `isAuthenticated` is set to `false` when there is no token (e.g. in `initializeAuthState()` when `getToken()` is null).

### 4.2 Race Condition: Guard vs. Auth Init

- `AppComponent.ngOnInit` calls `initializeAuthState()?.subscribe()`
- Navigation can happen before `getCurrentUser()` completes
- Until then, `isAuthenticated` is `null` → guard blocks

**Fix:** Use `APP_INITIALIZER` to block app bootstrap until auth state is resolved, or have the guard trigger `initializeAuthState()` when `isAuthenticated === null` and token exists, then wait for the result.

---

## 5. TenantGuard: Effectively Disabled

```typescript
canActivate(...): boolean {
  console.log('GUARD ' + route.url);
  return true;   // ← Always allows! Logic below never runs
  if (route.url[0].path === 'auth') { ... }
  ...
}
```

Tenant validation is bypassed. Any tenant code in the URL is accepted.

---

## 6. Auth Interceptor

- Adds `Authorization: Bearer {token}` when token exists
- Adds `X-Account-Id` when `accountId` is set
- 401 → redirect to login
- 403 → shows error for non-GET; does not redirect

Behavior is mostly correct; the main risk is `accountId` being wrong or missing after refresh.

---

## 7. Keycloak: Configured but Disabled

- `keycloak.config.ts` – Keycloak config present
- `app.config.ts` – Keycloak provider commented out
- `authGuardKC` – defined but not used in routes
- `auth.guard.ts` – imports Keycloak types but uses REST auth

This adds noise and potential confusion. Either remove Keycloak code or document the migration plan.

---

## 8. BASE_URL and API Base URL

- `BASE_URL` token defaults to `window.location.origin`
- Auth redirect uses `BASE_URL` for login URL (correct for SPA)
- `AuthService.baseApiUrl` = `${baseUrl}/api/v1/${tenant}`

If frontend and API are on different origins, `BASE_URL` must be set via `__env.BASE_URL` for API calls. The same token is used for both redirects and API base URL; ensure configuration matches your deployment.

---

## 9. Recommendations

### High Priority

1. **Fix auth guard** – Handle `null` and `false` for `isAuthenticated`; avoid indefinite blocking.
2. **Persist accountId** – Store selected account in localStorage and restore on init.
3. **Set isAuthenticated to false when no token** – In `initializeAuthState()`, when `getToken()` is null, call `isAuthenticated.next(false)`.

### Medium Priority

4. **Re-enable or remove role-based route protection** – Either use `route.data['role']` in the guard or remove the commented code.
5. **Fix TenantGuard** – Remove the early `return true` and implement tenant validation.
6. **Remove duplicate auth code** – Delete or merge `auth/auth.service.ts` and related unused guards/interceptors.

### Low Priority

7. **Clean up Keycloak** – Remove or fully integrate Keycloak.
8. **Remove debug logs** – e.g. `console.log` in guards and interceptors.
9. **Clarify BASE_URL** – Document when it points to frontend vs API and how `__env` overrides work.

---

## 10. File Reference

| File | Purpose |
|------|---------|
| `shared/services/auth.service.ts` | Main auth service (REST) |
| `shared/services/auth/auth.service.ts` | Unused alternate auth |
| `shared/guards/auth.guard.ts` | Route guard (has bugs) |
| `shared/guards/tenant.guard.ts` | Tenant guard (disabled) |
| `shared/interceptors/auth.interceptor.ts` | HTTP auth + error handling |
| `features/login/login.component.ts` | Login form |
| `shared/components/toolbar/toolbar.component.ts` | User menu, logout, account switch |
| `app.component.ts` | Calls `initializeAuthState()` |
| `app.config.ts` | Registers interceptors, Keycloak commented out |
