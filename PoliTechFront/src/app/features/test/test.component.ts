import { Component, OnInit, ViewChild } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { TestService, TestContext, TestError, PfType } from '../../shared';

@Component({
    selector: 'app-test',
    imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTabsModule,
    MatSnackBarModule,
    FormsModule
],
    templateUrl: './test.component.html',
    styleUrls: ['./test.component.scss']
})
export class TestComponent implements OnInit {
  @ViewChild('contextPaginator') contextPaginator!: MatPaginator;
  @ViewChild('errorPaginator') errorPaginator!: MatPaginator;

  // Toggle state
  toggleMode: 'quote' | 'policy' = 'quote';

  // Form data
  requestJson: string = '';
  selectedPfType: string = '';

  // Data for tables
  contextData: TestContext[] = [];
  errorData: TestError[] = [];
  calculationResult: string = '';

  // Table configurations
  contextDisplayedColumns: string[] = ['varCode', 'varValue'];
  errorDisplayedColumns: string[] = ['errorText', 'validator'];

  // Dropdown data
  pfTypes: PfType[] = [];

  constructor(
    private testService: TestService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadPfTypes();
  }

  loadPfTypes(): void {
    this.testService.getPfTypes().subscribe(types => {
      this.pfTypes = types;
    });
  }

  onValidatorClick(): void {
    if (!this.requestJson.trim()) {
      this.snackBar.open('Введите данные в поле "Запрос"', 'Закрыть', { duration: 3000 });
      return;
    }

    const requestData = this.requestJson.trim();

      this.testService.validateQuote(requestData).subscribe({
        next: (response) => {
          this.contextData = response.context;
          this.errorData = response.errorText;
          this.calculationResult = JSON.stringify(response.policy, null, 2);
          this.snackBar.open('Валидация завершена', 'Закрыть', { duration: 3000 });
        },
        error: (error) => {
          console.error('Validation error:', error);
          this.snackBar.open('Ошибка валидации', 'Закрыть', { duration: 3000 });
        }
      });
  }

  onCalculatorClick(): void {
    if (!this.requestJson.trim()) {
      this.snackBar.open('Введите данные в поле "Запрос"', 'Закрыть', { duration: 3000 });
      return
    }

    const requestData = this.requestJson.trim();

      this.testService.validatePolicy(requestData).subscribe({
        next: (response) => {
          this.contextData = response.context;
          this.errorData = response.errorText;
          this.calculationResult = JSON.stringify(response.policy, null, 2);
          this.snackBar.open('Валидация завершена', 'Закрыть', { duration: 3000 });
        },
        error: (error) => {
          console.error('Validation error:', error);
          this.snackBar.open('Ошибка валидации', 'Закрыть', { duration: 3000 });
        }
      });

    }
/*
    const requestData = this.requestJson.trim();

    // Clear tables
    this.contextData = [];
    this.errorData = [];

    if (this.toggleMode === 'quote') {
      this.testService.calculateQuote(requestData).subscribe({
        next: (result) => {
          this.calculationResult = result;
          this.snackBar.open('Расчет завершен', 'Закрыть', { duration: 3000 });
        },
        error: (error) => {
          console.error('Calculation error:', error);
          this.snackBar.open('Ошибка расчета', 'Закрыть', { duration: 3000 });
        }
      });
    } else {
      this.testService.calculatePolicy(requestData).subscribe({
        next: (result) => {
          this.calculationResult = result;
          this.snackBar.open('Расчет завершен', 'Закрыть', { duration: 3000 });
        },
        error: (error) => {
          console.error('Calculation error:', error);
          this.snackBar.open('Ошибка расчета', 'Закрыть', { duration: 3000 });
        }
      });
    }
    */


  onPrintPfClick(): void {
    if (!this.selectedPfType) {
      this.snackBar.open('Print form must be selected', 'Закрыть', { duration: 3000 });
      return;
    }

    if (!this.requestJson.trim()) {
      this.snackBar.open('Введите данные в поле "Запрос"', 'Закрыть', { duration: 3000 });
      return;
    }

    const requestData = this.requestJson.trim();

    this.testService.printPf(requestData, this.selectedPfType).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `print_form_${this.selectedPfType}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.snackBar.open('Форма скачана', 'Закрыть', { duration: 3000 });
      },
      error: (error) => {
        console.error('Print error:', error);
        this.snackBar.open('Ошибка печати формы', 'Закрыть', { duration: 3000 });
      }
    });
  }
}
