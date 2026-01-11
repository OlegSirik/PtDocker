import {Component, inject} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import {AuthService, Account} from '../../services/auth.service';
import {AsyncPipe} from '@angular/common';

@Component({
  selector: 'app-toolbar',
  imports: [MatToolbarModule, MatButtonModule, MatIconModule, MatMenuModule, MatDividerModule, RouterLink, AsyncPipe],
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss']
})
export class ToolbarComponent {
  authService = inject(AuthService);
  router = inject(Router);

  login(): void {
    this.router.navigate(['/', this.tenantCode, 'login']);
  }

  logout(): void {
    this.authService.logout();
    this.login();
  }

  navigateToProfile(): void {
    this.router.navigate(['/', this.tenantCode, 'user_profile']);
  }

  setAccountId(accountId: number): void {
    this.authService.setAccountId(accountId);
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
}
