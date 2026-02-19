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
import { CommissionService, Commission } from '../../../shared/services/api/commission.service';
import { AuthService } from '../../../shared/services/auth.service';

export const ACTION_OPTIONS = [
  { value: 'sale', label: 'Продажа' },
  { value: 'prolongation', label: 'Пролонгация' }
];

@Component({
  selector: 'app-commission-edit-page',
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
  templateUrl: './commission-edit.component.html',
  styleUrls: ['./commission-edit.component.scss']
})
export class CommissionEditPageComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private commissionService = inject(CommissionService);
  private snack = inject(MatSnackBar);
  private authService = inject(AuthService);

  readonly actionOptions = ACTION_OPTIONS;

  clientId = '';
  clientName = '';
  productName = '';
  isNewRecord = true;
  loading = false;

  model: Partial<Commission> & { accountId: number; productId: number } = {
    accountId: 0,
    productId: 0,
    action: 'sale',
    agdNumber: '',
    rateValue: undefined,
    commissionMinRate: undefined,
    commissionMaxRate: undefined,
    minAmount: undefined,
    maxAmount: undefined,
    fixedAmount: undefined
  };

  ngOnInit(): void {
    this.clientId = this.route.snapshot.paramMap.get('client-id') ?? '';
    const commissionId = this.route.snapshot.paramMap.get('commission-id');

    if (commissionId && commissionId !== 'edit') {
      this.isNewRecord = false;
      this.loading = true;
      this.commissionService.getConfigurationById(+commissionId).subscribe({
        next: (c) => {
          this.model = {
            ...c,
            accountId: c.accountId,
            productId: c.productId
          };
          const state = history.state as { productName?: string; clientName?: string } | undefined;
          this.productName = state?.productName || `Комиссия #${c.id}`;
          this.clientName = state?.clientName || '';
          this.loading = false;
        },
        error: () => {
          this.snack.open('Ошибка при загрузке комиссии', 'OK', { duration: 2000 });
          this.loading = false;
          this.goBack();
        }
      });
    } else {
      const q = this.route.snapshot.queryParamMap;
      const accountId = q.get('accountId');
      const productId = q.get('productId');
      const productName = q.get('productName');
      if (accountId) this.model.accountId = +accountId;
      if (productId) this.model.productId = +productId;
      if (productName) this.productName = decodeURIComponent(productName);
      const state = history.state as { productName?: string; clientName?: string } | undefined;
      if (state?.productName) this.productName = state.productName;
      if (state?.clientName) this.clientName = state.clientName;
    }
  }

  get maxRate(): number {
    const v = this.model.rateValue;
    return v != null && !isNaN(Number(v)) ? Number(v) : 100;
  }

  goBack(): void {
    this.router.navigate(['/', this.authService.tenant, 'admin', 'clients', this.clientId], {
      queryParams: { tab: 'products' }
    });
  }

  save(): void {
    const payload: Commission = {
      id: this.model.id,
      accountId: this.model.accountId,
      productId: this.model.productId,
      action: this.model.action || 'sale',
      agdNumber: this.model.agdNumber || undefined,
      rateValue: this.model.rateValue != null ? Number(this.model.rateValue) : undefined,
      commissionMinRate: this.model.commissionMinRate != null ? Number(this.model.commissionMinRate) : undefined,
      commissionMaxRate: this.model.commissionMaxRate != null ? Number(this.model.commissionMaxRate) : undefined,
      minAmount: this.model.minAmount != null ? Number(this.model.minAmount) : undefined,
      maxAmount: this.model.maxAmount != null ? Number(this.model.maxAmount) : undefined,
      fixedAmount: this.model.fixedAmount != null ? Number(this.model.fixedAmount) : undefined
    };

    this.loading = true;
    if (this.isNewRecord) {
      this.commissionService.createConfiguration(payload).subscribe({
        next: () => {
          this.snack.open('Комиссия добавлена', 'OK', { duration: 2000 });
          this.goBack();
        },
        error: () => {
          this.snack.open('Ошибка при добавлении комиссии', 'OK', { duration: 2000 });
          this.loading = false;
        }
      });
    } else {
      if (!payload.id) {
        this.snack.open('ID комиссии не найден', 'OK', { duration: 2000 });
        this.loading = false;
        return;
      }
      this.commissionService.updateConfiguration(payload.id, payload).subscribe({
        next: () => {
          this.snack.open('Комиссия обновлена', 'OK', { duration: 2000 });
          this.goBack();
        },
        error: () => {
          this.snack.open('Ошибка при обновлении комиссии', 'OK', { duration: 2000 });
          this.loading = false;
        }
      });
    }
  }
}
