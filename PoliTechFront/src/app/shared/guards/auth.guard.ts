import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { inject } from '@angular/core';
import { AuthGuardData, createAuthGuard } from 'keycloak-angular';
import {AuthService} from '../services/auth.service';
import {map} from 'rxjs';
import {filter} from 'rxjs/operators';

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

  return authService.isAuthenticated.pipe(
    filter(Boolean),
    map(isAuthenticated => {
      if (isAuthenticated) {
        return true;
      }
      // Get tenantCode from route or AuthService
      const tenantCode = route.params['tenantId'] || authService.tenant || '';
      return router.createUrlTree(['/', tenantCode, 'login']);
    })
  );
  /*
  if (authService.isAuthenticated()) {
    const requiredRole = route.data['role'] as string | string[];

    if (requiredRole) {
      const roles = Array.isArray(requiredRole) ? requiredRole : [requiredRole];

      if (!authService.hasAnyRole(roles)) {
        const tenantCode = route.params['tenantId'] || authService.tenant || '';
        router.navigate(['/', tenantCode, 'forbidden']);
        return false;
      }
    }

    return true;
  }

  const tenantCode = route.params['tenantId'] || authService.tenant || '';
  router.navigate(['/', tenantCode, 'login']);
  return false;*/
};
