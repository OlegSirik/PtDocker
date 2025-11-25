import { AfterViewInit, Component, ElementRef, OnInit, Renderer2, inject } from '@angular/core';

import { FormGroup, ReactiveFormsModule, FormBuilder, Validators, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import type { Field } from './formly-forms.service';
import { FormlyFormsService, FormData } from './formly-forms.service';
import { Product, ProductService } from '../../shared/services/product.service';
import { Policy } from '../../shared/models/policy.models';
import { PolicyService } from '../../shared/services/policy.service';
import { MatOption } from "@angular/material/core";
import { MatDialog } from '@angular/material/dialog';
import { MatDialogModule } from '@angular/material/dialog';


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

  policy: Policy = {
    policyNumber: '32523453245',
    previousPolicyNumber: '',
    product: { code: '', description: '' },
    status: { code: 'NEW', description: '' },
    startDate: '',
    endDate: '',
    issueDate: '',
    createDate: '',
    paymentDate: '',
    cancellationDate: '',
    premium: 0,
    premiumCur: 0,
    currencyCode: '',
    currencyRate: 0,
    placeOfIssue: '',
    draftId: '',
    policyHolder: { person: {firstName: ''}, phone: {phoneNumber: '', contactPerson: ''}, email: '', passport: {typeCode: '', serial: '', number: '', dateIssue: '', validUntil: '', whom: ''
      , divisionCode: '', vsk_id: '', ext_id: '', countryCode: ''},
      address: {typeCode: 'REGISTRATION', countryCode: '', region: '', city: '', street: '', house: '', building: '', flat: '', room: '', zipCode: '', kladrId: '',
        fiasId: '', addressStr: '', addressStrEn: '', vsk_id: '', ext_id: ''}, placeOfWork: {organization: '', occupationType: '', occupation: '',
          address: '', phone: ''}, inn: '', snils: '', otherAddresses: [], otherDocuments: [], organization: {country: '', inn: '', fullName: '',
            fullNameEn: '', shortName: '', legalForm: 'OOO', kpp: '', ogrn: '', okpo: '', bic: '', isResident: false, group: '', vsk_id: '', ext_id: '', nciCode: ''},
            isGovernmentService: false, customFields: {} },
    insuredObject: {
      packageCode: '',
      covers: [],
      objectId: '',
      insureds: [],
      device: {countryCode: 'DE', devicePrice: 0, imei: '23453245325235', licenseKey: 'XXX', model: 'Nova 5', deviceName: '', osName: '', osVersion: '', serialNr: '2412341241241243', tradeMark: 'Xiaomi', deviceTypeCode: 'SMARTPHONE'}
    }
  };

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
    this.policy = this.policyService.getMockPolicy();
  }


  ngOnInit(): void {


    const productId = this.route.snapshot.paramMap.get('product-id');
    const versionNo = this.route.snapshot.paramMap.get('version-no');

    this.loadProduct(parseInt(productId || '0'), parseInt(versionNo || '0'));

    this.policy = this.policyService.getMockPolicy();

  }

  get addFieldKeys(): string[] {
    return Object.keys(this.policy.policyHolder.customFields || {});
  }

  loadProduct(id?: number, versionNo?: number): void {
    if (id) {
      this.productService.getProduct(id, versionNo || 0).subscribe({
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

      };


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

    /*
"saveValidator": [
        {
            "lineNr": 12,
            "keyLeft": "io_device_typeCode",
            "dataType": "STRING",
            "ruleType": "IN_LIST",
            "errorText": "sfsda",
            "valueRight": "DIGITAL,HOME"
    */
    return this.ph_addr_typeCode;
  }


}

