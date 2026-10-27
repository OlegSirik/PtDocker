import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Injectable } from '@angular/core';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class TenantGuard implements CanActivate {
  constructor(private router: Router, private authService: AuthService) {}

  canActivate(route: ActivatedRouteSnapshot, _state: RouterStateSnapshot): boolean {
    const childPath = route.firstChild?.url[0]?.path;
    if (childPath === 'auth' || childPath === 'login' || childPath === 'forbidden') {
      return true;
    }

    const urlTenantCode = route.paramMap.get('tenantId') || route.paramMap.get('tenantCode');
    if (!urlTenantCode) {
      return true;
    }

    if (this.authService.isAuthenticated.value !== true) {
      return true;
    }

    if (this.authService.hasRole('SYS_ADMIN')) {
      return true;
    }

    const loginTenantCode = this.authService.getTenantCode();
    if (loginTenantCode && urlTenantCode.toLowerCase() !== loginTenantCode.toLowerCase()) {
      this.router.navigate(['/forbidden']);
      return false;
    }

    return true;
  }
}
