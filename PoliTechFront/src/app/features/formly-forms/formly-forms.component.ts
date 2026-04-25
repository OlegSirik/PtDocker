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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, provideNativeDateAdapter } from '@angular/material/core';
import type { Field } from './formly-forms.service';
import { FormlyFormsService} from './formly-forms.service';
import { Product, ProductService, UiProductData } from '../../shared/services/product.service';
import { BoxPolicy, BoxPolicyHolder, InsuredObject, Identifier, Address, Organization, Device, BoxIdentifier, BoxAddress, BoxOrganization, BoxDevice, Policy } from '../../shared/models/policy.models';
import { BoxTravelSegment } from '../../shared/models/box/travel-segment-box.model';
import { PolicyService } from '../../shared/services/policy.service';



interface LoV {
  value: string;
  viewValue: string;
}

/** ЧЧ:ММ (24 ч) для {@link HTMLInputElement.type} {@code time} (шаг по минутам). */
function normalizeDepartureTimeToHHmm(raw: string | undefined): string {
  if (!raw?.trim()) {
    return '';
  }
  const s = raw.trim();
  const m24 = /^(\d{1,2}):(\d{2})(?::\d{1,2})?(?:\.\d+)?$/.exec(s);
  if (m24) {
    const h = Number(m24[1]);
    const min = Number(m24[2]);
    if (Number.isFinite(h) && Number.isFinite(min) && h >= 0 && h <= 23 && min >= 0 && min <= 59) {
      return `${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}`;
    }
  }
  const m12 = /^(\d{1,2}):(\d{2})\s*(AM|PM)$/i.exec(s);
  if (m12) {
    let h = Number(m12[1]);
    const min = Number(m12[2]);
    const ap = m12[3].toUpperCase();
    if (!Number.isFinite(h) || !Number.isFinite(min) || min < 0 || min > 59) {
      return '';
    }
    if (ap === 'PM' && h !== 12) {
      h += 12;
    }
    if (ap === 'AM' && h === 12) {
      h = 0;
    }
    if (h < 0 || h > 23) {
      return '';
    }
    return `${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}`;
  }
  return '';
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
    MatDatepickerModule,
    MatNativeDateModule,
  ]
})
export class FormlyFormsComponent implements OnInit {

  lists: { [key: string]: { [key: string]: string } } = {};

  /**
   * Значение для mat-datepicker (Date); в {@link BoxPerson.birthDate} хранится строка yyyy-MM-dd.
   */
  policyHolderBirthDateValue: Date | null = null;

  /** mat-datepicker для {@link BoxIdentifier.dateIssue} / validUntil (в модели — строка yyyy-MM-dd). */
  policyHolderDocDateIssueValue: Date | null = null;
  policyHolderDocValidUntilValue: Date | null = null;

  /** mat-datepicker для {@link BoxPolicy.startDate} / {@link BoxPolicy.endDate} (строка yyyy-MM-dd). */
  policyStartDateValue: Date | null = null;
  policyEndDateValue: Date | null = null;

  /** Начальная позиция календаря, если дата рождения ещё не задана. */
  readonly birthDatePickerFallbackStart = new Date(1990, 0, 1);

  /** Календарь периода договора без сохранённой даты — текущий месяц. */
  readonly policyPeriodDatePickerFallbackStart = new Date();

  /** Открытие календаря документа без сохранённой даты — текущий месяц. */
  readonly docDatePickerFallbackStart = new Date();

  /**
   * Пары для mat-select: ключ объекта lists — значение в полис, значение — подпись в UI.
   */
  listEntries(listKey: string): { value: string; label: string }[] {
    const m = this.lists[listKey];
    if (m && Object.keys(m).length > 0) {
      return Object.entries(m).map(([value, label]) => ({ value, label }));
    }
    if (listKey === 'waitingPeriod' || listKey === 'policyTerm') {
      const fromRule = this.periodRuleListEntries(this.product[listKey]);
      if (fromRule.length) {
        return fromRule;
      }
    }
    const fallbacks: Record<string, { value: string; label: string }[]> = {
      ph_gender: [
        { value: 'M', label: 'Мужской' },
        { value: 'F', label: 'Женский' },
      ],
      ph_isPublicOfficial: [
        { value: 'true', label: 'Да' },
        { value: 'false', label: 'Нет' },
      ],
    };
    return fallbacks[listKey] ?? [];
  }

