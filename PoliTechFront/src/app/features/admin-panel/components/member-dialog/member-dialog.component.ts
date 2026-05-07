import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

import { AccountService } from '../../../../shared/services/account.service';

export interface MemberDialogData {
  accountId: number;
  role: string;
}

@Component({
  selector: 'app-member-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './member-dialog.component.html',
  styleUrls: ['./member-dialog.component.scss'],
})
export class MemberDialogComponent {
  form: FormGroup;

  constructor(
    private dialogRef: MatDialogRef<MemberDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: MemberDialogData,
    private fb: FormBuilder,
    private accountService: AccountService,
  ) {
    this.form = this.fb.group({
      userLogin: ['', Validators.required],
    });
  }

  get role(): string {
    return this.data.role;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.form.invalid) {
      return;
    }
    const userLogin = (this.form.value.userLogin as string).trim();
    if (!userLogin) {
      return;
    }

    this.accountService
      .addMember(this.data.accountId, this.data.role, userLogin, '', '', '')
      .subscribe({
        next: (res) => {
          this.dialogRef.close({ success: true, member: res });
        },
        error: (error) => {
          // Прокидываем ошибку наверх, чтобы вызвать снекбар снаружи
          this.dialogRef.close({ success: false, error });
        },
      });
  }
}

