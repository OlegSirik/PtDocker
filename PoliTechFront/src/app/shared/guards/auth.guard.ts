import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { inject } from '@angular/core';
import { AuthGuardData, createAuthGuard } from 'keycloak-angular';
import {AuthService} from '../services/auth.service';
import {map} from 'rxjs';
import {filter} from 'rxjs/operators';
import { BASE_URL } from '../tokens';

const isAccessAllowed = async (
  route: ActivatedRouteSnapshot,
  _: RouterStateSnapshot,
  authData: AuthGuardData
): Promise<boolean | UrlTree> => {
  console.log('authData', authData, route, _);
  const { authenticated, grantedRoles } = authData;

  if (!authenticated) {
    authData.keycloak.login({redirectUri: window.location.origin + _.url});
    return false;
  } else {
    return true;
  }

  /*const requiredRole = route.data['role'];
  const hasRequiredRole = (role: string): boolean =>
    Object.values(grantedRoles.resourceRoles).some((roles) => roles.includes(role));

  if (authenticated && hasRequiredRole(requiredRole)) {
    return true;
  }

  const router = inject(Router);
  return router.parseUrl('/forbidden');*/
};

export const authGuardKC = createAuthGuard<CanActivateFn>(isAccessAllowed);

export const authGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const baseUrl = inject(BASE_URL);

  return authService.isAuthenticated.pipe(
    filter(Boolean),
    map(isAuthenticated => {
      if (isAuthenticated) {
        return true;
      }
      // Get tenantCode from route or AuthService
      const tenantCode = route.params['tenantId'] || authService.getTenantCode() || '';
      if (tenantCode) {
        window.location.href = `${baseUrl}/${tenantCode}/login`;
        return false;
      } else {
        window.location.href = `${baseUrl}/login`;
        return false;
      }
    })
  );
  /*
  if (authService.isAuthenticated()) {
    const requiredRole = route.data['role'] as string | string[];

    if (requiredRole) {
      const roles = Array.isArray(requiredRole) ? requiredRole : [requiredRole];

      if (!authService.hasAnyRole(roles)) {
        const tenantCode = route.params['tenantId'] || authService.tenant || '';
        if (tenantCode) {
          router.navigate(['/', tenantCode, 'forbidden']);
        } else {
          router.navigate(['/forbidden']);
        }
        return false;
      }
    }

    return true;
  }

  const tenantCode = route.params['tenantId'] || authService.tenant || '';
  if (tenantCode) {
    router.navigate(['/', tenantCode, 'login']);
  } else {
    router.navigate(['/login']);
  }
  return false;*/
};
