import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatCardModule } from '@angular/material/card';
import { LoginService, Login } from '../../../../shared/services/api/logins.service';
import { AuthService } from '../../../../shared/services/auth.service';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-login-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    MatCardModule
  ],
  templateUrl: './login-dialog.component.html',
  styleUrls: ['./login-dialog.component.scss']
})
export class LoginDialogComponent {
  loginForm: FormGroup;
  changePassword: boolean = false;
  isEditMode: boolean = false;
  tenantCode: string = '';

  constructor(
    public dialogRef: MatDialogRef<LoginDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { login: Login | null, tenantCode: string },
    private fb: FormBuilder,
    private loginService: LoginService,
    private authService: AuthService
  ) {

    this.isEditMode = !!data.login;
    this.tenantCode = data.tenantCode;

    this.loginForm = this.fb.group({
      fullName: [data.login?.fullName || '', Validators.required],
      position: [data.login?.position || '', Validators.required],
      userLogin: [data.login?.userLogin || '', Validators.required],
      password: ['']
    });

    // If editing, password is not required initially
    if (!this.isEditMode) {
      this.loginForm.get('password')?.setValidators([Validators.required]);
    }
  }

  onPasswordChangeToggle(): void {
    const passwordControl = this.loginForm.get('password');
    if (this.changePassword) {
      passwordControl?.setValidators([Validators.required]);
    } else {
      passwordControl?.clearValidators();
    }
    passwordControl?.updateValueAndValidity();
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.loginForm.invalid) {
      return;
    }

    const formValue = this.loginForm.value;
    
    // Get tenantId from current user
    this.authService.currentUser$.pipe(take(1)).subscribe((user: any) => {
      if (!user) {
        console.error('User not authenticated');
        this.dialogRef.close({ success: false, error: 'User not authenticated' });
        return;
      }

      //const tenantCode = user.tenantCode;
      const loginData: Login = {
        tenantCode: this.tenantCode,
        userLogin: formValue.userLogin,
        password: formValue.password || '',
        fullName: formValue.fullName,
        position: formValue.position
      };

      if (this.isEditMode && this.data.login?.id) {
        // Update existing login
        loginData.id = this.data.login.id;
        this.loginService.update( this.data.login.id, loginData, {'X-Imp-Tenant': this.tenantCode}).subscribe({
          next: () => {
            // If password was changed, update password separately
            if (this.changePassword && formValue.password) {
              this.loginService.updatePassword(formValue.userLogin, this.tenantCode, formValue.password).subscribe({
                next: () => {
                  this.dialogRef.close({ success: true, action: 'update' });
                },
                error: (error: unknown) => {
                  console.error('Error updating password:', error);
                  this.dialogRef.close({ success: false, error });
                }
              });
            } else {
              this.dialogRef.close({ success: true, action: 'update' });
            }
          },
          error: (error: unknown) => {
            console.error('Error updating login:', error);
            this.dialogRef.close({ success: false, error });
          }
        });
      } else {
        // Create new login
        this.loginService.create(loginData, {'X-Imp-Tenant': this.tenantCode}).subscribe({
          next: () => {
            // Update password after creating login
            if (formValue.password) {
              this.loginService.updatePassword(formValue.userLogin, this.tenantCode, formValue.password).subscribe({
                next: () => {
                  this.dialogRef.close({ success: true, action: 'create', login: loginData });
                },
                error: (error: unknown) => {
                  console.error('Error updating password:', error);
                  this.dialogRef.close({ success: false, error });
                }
              });
            } else {
              this.dialogRef.close({ success: true, action: 'create', login: loginData });
            }
          },
          error: (error: unknown) => {
            console.error('Error creating login:', error);
            this.dialogRef.close({ success: false, error });
          }
        });
      }
    });
  }

  onDelete(): void {
    if (!this.isEditMode || !this.data.login?.id) {
      return;
    }

    if (confirm('Вы уверены, что хотите удалить этого пользователя?')) {
      this.loginService.delete(this.data.login.id, {'X-Imp-Tenant': this.tenantCode}).subscribe({
        next: () => {
          this.dialogRef.close({ success: true, action: 'delete' });
        },
        error: (error: unknown) => {
          console.error('Error deleting login:', error);
          this.dialogRef.close({ success: false, error });
        }
      });
    }
  }
}

