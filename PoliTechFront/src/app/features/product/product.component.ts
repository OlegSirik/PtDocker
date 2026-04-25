import { Component, OnInit, inject, Inject, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialogModule, MatDialog, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatBadgeModule } from '@angular/material/badge';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';

import {
  ProductService,
  Product,
  PolicyVar,
  QuoteValidator,
  Package,
  PackageFile,
  Cover,
  Deductible,
  Limit,
} from '../../shared/services/product.service';
import { ValidatorDialogComponent } from './validator-dialog/validator-dialog.component';
import { PackageDialogComponent } from './package-dialog/package-dialog.component';
import { CoverDialogComponent } from './cover-dialog/cover-dialog.component';
import { DeductibleDialogComponent } from './deductible-dialog/deductible-dialog.component';
import { LimitDialogComponent } from './limit-dialog/limit-dialog.component';
import { ProductPvVarEditDialogComponent } from './product-pv-var-edit-dialog.component';
import { Observable, of } from 'rxjs';
import { tap, map, filter, catchError, switchMap } from 'rxjs/operators';
import { BusinessLineService } from '../../shared/services/business-line.service';
import { BusinessLineEditService, BusinessLineFile, BusinessLineVar, BusinessLineEdit } from '../../shared/services/business-line-edit.service';
import { FilesService, FileTemplate } from '../../shared/services/api/files.service';
import { AuthService } from '../../shared/services/auth.service';
import { TestRequestService } from '../../shared/services/api/test-request.service';
import { VarsService } from '../../shared/services/vars.service';
import { InsCompanyService, InsuranceCompanyDto } from '../../shared/services/api/ins-company.service';
import { TreeTableComponent, isObjectVarTypeRow } from '../../shared/components/tree-table';
import type { TreeTableSourceRow } from '../../shared/components/tree-table';
import { MatDivider } from "@angular/material/divider";

@Component({
    selector: 'app-product',
    imports: [
    CommonModule,
    FormsModule,
    MatTabsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatTableModule,
    MatPaginatorModule,
    MatDialogModule,
    MatBadgeModule,
    MatSlideToggleModule,
    MatTooltipModule,
    TreeTableComponent,
    MatDivider
],
    templateUrl: './product.component.html',
    styleUrls: ['./product.component.scss']
})
export class ProductComponent implements OnInit {
  @ViewChild('productTreeTable') productTreeTable?: TreeTableComponent<TreeTableSourceRow>;

  lob: BusinessLineEdit | undefined;

  readonly isObjectVarTypeRow = isObjectVarTypeRow;
  
  product: Product = {
    lob: '',
    code: '',
    name: '',
    versionNo: 1,
    waitingPeriod: {validatorType: 'RANGE', validatorValue: ''},
    policyTerm: { validatorType: 'RANGE', validatorValue: '' },
    numberGenerator: { mask: '', maxValue: 0, resetPolicy: 'MONTHLY', xorMask: '' },
    quoteValidator: [],
    saveValidator: [],
    packages: [],
    vars: [],
    rules: {
      insuredEqualsPolicyHolder: false
    }
  };

  isNewRecord = false;
  hasChanges = false;

  /** Справочник страховых компаний (выбор ins_company_id) */
  insCompanies: InsuranceCompanyDto[] = [];

  /** Сравнение значений mat-select для null / id */
  compareInsCompanyIds = (a: number | null | undefined, b: number | null | undefined): boolean => {
    const na = a == null ? null : Number(a);
    const nb = b == null ? null : Number(b);
    return na === nb;
  };

  // Dropdown options
  lobOptions: string[] = [];
  keyLeftOptions: string[] = [];
  ruleTypeOptions: string[] = [];
  validatorTypeOptions: string[] = ['RANGE', 'LIST', 'NEXT_MONTH'];
  validatorTypeOptions2: string[] = ['RANGE', 'LIST'];
  resetPolicyOptions: string[] = [];

  // Validator type labels mapping
  validatorTypeLabels: { [key: string]: string } = {
    'RANGE': 'Допустимый диапазон',
    'LIST': 'Список возможных значений',
    'NEXT_MONTH': '1 число следующего месяца'
  };

  getValidatorTypeLabel(code: string): string {
    return this.validatorTypeLabels[code] || code;
  }

  // Quote Validator table
  quoteValidatorDisplayedColumns = ['lineNr', 'errorText', 'expression', 'actions'];
  quoteValidatorSearchText = '';
  quoteValidatorPageSize = 10;
  quoteValidatorPageIndex = 0;
  filteredQuoteValidators: QuoteValidator[] = [];
  paginatedQuoteValidators: QuoteValidator[] = [];

  // Save Validator table
  saveValidatorDisplayedColumns = ['lineNr', 'errorText', 'expression', 'actions'];
  saveValidatorSearchText = '';
  saveValidatorPageSize = 10;
  saveValidatorPageIndex = 0;
  filteredSaveValidators: QuoteValidator[] = [];
  paginatedSaveValidators: QuoteValidator[] = [];

  // Packages table
  packagesDisplayedColumns = ['code', 'name', 'actions'];
  packagesSearchText = '';
  packagesPageSize = 10;
  packagesPageIndex = 0;
  filteredPackages: Package[] = [];
  paginatedPackages: Package[] = [];

  selectedPackageIndex = -1;
  currentPackage: Package | null = null;

  // Covers table
  coversDisplayedColumns = ['code', 'isMandatory', 'waitingPeriod', 'coverageTerm', 'isDeductibleMandatory', 'actions'];
  paginatedCovers: Cover[] = [];
  selectedCoverIndex = -1;

  // Deductibles table
  deductiblesDisplayedColumns = ['id', 'text', 'actions'];
  paginatedDeductibles: Deductible[] = [];

  // Limits table
  limitsDisplayedColumns = ['sumInsured', 'premium', 'actions'];
  paginatedLimits: Limit[] = [];

  // Dropdown options
  coverCodeOptions: string[] = [];

  // Policy Variables table
  policyVarsDisplayedColumns = ['category', 'field', 'name', 'code', 'actions'];
  policyVarsFiltered = []
  /** Только шаблоны печати ({@code strings.*} в varCdm). */
  policyFilter = 'strings';

  /** Дерево атрибутов продукта (данные из {@link Product.vars}). */
  productTreeTableData: TreeTableSourceRow[] = [];
  productTreeDisplayedColumns: string[] = ['name', 'code', 'actions'];
  showDeletedProductTree = false;

  /** Дерево переменных: правки только в DEV-версии продукта. */
  get productTreeMutationsLocked(): boolean {
    return this.product.versionStatus === 'PROD';
  }

  // Files table
  filesDisplayedColumns = ['fileCode', 'fileName', 'actions'];
  businessLineFiles: PackageFile[] = [];
  
  // Files data
  paginatedFiles: PackageFile[] = [];

  // Auth service
  private authService = inject(AuthService);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private businessLineService: BusinessLineService,
    private businessLineEditService: BusinessLineEditService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private filesService: FilesService,
    private testRequestService: TestRequestService,
    private varsService: VarsService,
    private insCompanyService: InsCompanyService,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    const versionNo = this.route.snapshot.paramMap.get('versionNo');

    this.insCompanyService.list().subscribe({
      next: (list) => {
        this.insCompanies = list ?? [];
      },
      error: () => {
        this.insCompanies = [];
      },
    });

