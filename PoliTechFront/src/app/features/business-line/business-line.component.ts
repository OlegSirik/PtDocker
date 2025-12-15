import { Component, OnInit, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { BusinessLineService, BusinessLine } from '../../shared/services/business-line.service';
import { AuthService } from '../../shared/services/auth.service';

@Component({
    selector: 'app-business-line',
    imports: [
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatFormFieldModule,
    MatInputModule
],
    templateUrl: './business-line.component.html',
    styleUrls: ['./business-line.component.scss']
})
export class BusinessLineComponent implements OnInit {
  private authService = inject(AuthService);
  businessLines: BusinessLine[] = [];
  filteredBusinessLines: BusinessLine[] = [];
  displayedColumns: string[] = ['id', 'mpCode', 'mpName', 'actions'];
  searchText: string = '';

  constructor(
    private businessLineService: BusinessLineService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadBusinessLines();
  }

  loadBusinessLines(): void {
    this.businessLineService.getBusinessLines().subscribe(data => {
      this.businessLines = data;
      this.filteredBusinessLines = data;
    });
  }

  onSearch(searchValue: string): void {
    this.searchText = searchValue;
    if (!searchValue.trim()) {
      this.filteredBusinessLines = this.businessLines;
    } else {
      const search = searchValue.toLowerCase();
      this.filteredBusinessLines = this.businessLines.filter(item => 
        item.mpName.toLowerCase().includes(search) || 
        item.mpCode.toLowerCase().includes(search)
      );
    }
  }

  addBusinessLine(): void {
    this.router.navigate(['/', this.authService.tenant, 'lob-edit']);
  }

  editBusinessLine(row: BusinessLine): void {
    this.router.navigate(['/', this.authService.tenant, 'lob-edit', row.mpCode]);
  }

  deleteBusinessLine(businessLine: BusinessLine): void {
    if (confirm(`Вы уверены, что хотите удалить линию бизнеса "${businessLine.mpName}"?`)) {
      this.businessLineService.deleteBusinessLine(businessLine.id).subscribe({
        next: (success) => {
          if (success) {
            this.snackBar.open('Линия бизнеса успешно удалена', 'OK', { duration: 3000 });
            this.loadBusinessLines();
          } else {
            this.snackBar.open('Ошибка при удалении', 'OK', { duration: 3000 });
          }
        },
        error: () => {
          this.snackBar.open('Ошибка при удалении', 'OK', { duration: 3000 });
        }
      });
    }
  }
}
