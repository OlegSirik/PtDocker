import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

import { AccountService } from '../../../../shared/services/account.service';

export interface MemberLoginDialogData {
  accountId: number;
  role: string;
  fullName?: string;
  position?: string;
  userLogin?: string;
  password?: string;
}

@Component({
  selector: 'app-member-login-dialog',
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
  templateUrl: './member-login-dialog.component.html',
  styleUrls: ['./member-login-dialog.component.scss'],
})
export class MemberLoginDialogComponent {
  memberForm: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<MemberLoginDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: MemberLoginDialogData,
    private fb: FormBuilder,
    private accountService: AccountService
  ) {
    this.memberForm = this.fb.group({
      fullName: [this.data.fullName ?? ''],
      position: [this.data.position ?? ''],
      userLogin: [this.data.userLogin ?? '', Validators.required],
      password: [this.data.password ?? '']
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.memberForm.invalid) {
      return;
    }
    const userLogin = String(this.memberForm.value.userLogin ?? '').trim();
    if (!userLogin) {
      return;
    }

    const fullName = String(this.memberForm.value.fullName ?? '').trim();
    const position = String(this.memberForm.value.position ?? '').trim();
    const password = String(this.memberForm.value.password ?? '').trim();

    this.accountService.addMember(this.data.accountId, this.data.role, userLogin, fullName, position, password).subscribe({
      next: (member) => {
        this.dialogRef.close({ success: true, action: 'create', member });
      },
      error: (error: unknown) => {
        console.error('Error creating member login:', error);
        this.dialogRef.close({ success: false, error });
      }
    });
  }
}