    this.loadProduct(id ? parseInt(id) : undefined, versionNo ? parseInt(versionNo) : undefined).pipe(
      switchMap((product) => {
        if (product && product.lob) {
          return this.loadLob(product.lob);
        }
        return of(null as BusinessLineEdit | null);
      }),
      tap(() => {
        this.loadDropdownOptions();
        this.updateTables();
      })
    ).subscribe();

  }

  loadLob(lob: string): Observable<BusinessLineEdit> {
    return this.businessLineEditService.getBusinessLineByCode(lob).pipe(
      tap(result => {
        this.lob = result;
      })
    );
  }

  loadDropdownOptions(): void {
    //this.productService.getLobOptions().subscribe(options => this.lobOptions = options);
    this.businessLineService.getLobCodes().subscribe(options => this.lobOptions = options);
    //this.productService.getKeyLeftOptions().subscribe(options => this.keyLeftOptions = options);
    this.businessLineEditService.getLobVars(this.product.lob).subscribe(options => this.keyLeftOptions = options);


    this.productService.getRuleTypeOptions().subscribe(options => this.ruleTypeOptions = options);
    //this.productService.getValidatorTypeOptions().subscribe(options => this.validatorTypeOptions = options);
    this.productService.getResetPolicyOptions().subscribe(options => this.resetPolicyOptions = options);

    // Get cover codes from existing product covers
    const existingCoverCodes = this.product.packages.flatMap(pkg => pkg.covers.map(cover => cover.code));
    
    // Get cover codes from LOB (business line) if available
    const lobCoverCodes = this.lob?.mpCovers?.map(cover => cover.coverCode) || [];
    
    // Combine both sources and remove duplicates
    this.coverCodeOptions = [...new Set([...existingCoverCodes, ...lobCoverCodes])];
  }

  loadProduct(id?: number, versionNo?: number): Observable<Product | null> {
    if (id) {
      return this.productService.getProduct(id, versionNo || 0).pipe(
        tap((product) => {
          this.product = product;
          // Ensure rules is initialized
          if (!this.product.rules) {
            this.product.rules = { insuredEqualsPolicyHolder: false };
          }
          if (!this.product.packages) {
            this.product.packages = [];
          }
          this.product.packages.forEach(pkg => {
            if (!pkg.files) {
              pkg.files = [];
            }
            if (!pkg.covers) {
              pkg.covers = [];
            }
            // Ensure deductibles and limits arrays are initialized for all covers
            pkg.covers.forEach((cover: Cover) => {
              if (!cover.deductibles) {
                cover.deductibles = [];
              }
              if (!cover.limits) {
                cover.limits = [];
              }
            });
          });
        }),
        catchError((error) => {
          this.snackBar.open('Ошибка загрузки продукта', 'Закрыть', { duration: 3000 });
          return of(null);
        })
      );
    } else {
      return of(null);
    }
  }

  updateChanges(): void {
    this.hasChanges = true;
  }

  /** Пока форма «грязная», CRUD через API отключён — сначала «Сохранить». */
  get crudLockedPendingSave(): boolean {
    return this.hasChanges;
  }

  private canRunServerCrud(): boolean {
    return !this.hasChanges && !!this.product?.id && this.product.versionNo != null;
  }

  /** Редактирование/удаление строки валидатора (не системные: isUpdatable !== false). */
  validatorRowMutable(v: QuoteValidator): boolean {
    return this.canRunServerCrud() && v.isUpdatable !== false;
  }

  private nextValidatorLineNr(list: QuoteValidator[]): number {
    const nums = list
      .map((x) => x.lineNr)
      .filter((n): n is number => n != null && Number.isFinite(Number(n)))
      .map((n) => Number(n));
    return nums.length ? Math.max(...nums) + 1 : 1;
  }

  private buildValidatorPayload(v: QuoteValidator, kind: 'QUOTE' | 'SAVE'): QuoteValidator {
    return { ...v, validatorType: kind };
  }

  save(): void {

    // Validate mandatory fields before saving
    if (!this.product.name || !this.product.lob || !this.product.code) {
      this.snackBar.open('Пожалуйста, заполните все обязательные поля: Наименование, Линия бизнеса, Код', 'Закрыть', { duration: 3000 });
      return;
    }

    if (this.isNewRecord) {
      this.productService.createProduct(this.product).subscribe({
        next: (savedProduct: any) => {
          this.product = savedProduct;
          this.isNewRecord = false;
          this.hasChanges = false;
//          this.router.navigate(['/product/{id}/version/{no}', savedProduct.id, savedProduct.versionNo]);
          this.snackBar.open('Продукт создан успешно', 'Закрыть', { duration: 3000 });
        },
        error: (error: any) => {
          console.error('Error creating product:', error);
          this.snackBar.open('Ошибка создания продукта', 'Закрыть', { duration: 3000 });
        }
      });
    } else {
      this.productService.updateProduct(this.product.id!, this.product).subscribe({
        next: (updatedProduct: any) => {
          this.product = updatedProduct;
          this.hasChanges = false;
          this.snackBar.open('Продукт обновлен успешно', 'Закрыть', { duration: 3000 });
        },
        error: (error: any) => {
          console.error('Error updating product:', error);
          this.snackBar.open('Ошибка обновления продукта', 'Закрыть', { duration: 3000 });
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/', this.authService.tenant, 'products']);
  }

  reloadProduct(productId: string, versionNo: string): void {
    if (!productId) return;
    if (!versionNo) return;

    this.router.navigate(['/', this.authService.tenant, 'product', productId, 'version', versionNo]);
  }


  createNewVersion(): void {
    
    this.productService.createVersion(this.product.id!, this.product.versionNo!).subscribe({
      next: (createdProduct: any) => {
        this.product = createdProduct;
        this.snackBar.open('Новая версия создана успешно', 'Закрыть', { duration: 3000 });
        this.reloadProduct(createdProduct.id, createdProduct.versionNo);
      },
      error: (error: any) => {
        console.error('Error creating new version:', error);
        this.snackBar.open('Ошибка создания новой версии', 'Закрыть', { duration: 3000 });
      }
    });
  }

  goToProduction(): void {
    this.productService.publishToProd(this.product.id!, this.product.versionNo!).subscribe({
      next: (publishedProduct: any) => {
        this.product = publishedProduct;
        this.snackBar.open('Продукт переведен в продакшн успешно', 'Закрыть', { duration: 3000 });
        this.reloadProduct(publishedProduct.id, publishedProduct.versionNo);
      },
      error: (error: any) => {
        console.error('Error publishing product to production:', error);
        this.snackBar.open('Ошибка перевода продукта в продакшн', 'Закрыть', { duration: 3000 });
      }
    });
  }

  deleteProductVersion(): void {
    if (!confirm('Вы уверены, что хотите удалить эту версию продукта?')) {
      return;
    }

    if (!this.product.id || !this.product.versionNo) {
      this.snackBar.open('Не удалось определить ID продукта или номер версии', 'Закрыть', { duration: 3000 });
      return;
    }

    this.productService.deleteProductVersion(this.product.id, this.product.versionNo).subscribe({
      next: () => {
        this.snackBar.open('Версия продукта удалена успешно', 'Закрыть', { duration: 3000 });
        // Navigate back to products list or previous page
        this.router.navigate(['/products']);
      },
      error: (error: any) => {
        console.error('Error deleting product version:', error);
        this.snackBar.open('Ошибка удаления версии продукта', 'Закрыть', { duration: 3000 });
      }
    });
  }

  openForm(): void {
    if (this.product.id && this.product.versionNo !== undefined) {
      this.router.navigate(['/', this.authService.tenant, 'product', this.product.id, 'version', this.product.versionNo, 'form']);
    } else {
      this.snackBar.open('Продукт должен быть сохранен перед открытием формы', 'Закрыть', { duration: 3000 });
    }
  }

  // Quote Validator methods
  addQuoteValidator(): void {
    if (!this.canRunServerCrud()) {
      this.snackBar.open(
        this.hasChanges ? 'Сначала сохраните изменения в продукте' : 'Сначала сохраните продукт',
        'Закрыть',
        { duration: 3000 }
      );
      return;
    }
    const productId = this.product.id!;
    const versionNo = this.product.versionNo!;

    this.loadDropdownOptions();
    const dialogRef = this.dialog.open(ValidatorDialogComponent, {
      width: '600px',
      minWidth: '900px',
      data: {
        isNew: true,
        keyLeftOptions: this.product.vars.slice().sort((a, b) => a.varCode.localeCompare(b.varCode)),
        ruleTypeOptions: this.ruleTypeOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      const lineNr = result.lineNr ?? this.nextValidatorLineNr(this.product.quoteValidator);
      const payload = this.buildValidatorPayload({ ...result, lineNr, isUpdatable: true }, 'QUOTE');
      this.productService.addValidator(productId, versionNo, payload).subscribe({
        next: (updated) => {
          this.product = updated;
          this.updateTables();
          this.hasChanges = false;
          this.snackBar.open('Проверка добавлена', 'Закрыть', { duration: 2000 });
        },
        error: (error: any) => {
          console.error('Error adding quote validator:', error);
          this.snackBar.open('Ошибка добавления проверки', 'Закрыть', { duration: 3000 });
        }
      });
    });
  }

  editQuoteValidator(validator: QuoteValidator, index: number): void {
    if (!this.validatorRowMutable(validator)) {
      return;
    }
    if (!this.canRunServerCrud()) {
      return;
    }
    const productId = this.product.id!;
    const versionNo = this.product.versionNo!;

    this.loadDropdownOptions();
    const dialogRef = this.dialog.open(ValidatorDialogComponent, {
      width: '600px',
      minWidth: '900px',
      data: {
        validator: validator,
        isNew: false,
        keyLeftOptions: this.product.vars.slice().sort((a, b) => a.varCode.localeCompare(b.varCode)),
        ruleTypeOptions: this.ruleTypeOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      const payload = this.buildValidatorPayload(
        { ...result, validatorType: 'QUOTE', isUpdatable: validator.isUpdatable !== false },
        'QUOTE'
      );
      this.productService.updateValidator(productId, versionNo, payload).subscribe({
        next: (updated) => {
          this.product = updated;
          this.updateTables();
          this.hasChanges = false;
          this.snackBar.open('Проверка обновлена', 'Закрыть', { duration: 2000 });
        },
        error: (error: any) => {
          console.error('Error updating quote validator:', error);
          this.snackBar.open('Ошибка обновления проверки', 'Закрыть', { duration: 3000 });
        }
      });
    });
  }

  deleteQuoteValidator(validator: QuoteValidator, index: number): void {
    if (!this.validatorRowMutable(validator)) {
      return;
    }
    if (!this.canRunServerCrud()) {
      return;
    }
    if (!confirm('Удалить проверку предрасчета?')) {
      return;
    }
    const productId = this.product.id!;
    const versionNo = this.product.versionNo!;
    const payload = this.buildValidatorPayload({ ...validator, validatorType: 'QUOTE' }, 'QUOTE');
    this.productService.deleteValidator(productId, versionNo, payload).subscribe({
      next: (updated) => {
        this.product = updated;
        this.updateTables();
        this.hasChanges = false;
        this.snackBar.open('Проверка удалена', 'Закрыть', { duration: 2000 });
      },
      error: (error: any) => {
        console.error('Error deleting quote validator:', error);
        this.snackBar.open('Ошибка удаления проверки', 'Закрыть', { duration: 3000 });
      }
    });
  }

  onQuoteValidatorRowClick(row: QuoteValidator): void {
    if (!this.validatorRowMutable(row)) {
      return;
    }
    this.editQuoteValidator(row, this.getQuoteValidatorIndex(row));
  }

  updateQuoteValidatorTable(): void {
    this.filteredQuoteValidators = this.product.quoteValidator.filter(item =>
      (item.keyLeft && item.keyLeft.toLowerCase().includes(this.quoteValidatorSearchText.toLowerCase())) ||
      (item.ruleType && item.ruleType.toLowerCase().includes(this.quoteValidatorSearchText.toLowerCase())) ||
      (item.errorText && item.errorText.toLowerCase().includes(this.quoteValidatorSearchText.toLowerCase()))
    );
    this.updateQuoteValidatorPagination();
  }

  onQuoteValidatorPageChange(event: PageEvent): void {
    this.quoteValidatorPageSize = event.pageSize;
    this.quoteValidatorPageIndex = event.pageIndex;
    this.updateQuoteValidatorPagination();
  }

  updateQuoteValidatorPagination(): void {
    const startIndex = this.quoteValidatorPageIndex * this.quoteValidatorPageSize;
    this.paginatedQuoteValidators = this.filteredQuoteValidators.slice(startIndex, startIndex + this.quoteValidatorPageSize);
  }

  // Save Validator methods
  addSaveValidator(): void {
    if (!this.canRunServerCrud()) {
      this.snackBar.open(
        this.hasChanges ? 'Сначала сохраните изменения в продукте' : 'Сначала сохраните продукт',
        'Закрыть',
        { duration: 3000 }
      );
      return;
    }
    const productId = this.product.id!;
    const versionNo = this.product.versionNo!;

    this.loadDropdownOptions();
    const dialogRef = this.dialog.open(ValidatorDialogComponent, {
      width: '600px',
      minWidth: '900px',
      data: {
        isNew: true,
        keyLeftOptions: this.product.vars.slice().sort((a, b) => a.varCode.localeCompare(b.varCode)),
        ruleTypeOptions: this.ruleTypeOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      const lineNr = result.lineNr ?? this.nextValidatorLineNr(this.product.saveValidator);
      const payload = this.buildValidatorPayload({ ...result, lineNr, isUpdatable: true }, 'SAVE');
      this.productService.addValidator(productId, versionNo, payload).subscribe({
        next: (updated) => {
          this.product = updated;
          this.updateTables();
          this.hasChanges = false;
          this.snackBar.open('Проверка добавлена', 'Закрыть', { duration: 2000 });
        },
        error: (error: any) => {
          console.error('Error adding save validator:', error);
          this.snackBar.open('Ошибка добавления проверки', 'Закрыть', { duration: 3000 });
        }
      });
    });
  }

  reloadPolicyVars(): void {
    if (!this.product?.id || !this.product?.versionNo) {
      this.snackBar.open('Не удалось определить ID продукта или номер версии', 'Закрыть', { duration: 3000 });
      return;
    }

    const category = (this.policyFilter || '').trim();
    this.productService.reloadVars(this.product.id, this.product.versionNo, category).subscribe({
      next: (updatedProduct: any) => {
        this.product = updatedProduct;
        this.updatePolicyTable();
        this.updateTables();
        this.snackBar.open('Переменные успешно обновлены', 'Закрыть', { duration: 3000 });
      },
      error: (error: any) => {
        console.error('Error reloading vars:', error);
        this.snackBar.open('Ошибка обновления переменных', 'Закрыть', { duration: 3000 });
      }
    });
  }

  editSaveValidator(validator: QuoteValidator, index: number): void {
    if (!this.validatorRowMutable(validator)) {
      return;
    }
    if (!this.canRunServerCrud()) {
      return;
    }
    const productId = this.product.id!;
    const versionNo = this.product.versionNo!;

    this.loadDropdownOptions();
    const dialogRef = this.dialog.open(ValidatorDialogComponent, {
      width: '600px',
      minWidth: '900px',
      data: {
        validator: validator,
        isNew: false,
        keyLeftOptions: this.product.vars.slice().sort((a, b) => a.varCode.localeCompare(b.varCode)),
        ruleTypeOptions: this.ruleTypeOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (!result) {
        return;
      }
      const payload = this.buildValidatorPayload(
        { ...result, validatorType: 'SAVE', isUpdatable: validator.isUpdatable !== false },
        'SAVE'
      );
      this.productService.updateValidator(productId, versionNo, payload).subscribe({
        next: (updated) => {
          this.product = updated;
          this.updateTables();
          this.hasChanges = false;
          this.snackBar.open('Проверка обновлена', 'Закрыть', { duration: 2000 });
        },
        error: (error: any) => {
          console.error('Error updating save validator:', error);
          this.snackBar.open('Ошибка обновления проверки', 'Закрыть', { duration: 3000 });
        }
      });
    });
  }

  deleteSaveValidator(validator: QuoteValidator, index: number): void {
    if (!this.validatorRowMutable(validator)) {
      return;
    }
    if (!this.canRunServerCrud()) {
      return;
    }
    if (!confirm('Удалить проверку договора?')) {
      return;
    }
    const productId = this.product.id!;
    const versionNo = this.product.versionNo!;
    const payload = this.buildValidatorPayload({ ...validator, validatorType: 'SAVE' }, 'SAVE');
    this.productService.deleteValidator(productId, versionNo, payload).subscribe({
      next: (updated) => {
        this.product = updated;
        this.updateTables();
        this.hasChanges = false;
        this.snackBar.open('Проверка удалена', 'Закрыть', { duration: 2000 });
      },
      error: (error: any) => {
        console.error('Error deleting save validator:', error);
        this.snackBar.open('Ошибка удаления проверки', 'Закрыть', { duration: 3000 });
      }
    });
  }

  onSaveValidatorRowClick(row: QuoteValidator): void {
    if (!this.validatorRowMutable(row)) {
      return;
    }
    this.editSaveValidator(row, this.getSaveValidatorIndex(row));
  }

  updateSaveValidatorTable(): void {
    this.filteredSaveValidators = this.product.saveValidator.filter(item =>
      (item.keyLeft && item.keyLeft.toLowerCase().includes(this.saveValidatorSearchText.toLowerCase())) ||
      (item.ruleType && item.ruleType.toLowerCase().includes(this.saveValidatorSearchText.toLowerCase())) ||
      (item.errorText && item.errorText.toLowerCase().includes(this.saveValidatorSearchText.toLowerCase()))
    );
    this.updateSaveValidatorPagination();
  }

  onSaveValidatorPageChange(event: PageEvent): void {
    this.saveValidatorPageSize = event.pageSize;
    this.saveValidatorPageIndex = event.pageIndex;
    this.updateSaveValidatorPagination();
  }

  updateSaveValidatorPagination(): void {
    const startIndex = this.saveValidatorPageIndex * this.saveValidatorPageSize;
    this.paginatedSaveValidators = this.filteredSaveValidators.slice(startIndex, startIndex + this.saveValidatorPageSize);
  }

  /** True if another package already uses this code (optionally ignore `excludeIndex`). */
  private isPackageCodeTaken(code: string, excludeIndex?: number): boolean {
    const normalized = (code ?? '').trim();
    if (!normalized) {
      return true;
    }
    return this.product.packages.some((p, i) => {
      if (excludeIndex !== undefined && i === excludeIndex) {
        return false;
      }
      return (p.code ?? '').trim() === normalized;
    });
  }

  addPackage(): void {
    if (!this.product.id || !this.product.versionNo) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    const productId = this.product.id;
    const versionNo = this.product.versionNo;
    const dialogRef = this.dialog.open(PackageDialogComponent, {
      width: '500px',
      data: {
        isNew: true,
      },
    });

    dialogRef.afterClosed().subscribe((result: Package | undefined) => {
      if (!result) {
        return;
      }
      const code = String(result.code ?? '').trim();
      if (!code) {
        this.snackBar.open('Укажите код пакета', 'Закрыть', { duration: 3000 });
        return;
      }
      if (this.isPackageCodeTaken(code)) {
        this.snackBar.open('Код пакета должен быть уникальным', 'Закрыть', { duration: 3000 });
        return;
      }
      result.code = code;
      if (!result.files) {
        result.files = [];
      }
      if (!result.covers) {
        result.covers = [];
      }

      this.productService.addPackage(productId, versionNo, result).subscribe({
        next: (updatedProduct) => {
          this.product = updatedProduct;
          this.updateTables();
          const newIndex = this.product.packages.findIndex((p) => p.code === code);
          this.selectedPackageIndex = newIndex;
          this.currentPackage = newIndex >= 0 ? this.product.packages[newIndex] : null;
          this.updateChildTables();
          this.hasChanges = false;
          this.snackBar.open('Пакет добавлен', 'Закрыть', { duration: 2000 });
        },
        error: (error: any) => {
          console.error('Error adding package:', error);
          this.snackBar.open('Ошибка добавления пакета', 'Закрыть', { duration: 3000 });
        }
      });
    });
  }

  editPackage(pkg: Package, index: number): void {
    if (!this.product.id || !this.product.versionNo) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    const productId = this.product.id;
    const versionNo = this.product.versionNo;
    if (index < 0 || index >= this.product.packages.length) {
      this.snackBar.open('Не удалось найти пакет в списке', 'Закрыть', { duration: 3000 });
      return;
    }
    const dialogRef = this.dialog.open(PackageDialogComponent, {
      width: '500px',
      data: {
        package: pkg,
        isNew: false,
      },
    });

    dialogRef.afterClosed().subscribe((result: Package | undefined) => {
      if (!result) {
        return;
      }
      const code = String(result.code ?? '').trim();
      if (!code) {
        this.snackBar.open('Укажите код пакета', 'Закрыть', { duration: 3000 });
        return;
      }
      if (this.isPackageCodeTaken(code, index)) {
        this.snackBar.open('Код пакета должен быть уникальным', 'Закрыть', { duration: 3000 });
        return;
      }
      // Preserve existing files and covers arrays if not provided
      if (!result.files) {
        result.files = pkg.files || [];
      }
      if (!result.covers) {
        result.covers = pkg.covers || [];
      }
      result.code = code;
      this.productService.updatePackage(productId, versionNo, pkg.code, result).subscribe({
        next: (updatedProduct) => {
          this.product = updatedProduct;
          this.updateTables();
          const updatedIndex = this.product.packages.findIndex((p) => p.code === code);
          if (updatedIndex >= 0) {
            this.selectedPackageIndex = updatedIndex;
            this.currentPackage = this.product.packages[updatedIndex];
          } else {
            this.selectedPackageIndex = -1;
            this.currentPackage = null;
          }
          this.updateChildTables();
          this.hasChanges = false;
          this.snackBar.open('Пакет обновлен', 'Закрыть', { duration: 2000 });
        },
        error: (error: any) => {
          console.error('Error updating package:', error);
          this.snackBar.open('Ошибка обновления пакета', 'Закрыть', { duration: 3000 });
        }
      });
    });
  }

  deletePackage(pkg: Package, index: number): void {
    if (!this.product.id || !this.product.versionNo) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    const productId = this.product.id;
    const versionNo = this.product.versionNo;
    if (index < 0 || index >= this.product.packages.length) {
      this.snackBar.open('Не удалось найти пакет в списке', 'Закрыть', { duration: 3000 });
      return;
    }
    if (confirm('Удалить пакет?')) {
      this.productService.deletePackage(productId, versionNo, pkg.code).subscribe({
        next: (updatedProduct) => {
          this.product = updatedProduct;
          this.selectedPackageIndex = -1;
          this.currentPackage = null;
          this.selectedCoverIndex = -1;
          this.updateTables();
          this.updateChildTables();
          this.hasChanges = false;
          this.snackBar.open('Пакет удален', 'Закрыть', { duration: 2000 });
        },
        error: (error: any) => {
          console.error('Error deleting package:', error);
          this.snackBar.open('Ошибка удаления пакета', 'Закрыть', { duration: 3000 });
        }
      });
    }
  }
  updateFilesTable() {
    if (this.product.packages && 
        this.selectedPackageIndex >= 0 && 
        this.selectedPackageIndex < this.product.packages.length &&
        this.product.packages[this.selectedPackageIndex]) {
      const selectedPackage = this.product.packages[this.selectedPackageIndex];
      this.paginatedFiles = selectedPackage.files || [];
    } else {
      this.paginatedFiles = [];
    }
  }

  showFiles(pkg: Package, index: number): void {
    if (index < 0 || index >= this.product.packages.length) {
      return;
    }
    this.selectedPackageIndex = index;
    this.currentPackage = pkg;
    this.selectedCoverIndex = -1;
    this.updateChildTables();
  }

  updateChildTables(): void {
    this.updateFilesTable();
    this.updateCoversTable();
    this.updateDeductiblesTable();
    this.updateLimitsTable();
  }

  openCalculator(pkg: Package): void {
    if (this.product.id && this.product.versionNo) {
      this.router.navigate(['/', this.authService.tenant, 'products', this.product.id, 'versions', this.product.versionNo, 'packages', pkg.code, 'calculator']);
    } else {
      this.snackBar.open('Продукт должен быть сохранен перед открытием калькулятора', 'Закрыть', { duration: 3000 });
    }
  }

  updatePackagesTable(): void {
    const pkgs = this.product.packages ?? [];
    // New array reference so mat-table picks up push/splice/in-place edits
    this.filteredPackages = [...pkgs];
    this.paginatedPackages = [...pkgs];
  }

  onPackagesPageChange(event: PageEvent): void {
    this.packagesPageSize = event.pageSize;
    this.packagesPageIndex = event.pageIndex;
    this.updatePackagesPagination();
  }

  updatePackagesPagination() {
    console.log('updatePackagesPagination');
  }

  // Cover methods
  addCover(): void {
    if (this.selectedPackageIndex === -1) return;
    if (!this.product.id || !this.product.versionNo) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    const productId = this.product.id;
    const versionNo = this.product.versionNo;

    this.loadDropdownOptions();

    const dialogRef = this.dialog.open(CoverDialogComponent, {
      width: '600px',
      data: {
        isNew: true,
        coverCodeOptions: this.coverCodeOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (!result.deductibles) {
          result.deductibles = [];
        }
        if (!result.limits) {
          result.limits = [];
        }
        const packageCode = this.product.packages[this.selectedPackageIndex].code;
        this.productService.addCover(productId, versionNo, packageCode, result).subscribe({
          next: (updatedProduct) => {
            this.product = updatedProduct;
            const pkgIndex = this.product.packages.findIndex((p) => p.code === packageCode);
            this.selectedPackageIndex = pkgIndex;
            this.currentPackage = pkgIndex >= 0 ? this.product.packages[pkgIndex] : null;
            this.updateTables();
            this.hasChanges = false;
            this.snackBar.open('Покрытие добавлено', 'Закрыть', { duration: 2000 });
          },
          error: (error: any) => {
            console.error('Error adding cover:', error);
            this.snackBar.open('Ошибка добавления покрытия', 'Закрыть', { duration: 3000 });
          }
        });
      }
    });
  }

  editCover(cover: Cover, index: number): void {
    if (!this.product.id || !this.product.versionNo || this.selectedPackageIndex === -1) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    const productId = this.product.id;
    const versionNo = this.product.versionNo;
    this.loadDropdownOptions();
    
    const dialogRef = this.dialog.open(CoverDialogComponent, {
      width: '600px',
      data: {
        cover: cover,
        isNew: false,
        coverCodeOptions: this.coverCodeOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Preserve existing deductibles and limits arrays
        if (!result.deductibles) {
          result.deductibles = cover.deductibles || [];
        }
        if (!result.limits) {
          result.limits = cover.limits || [];
        }
        const packageCode = this.product.packages[this.selectedPackageIndex].code;
        this.productService.updateCover(productId, versionNo, packageCode, cover.code, result).subscribe({
          next: (updatedProduct) => {
            this.product = updatedProduct;
            const pkgIndex = this.product.packages.findIndex((p) => p.code === packageCode);
            this.selectedPackageIndex = pkgIndex;
            this.currentPackage = pkgIndex >= 0 ? this.product.packages[pkgIndex] : null;
            this.updateTables();
            this.hasChanges = false;
            this.snackBar.open('Покрытие обновлено', 'Закрыть', { duration: 2000 });
          },
          error: (error: any) => {
            console.error('Error updating cover:', error);
            this.snackBar.open('Ошибка обновления покрытия', 'Закрыть', { duration: 3000 });
          }
        });
      }
    });
  }

  deleteCover(cover: Cover, index: number): void {
    if (!this.product.id || !this.product.versionNo || this.selectedPackageIndex === -1) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    const productId = this.product.id;
    const versionNo = this.product.versionNo;
    if (confirm('Удалить покрытие?')) {
      const packageCode = this.product.packages[this.selectedPackageIndex].code;
      this.productService.deleteCover(productId, versionNo, packageCode, cover.code).subscribe({
        next: (updatedProduct) => {
          this.product = updatedProduct;
          const pkgIndex = this.product.packages.findIndex((p) => p.code === packageCode);
          this.selectedPackageIndex = pkgIndex;
          this.currentPackage = pkgIndex >= 0 ? this.product.packages[pkgIndex] : null;
          this.selectedCoverIndex = -1;
          this.updateTables();
          this.hasChanges = false;
          this.snackBar.open('Покрытие удалено', 'Закрыть', { duration: 2000 });
        },
        error: (error: any) => {
          console.error('Error deleting cover:', error);
          this.snackBar.open('Ошибка удаления покрытия', 'Закрыть', { duration: 3000 });
        }
      });
    }
  }

  updateCoversTable(): void {
    if (this.selectedPackageIndex === -1) {
      this.paginatedCovers = [];
      return;
    }

    const selectedPackage = this.product.packages[this.selectedPackageIndex];
    if (!selectedPackage || !selectedPackage.covers) {
      this.paginatedCovers = [];
      return;
    }

    // Create a new array reference to trigger Angular change detection
    this.paginatedCovers = [...selectedPackage.covers];
  }

  selectCover(cover: Cover, index: number): void {
    this.selectedCoverIndex = index;
    this.updateDeductiblesTable();
    this.updateLimitsTable();
  }

  private persistSelectedCover(updatedCover: Cover, successMessage: string, errorMessage: string): void {
    if (!this.canRunServerCrud() || this.selectedPackageIndex === -1 || this.selectedCoverIndex === -1) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    const productId = this.product.id!;
    const versionNo = this.product.versionNo!;
    const selectedPackage = this.product.packages[this.selectedPackageIndex];
    const currentCover = selectedPackage?.covers?.[this.selectedCoverIndex];
    if (!selectedPackage || !currentCover) {
      this.snackBar.open('Покрытие не найдено', 'Закрыть', { duration: 3000 });
      return;
    }
    const packageCode = selectedPackage.code;
    const coverCode = currentCover.code;
    this.productService.updateCover(productId, versionNo, packageCode, coverCode, updatedCover).subscribe({
      next: (updatedProduct) => {
        this.product = updatedProduct;
        const pkgIndex = this.product.packages.findIndex((p) => p.code === packageCode);
        this.selectedPackageIndex = pkgIndex;
        this.currentPackage = pkgIndex >= 0 ? this.product.packages[pkgIndex] : null;
        if (pkgIndex >= 0) {
          const nextCoverIndex = this.product.packages[pkgIndex].covers.findIndex((c) => c.code === updatedCover.code);
          this.selectedCoverIndex = nextCoverIndex >= 0 ? nextCoverIndex : -1;
        } else {
          this.selectedCoverIndex = -1;
        }
        this.updateTables();
        this.hasChanges = false;
        this.snackBar.open(successMessage, 'Закрыть', { duration: 2000 });
      },
      error: (error: any) => {
        console.error(errorMessage, error);
        this.snackBar.open(errorMessage, 'Закрыть', { duration: 3000 });
      }
    });
  }

  // Deductible methods
  addDeductible(): void {
    if (this.selectedPackageIndex === -1 || this.selectedCoverIndex === -1) return;
    if (!this.canRunServerCrud()) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(DeductibleDialogComponent, {
      width: '600px',
      data: {
        isNew: true
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const cover = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex];
        const deductibles = cover.deductibles ?? [];
        
        // Validate: id must be unique and not empty
        if (!result.id && result.id !== 0) {
          this.snackBar.open('ID не может быть пустым', 'Закрыть', { duration: 3000 });
          return;
        }
        if (!result.text || result.text.trim() === '') {
          this.snackBar.open('Текст не может быть пустым', 'Закрыть', { duration: 3000 });
          return;
        }
        if (deductibles.some(d => d.id === result.id)) {
          this.snackBar.open('ID должен быть уникальным', 'Закрыть', { duration: 3000 });
          return;
        }
        const updatedCover: Cover = {
          ...cover,
          deductibles: [...deductibles, result],
          limits: cover.limits ?? []
        };
        this.persistSelectedCover(updatedCover, 'Франшиза добавлена', 'Ошибка добавления франшизы');
      }
    });
  }

  editDeductible(deductible: Deductible, index: number): void {
    if (!this.canRunServerCrud()) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    const dialogRef = this.dialog.open(DeductibleDialogComponent, {
      width: '600px',
      data: {
        deductible: deductible,
        isNew: false
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const cover = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex];
        const deductibles = [...(cover.deductibles ?? [])];
        
        // Validate: id must be unique (excluding current index) and not empty
        if (!result.id && result.id !== 0) {
          this.snackBar.open('ID не может быть пустым', 'Закрыть', { duration: 3000 });
          return;
        }
        if (!result.text || result.text.trim() === '') {
          this.snackBar.open('Текст не может быть пустым', 'Закрыть', { duration: 3000 });
          return;
        }
        if (deductibles.some((d, i) => d.id === result.id && i !== index)) {
          this.snackBar.open('ID должен быть уникальным', 'Закрыть', { duration: 3000 });
          return;
        }
        deductibles[index] = result;
        const updatedCover: Cover = {
          ...cover,
          deductibles,
          limits: cover.limits ?? []
        };
        this.persistSelectedCover(updatedCover, 'Франшиза обновлена', 'Ошибка обновления франшизы');
      }
    });
  }

  deleteDeductible(deductible: Deductible, index: number): void {
    if (!this.canRunServerCrud()) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    if (confirm('Удалить франшизу?')) {
      const cover = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex];
      const deductibles = [...(cover.deductibles ?? [])];
      deductibles.splice(index, 1);
      const updatedCover: Cover = {
        ...cover,
        deductibles,
        limits: cover.limits ?? []
      };
      this.persistSelectedCover(updatedCover, 'Франшиза удалена', 'Ошибка удаления франшизы');
    }
  }

  updateDeductiblesTable(): void {
    if (this.selectedPackageIndex === -1 || this.selectedCoverIndex === -1) {
      this.paginatedDeductibles = [];
      return;
    }

    const selectedPackage = this.product.packages[this.selectedPackageIndex];
    if (!selectedPackage || !selectedPackage.covers || this.selectedCoverIndex >= selectedPackage.covers.length) {
      this.paginatedDeductibles = [];
      return;
    }

    const selectedCover = selectedPackage.covers[this.selectedCoverIndex];
    if (!selectedCover || !selectedCover.deductibles) {
      this.paginatedDeductibles = [];
      return;
    }

    // Create a new array reference to trigger Angular change detection
    this.paginatedDeductibles = [...selectedCover.deductibles];
  }

  // Limits methods
  addLimit(): void {
    if (this.selectedPackageIndex === -1 || this.selectedCoverIndex === -1) return;
    if (!this.canRunServerCrud()) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(LimitDialogComponent, {
      width: '500px',
      data: {
        isNew: true
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const cover = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex];
        const limits = cover.limits ?? [];
        
        // Validate: sumInsured must be unique, sumInsured and premium must be > 0
        if (!result.sumInsured || result.sumInsured <= 0) {
          this.snackBar.open('Страховая сумма должна быть больше 0', 'Закрыть', { duration: 3000 });
          return;
        }
        if (!result.premium || result.premium <= 0) {
          this.snackBar.open('Премия должна быть больше 0', 'Закрыть', { duration: 3000 });
          return;
        }
        if (limits.some(l => l.sumInsured === result.sumInsured)) {
          this.snackBar.open('Страховая сумма должна быть уникальной', 'Закрыть', { duration: 3000 });
          return;
        }
        const updatedCover: Cover = {
          ...cover,
          limits: [...limits, result],
          deductibles: cover.deductibles ?? []
        };
        this.persistSelectedCover(updatedCover, 'Лимит добавлен', 'Ошибка добавления лимита');
      }
    });
  }

  editLimit(limit: Limit, index: number): void {
    if (!this.canRunServerCrud()) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    const dialogRef = this.dialog.open(LimitDialogComponent, {
      width: '500px',
      data: {
        limit: limit,
        isNew: false
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const cover = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex];
        const limits = [...(cover.limits ?? [])];
        
        // Validate: sumInsured must be unique (excluding current index), sumInsured and premium must be > 0
        if (!result.sumInsured || result.sumInsured <= 0) {
          this.snackBar.open('Страховая сумма должна быть больше 0', 'Закрыть', { duration: 3000 });
          return;
        }
        if (!result.premium || result.premium <= 0) {
          this.snackBar.open('Премия должна быть больше 0', 'Закрыть', { duration: 3000 });
          return;
        }
        if (limits.some((l, i) => l.sumInsured === result.sumInsured && i !== index)) {
          this.snackBar.open('Страховая сумма должна быть уникальной', 'Закрыть', { duration: 3000 });
          return;
        }
        limits[index] = result;
        const updatedCover: Cover = {
          ...cover,
          limits,
          deductibles: cover.deductibles ?? []
        };
        this.persistSelectedCover(updatedCover, 'Лимит обновлен', 'Ошибка обновления лимита');
      }
    });
  }

  deleteLimit(limit: Limit, index: number): void {
    if (!this.canRunServerCrud()) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    if (confirm('Удалить лимит?')) {
      const cover = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex];
      const limits = [...(cover.limits ?? [])];
      limits.splice(index, 1);
      const updatedCover: Cover = {
        ...cover,
        limits,
        deductibles: cover.deductibles ?? []
      };
      this.persistSelectedCover(updatedCover, 'Лимит удален', 'Ошибка удаления лимита');
    }
  }

  updateLimitsTable(): void {
    if (this.selectedPackageIndex === -1 || this.selectedCoverIndex === -1) {
      this.paginatedLimits = [];
      return;
    }

    const selectedPackage = this.product.packages[this.selectedPackageIndex];
    if (!selectedPackage || !selectedPackage.covers || this.selectedCoverIndex >= selectedPackage.covers.length) {
      this.paginatedLimits = [];
      return;
    }

    const selectedCover = selectedPackage.covers[this.selectedCoverIndex];
    if (!selectedCover || !selectedCover.limits) {
      this.paginatedLimits = [];
      return;
    }

    // Create a new array reference to trigger Angular change detection
    this.paginatedLimits = [...selectedCover.limits];
  }

  updateTables(): void {
    this.updateQuoteValidatorTable();
    this.updateSaveValidatorTable();
    this.updatePackagesTable();
    
    // Validate selectedPackageIndex before updating child tables
    if (this.selectedPackageIndex >= 0 && 
        this.selectedPackageIndex < this.product.packages.length) {
      this.currentPackage = this.product.packages[this.selectedPackageIndex];
      this.updateChildTables();
    } else {
      this.selectedPackageIndex = -1;
      this.currentPackage = null;
      this.selectedCoverIndex = -1;
      this.updateChildTables();
    }
    
    this.updatePolicyTable();
  }

 
  updatePolicyTable(): void {
    this.syncProductTreeFromVars();
  }

  onShowDeletedProductTreeChange(): void {
    this.syncProductTreeFromVars();
  }

  /**
   * Плоский список {@link Product.vars} → строки для {@link TreeTableComponent}
   * (иерархия по {@code id} / {@code parent_id}, как в PvVar с бэкенда).
   */
  syncProductTreeFromVars(): void {
    const vars = this.product?.vars ?? [];
    const syntheticBase = 1_000_000;
    const used = new Set<number>();
    // delete vars with parent_id is null and varCode != 'policy'
    const vars2 = vars.filter(v => (!(v.parent_id == null && v.varCode !== 'policy')));
    
    this.productTreeTableData = vars2.map((v, i) => this.policyVarToTreeRow(v, i, syntheticBase, used));
  }

  private policyVarToTreeRow(
    v: PolicyVar,
    index: number,
    syntheticBase: number,
    used: Set<number>,
  ): TreeTableSourceRow {
    let id = v.id != null ? Number(v.id) : NaN;
    if (!Number.isFinite(id) || used.has(id)) {
      id = syntheticBase + index;
    }
    used.add(id);

    const p = v.parent_id;
    const parentNum = p === null || p === undefined ? NaN : Number(p);
    const parent_id = Number.isFinite(parentNum) ? parentNum : null;

    const nrRaw = v.varNr;
    const varNr =
      typeof nrRaw === 'number' && Number.isFinite(nrRaw)
        ? nrRaw
        : Number(String(nrRaw ?? '').trim()) || 0;

    return {
      id,
      parent_id,
      varNr,
      varName: v.varName ?? '',
      varCode: v.varCode ?? '',
      varType: v.varType,
      varDataType: v.varDataType,
      isSystem: v.isSystem ?? false,
      isDeleted: v.isDeleted ?? false,
      isTarifFactor: v.isTarifFactor ?? false,
      isOptional: v.isOptional ?? false,
      varList: v.varList,
      varPath: v.varPath ?? '',
      varValue: v.varValue ?? '',
      varCdm: v.varCdm ?? '',
      name: v.name?.trim() || v.varName || '',
    };
  }

  /** Сопоставление строки дерева с элементом {@link Product.vars}. */
  findPolicyVarForTreeRow(row: TreeTableSourceRow): PolicyVar | undefined {
    return this.product.vars.find((v) => {
      if (v.id != null && Number(v.id) === row.id) {
        return true;
      }
      return v.varCode === row.varCode && (v.varCdm ?? '') === (row.varCdm ?? '');
    });
  }

  openProductPvVarEditDialog(row: TreeTableSourceRow): void {
    if (this.productTreeMutationsLocked) {
      return;
    }
    const v = this.findPolicyVarForTreeRow(row);
    if (!v) {
      return;
    }
    if (!this.product.id || !this.product.versionNo) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    const productId = this.product.id;
    const versionNo = this.product.versionNo;
    const originalVarCode = v.varCode;
    this.dialog
      .open(ProductPvVarEditDialogComponent, {
        width: '760px',
        maxHeight: '90vh',
        data: { variable: { ...v } },
      })
      .afterClosed()
      .subscribe((result?: PolicyVar) => {
        if (!result) {
          return;
        }
        this.productService.updateVar(productId, versionNo, originalVarCode, result).subscribe({
          next: (updatedProduct) => {
            this.product = updatedProduct;
            this.updateTables();
            this.hasChanges = false;
            this.snackBar.open('Переменная обновлена', 'Закрыть', { duration: 2000 });
          },
          error: (error: any) => {
            console.error('Error updating variable:', error);
            this.snackBar.open('Ошибка обновления переменной', 'Закрыть', { duration: 3000 });
          }
        });
      });
  }

  private applyPolicyVarEditFromDialog(target: PolicyVar, edited: PolicyVar): void {
    target.id = edited.id;
    target.parent_id = edited.parent_id;
    target.varList = edited.varList;
    target.isSystem = edited.isSystem;
    target.isDeleted = edited.isDeleted;
    target.varPath = edited.varPath;
    target.varName = edited.varName;
    target.varCode = edited.varCode;
    target.varDataType = edited.varDataType;
    target.varValue = edited.varValue;
    target.varType = edited.varType;
    target.varCdm = edited.varCdm;
    target.varNr = edited.varNr;
    target.varRefCode = edited.varRefCode;
    target.isTarifFactor = edited.isTarifFactor;
    target.isOptional = edited.isOptional;
  }

  toggleProductTreeOptional(row: TreeTableSourceRow): void {
    if (this.productTreeMutationsLocked) {
      return;
    }
    const v = this.findPolicyVarForTreeRow(row);
    if (!v) {
      return;
    }
    if (!this.product.id || !this.product.versionNo) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    const productId = this.product.id;
    const versionNo = this.product.versionNo;
    const nextValue = !(v.isOptional ?? false);
    const payload: PolicyVar = { ...v, isOptional: nextValue };
    this.productService.updateVar(productId, versionNo, v.varCode, payload).subscribe({
      next: (updatedProduct) => {
        this.product = updatedProduct;
        this.updateTables();
        this.hasChanges = false;
      },
      error: (error: any) => {
        console.error('Error updating optional flag:', error);
        this.snackBar.open('Ошибка обновления обязательности', 'Закрыть', { duration: 3000 });
      }
    });
  }

  /**
   * После встроенного soft-delete в дереве у потомков уже стоит {@code isDeleted} на строках —
   * переносим флаги в {@link Product.vars}.
   */
  onProductTreeSoftDeleted(row: TreeTableSourceRow): void {
    const v = this.findPolicyVarForTreeRow(row);
    if (!v) {
      return;
    }
    if (!this.product.id || !this.product.versionNo) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    const productId = this.product.id;
    const versionNo = this.product.versionNo;
    this.productService.deleteVar(productId, versionNo, v.varCode).subscribe({
      next: (updatedProduct) => {
        this.product = updatedProduct;
        this.updateTables();
        this.hasChanges = false;
      },
      error: (error: any) => {
        console.error('Error deleting variable:', error);
        this.snackBar.open('Ошибка удаления переменной', 'Закрыть', { duration: 3000 });
      }
    });
  }

  onProductTreeRestoreNode(row: TreeTableSourceRow): void {
    const v = this.findPolicyVarForTreeRow(row);
    if (!v) {
      return;
    }
    if (!this.product.id || !this.product.versionNo) {
      this.snackBar.open('Сначала сохраните продукт', 'Закрыть', { duration: 3000 });
      return;
    }
    const productId = this.product.id;
    const versionNo = this.product.versionNo;
    const payload: PolicyVar = { ...v, isDeleted: false };
    this.productService.updateVar(productId, versionNo, v.varCode, payload).subscribe({
      next: (updatedProduct) => {
        this.product = updatedProduct;
        this.updateTables();
        this.hasChanges = false;
      },
      error: (error: any) => {
        console.error('Error restoring variable:', error);
        this.snackBar.open('Ошибка восстановления переменной', 'Закрыть', { duration: 3000 });
      }
    });
  }

  get paginatedPolicyVars(): any[] {
    const vars = this.product?.vars ?? [];
    let filtered = vars;
    let categories: string[] = [];
    let prefix = '';
    let result: any[] = [];

    const cdm = (v: PolicyVar) => (v.varCdm ?? '').trim();

    // Apply filter based on selected option
    if (this.policyFilter === 'policyHolder') {
      prefix = 'policyHolder.';
      filtered = vars.filter((v) => cdm(v).startsWith(prefix));
      categories = this.varsService.getPhCategories(this.lob?.mpPhType || '');
    } else if (this.policyFilter === 'insuredObject') {
      prefix = 'insuredObject.';
      filtered = vars.filter((v) => cdm(v).startsWith(prefix));
      categories = this.varsService.getIoCategories(this.lob?.mpInsObjectType || 'person');
    } else if (this.policyFilter === 'policy') {
      prefix = 'policy.';
      filtered = vars.filter((v) => cdm(v).startsWith(prefix));
      categories = [''];
    } else if (this.policyFilter === 'coverage') {
      prefix = 'coverage.';
      filtered = vars.filter((v) => cdm(v).startsWith(prefix));
      categories = [''];
    } else if (this.policyFilter === 'strings') {
      prefix = 'strings.';
      filtered = vars.filter((v) => cdm(v).startsWith(prefix));
      categories = [''];
    }

    for (const category of categories) {
      let category_filtered = filtered.filter((v) => cdm(v).startsWith(prefix + category));
    
      // Sort by varNr
      category_filtered = category_filtered.sort((a, b) => (a.varNr ?? 0) - (b.varNr ?? 0));

      let prevCategory = '';
      // Process category and field columns based on varPath
      category_filtered.map((v, index) => {
        let category = '';
        let field = '';
        const path = cdm(v);
        const parts = path.split('.');

        if (path.startsWith('policyHolder.')) {
          category = parts[1];
          field = parts[2];
        } else if (path.startsWith('insuredObject.')) {
          category = parts[1];
          field = parts[2];
        } else if (path.startsWith('policy.')) {
          category = '';
          field = parts[1];
        } else if (path.startsWith('coverage.')) {
          category = '';
          field = parts[1];
        } else if (path.startsWith('strings.')) {
          category = '';
          field = parts.length > 1 ? parts.slice(1).join('.') : '';
        } else {
          category = parts.join('.');
          field = '';
        } 

        if (category === prevCategory) {
          category = ''; // Hide duplicate category
        } else {
          prevCategory = category;
        }

        result.push({
          ...v,
          category,
          field,
          name: v.varName,
          code: v.varCode
        });
      });
    }

    return result;
    
  }

  showPolicyVarDetails(variable: PolicyVar): void {
    this.dialog.open(PolicyVarDetailsDialog, {
      width: '600px',
      data: { variable }
    });
  }

  deletePolicyVar(variable: PolicyVar, index: number): void {
    //if (confirm(`Удалить переменную "${variable.name}"?`)) {

      // Удаляем переменную из this.product.vars по varCode
      this.product.vars = this.product.vars.filter(v => v.varCode !== variable.varCode);

      // Find the original index in policyVars array
      //const originalIndex = this.policyVars.findIndex(v => v.varPath === variable.varPath);
      //if (originalIndex !== -1) {
        //this.policyVars.splice(originalIndex, 1);
        this.updatePolicyTable();
        this.updateChanges();
      //}
    //}
  }

  // JSON File operations
  saveToFile(): void {
    const dataStr = JSON.stringify(this.product, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `product_${this.product.code || 'new'}_${Date.now()}.json`;
    link.click();
    URL.revokeObjectURL(url);
    this.snackBar.open('JSON файл сохранен', 'Закрыть', { duration: 2000 });
  }

  loadFromFile(): void {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          try {
            const jsonData = JSON.parse(e.target.result);

            jsonData.id = this.product.id;
            jsonData.versionNo = this.product.versionNo;
            jsonData.versionStatus = this.product.versionStatus;
            jsonData.code = this.product.code;
            jsonData.name = this.product.name;
            jsonData.lob = this.product.lob;

            this.product = { ...this.product, ...jsonData };
            this.updateTables();
            this.updateChanges();
            this.snackBar.open('JSON файл загружен успешно', 'Закрыть', { duration: 2000 });
          } catch (error) {
            console.error('Error parsing JSON:', error);
            this.snackBar.open('Ошибка при загрузке JSON файла', 'Закрыть', { duration: 3000 });
          }
        };
        reader.readAsText(file);
      }
    };
    input.click();
  }

  // Test tab
  testType: 'quote' | 'policy' = 'quote';
  testRequestText = '';
  testResponseText = '';

  onTestTypeChange(): void {
    this.loadTestRequest();
  }

  loadTestRequest(): void {
    const productId = this.product.id;
    const versionNo = this.product.versionNo;
    if (!productId || versionNo == null) {
      this.snackBar.open('Product not loaded', 'Close', { duration: 3000 });
      return;
    }
    const obs = this.testType === 'quote'
      ? this.testRequestService.getTestQuote(productId, versionNo)
      : this.testRequestService.getTestPolicy(productId, versionNo);
    obs.subscribe({
      next: (json) => {
        this.testRequestText = json;
        try {
          this.testRequestText = JSON.stringify(JSON.parse(json), null, 2);
        } catch {
          this.testRequestText = json;
        }
      },
      error: (e) => this.snackBar.open('Error: ' + (e?.error?.message || e?.message || 'Unknown'), 'Close', { duration: 5000 })
    });
  }

  executeTest(): void {
    const json = this.testRequestText?.trim();
    if (!json) {
      this.snackBar.open('Request is empty', 'Close', { duration: 3000 });
      return;
    }
    const obs = this.testType === 'quote'
      ? this.testRequestService.executeQuote(json)
      : this.testRequestService.executePolicy(json);
    obs.subscribe({
      next: (res) => {
        try {
          this.testResponseText = JSON.stringify(JSON.parse(res), null, 2);
        } catch {
          this.testResponseText = res;
        }
      },
      error: (e) => {
        const msg = e?.error?.message || e?.message || e?.statusText || 'Unknown error';
        this.testResponseText = typeof e?.error === 'string' ? e.error : (e?.error?.message || msg);
      }
    });
  }

  saveTestRequest(): void {
    const productId = this.product.id;
    const versionNo = this.product.versionNo;
    if (!productId || versionNo == null) {
      this.snackBar.open('Product not loaded', 'Close', { duration: 3000 });
      return;
    }
    const json = this.testRequestText?.trim();
    if (!json) {
      this.snackBar.open('Request is empty', 'Close', { duration: 3000 });
      return;
    }
    const obs = this.testType === 'quote'
      ? this.testRequestService.saveTestQuote(productId, versionNo, json)
      : this.testRequestService.saveTestPolicy(productId, versionNo, json);
    obs.subscribe({
      next: () => this.snackBar.open('Saved', 'Close', { duration: 2000 }),
      error: (e) => this.snackBar.open('Error: ' + (e?.error?.message || e?.message || 'Unknown'), 'Close', { duration: 5000 })
    });
  }

  getTestRequest(): void {
    const productId = this.product.id;
    const versionNo = this.product.versionNo;

    if (!productId || !versionNo) {
      alert('Product ID and Version are required');
      return;
    }

    this.productService.getTestRequestQuote(productId, versionNo).subscribe(result => {
      this.dialog.open(TestRequestDialog, {
        data: { title: 'Test request', object: result },
        width: '800px',
        maxHeight: '80vh'
      });
    });
  }

  getTestRequestSave(): void {
    const productId = this.product.id;
    const versionNo = this.product.versionNo;

    if (!productId || !versionNo) {
      alert('Product ID and Version are required');
      return;
    }

    this.productService.getTestRequestSave(productId, versionNo).subscribe(result => {
      this.dialog.open(TestRequestDialog, {
        data: { title: 'Test request save', object: result },
        width: '800px',
        maxHeight: '80vh'
      });
    });
  }

  // File methods - now using Business Line files
  uploadFile(file: PackageFile): void {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '*/*';
    input.onchange = (event: any) => {
      const selectedFile = event.target.files[0];
      if (selectedFile) {
        const fileName = selectedFile.name; // File name includes extension (e.g., "document.pdf")
        console.log(fileName);
        this.filesService.uploadFile(selectedFile).subscribe({
          next: (response) => {
            file.fileId = typeof response.id === 'string' ? parseInt(response.id, 10) : response.id;
            file.fileName = fileName;
            this.updateFilesTable();
            this.updateChanges();
            this.snackBar.open('Файл загружен успешно', 'Закрыть', { duration: 3000 });
          },
          error: (error) => {
            console.error('Error uploading file:', error);
            this.snackBar.open('Ошибка загрузки файла', 'Закрыть', { duration: 3000 });
          }
        });
      }
    };
    input.click();
  }

  downloadFile(file: PackageFile): void {
    if (!file.fileId) {
      this.snackBar.open('Файл не загружен', 'Закрыть', { duration: 3000 });
      return;
    }
    this.filesService.downloadFile(file.fileId).subscribe({
      next: ({ blob, filename }) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename || file.fileName || 'download';
        link.click();
        window.URL.revokeObjectURL(url);
        this.snackBar.open('Файл скачан', 'Закрыть', { duration: 2000 });
      },
      error: () => this.snackBar.open('Ошибка скачивания файла', 'Закрыть', { duration: 3000 })
    });
  }

  

  refreshFiles(): void {
    console.log('refresh' + this.lob?.mpName );
    if (!this.lob?.mpFiles) {
      // Handle the undefined case: set to empty array or handle error as needed
      var lobFiles: PackageFile[] = [];
    } else {
      var lobFiles: PackageFile[] = this.lob.mpFiles.map(f => ({
        fileCode: f.fileCode,
        fileName: f.fileName || ''
      }));
    }
console.log(lobFiles);

    this.product.packages.forEach(pkg => {
console.log(pkg)

      // Add to pkg.files file from lobFiles.
      // if file exist, check by fileCode, the do nothing
      // otherwise add fileCode, fileName, null
      // Объединяем с приоритетом для updates
      pkg.files = Array.from(
        new Map([...pkg.files, ...lobFiles].map(item => [item.fileCode, item])).values());
    });
    
    this.updateFilesTable();
  }

  getFileIndex(file: BusinessLineFile): number {
    const index = this.businessLineFiles.findIndex(f => f.fileCode === file.fileCode);
    return index !== -1 ? index : 0;
  }

  // Helper methods to get indices from paginated arrays
  getQuoteValidatorIndex(validator: QuoteValidator): number {
    // Try to find by reference first (most reliable)
    const refIndex = this.product.quoteValidator.indexOf(validator);
    if (refIndex !== -1) return refIndex;
    
    // Fallback to matching by properties
    const index = this.product.quoteValidator.findIndex(v => 
      v.lineNr === validator.lineNr && 
      v.keyLeft === validator.keyLeft && 
      v.ruleType === validator.ruleType &&
      v.keyRight === validator.keyRight
    );
    return index !== -1 ? index : 0;
  }

  getSaveValidatorIndex(validator: QuoteValidator): number {
    // Try to find by reference first (most reliable)
    const refIndex = this.product.saveValidator.indexOf(validator);
    if (refIndex !== -1) return refIndex;
    
    // Fallback to matching by properties
    const index = this.product.saveValidator.findIndex(v => 
      v.lineNr === validator.lineNr && 
      v.keyLeft === validator.keyLeft && 
      v.ruleType === validator.ruleType &&
      v.keyRight === validator.keyRight
    );
    return index !== -1 ? index : 0;
  }

  truncateText(text: string | undefined | null, maxLen: number): string {
    const s = text ?? '';
    return s.length > maxLen ? s.slice(0, maxLen) + '...' : s;
  }

  getPackageIndex(pkg: Package): number {
    const list = this.product.packages ?? [];
    const refIndex = list.indexOf(pkg);
    if (refIndex !== -1) {
      return refIndex;
    }
    if (pkg.id != null) {
      const byId = list.findIndex((p) => p.id === pkg.id);
      if (byId !== -1) {
        return byId;
      }
    }
    return list.findIndex((p) => p.code === pkg.code);
  }

  getCoverIndex(cover: Cover): number {
    if (this.selectedPackageIndex === -1) return 0;
    
    const covers = this.product.packages[this.selectedPackageIndex].covers;
    if (!covers) return 0;
    
    // Try to find by reference first (most reliable)
    const refIndex = covers.indexOf(cover);
    if (refIndex !== -1) return refIndex;
    
    // Fallback to matching by code
    const index = covers.findIndex(c => c.code === cover.code);
    return index !== -1 ? index : 0;
  }

  getValidatorTypeHelp(code: string|undefined): string {
    if (code == 'LIST') return 'Список возможных значений через запятую. Формат ISO 8601, например P14D - 14 дней, P2M - 2 месяца, P1Y - 1 год.';
    if (code == 'RANGE') return 'Минимальное и максимальное количество дней, между датой выпуска договора и датой начала действия договора. Формат ISO 8601, например P5D-P365D диапазон от 5 дней до 365.';
    if (code == 'NEXT_MONTH') return 'Договор начнет действовать с 1 числа следующего месяца';
    return '';
  }
  
  getValidatorTypeHelp2(code: string|undefined): string {
    if (code == 'LIST') return 'Список возможных значений через запятую. Формат ISO 8601, например P14D - 14 дней, P2M - 2 месяца, P1Y - 1 год.';
    if (code == 'RANGE') return 'Минимальный и максимальный срок действия договора в днях. ' +
      ' Срок рассчитывается как период между датой начала и датой окончания действия договора. ' +
      ' Пример: P5D–P365D — договор может действовать от 5 до 365 дней.';
    return '';
  }
}

