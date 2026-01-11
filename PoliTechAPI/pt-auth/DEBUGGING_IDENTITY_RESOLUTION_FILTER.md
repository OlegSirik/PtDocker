# Debugging IdentityResolutionFilter

## Why requests might not reach IdentityResolutionFilter

The `IdentityResolutionFilter` has several early return conditions that can cause it to skip execution:

### 1. **Public Request Check** (Line 36-39)
```java
if (isPublicRequest(request)) {
    filterChain.doFilter(request, response);
    return;
}
```
**Check**: Is your request URL matching any pattern in `security.public-urls`?

### 2. **Already Authenticated** (Line 41-47)
```java
Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
if (existingAuth != null && existingAuth.isAuthenticated()) {
    filterChain.doFilter(request, response);
    return;
}
```
**Check**: Is there already an authentication in SecurityContext? This could happen if:
- Another filter (like JWT filter) authenticated the user before
- Session-based authentication is active

### 3. **Missing Tenant or AccountId** (Line 49-56)
```java
String tenant = requestContext.getTenant();
String accountId = requestContext.getAccount();
if (tenant == null || accountId == null) {
    filterChain.doFilter(request, response);
    return;
}
```
**Check**: 
- Is `TenantResolutionFilter` setting the tenant correctly?
- Is `AccountResolutionFilter` setting the accountId correctly?

## Filter Chain Order

The filters execute in this order:
1. `TenantResolutionFilter` - Sets tenant in RequestContext
2. `AccountResolutionFilter` - Sets accountId in RequestContext
3. **`IdentityResolutionFilter`** - Should authenticate using accountId
4. `TenantImpersonationFilter`
5. `ContextCleanupFilter`

## How to Debug

### Step 1: Add Logging to IdentityResolutionFilter

Add logging at the start of `doFilterInternal`:

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
        throws ServletException, IOException {

    logger.debug("IdentityResolutionFilter: Processing request {}", request.getRequestURI());
    
    // Check public request
    if (isPublicRequest(request)) {
        logger.debug("IdentityResolutionFilter: Skipping - public request");
        filterChain.doFilter(request, response);
        return;
    }

    Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
    logger.debug("IdentityResolutionFilter: Existing auth = {}", existingAuth);
    
    if (existingAuth != null && existingAuth.isAuthenticated()) {
        logger.debug("IdentityResolutionFilter: Skipping - already authenticated");
        filterChain.doFilter(request, response);
        return;
    }

    String tenant = requestContext.getTenant();
    String accountId = requestContext.getAccount();
    logger.debug("IdentityResolutionFilter: tenant={}, accountId={}", tenant, accountId);
    
    if (tenant == null || accountId == null) {
        logger.debug("IdentityResolutionFilter: Skipping - missing tenant or accountId");
        filterChain.doFilter(request, response);
        return;
    }
    
    logger.debug("IdentityResolutionFilter: Proceeding with authentication for accountId={}", accountId);
    // ... rest of the code
}
```

### Step 2: Check Filter Registration

Verify the filter is registered correctly:

1. Check `SecurityFilterConfig.java` - `identityResolutionFilter` bean exists
2. Check `SecurityConfig.java` - filter is added to chain with `.addFilterAfter(identityResolutionFilter, AccountResolutionFilter.class)`
3. Check application logs for filter chain initialization

### Step 3: Verify RequestContext Population

Add logging to `AccountResolutionFilter` to verify it's setting accountId:

```java
accountResolverService.resolveAccounts();
String accountId = requestContext.getAccount();
logger.debug("AccountResolutionFilter: Set accountId={}", accountId);
```

### Step 4: Check for Other Authentication Filters

Look for any filters that might authenticate before `IdentityResolutionFilter`:
- JWT filters
- OAuth2 filters
- Session-based authentication

### Step 5: Enable Debug Logging

Add to `application.yml`:
```yaml
logging:
  level:
    ru.pt.auth.security.filter: DEBUG
    org.springframework.security: DEBUG
```

## Common Issues

1. **AccountResolutionFilter fails silently**: If `AccountResolutionFilter` doesn't set accountId, `IdentityResolutionFilter` will skip
2. **JWT filter authenticates first**: If there's a JWT filter before these filters, it might authenticate the user
3. **Public URL matching**: Check if your URL pattern matches public URLs
4. **RequestContext scope**: Ensure `RequestContext` is request-scoped and shared between filters

## Quick Test

Add this temporary logging at the very beginning of `doFilterInternal`:

```java
logger.error("=== IdentityResolutionFilter HIT === URI: {}", request.getRequestURI());
```

If you don't see this log, the filter is not being called at all (registration issue).
If you see it but execution stops, check which early return condition is triggered.

