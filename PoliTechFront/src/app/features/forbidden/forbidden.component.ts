import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-forbidden',
  imports: [CommonModule],
  templateUrl: './forbidden.component.html',
  styleUrls: ['./forbidden.component.scss']
})
export class ForbiddenComponent {
  private router = inject(Router);
  authService = inject(AuthService);

  goHome(): void {
    // Navigate to tenant home if tenant exists, otherwise stay on forbidden
    if (this.authService.tenant) {
      this.router.navigate(['/', this.authService.tenant]);
    }
  }

  goBack(): void {
    window.history.back();
  }
}