  /** Варианты для периода, если в {@link lists} ещё нет (тип LIST в продукте). */
  private periodRuleListEntries(rule: { validatorType?: string; validatorValue?: string } | undefined): {
    value: string;
    label: string;
  }[] {
    if (!rule || String(rule.validatorType ?? '').toUpperCase() !== 'LIST' || !rule.validatorValue?.trim()) {
      return [];
    }
    return rule.validatorValue
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean)
      .map((value) => ({ value, label: value }));
  }

  /**
   * Если для периода в списке ровно один вариант, а в полисе значение пустое — подставить его
   * (селект «Период охлаждения» / «Период страхования» виден только без даты начала/окончания).
   */
  private applySingleOptionForPeriodLists(): void {
    if (!this.policy.startDate) {
      const wp = this.listEntries('waitingPeriod');
      if (wp.length === 1 && !(this.policy.waitingPeriod || '').trim()) {
        this.policy.waitingPeriod = wp[0].value;
      }
    }
    if (!this.policy.endDate) {
      const pt = this.listEntries('policyTerm');
      if (pt.length === 1 && !(this.policy.policyTerm || '').trim()) {
        this.policy.policyTerm = pt[0].value;
      }
    }
  }

  getLov(): LoV[] {
    return this.getAdressTypes();
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
    vars: [],
    rules: { insuredEqualsPolicyHolder: false }
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

  /** Премия как текст: жирный размер задаётся в .premium-value; формат «999 999.00». */
  formatPremium(value: unknown): string {
    if (value === null || value === undefined || value === '') {
      return '—';
    }
    const n =
      typeof value === 'number'
        ? value
        : Number(String(value).replace(/\s/g, '').replace(',', '.'));
    if (Number.isNaN(n)) {
      return String(value);
    }
    const negative = n < 0;
    const abs = Math.abs(n);
    const [intRaw, frac = '00'] = abs.toFixed(2).split('.');
    const intSpaced = intRaw.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
    return (negative ? '-' : '') + intSpaced + '.' + frac;
  }

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
    this.productService.getUiProductData(parseInt(productId || '0')).subscribe({
      next: (uiProductData: UiProductData) => {
        // Convert to string primitive if needed (handle both string and String wrapper)
        const jsonString: string = typeof uiProductData.jsonExample === 'string' ? uiProductData.jsonExample : String(uiProductData.jsonExample);
        
        try {
          // Parse JSON
          const parsedData = JSON.parse(jsonString);
          this.policy = new BoxPolicy(parsedData);
          this.syncPolicyHolderDatePickersFromModel();
          this.updateCoverageTable();
          this.updateTravelSegmentsTable();

          this.lists = uiProductData.lists ?? {};
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
          this.product = {
            ...product,
            vars: (product.vars ?? []).filter((v) => !v.isDeleted),
          };
          this.updateTables();
          this.applySingleOptionForPeriodLists();
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

  onSubmit() {
    
    const plc = this.policyService.conversBox2Policy(this.policy);

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
        this.syncPolicyHolderDatePickersFromModel();
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
        this.syncPolicyHolderDatePickersFromModel();
        this.updateCoverageTable();
        this.snackBar.open('Политика сохранена успешно', 'Закрыть', { duration: 3000 });
      },
      error: (error) => {
        console.error('Save error:', error);
        this.snackBar.open('Ошибка при сохранении: ' + (error.error?.message || error.message), 'Закрыть', { duration: 5000 });
      }
    });
  }

  syncPolicyHolderDatePickersFromModel(): void {
    this.policyHolderBirthDateValue = this.parseIsoDateStringToLocalDate(
      this.policy?.policyHolder?.person?.birthDate,
    );
    const id = this.policy?.policyHolder?.identifiers;
    this.policyHolderDocDateIssueValue = this.parseIsoDateStringToLocalDate(id?.dateIssue);
    this.policyHolderDocValidUntilValue = this.parseIsoDateStringToLocalDate(id?.validUntil);
    this.policyStartDateValue = this.parseIsoDateStringToLocalDate(this.policy?.startDate);
    this.policyEndDateValue = this.parseIsoDateStringToLocalDate(this.policy?.endDate);
  }

  onPolicyHolderBirthDateChange(value: Date | null): void {
    const p = this.policy?.policyHolder?.person;
    if (!p) {
      return;
    }
    if (!value) {
      p.birthDate = '';
      return;
    }
    p.birthDate = this.formatLocalDateToIsoDate(value);
  }

  onPolicyStartDateChange(value: Date | null): void {
    if (!value) {
      this.policy.startDate = '';
      return;
    }
    this.policy.startDate = this.formatLocalDateToIsoDate(value);
  }

  onPolicyEndDateChange(value: Date | null): void {
    if (!value) {
      this.policy.endDate = '';
      return;
    }
    this.policy.endDate = this.formatLocalDateToIsoDate(value);
  }

  onPolicyHolderDocDateIssueChange(value: Date | null): void {
    const id = this.ensureIdentifier();
    if (!value) {
      id.dateIssue = '';
      return;
    }
    id.dateIssue = this.formatLocalDateToIsoDate(value);
  }

  onPolicyHolderDocValidUntilChange(value: Date | null): void {
    const id = this.ensureIdentifier();
    if (!value) {
      id.validUntil = '';
      return;
    }
    id.validUntil = this.formatLocalDateToIsoDate(value);
  }

  private parseIsoDateStringToLocalDate(raw: string | undefined): Date | null {
    if (!raw?.trim()) {
      return null;
    }
    const s = raw.trim();
    const iso = /^(\d{4})-(\d{2})-(\d{2})/.exec(s);
    if (iso) {
      const y = Number(iso[1]);
      const m = Number(iso[2]) - 1;
      const d = Number(iso[3]);
      const dt = new Date(y, m, d);
      return Number.isNaN(dt.getTime()) ? null : dt;
    }
    const dt = new Date(s);
    return Number.isNaN(dt.getTime()) ? null : dt;
  }

  private formatLocalDateToIsoDate(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  getShowStartDate(): boolean {
    // если список для периода ожидания пустой то вводим дату
    const wp = this.listEntries('waitingPeriod');
    if (wp && wp.length === 1) {
      return false;
    }
    return true;
  }

  getShowEndDate(): boolean {
    const pt = this.listEntries('policyTerm');
    if (pt && pt.length === 1) {
      return false;
    }
    return true;
  }

  getFieldVisible(fieldName: string): boolean {
    const found = this.product.vars.some(v => v.varCode === fieldName);
    return found;
  }

  getFieldLabel(fieldName: string): string {
    var v = this.product.vars.find(v => v.varCode === fieldName);
    return v?.name || v?.varName || fieldName;
  }

  getShowOrganization(): boolean {
    return this.product.vars.some(v => v.varPath && v.varPath.startsWith("policyHolder.organization"));
  }

  getShowPHCustom(): boolean {
    return this.product.vars.some(v => v.varPath && v.varPath.startsWith("policyHolder.customFields"));
  }

  getShowIoDevice(): boolean {
    return this.product.vars.some(v => v.varCdm && v.varCdm.startsWith("insuredObjects.device"));
  }

  getShowIoTravelSegments(): boolean {
    return this.product.vars.some(v => v.varCdm && (
      v.varCdm.startsWith("insuredObjects.travelSegments") ||
      v.varCdm.startsWith("insuredObjects.travelSegment")
    ));
  }

  // Backwards-compatible wrapper
  getShowIoTravelSegment(): boolean {
    return this.getShowIoTravelSegments();
  }

  getAdressTypes(): LoV[] {
    const fromLists = this.listEntries('ph_addr_typeCode');
    if (fromLists.length) {
      return fromLists.map(({ value, label }) => ({ value, viewValue: label }));
    }
    const validator = this.product?.saveValidator?.find(
      (v: any) => v.keyLeft === 'ph_addr_typeCode' && v.ruleType === 'IN_LIST',
    );
    if (validator && typeof validator.valueRight === 'string') {
      const values = validator.valueRight.split(',').map((s: string) => s.trim());
      return values.map((value) => ({ value, viewValue: value }));
    }
    return [];
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
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
    FormsModule,
  ],
  providers: [provideNativeDateAdapter()],
  template: `
    <h2 mat-dialog-title>{{ data.isNew ? 'Добавить перевозку' : 'Редактировать перевозку' }}</h2>
    <div mat-dialog-content class="travel-segment-dialog" style="padding-top: 30px;">
      <div class="row-full">
        <mat-form-field appearance="outline" class="field">
          <mat-label>{{ getFieldLabel('io_ticketNr') }}</mat-label>
          <input matInput [(ngModel)]="segment.ticketNr" name="ticketNr" />
        </mat-form-field>
      </div>
      <div class="row-full">
        <mat-form-field appearance="outline" class="field">
          <mat-label>{{ getFieldLabel('io_ticketPrice') }}</mat-label>
          <input matInput [(ngModel)]="segment.ticketPrice" name="ticketPrice" />
        </mat-form-field>
      </div>
      <div class="row-2col">
        <mat-form-field appearance="outline" class="field">
          <mat-label>{{ getFieldLabel('io_departureDate') }}</mat-label>
          <input
            matInput
            [matDatepicker]="departureDatePicker"
            [(ngModel)]="departureDateValue"
            (ngModelChange)="onDepartureDateChange($event)"
            name="departureDate"
          />
          <mat-datepicker-toggle matSuffix [for]="departureDatePicker"></mat-datepicker-toggle>
          <mat-datepicker
            #departureDatePicker
            [restoreFocus]="false"
            [startAt]="departureDateValue ?? departureDatePickerFallbackStart"
          ></mat-datepicker>
        </mat-form-field>
        <mat-form-field appearance="outline" class="field">
          <mat-label>{{ getFieldLabel('io_departureTime') }}</mat-label>
          <input
            matInput
            type="time"
            step="60"
            [(ngModel)]="segment.departureTime"
            name="departureTime"
          />
        </mat-form-field>
      </div>
      <div class="row-full">
        <mat-form-field appearance="outline" class="field">
          <mat-label>{{ getFieldLabel('io_departureCity') }}</mat-label>
          <input matInput [(ngModel)]="segment.departureCity" name="departureCity" />
        </mat-form-field>
      </div>
      <div class="row-full">
        <mat-form-field appearance="outline" class="field">
          <mat-label>{{ getFieldLabel('io_arrivalCity') }}</mat-label>
          <input matInput [(ngModel)]="segment.arrivalCity" name="arrivalCity" />
        </mat-form-field>
      </div>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-stroked-button mat-dialog-close>Отмена</button>
      <button mat-flat-button color="primary" [mat-dialog-close]="segment">Сохранить</button>
    </div>
  `,
  styles: [`
    .travel-segment-dialog {
      display: flex;
      flex-direction: column;
      gap: 16px;
      min-width: 520px;
    }
    .row-full {
      display: flex;
      width: 100%;
    }
    .row-full > .field {
      flex: 1 1 100%;
      width: 100%;
      min-width: 0;
    }
    .row-2col {
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;
      gap: 16px;
      align-items: flex-start;
      width: 100%;
    }
    .row-2col > .field {
      flex: 1 1 0;
      min-width: 0;
    }
    .field {
      width: 100%;
    }
  `]
})
export class TravelSegmentDialog {
  segment: BoxTravelSegment;

  /** Для mat-datepicker; в {@link BoxTravelSegment.departureDate} — строка yyyy-MM-dd. */
  departureDateValue: Date | null = null;

  readonly departureDatePickerFallbackStart = new Date();

  constructor(
    public dialogRef: MatDialogRef<TravelSegmentDialog>,
    @Inject(MAT_DIALOG_DATA) public data: { segment: BoxTravelSegment; isNew: boolean }
  ) {
    this.segment = data.segment ? new BoxTravelSegment(data.segment) : new BoxTravelSegment();
    this.segment.departureTime = normalizeDepartureTimeToHHmm(this.segment.departureTime);
    this.departureDateValue = this.parseIsoDateStringToLocalDate(this.segment.departureDate);
  }

  onDepartureDateChange(value: Date | null): void {
    if (!value) {
      this.segment.departureDate = '';
      return;
    }
    this.segment.departureDate = this.formatLocalDateToIsoDate(value);
  }

  private parseIsoDateStringToLocalDate(raw: string | undefined): Date | null {
    if (!raw?.trim()) {
      return null;
    }
    const s = raw.trim();
    const iso = /^(\d{4})-(\d{2})-(\d{2})/.exec(s);
    if (iso) {
      const y = Number(iso[1]);
      const m = Number(iso[2]) - 1;
      const d = Number(iso[3]);
      const dt = new Date(y, m, d);
      return Number.isNaN(dt.getTime()) ? null : dt;
    }
    const dt = new Date(s);
    return Number.isNaN(dt.getTime()) ? null : dt;
  }

  private formatLocalDateToIsoDate(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  getFieldLabel(fieldName: string): string {
    const labels: Record<string, string> = {
      io_ticketNr: 'Номер билета',
      io_ticketPrice: 'Цена билета',
      io_departureDate: 'Дата вылета',
      io_departureTime: 'Время вылета',
      io_departureCity: 'Город вылета',
      io_arrivalCity: 'Город прилёта',
    };
    return labels[fieldName] ?? fieldName;
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

