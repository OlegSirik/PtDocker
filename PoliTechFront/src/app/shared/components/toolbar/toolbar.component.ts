import {Component, inject} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {AuthService} from '../../services/auth.service';
import {AsyncPipe} from '@angular/common';

@Component({
  selector: 'app-toolbar',
  imports: [MatToolbarModule, MatButtonModule, MatIconModule, RouterLink, AsyncPipe],
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

  get tenantCode() {
    return this.authService.tenant;
  }
  get currentUser$() {
    return this.authService.currentUser$;
  }
}
