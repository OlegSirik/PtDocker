// rest auth - temporary file
import { Component, inject, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../shared/services/auth.service';
import { CommonModule } from '@angular/common';

// Angular Material imports
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatIconModule
  ],
  templateUrl: 'login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);

  loginForm!: FormGroup;
  isLoading = false;
  hidePassword = true;
  returnUrl = '';

  ngOnInit() {
    this.initializeForm();

    // Получаем returnUrl из query параметров или используем корневой путь
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  private initializeForm(): void {
    this.loginForm = this.fb.group({
      userLogin: ['', [Validators.required]],
      password: ['', [Validators.required]],
      clientId: [1, [Validators.required, Validators.min(1)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;

      this.authService.login(this.loginForm.value).subscribe({
        next: () => {
          this.isLoading = false;
          this.snackBar.open('Вход выполнен успешно!', 'Закрыть', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          this.router.navigate([this.returnUrl]);
        },
        error: (error) => {
          this.isLoading = false;
          const errorMessage = error.error?.message || 'Ошибка входа. Проверьте логин и пароль.';

          this.snackBar.open(errorMessage, 'Закрыть', {
            duration: 5000,
            panelClass: ['error-snackbar']
          });
        }
      });
    } else {
      // Показываем ошибки валидации
      Object.keys(this.loginForm.controls).forEach(key => {
        const control = this.loginForm.get(key);
        if (control?.invalid) {
          control.markAsTouched();
        }
      });
    }
  }

  fillDemoCredentials(): void {
    this.loginForm.patchValue({
      userLogin: 'sys_admin',
      password: '5222',
      clientId: 1
    });
  }

  // Геттеры для удобного доступа к полям формы в шаблоне
  get userLogin() { return this.loginForm.get('userLogin'); }
  get password() { return this.loginForm.get('password'); }
  get clientId() { return this.loginForm.get('clientId'); }
}
