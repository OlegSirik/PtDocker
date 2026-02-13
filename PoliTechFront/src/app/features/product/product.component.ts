import { Component, OnInit, inject, Inject } from '@angular/core';
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

import { ProductService, Product, QuoteValidator, Package, PackageFile, Cover, Deductible, Limit } from '../../shared/services/product.service';

export interface PolicyVar {
  varPath: string;
  varName: string;
  varCode: string;
  category: string;
  field: string;
  name: string;
  code: string;
  varNr:  number;
  varCdm: string;
}
import { ValidatorDialogComponent } from './validator-dialog/validator-dialog.component';
import { PackageDialogComponent } from './package-dialog/package-dialog.component';
import { CoverDialogComponent } from './cover-dialog/cover-dialog.component';
import { DeductibleDialogComponent } from './deductible-dialog/deductible-dialog.component';
import { LimitDialogComponent } from './limit-dialog/limit-dialog.component';
import { Observable, of } from 'rxjs';
import { tap, map, filter, catchError, switchMap } from 'rxjs/operators';
import { BusinessLineService } from '../../shared/services/business-line.service';
import { BusinessLineEditService, BusinessLineFile, BusinessLineVar, BusinessLineEdit } from '../../shared/services/business-line-edit.service';
import { FilesService, FileTemplate } from '../../shared/services/api/files.service';
import { AuthService } from '../../shared/services/auth.service';
import { TestRequestService } from '../../shared/services/api/test-request.service';
import { VarsService } from '../../shared/services/vars.service';

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
        MatBadgeModule
    ],
    templateUrl: './product.component.html',
    styleUrls: ['./product.component.scss']
})
export class ProductComponent implements OnInit {
  lob: BusinessLineEdit | undefined;
  
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
    vars: []
  };

  isNewRecord = false;
  hasChanges = false;

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
  policyFilter = 'Policy Holder';

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
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    const versionNo = this.route.snapshot.paramMap.get('versionNo');

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
          // Ensure arrays are initialized for all packages and covers
          if (this.product.packages) {
            this.product.packages.forEach(pkg => {
              if (!pkg.files) {
                pkg.files = [];
              }
              if (!pkg.covers) {
                pkg.covers = [];
              }
              // Ensure deductibles and limits arrays are initialized for all covers
              if (pkg.covers) {
                pkg.covers.forEach((cover: Cover) => {
                  if (!cover.deductibles) {
                    cover.deductibles = [];
                  }
                  if (!cover.limits) {
                    cover.limits = [];
                  }
                });
              }
            });
          }
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
    

    this.loadDropdownOptions()
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
      if (result) {
        this.product.quoteValidator.push(result);
        this.updateQuoteValidatorTable();
        this.updateChanges();
      }
    });
  }

  editQuoteValidator(validator: QuoteValidator, index: number): void {
    this.loadDropdownOptions()
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
      if (result) {
        this.product.quoteValidator[index] = result;
        this.updateQuoteValidatorTable();
        this.updateChanges();
      }
    });
  }

  deleteQuoteValidator(validator: QuoteValidator, index: number): void {
    if (confirm('Удалить проверку предрасчета?')) {
      this.product.quoteValidator.splice(index, 1);
      this.updateQuoteValidatorTable();
      this.updateChanges();
    }
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
    this.loadDropdownOptions()
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
      if (result) {
        this.product.saveValidator.push(result);
        this.updateSaveValidatorTable();
        this.updateChanges();
      }
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
    this.loadDropdownOptions()
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
      if (result) {
        this.product.saveValidator[index] = result;
        this.updateSaveValidatorTable();
        this.updateChanges();
      }
    });
  }

  deleteSaveValidator(validator: QuoteValidator, index: number): void {
    if (confirm('Удалить проверку договора?')) {
      this.product.saveValidator.splice(index, 1);
      this.updateSaveValidatorTable();
      this.updateChanges();
    }
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

  // Package methods
  addPackage(): void {
    const dialogRef = this.dialog.open(PackageDialogComponent, {
      width: '500px',
      data: {
        isNew: true
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Calculate package.id as max(id) + 1
        const maxId = this.product.packages.length > 0 
          ? Math.max(...this.product.packages.map(p => p.id || 0))
          : 0;
        result.id = maxId + 1;
        result.code = result.id.toString();
        
        // Ensure arrays are initialized
        if (!result.files) {
          result.files = [];
        }
        if (!result.covers) {
          result.covers = [];
        }
        
        this.product.packages.push(result);
        this.updatePackagesTable();
        
        // Make new record selected and update child tables
        this.selectedPackageIndex = this.product.packages.length - 1;
        this.currentPackage = result;
        this.updateChildTables();
        
        this.updateChanges();
      }
    });
  }

  editPackage(pkg: Package, index: number): void {
    const dialogRef = this.dialog.open(PackageDialogComponent, {
      width: '500px',
      data: {
        package: pkg,
        isNew: false
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Preserve existing files and covers arrays if not provided
        if (!result.files) {
          result.files = pkg.files || [];
        }
        if (!result.covers) {
          result.covers = pkg.covers || [];
        }
        // Preserve id and code
        result.id = pkg.id;
        result.code = pkg.code;
        this.product.packages[index] = result;
        this.updatePackagesTable();
        // Update child tables if this package is selected
        if (this.selectedPackageIndex === index) {
          this.currentPackage = result;
          this.updateChildTables();
        }
        this.updateChanges();
      }
    });
  }

  deletePackage(pkg: Package, index: number): void {
    if (confirm('Удалить пакет?')) {
      this.product.packages.splice(index, 1);
      
      // Reset selection if deleted package was selected
      if (this.selectedPackageIndex === index) {
        this.selectedPackageIndex = -1;
        this.currentPackage = null;
        this.selectedCoverIndex = -1;
      } else if (this.selectedPackageIndex > index) {
        // Adjust index if a package before the selected one was deleted
        this.selectedPackageIndex--;
        this.currentPackage = this.product.packages[this.selectedPackageIndex];
      }
      
      this.updatePackagesTable();
      this.updateChildTables();
      this.updateChanges();
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
    this.filteredPackages = this.product.packages;
    this.paginatedPackages = this.product.packages;
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
        this.product.packages[this.selectedPackageIndex].covers.push(result);
        this.updateCoversTable();
        this.updateChanges();
      }
    });
  }

  editCover(cover: Cover, index: number): void {
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
        this.product.packages[this.selectedPackageIndex].covers[index] = result;
        this.updateCoversTable();
        // Update detail tables if this cover is selected
        if (this.selectedCoverIndex === index) {
          this.updateDeductiblesTable();
          this.updateLimitsTable();
        }
        this.updateChanges();
      }
    });
  }

  deleteCover(cover: Cover, index: number): void {
    if (confirm('Удалить покрытие?')) {
      // Reset cover selection if deleted cover was selected
      if (this.selectedCoverIndex === index) {
        this.selectedCoverIndex = -1;
      } else if (this.selectedCoverIndex > index) {
        this.selectedCoverIndex--;
      }
      
      this.product.packages[this.selectedPackageIndex].covers.splice(index, 1);
      this.updateCoversTable();
      this.updateDeductiblesTable();
      this.updateLimitsTable();
      this.updateChanges();
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

  // Deductible methods
  addDeductible(): void {
    if (this.selectedPackageIndex === -1 || this.selectedCoverIndex === -1) return;

    const dialogRef = this.dialog.open(DeductibleDialogComponent, {
      width: '600px',
      data: {
        isNew: true
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const cover = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex];
        if (!cover.deductibles) {
          cover.deductibles = [];
        }
        
        // Validate: id must be unique and not empty
        if (!result.id && result.id !== 0) {
          this.snackBar.open('ID не может быть пустым', 'Закрыть', { duration: 3000 });
          return;
        }
        if (!result.text || result.text.trim() === '') {
          this.snackBar.open('Текст не может быть пустым', 'Закрыть', { duration: 3000 });
          return;
        }
        if (cover.deductibles.some(d => d.id === result.id)) {
          this.snackBar.open('ID должен быть уникальным', 'Закрыть', { duration: 3000 });
          return;
        }
        
        cover.deductibles.push(result);
        this.updateDeductiblesTable();
        this.updateChanges();
      }
    });
  }

  editDeductible(deductible: Deductible, index: number): void {
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
        if (!cover.deductibles) {
          cover.deductibles = [];
        }
        
        // Validate: id must be unique (excluding current index) and not empty
        if (!result.id && result.id !== 0) {
          this.snackBar.open('ID не может быть пустым', 'Закрыть', { duration: 3000 });
          return;
        }
        if (!result.text || result.text.trim() === '') {
          this.snackBar.open('Текст не может быть пустым', 'Закрыть', { duration: 3000 });
          return;
        }
        if (cover.deductibles.some((d, i) => d.id === result.id && i !== index)) {
          this.snackBar.open('ID должен быть уникальным', 'Закрыть', { duration: 3000 });
          return;
        }
        
        cover.deductibles[index] = result;
        this.updateDeductiblesTable();
        this.updateChanges();
      }
    });
  }

  deleteDeductible(deductible: Deductible, index: number): void {
    if (confirm('Удалить франшизу?')) {
      const cover = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex];
      if (cover.deductibles) {
        cover.deductibles.splice(index, 1);
      }
      this.updateDeductiblesTable();
      this.updateChanges();
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

    const dialogRef = this.dialog.open(LimitDialogComponent, {
      width: '500px',
      data: {
        isNew: true
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const cover = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex];
        if (!cover.limits) {
          cover.limits = [];
        }
        
        // Validate: sumInsured must be unique, sumInsured and premium must be > 0
        if (!result.sumInsured || result.sumInsured <= 0) {
          this.snackBar.open('Страховая сумма должна быть больше 0', 'Закрыть', { duration: 3000 });
          return;
        }
        if (!result.premium || result.premium <= 0) {
          this.snackBar.open('Премия должна быть больше 0', 'Закрыть', { duration: 3000 });
          return;
        }
        if (cover.limits.some(l => l.sumInsured === result.sumInsured)) {
          this.snackBar.open('Страховая сумма должна быть уникальной', 'Закрыть', { duration: 3000 });
          return;
        }
        
        cover.limits.push(result);
        this.updateLimitsTable();
        this.updateChanges();
      }
    });
  }

  editLimit(limit: Limit, index: number): void {
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
        if (!cover.limits) {
          cover.limits = [];
        }
        
        // Validate: sumInsured must be unique (excluding current index), sumInsured and premium must be > 0
        if (!result.sumInsured || result.sumInsured <= 0) {
          this.snackBar.open('Страховая сумма должна быть больше 0', 'Закрыть', { duration: 3000 });
          return;
        }
        if (!result.premium || result.premium <= 0) {
          this.snackBar.open('Премия должна быть больше 0', 'Закрыть', { duration: 3000 });
          return;
        }
        if (cover.limits.some((l, i) => l.sumInsured === result.sumInsured && i !== index)) {
          this.snackBar.open('Страховая сумма должна быть уникальной', 'Закрыть', { duration: 3000 });
          return;
        }
        
        cover.limits[index] = result;
        this.updateLimitsTable();
        this.updateChanges();
      }
    });
  }

  deleteLimit(limit: Limit, index: number): void {
    if (confirm('Удалить лимит?')) {
      const cover = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex];
      if (cover.limits) {
        cover.limits.splice(index, 1);
      }
      this.updateLimitsTable();
      this.updateChanges();
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

 
  updatePolicyTable() {}

  get paginatedPolicyVars(): any[] {

    let filtered = this.product.vars;
    let categories: string[] = [];
    let prefix = '';
    let result: any[] = [];

    // Apply filter based on selected option
    if (this.policyFilter === 'policyHolder') {
      prefix = 'policyHolder.';
      filtered = this.product.vars.filter(v => v.varCdm.startsWith(prefix));
      categories = this.varsService.getPhCategories(this.lob?.mpPhType || '');
    } else if (this.policyFilter === 'insuredObject') {
      prefix = 'insuredObject.';
      filtered = this.product.vars.filter(v => v.varCdm.startsWith(prefix));
      categories = this.varsService.getIoCategories(this.lob?.mpInsObjectType || 'person');
    } else if (this.policyFilter === 'policy') {
      prefix = 'policy.';
      filtered = this.product.vars.filter(v => v.varCdm.startsWith(prefix));
      categories = [''];
    } else if (this.policyFilter === 'coverage') {
      prefix = 'coverage.';
      filtered = this.product.vars.filter(v => v.varCdm.startsWith(prefix));
      categories = [''];
    } else if (this.policyFilter === 'strings') {
      prefix = 'strings.';
      filtered = this.product.vars.filter(v => v.varCdm.startsWith(prefix));
      categories = [''];
    }

    for (const category of categories) {
      let category_filtered = filtered.filter(v => v.varCdm.startsWith(prefix + category));
    
      // Sort by varNr
      category_filtered = category_filtered.sort((a, b) => (a.varNr ?? 0) - (b.varNr ?? 0));

      let prevCategory = '';
      // Process category and field columns based on varPath
      category_filtered.map((v, index) => {
        let category = '';
        let field = '';
        const parts = v.varCdm.split('.');

        if (v.varCdm.startsWith('policyHolder.')) {
          category = parts[1];
          field = parts[2];
        } else if (v.varCdm.startsWith('insuredObject.')) {
          category = parts[1];
          field = parts[2];
        } else if (v.varCdm.startsWith('policy.')) {
          category = '';
          field = parts[1];
        } else if (v.varCdm.startsWith('coverage.')) {
          category = '';
          field = parts[1];
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
        this.filesService.uploadFileWithResponse(selectedFile, fileName).subscribe({
          next: (response) => {
            file.fileId = response.id;
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

  downloadFile(file: BusinessLineFile): void {
  
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
    // Try to find by reference first (most reliable)
    const refIndex = this.product.packages.indexOf(pkg);
    if (refIndex !== -1) return refIndex;
    
    // Fallback to matching by code or id
    const index = this.product.packages.findIndex(p => p.code === pkg.code || p.id === pkg.id);
    return index !== -1 ? index : 0;
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
      <button mat-raised-button color="primary" mat-dialog-close>Close</button>
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
        <strong>Category:</strong> {{ data.variable.category }}
      </div>
      <div style="margin-bottom: 16px;">
        <strong>Field:</strong> {{ data.variable.field }}
      </div>
      <div style="margin-bottom: 16px;">
        <strong>Name:</strong> {{ data.variable.name }}
      </div>
      <div style="margin-bottom: 16px;">
        <strong>Code:</strong> {{ data.variable.code }}
      </div>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-raised-button color="primary" mat-dialog-close>Close</button>
    </div>
  `
})
export class PolicyVarDetailsDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public data: { variable: PolicyVar }) {}
}

