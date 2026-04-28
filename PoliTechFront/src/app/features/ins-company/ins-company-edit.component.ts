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
import { AuthService } from '../../shared/services/auth.service';
import { InsCompanyService, InsuranceCompanyDto } from '../../shared/services/api/ins-company.service';

@Component({
  selector: 'app-ins-company-edit',
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
    MatSnackBarModule,
  ],
  templateUrl: './ins-company-edit.component.html',
  styleUrls: ['./ins-company-edit.component.scss'],
})
export class InsCompanyEditComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private insCompanyService = inject(InsCompanyService);
  private snack = inject(MatSnackBar);
  private authService = inject(AuthService);

  company: InsuranceCompanyDto = {
    code: '',
    name: '',
    status: 'ACTIVE',
    contractText: '',
    contractRepresentative: '',
  };
  isNewRecord = true;
  loading = false;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isNewRecord = false;
      this.loading = true;
      this.insCompanyService.getById(id).subscribe({
        next: (c) => {
          this.company = { ...c };
          this.loading = false;
        },
        error: () => {
          this.snack.open('Ошибка при загрузке страховой компании', 'OK', { duration: 2500 });
          this.loading = false;
          this.goBack();
        },
      });
    }
  }

  save(): void {
    if (!this.company.code?.trim()) {
      this.snack.open('Введите код', 'OK', { duration: 2000 });
      return;
    }
    if (!this.company.name?.trim()) {
      this.snack.open('Введите название', 'OK', { duration: 2000 });
      return;
    }

    this.loading = true;
    if (this.isNewRecord) {
      this.insCompanyService.create(this.company).subscribe({
        next: () => {
          this.snack.open('Сохранено', 'OK', { duration: 2000 });
          this.goBack();
        },
        error: () => {
          this.snack.open('Ошибка при создании', 'OK', { duration: 2500 });
          this.loading = false;
        },
      });
    } else {
      if (this.company.id == null) {
        this.snack.open('ID не найден', 'OK', { duration: 2000 });
        this.loading = false;
        return;
      }
      this.insCompanyService.update(this.company.id, this.company).subscribe({
        next: () => {
          this.snack.open('Сохранено', 'OK', { duration: 2000 });
          this.goBack();
        },
        error: () => {
          this.snack.open('Ошибка при сохранении', 'OK', { duration: 2500 });
          this.loading = false;
        },
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/', this.authService.tenant, 'products']);
  }
}
