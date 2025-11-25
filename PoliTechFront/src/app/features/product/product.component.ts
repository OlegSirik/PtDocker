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

import { ProductService, Product, QuoteValidator, Package, Cover, Deductible, Limit } from '../../shared/services/product.service';

export interface PolicyVar {
  varPath: string;
  varName: string;
  varCode: string;
  category: string;
  field: string;
  name: string;
  code: string;
}
import { ValidatorDialogComponent } from './validator-dialog/validator-dialog.component';
import { PackageDialogComponent } from './package-dialog/package-dialog.component';
import { CoverDialogComponent } from './cover-dialog/cover-dialog.component';
import { DeductibleDialogComponent } from './deductible-dialog/deductible-dialog.component';
import { LimitDialogComponent } from './limit-dialog/limit-dialog.component';
import { tap } from 'rxjs';
import { BusinessLineService } from '../../shared/services/business-line.service';
import { BusinessLineEditService } from '../../shared/services/business-line-edit.service';

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

    if (id === 'new') {
      this.isNewRecord = true;
      this.loadProduct();
    } else if (id) {
      this.loadProduct(parseInt(id), versionNo ? parseInt(versionNo) : undefined);
    }
    this.loadDropdownOptions();
    this.reloadPolicyVars(); // Initialize policy variables
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

  loadProduct(id?: number, versionNo?: number): void {
    if (id) {
      this.productService.getProduct(id, versionNo || 0).subscribe({
        next: (product) => {
          this.product = product;
          this.updateTables();
        },
        error: (error) => {
          console.error('Error loading product:', error);
          this.snackBar.open('Ошибка загрузки продукта', 'Закрыть', { duration: 3000 });
        }
      });
    } else {
      this.updateTables();
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
      this.router.navigate(['/product', this.product.id, 'version', this.product.versionNo, 'form']);
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
      this.updatePackagesTable();
      this.updateChanges();
    }
  }

  showCovers(pkg: Package, index: number): void {
    this.selectedPackageIndex = index;
    this.updateCoversTable();
  }

  openCalculator(pkg: Package): void {
    if (this.product.id && this.product.versionNo) {
      this.router.navigate(['/products', this.product.id, 'versions', this.product.versionNo, 'packages', pkg.code, 'calculator']);
    } else {
      this.snackBar.open('Продукт должен быть сохранен перед открытием калькулятора', 'Закрыть', { duration: 3000 });
    }
  }

  updatePackagesTable(): void {
    this.filteredPackages = this.product.packages.filter(item =>
     // (item.code && item.code.toLowerCase().includes(this.packagesSearchText.toLowerCase())) ||
      (item.name && item.name.toLowerCase().includes(this.packagesSearchText.toLowerCase()))
    );
    this.updatePackagesPagination();
  }

  onPackagesPageChange(event: PageEvent): void {
    this.packagesPageSize = event.pageSize;
    this.packagesPageIndex = event.pageIndex;
    this.updatePackagesPagination();
  }

  updatePackagesPagination(): void {
    const startIndex = this.packagesPageIndex * this.packagesPageSize;
    this.paginatedPackages = this.filteredPackages.slice(startIndex, startIndex + this.packagesPageSize);
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
        this.updateChanges();
      }
    });
  }

  deleteCover(cover: Cover, index: number): void {
    if (confirm('Удалить покрытие?')) {
      this.product.packages[this.selectedPackageIndex].covers.splice(index, 1);
      this.updateCoversTable();
      this.updateChanges();
    }
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
    if (this.selectedPackageIndex === -1) {
      this.filteredCovers = [];
      this.paginatedCovers = [];
      return;
    }

    this.filteredCovers = this.product.packages[this.selectedPackageIndex].covers.filter(item =>
      (item.code && item.code.toLowerCase().includes(this.coversSearchText.toLowerCase()))
    );
    this.updateCoversPagination();
  }

  onCoversPageChange(event: PageEvent): void {
    this.coversPageSize = event.pageSize;
    this.coversPageIndex = event.pageIndex;
    this.updateCoversPagination();
  }

  updateCoversPagination(): void {
    const startIndex = this.coversPageIndex * this.coversPageSize;
    this.paginatedCovers = this.filteredCovers.slice(startIndex, startIndex + this.coversPageSize);
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
    this.updateCoversTable();
    this.updateDeductiblesTable();
    this.updateLimitsTable();
    this.updatePolicyTable();
  }

  // Policy Variables methods
  reloadPolicyVars(): void {
    // Mock data for policy variables - in real implementation, this would come from policyVersion.vars
    this.updatePolicyTable();
  }

  updatePolicyTable(): void {
    this.policyVars = this.product.vars.map(v => ({
      varPath: v.varPath,
      varName: v.varName,
      varCode: v.varCode,
      category: '',
      field: '',
      name: v.varName,
      code: v.varCode
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

    // Sort by varPath
    filtered = filtered.sort((a, b) => a.varPath.localeCompare(b.varPath));

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
