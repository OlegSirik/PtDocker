import {Component, inject} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import {AuthService} from '../../services/auth.service';
import {AsyncPipe} from '@angular/common';
import { ThemeService } from '../../theme/theme.service';

/** Имена ролей с бэкенда (см. JWT /auth/me: userRole, authorities). */
export const TOOLBAR_ROLE = {
  SYS_ADMIN: 'SYS_ADMIN',
  TNT_ADMIN: 'TNT_ADMIN',
  GROUP_ADMIN: 'GROUP_ADMIN',
  PRODUCT_ADMIN: 'PRODUCT_ADMIN',  
  ACCOUNT: 'ACCOUNT',
  SUB: 'SUB',
} as const;

@Component({
  selector: 'app-toolbar',
  imports: [MatToolbarModule, MatButtonModule, MatIconModule, MatMenuModule, MatDividerModule, RouterLink, AsyncPipe],
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss']
})
export class ToolbarComponent {
  /** Для шаблона: `@if (hasRole(TOOLBAR_ROLE.SYS_ADMIN))`. */
  readonly TOOLBAR_ROLE = TOOLBAR_ROLE;

  authService = inject(AuthService);
  router = inject(Router);
  themeService = inject(ThemeService);

  /** Удобно в шаблоне рядом с {@link currentUser$} — перерисовка при смене пользователя. */
  hasRole(role: string): boolean {
    return this.authService.hasRole(role);
  }

  hasAnyRole(roles: string[]): boolean {
    return this.authService.hasAnyRole(roles);
  }

  login(): void {
    this.router.navigate(['/', this.tenantCode, 'login']);
  }

  logout(): void {
    this.authService.logout();
    this.login();
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  navigateToProfile(): void {
    this.router.navigate(['/', this.tenantCode, 'user_profile']);
  }

  setAccountId(accountId: number): void {
    this.authService.setAccountId(accountId);
    this.authService.getCurrentUser().subscribe();
  }

  isCurrentAccount(accountId: number): boolean {
    const currentAccountId = this.authService.getAccountId();
    return currentAccountId !== null && currentAccountId === accountId;
  }

  get tenantCode() {
    return this.authService.tenant;
  }
  get currentUser$() {
    return this.authService.currentUser$;
  }

  get isDarkMode(): boolean {
    return this.themeService.isDarkMode;
  }
}
