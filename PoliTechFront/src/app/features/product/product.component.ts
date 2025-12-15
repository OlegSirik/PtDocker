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
  coverCodeOptions: string[] = [];
  deductibleTypeOptions: string[] = [];
  deductibleUnitOptions: string[] = [];
  deductibleSpecificOptions: string[] = [];
  validatorTypeOptions: string[] = [];
  resetPolicyOptions: string[] = [];

  // Quote Validator table
  quoteValidatorDisplayedColumns = ['lineNr', 'keyLeft', 'ruleType', 'keyRight', 'valueRight', 'dataType', 'errorText', 'actions'];
  quoteValidatorSearchText = '';
  quoteValidatorPageSize = 10;
  quoteValidatorPageIndex = 0;
  filteredQuoteValidators: QuoteValidator[] = [];
  paginatedQuoteValidators: QuoteValidator[] = [];

  // Save Validator table
  saveValidatorDisplayedColumns = ['lineNr', 'keyLeft', 'ruleType', 'keyRight', 'valueRight', 'dataType', 'errorText', 'actions'];
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

  // Covers table
  coversDisplayedColumns = ['code', 'name', 'isMandatory', 'waitingPeriod', 'coverageTerm', 'isDeductibleMandatory', 'actions'];
  coversSearchText = '';
  coversPageSize = 10;
  coversPageIndex = 0;
  filteredCovers: Cover[] = [];
  paginatedCovers: Cover[] = [];
  selectedPackageIndex = -1;

  // Deductibles table
  deductiblesDisplayedColumns = ['nr', 'deductibleType', 'deductible', 'deductibleUnit', 'deductibleSpecific', 'actions'];
  deductiblesSearchText = '';
  deductiblesPageSize = 10;
  deductiblesPageIndex = 0;
  filteredDeductibles: Deductible[] = [];
  paginatedDeductibles: Deductible[] = [];
  selectedCoverIndex = -1;

  // Limits table
  limitsDisplayedColumns = ['nr', 'sumInsured', 'premium', 'actions'];
  limitsSearchText = '';
  limitsPageSize = 10;
  limitsPageIndex = 0;
  filteredLimits: Limit[] = [];
  paginatedLimits: Limit[] = [];
  limitsHasChanges = false;

  // Policy Variables table
  policyVarsDisplayedColumns = ['category', 'field', 'name', 'code', 'actions'];
  policyFilter = 'Policy Holder';
  policyVarsPageSize = 10;
  policyVarsPageIndex = 0;
  filteredPolicyVars: PolicyVar[] = [];
  paginatedPolicyVars: PolicyVar[] = [];
  policyVars: PolicyVar[] = [];

  // Files table
  filesDisplayedColumns = ['fileCode', 'fileName', 'actions'];
  businessLineFiles: PackageFile[] = [];
  
  // Files service
  filesService: FilesService = inject(FilesService);
  
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
    private snackBar: MatSnackBar
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
        this.updateTables();
        this.loadDropdownOptions();
        this.reloadPolicyVars();
      })
    ).subscribe();
  }

  loadLob(lob: string): Observable<BusinessLineEdit> {
    console.log('load lob  ' + this.product.lob);
    
    return this.businessLineEditService.getBusinessLineByCode(lob).pipe(
      
      tap(result => {
        this.lob = result;
        console.log('load lob 3' + this.lob?.mpName);
      })
    );
  }

  loadDropdownOptions(): void {
    //this.productService.getLobOptions().subscribe(options => this.lobOptions = options);
    this.businessLineService.getLobCodes().subscribe(options => this.lobOptions = options);
    //this.productService.getKeyLeftOptions().subscribe(options => this.keyLeftOptions = options);
    this.businessLineEditService.getLobVars(this.product.lob).subscribe(options => this.keyLeftOptions = options);

    this.productService.getRuleTypeOptions().subscribe(options => this.ruleTypeOptions = options);
    //this.productService.getCoverCodeOptions().subscribe(options => this.coverCodeOptions = options);
    this.businessLineEditService.getLobCovers(this.product.lob).subscribe(options => this.coverCodeOptions = options);

    this.productService.getDeductibleTypeOptions().subscribe(options => this.deductibleTypeOptions = options);
    this.productService.getDeductibleUnitOptions().subscribe(options => this.deductibleUnitOptions = options);
    this.productService.getDeductibleSpecificOptions().subscribe(options => this.deductibleSpecificOptions = options);
    this.productService.getValidatorTypeOptions().subscribe(options => this.validatorTypeOptions = options);
    this.productService.getResetPolicyOptions().subscribe(options => this.resetPolicyOptions = options);
  }

  loadProduct(id?: number, versionNo?: number): Observable<Product | null> {
    if (id) {
      return this.productService.getProduct(id, versionNo || 0).pipe(
        tap((product) => {
          this.product = product;
          // Ensure files arrays are initialized for all packages
          if (this.product.packages) {
            this.product.packages.forEach(pkg => {
              if (!pkg.files) {
                pkg.files = [];
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

  createNewVersion(): void {
    // Mock implementation
    this.snackBar.open('Создание новой версии...', 'Закрыть', { duration: 2000 });
  }

  goToProduction(): void {
    // Mock implementation
    this.snackBar.open('Переход в продакшн...', 'Закрыть', { duration: 2000 });
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
        keyLeftOptions: this.keyLeftOptions,
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
        keyLeftOptions: this.keyLeftOptions,
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
        keyLeftOptions: this.keyLeftOptions,
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

  editSaveValidator(validator: QuoteValidator, index: number): void {
    this.loadDropdownOptions()
    const dialogRef = this.dialog.open(ValidatorDialogComponent, {
      width: '600px',
      minWidth: '900px',
      data: {
        validator: validator,
        isNew: false,
        keyLeftOptions: this.keyLeftOptions,
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
        // Ensure files array is initialized
        if (!result.files) {
          result.files = [];
        }
        this.product.packages.push(result);
        this.updatePackagesTable();
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
        this.product.packages[index] = result;
        this.updatePackagesTable();
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
        this.selectedCoverIndex = -1;
      } else if (this.selectedPackageIndex > index) {
        // Adjust index if a package before the selected one was deleted
        this.selectedPackageIndex--;
      }
      
      this.updatePackagesTable();
      this.updateCoversTable();
      this.updateDeductiblesTable();
      this.updateLimitsTable();
      this.updateFilesTable();
      this.updateChanges();
    }
  }
  updateFilesTable() {
    this.paginatedFiles = this.product.packages[this.selectedPackageIndex].files;
  }

  showCovers(pkg: Package, index: number): void {
    this.selectedPackageIndex = index;
    this.updateCoversTable();
    this.updateFilesTable();
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
        this.product.packages[this.selectedPackageIndex].covers.push(result);
        this.updateCoversTable();
        this.updateChanges();
      }
    });
  }

  editCover(cover: Cover, index: number): void {
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
        this.product.packages[this.selectedPackageIndex].covers[index] = result;
        this.updateCoversTable();
        this.addCoverVars(result.code);
        this.updateChanges();
      }
    });
  }

  addCoverVars(code: string): void {
    // add var to mpVars if not exists - co_+code+_premium, co_+code+_sumInsured, co_+code+_deductibleNr
      const newVars: any[] = [];
      newVars.push({ varCode: 'co_' + code + '_premium', varType: 'VAR', 
        varPath: '$..covers[?(@.cover.code == "' + code + '")].premium', 
        varName: 'Премия по покрытию ' + code, varDataType: 'NUMBER' });
      newVars.push({ varCode: 'co_' + code + '_sumInsured', varType: 'VAR', 
        varPath: '$..covers[?(@.cover.code == "' + code + '")].sumInsured', 
        varName: 'Сумма страхования по покрытию ' + code, varDataType: 'NUMBER' });
      newVars.push({ varCode: 'co_' + code + '_deductibleNr', varType: 'VAR', 
        varPath: '', 
        varName: 'Id франшизы по покрытию ' + code, varDataType: 'NUMBER' });
        newVars.push({ varCode: 'co_' + code + '_deductible', varType: 'VAR', 
          varPath: '$..covers[?(@.cover.code == "' + code + '")].deductible', 
          varName: 'Франшиза по покрытию ' + code, varDataType: 'NUMBER' });

        this.product.vars = [...this.product.vars, ...newVars];
      }
    

  deleteCover(cover: Cover, index: number): void {
    if (confirm('Удалить покрытие?')) {
      this.product.packages[this.selectedPackageIndex].covers.splice(index, 1);
      this.updateCoversTable();
      this.deleteCoverVars(cover.code);
      this.updateChanges();
    }
  }

  deleteCoverVars(code: string): void {
    this.product.vars = this.product.vars.filter(v => v.varCode !== 'co_' + code);
  }

  showDeductibles(cover: Cover, index: number): void {
    this.selectedCoverIndex = index;
    this.updateDeductiblesTable();
  }

  showLimits(cover: Cover, index: number): void {
    this.selectedCoverIndex = index;
    this.updateLimitsTable();
  }

  updateCoversTable(): void {
    if (this.selectedPackageIndex === -1 || 
        this.selectedPackageIndex >= this.product.packages.length ||
        !this.product.packages[this.selectedPackageIndex]) {
      this.filteredCovers = [];
      this.paginatedCovers = [];
      this.selectedPackageIndex = -1;
      this.selectedCoverIndex = -1;
      return;
    }

    // Only show covers for the currently selected package
    const selectedPackage = this.product.packages[this.selectedPackageIndex];
    if (!selectedPackage || !selectedPackage.covers) {
      this.filteredCovers = [];
      this.paginatedCovers = [];
      this.paginatedFiles = [];
      return;
    }

    this.filteredCovers = selectedPackage.covers.filter(item =>
      (item.code && item.code.toLowerCase().includes(this.coversSearchText.toLowerCase()))
    );
    this.updateCoversPagination();

    this.paginatedFiles = selectedPackage.files;
    
  }

  onCoversPageChange(event: PageEvent): void {
    this.coversPageSize = event.pageSize;
    this.coversPageIndex = event.pageIndex;
    this.updateCoversPagination();
  }

  updateCoversPagination(): void {
    this.paginatedCovers = this.filteredCovers
  }

  // Deductible methods
  addDeductible(): void {
    if (this.selectedPackageIndex === -1 || this.selectedCoverIndex === -1) return;

    const dialogRef = this.dialog.open(DeductibleDialogComponent, {
      width: '600px',
      data: {
        isNew: true,
        deductibleTypeOptions: this.deductibleTypeOptions,
        deductibleUnitOptions: this.deductibleUnitOptions,
        deductibleSpecificOptions: this.deductibleSpecificOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex].deductibles.push(result);
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
        isNew: false,
        deductibleTypeOptions: this.deductibleTypeOptions,
        deductibleUnitOptions: this.deductibleUnitOptions,
        deductibleSpecificOptions: this.deductibleSpecificOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex].deductibles[index] = result;
        this.updateDeductiblesTable();
        this.updateChanges();
      }
    });
  }

  deleteDeductible(deductible: Deductible, index: number): void {
    if (confirm('Удалить франшизу?')) {
      this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex].deductibles.splice(index, 1);
      this.updateDeductiblesTable();
      this.updateChanges();
    }
  }

  updateDeductiblesTable(): void {
    if (this.selectedPackageIndex === -1 || this.selectedCoverIndex === -1) {
      this.filteredDeductibles = [];
      this.paginatedDeductibles = [];
      return;
    }

    this.filteredDeductibles = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex].deductibles.filter(item =>
      (item.deductibleType && item.deductibleType.toLowerCase().includes(this.deductiblesSearchText.toLowerCase()))
    );
    this.updateDeductiblesPagination();
  }

  onDeductiblesPageChange(event: PageEvent): void {
    this.deductiblesPageSize = event.pageSize;
    this.deductiblesPageIndex = event.pageIndex;
    this.updateDeductiblesPagination();
  }

  updateDeductiblesPagination(): void {
    const startIndex = this.deductiblesPageIndex * this.deductiblesPageSize;
    this.paginatedDeductibles = this.filteredDeductibles.slice(startIndex, startIndex + this.deductiblesPageSize);
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
        this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex].limits.push(result);
        this.updateLimitsTable();
        this.limitsHasChanges = true;
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
        this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex].limits[index] = result;
        this.updateLimitsTable();
        this.limitsHasChanges = true;
        this.updateChanges();
      }
    });
  }

  deleteLimit(limit: Limit, index: number): void {
    if (confirm('Удалить лимит?')) {
      this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex].limits.splice(index, 1);
      this.updateLimitsTable();
      this.limitsHasChanges = true;
      this.updateChanges();
    }
  }

  updateLimitsTable(): void {
    if (this.selectedPackageIndex === -1 || this.selectedCoverIndex === -1) {
      this.filteredLimits = [];
      this.paginatedLimits = [];
      return;
    }

    this.filteredLimits = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex].limits.filter(item =>
      (item.sumInsured && item.sumInsured.toString().toLowerCase().includes(this.limitsSearchText.toLowerCase())) ||
      (item.premium && item.premium.toString().toLowerCase().includes(this.limitsSearchText.toLowerCase()))
    );
    this.updateLimitsPagination();
  }

  onLimitsPageChange(event: PageEvent): void {
    this.limitsPageSize = event.pageSize;
    this.limitsPageIndex = event.pageIndex;
    this.updateLimitsPagination();
  }

  updateLimitsPagination(): void {
    const startIndex = this.limitsPageIndex * this.limitsPageSize;
    this.paginatedLimits = this.filteredLimits.slice(startIndex, startIndex + this.limitsPageSize);
  }


  saveLimits(): void {
    // Save logic here - for now just mark as saved
    this.limitsHasChanges = false;
    this.snackBar.open('Лимиты сохранены', 'Закрыть', { duration: 2000 });
  }

  updateTables(): void {
    this.updateQuoteValidatorTable();
    this.updateSaveValidatorTable();
    this.updatePackagesTable();
    
    // Validate selectedPackageIndex before updating covers
    if (this.selectedPackageIndex >= 0 && 
        this.selectedPackageIndex < this.product.packages.length) {
      this.updateCoversTable();
      this.updateFilesTable();
    } else {
      this.selectedPackageIndex = -1;
      this.selectedCoverIndex = -1;
      this.filteredCovers = [];
      this.paginatedCovers = [];
      this.updateFilesTable();
    }
    
    this.updateDeductiblesTable();
    this.updateLimitsTable();
    this.updatePolicyTable();
  }

  // Policy Variables methods
  reloadPolicyVars(): void {
    // Mock data for policy variables - in real implementation, this would come from policyVersion.vars
    //this.updatePolicyTable();
    this.businessLineEditService.getBusinessLineByCode(this.product.lob).subscribe(result => {
      if (result) {
        let policyVars: BusinessLineVar[] = result.mpVars;

        // add vars to this.product.vars if it is not exists
        policyVars.forEach(v => {
          if (!this.product.vars.some(v2 => v2.varCode === v.varCode)) {
            this.product.vars.push({
              varPath: v.varPath,
              varName: v.varName,
              varCode: v.varCode,
              varDataType: v.varDataType,
              varValue: v.varValue,
              varType: v.varType,
              varCdm: "",
              varNr: v.varNr
            });
          }
        });
        this.updatePolicyTable();
        // Also reload files when LOB changes
        
      }
    });
  }

  updatePolicyTable(): void {
    this.policyVars = this.product.vars.map(v => ({
      varPath: v.varPath,
      varName: v.varName,
      varCode: v.varCode,
      category: '',
      field: '',
      name: v.varName,
      code: v.varCode,
      varNr: v.varNr,
      varCdm: v.varCdm
    }));

    let filtered = this.policyVars;

    // Apply filter based on selected option
    if (this.policyFilter === 'Policy Holder') {
      filtered = this.policyVars.filter(v => v.varPath.startsWith('policyHolder.'));
    } else if (this.policyFilter === 'Insured Object') {
      filtered = this.policyVars.filter(v => v.varPath.startsWith('insuredObject.'));
    } else if (this.policyFilter === 'Others') {
      filtered = this.policyVars.filter(v => !v.varPath.startsWith('policyHolder.') && !v.varPath.startsWith('insuredObject.'));
    }

    // Sort by varNr
    filtered = filtered.sort((a, b) => (a.varNr ?? 0) - (b.varNr ?? 0));

    // Process category and field columns based on varPath
    filtered = filtered.map((v, index) => {
      let category = '';
      let field = '';

      if (v.varPath.startsWith('policyHolder.')) {
        const afterPolicyHolder = v.varPath.substring('policyHolder.'.length);
        if (afterPolicyHolder.includes('.')) {
          const parts = afterPolicyHolder.split('.');
          category = parts[0];
          field = parts.slice(1).join('.');
        } else {
          category = 'policyHolder';
          field = afterPolicyHolder;
        }
      } else if (v.varPath.startsWith('insuredObject.')) {
        const afterInsuredObject = v.varPath.substring('insuredObject.'.length);
        if (afterInsuredObject.includes('.')) {
          const parts = afterInsuredObject.split('.');
          category = parts[0];
          field = parts.slice(1).join('.');
        } else {
          category = 'insuredObject';
          field = afterInsuredObject;
        }
      } else {
        category = 'others';
        field = v.varPath;
      }

      // Hide category value if it equals the previous record's category
      if (index > 0 && filtered[index - 1]) {
        const prevVar = filtered[index - 1];
        let prevCategory = '';
        if (prevVar.varPath.startsWith('policyHolder.')) {
          const afterPolicyHolder = prevVar.varPath.substring('policyHolder.'.length);
          if (afterPolicyHolder.includes('.')) {
            prevCategory = afterPolicyHolder.split('.')[0];
          } else {
            prevCategory = 'policyHolder';
          }
        } else if (prevVar.varPath.startsWith('insuredObject.')) {
          const afterInsuredObject = prevVar.varPath.substring('insuredObject.'.length);
          if (afterInsuredObject.includes('.')) {
            prevCategory = afterInsuredObject.split('.')[0];
          } else {
            prevCategory = 'insuredObject';
          }
        } else {
          prevCategory = 'others';
        }

        if (category === prevCategory) {
          category = ''; // Hide duplicate category
        }
      }

      return {
        ...v,
        category,
        field,
        name: v.varName,
        code: v.varCode
      };
    });

    this.filteredPolicyVars = filtered;
    this.updatePolicyVarsPagination();
  }

  onPolicyVarsPageChange(event: PageEvent): void {
    this.policyVarsPageSize = event.pageSize;
    this.policyVarsPageIndex = event.pageIndex;
    this.updatePolicyVarsPagination();
  }

  updatePolicyVarsPagination(): void {
    const startIndex = this.policyVarsPageIndex * this.policyVarsPageSize;
    this.paginatedPolicyVars = this.filteredPolicyVars.slice(startIndex, startIndex + this.policyVarsPageSize);
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

  getPackageIndex(pkg: Package): number {
    // Try to find by reference first (most reliable)
    const refIndex = this.product.packages.indexOf(pkg);
    if (refIndex !== -1) return refIndex;
    
    // Fallback to matching by code
    const index = this.product.packages.findIndex(p => p.code === pkg.code);
    return index !== -1 ? index : 0;
  }

  getCoverIndex(cover: Cover): number {
    if (this.selectedPackageIndex === -1) return 0;
    
    // Try to find by reference first (most reliable)
    const covers = this.product.packages[this.selectedPackageIndex].covers;
    const refIndex = covers.indexOf(cover);
    if (refIndex !== -1) return refIndex;
    
    // Fallback to matching by code
    const index = covers.findIndex(c => c.code === cover.code);
    return index !== -1 ? index : 0;
  }

  getDeductibleIndex(deductible: Deductible): number {
    if (this.selectedPackageIndex === -1 || this.selectedCoverIndex === -1) return 0;
    
    // Try to find by reference first (most reliable)
    const deductibles = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex].deductibles;
    const refIndex = deductibles.indexOf(deductible);
    if (refIndex !== -1) return refIndex;
    
    // Fallback to matching by properties
    const index = deductibles.findIndex(d => 
      d.nr === deductible.nr && 
      d.deductibleType === deductible.deductibleType &&
      d.deductible === deductible.deductible
    );
    return index !== -1 ? index : 0;
  }

  getLimitIndex(limit: Limit): number {
    if (this.selectedPackageIndex === -1 || this.selectedCoverIndex === -1) return 0;
    
    // Try to find by reference first (most reliable)
    const limits = this.product.packages[this.selectedPackageIndex].covers[this.selectedCoverIndex].limits;
    const refIndex = limits.indexOf(limit);
    if (refIndex !== -1) return refIndex;
    
    // Fallback to matching by properties
    const index = limits.findIndex(l => 
      l.nr === limit.nr && 
      l.sumInsured === limit.sumInsured &&
      l.premium === limit.premium
    );
    return index !== -1 ? index : 0;
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
