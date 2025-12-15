// auth.guard.ts
import { inject, Injectable } from '@angular/core';
import { Router, CanActivateFn, CanActivate, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

  constructor(private auth: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    return true;
    if (this.auth.isAuthenticated()) {
      return true;
    }

    // Get tenantCode from route params
    const tenantCode = route.params['tenantId'] || '';
    this.router.navigate(['/', tenantCode, 'login']);
    return false;
  }
}