@Component({
    selector: 'app-test-request-dialog',
    imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
    template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <div mat-dialog-content style="max-height: 60vh; overflow: auto;">
      <pre style="background-color: #f5f5f5; padding: 16px; border-radius: 4px; overflow-x: auto;">{{ data.object | json }}</pre>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button (click)="copyToClipboard()">
        <mat-icon>content_copy</mat-icon>
        Copy to Clipboard
      </button>
      <button mat-flat-button color="primary" mat-dialog-close>Close</button>
    </div>
  `,
    styles: [`
    pre {
      margin: 0;
      font-family: 'Courier New', Courier, monospace;
      font-size: 13px;
      line-height: 1.5;
      white-space: pre-wrap;
      word-wrap: break-word;
    }
  `]
})
export class TestRequestDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public data: { title: string; object: any }) {}

  copyToClipboard(): void {
    const jsonString = JSON.stringify(this.data.object, null, 2);
    navigator.clipboard.writeText(jsonString).then(() => {
      // Could show a snackbar notification here if needed
    });
  }
}

@Component({
    selector: 'app-policy-var-details-dialog',
    imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
    template: `
    <h2 mat-dialog-title>Variable Details</h2>
    <div mat-dialog-content>
      <div style="margin-bottom: 16px;">
        <strong>Variable Path:</strong> {{ data.variable.varPath }}
      </div>
      <div style="margin-bottom: 16px;">
        <strong>Variable Name:</strong> {{ data.variable.varName }}
      </div>
      <div style="margin-bottom: 16px;">
        <strong>Variable Code:</strong> {{ data.variable.varCode }}
      </div>
      <div style="margin-bottom: 16px;">
        <strong>CDM:</strong> {{ data.variable.varCdm }}
      </div>
      <div style="margin-bottom: 16px;">
        <strong>Type / Data type:</strong> {{ data.variable.varType }} / {{ data.variable.varDataType }}
      </div>
      @if (data.variable.id != null) {
        <div style="margin-bottom: 16px;">
          <strong>id / parent_id:</strong> {{ data.variable.id }} / {{ data.variable.parent_id ?? '—' }}
        </div>
      }
      @if (data.variable.varList) {
        <div style="margin-bottom: 16px;">
          <strong>varList:</strong> {{ data.variable.varList }}
        </div>
      }
    </div>
    <div mat-dialog-actions align="end">
      <button mat-flat-button color="primary" mat-dialog-close>Close</button>
    </div>
  `
})
export class PolicyVarDetailsDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public data: { variable: PolicyVar }) {}
}

