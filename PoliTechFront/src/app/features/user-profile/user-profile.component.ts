import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../shared/services/auth.service';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, AsyncPipe],
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent {
  authService = inject(AuthService);

  get currentUser$() {
    return this.authService.currentUser$;
  }
}

