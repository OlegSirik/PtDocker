import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../shared/services/auth.service';
import { AddonProvidersService, ProviderDto } from '../../../shared/services/api/addon-providers.service';

@Component({
  selector: 'app-sp-provider',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSelectModule,
    MatOptionModule,
    MatSnackBarModule
  ],
  templateUrl: './sp-provider.component.html',
  styleUrls: ['./sp-provider.component.scss']
})
export class SpProviderComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private providersService = inject(AddonProvidersService);
  private snack = inject(MatSnackBar);
  private authService = inject(AuthService);

  provider: ProviderDto = {
    name: '',
    status: 'ACTIVE'
  };
  isNewRecord = true;
  loading = false;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'edit') {
      this.isNewRecord = false;
      this.loading = true;
      this.providersService.getById(id).subscribe({
        next: (p) => {
          this.provider = { ...p };
          this.loading = false;
        },
        error: () => {
          this.snack.open('Ошибка при загрузке провайдера', 'OK', { duration: 2000 });
          this.loading = false;
          this.goBack();
        }
      });
    }
  }

  save(): void {
    if (!this.provider.name?.trim()) {
      this.snack.open('Введите название', 'OK', { duration: 2000 });
      return;
    }

    this.loading = true;
    if (this.isNewRecord) {
      this.providersService.create(this.provider).subscribe({
        next: (saved) => {
          this.snack.open('Сохранено', 'OK', { duration: 2000 });
          this.router.navigate(['/', this.authService.tenant, 'admin', 'addon', 'sp-list']);
        },
        error: () => {
          this.snack.open('Ошибка при создании', 'OK', { duration: 2000 });
          this.loading = false;
        }
      });
    } else {
      if (!this.provider.id) {
        this.snack.open('ID не найден', 'OK', { duration: 2000 });
        this.loading = false;
        return;
      }
      this.providersService.update(this.provider.id.toString(), this.provider).subscribe({
        next: () => {
          this.snack.open('Сохранено', 'OK', { duration: 2000 });
          this.goBack();
        },
        error: () => {
          this.snack.open('Ошибка при сохранении', 'OK', { duration: 2000 });
          this.loading = false;
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/', this.authService.tenant, 'admin', 'addon', 'sp-list']);
  }
}
