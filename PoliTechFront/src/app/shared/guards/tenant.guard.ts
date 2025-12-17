import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Injectable } from '@angular/core';
//import { TenantService } from './tenant.service';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class TenantGuard implements CanActivate {
//constructor(private tenantService: TenantService, private router: Router) {}
  constructor( private router: Router, private authService: AuthService) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    //if route = login return true

    console.log('GUARD ' +route.url);
return true;
    if (route.url[0].path === 'auth') {
      return true;
    }


    const urlTenantCode = route.paramMap.get('tenantId');
    const loginTenantCode = this.authService.tenant;
    console.log('tenant', urlTenantCode, '==', loginTenantCode);
    if (urlTenantCode !== loginTenantCode) {
      this.router.navigate(['/forbidden']);
      return false;
    }

    return true;
  }
}