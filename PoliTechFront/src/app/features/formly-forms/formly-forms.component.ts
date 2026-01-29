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
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatOption } from "@angular/material/core";
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import type { Field } from './formly-forms.service';
import { FormlyFormsService} from './formly-forms.service';
import { Product, ProductService } from '../../shared/services/product.service';
import { BoxPolicy, BoxPolicyHolder, InsuredObject, Identifier, Address, Organization, Device, BoxIdentifier, BoxAddress, BoxOrganization, BoxDevice, Policy } from '../../shared/models/policy.models';
import { BoxTravelSegment } from '../../shared/models/box/travel-segment-box.model';
import { PolicyService } from '../../shared/services/policy.service';



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
    MatDividerModule,
    MatTableModule,
  ]
})
export class FormlyFormsComponent implements OnInit {


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


  policy: BoxPolicy = new BoxPolicy();
  
  coverageDataSource = new MatTableDataSource<any>([]);
  coverageDisplayedColumns: string[] = ['code', 'risk', 'startDate', 'endDate', 'sumInsured', 'premium', 'deductibleType', 'deductibleText'];
  travelSegmentsDataSource = new MatTableDataSource<BoxTravelSegment>([]);
  travelSegmentsDisplayedColumns: string[] = [
    'ticketNr',
    'ticketPrice',
    'departureDate',
    'departureTime',
    'departureCity',
    'arrivalCity',
    'actions'
  ];

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
    this.productService.getTestRequestSave(parseInt(productId || '0'), parseInt(versionNo || '1')).subscribe({
      next: (json: string) => {
        // Convert to string primitive if needed (handle both string and String wrapper)
        const jsonString: string = typeof json === 'string' ? json : String(json);
        
        // Print JSON to console
        //console.log('Received JSON:', jsonString);
        //console.log('JSON length:', jsonString.length);
        
        try {
          // Parse JSON
          const parsedData = JSON.parse(jsonString);
          console.log('Parsed JSON object:', parsedData);
          
          // Use BoxPolicy constructor to properly instantiate nested objects
          this.policy = new BoxPolicy(parsedData);
          console.log('Converted to BoxPolicy:', this.policy);
          this.updateCoverageTable();
          this.updateTravelSegmentsTable();
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

  updateTables(): void {
    this.updateCoverageTable();
    this.updateTravelSegmentsTable();
  }

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
        let boxPolicy = this.policyService.conversPolicy2Box(response);
        console.log('BoxPolicy:', boxPolicy);
        this.policy = boxPolicy;
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

  getShowIoDevice(): boolean {
    return this.product.vars.some(v => v.varCdm && v.varCdm.startsWith("insuredObject.device"));
  }

  getShowIoTravelSegments(): boolean {
    return this.product.vars.some(v => v.varCdm && (
      v.varCdm.startsWith("insuredObject.travelSegments") ||
      v.varCdm.startsWith("insuredObject.travelSegment")
    ));
  }

  // Backwards-compatible wrapper
  getShowIoTravelSegment(): boolean {
    return this.getShowIoTravelSegments();
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

  ensureTravelSegments(): BoxTravelSegment[] {
    if (!this.policy.insuredObject.travelSegments) {
      this.policy.insuredObject.travelSegments = [];
    }
    return this.policy.insuredObject.travelSegments;
  }

  updateCoverageTable(): void {
    if (this.policy.coverage && Array.isArray(this.policy.coverage)) {
      this.coverageDataSource.data = this.policy.coverage;
    } else {
      this.coverageDataSource.data = [];
    }
  }

  updateTravelSegmentsTable(): void {
    if (this.policy.insuredObject.travelSegments && Array.isArray(this.policy.insuredObject.travelSegments)) {
      this.travelSegmentsDataSource.data = [...this.policy.insuredObject.travelSegments];
    } else {
      this.travelSegmentsDataSource.data = [];
    }
  }

  addTravelSegment(): void {
    const dialogRef = this.dialog.open(TravelSegmentDialog, {
      width: '600px',
      data: {
        segment: new BoxTravelSegment(),
        isNew: true
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const segments = this.ensureTravelSegments();
        segments.push(new BoxTravelSegment(result));
        this.updateTravelSegmentsTable();
      }
    });
  }

  deleteTravelSegment(index: number): void {
    const segments = this.ensureTravelSegments();
    if (index >= 0 && index < segments.length) {
      segments.splice(index, 1);
      this.updateTravelSegmentsTable();
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
  selector: 'app-travel-segment-dialog',
  imports: [MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, FormsModule],
  template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Add Travel Segment' : 'Edit Travel Segment' }}</h2>
    <div mat-dialog-content class="travel-segment-dialog">
      <div class="row">
        <mat-form-field appearance="outline" class="field">
          <mat-label>Ticket Nr</mat-label>
          <input matInput [(ngModel)]="segment.ticketNr" name="ticketNr">
        </mat-form-field>
        <mat-form-field appearance="outline" class="field">
          <mat-label>Ticket Price</mat-label>
          <input matInput [(ngModel)]="segment.ticketPrice" name="ticketPrice">
        </mat-form-field>
      </div>
      <div class="row">
        <mat-form-field appearance="outline" class="field">
          <mat-label>Departure Date</mat-label>
          <input matInput [(ngModel)]="segment.departureDate" name="departureDate">
        </mat-form-field>
        <mat-form-field appearance="outline" class="field">
          <mat-label>Departure Time</mat-label>
          <input matInput [(ngModel)]="segment.departureTime" name="departureTime">
        </mat-form-field>
      </div>
      <div class="row">
        <mat-form-field appearance="outline" class="field">
          <mat-label>Departure City</mat-label>
          <input matInput [(ngModel)]="segment.departureCity" name="departureCity">
        </mat-form-field>
        <mat-form-field appearance="outline" class="field">
          <mat-label>Arrival City</mat-label>
          <input matInput [(ngModel)]="segment.arrivalCity" name="arrivalCity">
        </mat-form-field>
      </div>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="segment">Save</button>
    </div>
  `,
  styles: [`
    .travel-segment-dialog {
      display: flex;
      flex-direction: column;
      gap: 16px;
      min-width: 520px;
    }
    .row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    .field {
      width: 100%;
    }
  `]
})
export class TravelSegmentDialog {
  segment: BoxTravelSegment;

  constructor(
    public dialogRef: MatDialogRef<TravelSegmentDialog>,
    @Inject(MAT_DIALOG_DATA) public data: { segment: BoxTravelSegment; isNew: boolean }
  ) {
    this.segment = data.segment ? new BoxTravelSegment(data.segment) : new BoxTravelSegment();
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

