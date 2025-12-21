import { AfterViewInit, Component, ElementRef, OnInit, Renderer2, inject, Inject } from '@angular/core';
import { FormGroup, ReactiveFormsModule, FormBuilder, Validators, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { JsonPipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatOption } from "@angular/material/core";
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import type { Field } from './formly-forms.service';
import { FormlyFormsService, FormData } from './formly-forms.service';
import { Product, ProductService } from '../../shared/services/product.service';
import { BoxPolicy, BoxPolicyHolder, InsuredObject, Identifier, Address, Organization, Device, BoxIdentifier, BoxAddress, BoxOrganization, BoxDevice, Policy } from '../../shared/models/policy.models';
import { PolicyService } from '../../shared/services/policy.service';


interface Food {
  value: string;
  viewValue: string;
}

interface LoV {
  value: string;
  viewValue: string;
}

@Component({
  selector: 'app-formly-forms',
  templateUrl: './formly-forms.component.html',
  styleUrls: ['./formly-forms.component.scss'],
  imports: [
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSelectModule,
    MatTableModule,
  ]
})
export class FormlyFormsComponent implements OnInit {

  foods: Food[] = [
    {value: 'steak-0', viewValue: 'Steak'},
    {value: 'pizza-1', viewValue: 'Pizza'},
    {value: 'tacos-2', viewValue: 'Tacos'},
  ];

  // todo получать полный список с сервера через АПИ
  io_device_typeCode : LoV[] = [
    {value: 'SMARTPHONE', viewValue: 'Smartphone'},
    {value: 'TABLET', viewValue: 'Tablet'},
    {value: 'LAPTOP', viewValue: 'Laptop'},
    {value: 'DESKTOP', viewValue: 'Desktop'},
    {value: 'OTHER', viewValue: 'Other'}
  ];

  ph_addr_typeCode : LoV[] = [
    {value: 'REGISTRATION', viewValue: 'Registration'},
    {value: 'RESIDENCE', viewValue: 'Residence'},
    {value: 'OTHER', viewValue: 'Other'}
  ];

  getLov(): LoV[] {
    return this.ph_addr_typeCode;
    const ret: LoV[] = [
      {value: 'SMARTPHONE', viewValue: 'Smart- phone'},
      {value: 'TABLET', viewValue: 'Tabletka'}];
    return ret;
  }

  getSelectValues(): string[] {
      return ['Option 1', 'Option 2', 'Option 3'];
  }


onCancel() {
throw new Error('Method not implemented.');
}

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

  formData1: FormData = {
    item1: '',
    item2: 'item2',
    item3: 'item3',
    item4: '',
    item5: ''
  };

  policy: BoxPolicy = new BoxPolicy();
  
  coverageDataSource = new MatTableDataSource<any>([]);
  coverageDisplayedColumns: string[] = ['code', 'risk', 'startDate', 'endDate', 'sumInsured', 'premium', 'deductibleType', 'deductibleText'];

  constructor(
    private fb: FormBuilder,
    private renderer: Renderer2,
    private elementRef: ElementRef,
    private formlyFormsService: FormlyFormsService,
    private route: ActivatedRoute,
    private productService: ProductService,
    private snackBar: MatSnackBar,
    private policyService: PolicyService,
    private dialog: MatDialog,
  ) {
    //this.policy = this.policyService.getMockPolicy();
  }


  ngOnInit(): void {


    const productId = this.route.snapshot.paramMap.get('product-id');
    const versionNo = this.route.snapshot.paramMap.get('version-no');

    this.loadProduct(parseInt(productId || '0'), parseInt(versionNo || '1'));

    //this.policy = this.policyService. .getMockPolicy();
    this.productService.getTestRequestQuote(parseInt(productId || '0'), parseInt(versionNo || '1')).subscribe({
      next: (json: string) => {
        // Convert to string primitive if needed (handle both string and String wrapper)
        const jsonString: string = typeof json === 'string' ? json : String(json);
        
        // Print JSON to console
        console.log('Received JSON:', jsonString);
        console.log('JSON length:', jsonString.length);
        
        try {
          // Parse JSON
          const parsedData = JSON.parse(jsonString);
          console.log('Parsed JSON object:', parsedData);
          
          // Use BoxPolicy constructor to properly instantiate nested objects
          this.policy = new BoxPolicy(parsedData);
          console.log('Converted to BoxPolicy:', this.policy);
        } catch (parseError) {
          console.error('Error parsing JSON:', parseError);
          console.error('Invalid JSON string:', jsonString);
        }
      },
      error: (error) => {
        console.error('Error loading test request:', error);
      }
    });
  }

  get addFieldKeys(): string[] {
    return Object.keys(this.policy.policyHolder.customFields || {});
  }

  loadProduct(id?: number, versionNo?: number): void {
    if (id) {
      this.productService.getProduct(id, versionNo || 1).subscribe({
        next: (product) => {
          this.product = product;
          this.getAdressTypes();
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

  updateTables(): void {}

  /*
  private applyStylesToField(wrapper: HTMLElement, config: any): void {
    const matFormField = wrapper.querySelector('mat-form-field');
    const input = wrapper.querySelector('input, textarea');
    const label = wrapper.querySelector('mat-label');

    // Применяем стили к input/textarea
    if (input) {
      Object.keys(config.style).forEach(styleKey => {
        this.renderer.setStyle(input, styleKey, config.style[styleKey]);
      });
    }

    // Меняем текст лейбла
    if (label && config.label) {
      this.renderer.setProperty(label, 'textContent', config.label);
    }
  }
  */
  onSubmit() {
    console.log("onSubmit-----------------------------------------------------------------------------");
    console.log(this.policy);
    
    const plc = this.policyService.conversBox2Policy(this.policy);

    console.log(plc);

    this.dialog.open(JsonViewDialog, {
      width: '800px',
      maxHeight: '80vh',
      data: {
        title: 'Policy JSON',
        object: plc
      }
    });
  }

  onFastCalc() {
    console.log("onFastCalc-----------------------------------------------------------------------------");
    const policy = this.policyService.conversBox2Policy(this.policy);
    console.log('Policy to send:', policy);

    this.policyService.fastCalc(policy).subscribe({
      next: (response: Policy) => {
        console.log('Fast Calc response:', response);
        // Convert Policy back to BoxPolicy
        this.policy = this.policyService.conversPolicy2Box(response);
        this.updateCoverageTable();
        this.snackBar.open('Расчет выполнен успешно', 'Закрыть', { duration: 3000 });
      },
      error: (error) => {
        console.error('Fast Calc error:', error);
        this.snackBar.open('Ошибка при расчете: ' + (error.error?.message || error.message), 'Закрыть', { duration: 5000 });
      }
    });
  }

  onSave() {
    console.log("onSave-----------------------------------------------------------------------------");
    const policy = this.policyService.conversBox2Policy(this.policy);
    console.log('Policy to save:', policy);

    this.policyService.savePolicy(policy).subscribe({
      next: (response: Policy) => {
        console.log('Save response:', response);
        // Convert Policy back to BoxPolicy
        this.policy = this.policyService.conversPolicy2Box(response);
        this.updateCoverageTable();
        this.snackBar.open('Политика сохранена успешно', 'Закрыть', { duration: 3000 });
      },
      error: (error) => {
        console.error('Save error:', error);
        this.snackBar.open('Ошибка при сохранении: ' + (error.error?.message || error.message), 'Закрыть', { duration: 5000 });
      }
    });
  }


  getFieldVisible(fieldName: string): boolean {
    const found = this.product.vars.some(v => v.varCode === fieldName);
    //console.log(fieldName + "=" + found.toString());
    return found;
  }

  getFieldLabel(fieldName: string): string {
    return this.product.vars.find(v => v.varCode === fieldName)?.varName || fieldName;
  }

  getDeviceTypeOptions(): {value: string, label: string}[] {
    return [
      {value: 'SMARTPHONE', label: 'Smartphone'},
      {value: 'TABLET', label: 'Tablet'},
      {value: 'LAPTOP', label: 'Laptop'},
      {value: 'DESKTOP', label: 'Desktop'},
      {value: 'OTHER', label: 'Other'}
    ];
  }

  getShowOrganization(): boolean {
    return this.product.vars.some(v => v.varPath && v.varPath.startsWith("policyHolder.organization"));
  }

  getShowPHCustom(): boolean {
    return this.product.vars.some(v => v.varPath && v.varPath.startsWith("policyHolder.customFields"));
  }

  getAdressTypes(): LoV[] {
    const validator = this.product?.saveValidator?.find(
      (v: any) => v.keyLeft === "ph_addr_typeCode" && v.ruleType === "IN_LIST"
    );
    let values: string[] = [];
    if (validator && typeof validator.valueRight === 'string') {
      values = validator.valueRight.split(',').map((s: string) => s.trim());
      this.ph_addr_typeCode = values.map(value => ({value: value, viewValue: value}));
    }

    return this.ph_addr_typeCode;
  }

  // Helper methods to ensure identifiers and addresses exist
  ensureIdentifier(): BoxIdentifier {
    if (!this.policy.policyHolder.identifiers) {
      this.policy.policyHolder.identifiers = new BoxIdentifier();
    }
    return this.policy.policyHolder.identifiers;
  }

  ensureAddress(): BoxAddress {
    if (!this.policy.policyHolder.addresses) {
      this.policy.policyHolder.addresses = new BoxAddress();
    }
    return this.policy.policyHolder.addresses;
  }

  ensureOrganization(): BoxOrganization {
    if (!this.policy.policyHolder.organization) {
      this.policy.policyHolder.organization = new BoxOrganization();
    }
    return this.policy.policyHolder.organization;
  }

  ensureDevice(): BoxDevice {
    if (!this.policy.insuredObject.device) {
      this.policy.insuredObject.device = new BoxDevice();
    }
    return this.policy.insuredObject.device;
  }

  updateCoverageTable(): void {
    if (this.policy.coverage && Array.isArray(this.policy.coverage)) {
      this.coverageDataSource.data = this.policy.coverage;
    } else {
      this.coverageDataSource.data = [];
    }
  }

  getRiskAsString(risk: string[] | undefined): string {
    if (!risk || !Array.isArray(risk)) {
      return '';
    }
    return risk.join(', ');
  }

}

@Component({
  selector: 'app-json-view-dialog',
  imports: [MatDialogModule, MatButtonModule, MatIconModule, JsonPipe],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <div mat-dialog-content style="max-height: 60vh; overflow: auto;">
      <pre style="background-color: #f5f5f5; padding: 16px; border-radius: 4px; overflow-x: auto;">{{ data.object | json }}</pre>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button (click)="copyToClipboard()">
        <mat-icon>content_copy</mat-icon>
        Copy
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
export class JsonViewDialog {
  constructor(
    public dialogRef: MatDialogRef<JsonViewDialog>,
    @Inject(MAT_DIALOG_DATA) public data: { title: string; object: any }
  ) {}

  copyToClipboard(): void {
    const jsonString = JSON.stringify(this.data.object, null, 2);
    navigator.clipboard.writeText(jsonString).then(() => {
      // Could show a snackbar notification here if needed
    });
  }
}

