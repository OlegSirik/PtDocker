import { Injectable, computed, inject, signal } from '@angular/core';
import {
  Policy,
  PolicyHolder,
  InsuredObject,
  Person,
  Contacts,
  Identifier,
  Address,
  Device
} from '../../../shared/models/policy.models';
import { PolicyDraftMeta } from '../models/policy-draft-meta.model';
import type { PolicyIoType } from '../models/policy-page-config.model';
import { PolicyService } from '../../../shared/services/policy.service';

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

/** Date-only UI value → wire ZonedDateTime */
function toWireDate(startIso: string): string {
  const day = startIso.slice(0, 10);
  return `${day}T00:00:00Z`;
}

function dateInputPart(iso?: string): string {
  return (iso || '').slice(0, 10);
}

/** P12M → 12; invalid → null */
export function parsePolicyTermMonths(term?: string | null): number | null {
  if (!term) {
    return null;
  }
  const m = /^P(\d+)M$/i.exec(term.trim());
  if (!m) {
    return null;
  }
  const n = Number(m[1]);
  return n >= 1 && n <= 36 ? n : null;
}

export function toPolicyTerm(months: number): string {
  return `P${months}M`;
}

@Injectable()
export class PolicyDraftStore {
  private readonly policyService = inject(PolicyService);

  private readonly _policy = signal<Policy>(new Policy());
  private readonly _meta = signal<PolicyDraftMeta>({
    pageId: '',
    productCode: '',
    stepIndex: 0,
    quoted: false,
    loading: false,
    error: null
  });

  readonly policy = this._policy.asReadonly();
  readonly meta = this._meta.asReadonly();
  readonly loading = computed(() => this._meta().loading);
  readonly error = computed(() => this._meta().error);
  readonly quoted = computed(() => this._meta().quoted);
  readonly stepIndex = computed(() => this._meta().stepIndex);
  readonly premium = computed(() => this._policy().premium ?? 0);
  readonly policyNumber = computed(() => this._policy().policyNumber ?? '');
  readonly canIssue = computed(() => {
    const p = this._policy().premium;
    return p != null && p > 0;
  });
  readonly canGoBack = computed(() => this._meta().stepIndex > 0);

  private readonly _maxReachedStep = signal(0);

  init(pageId: string, productCode: string, options?: { ioType?: PolicyIoType }): void {
    const startDate = toWireDate(todayIso());
    const policyTerm = toPolicyTerm(12);
    const ioType: PolicyIoType = options?.ioType ?? 'PERSON';

    const policy = new Policy({
      productCode,
      statusCode: 'DRAFT',
      startDate,
      policyTerm
    });
    policy.id = undefined;
    policy.policyHolder = new PolicyHolder();
    policy.policyHolder.person = new Person();
    policy.policyHolder.contacts = new Contacts();
    policy.policyHolder.identifiers = [new Identifier({ typeCode: 'PASSPORT' })];
    policy.policyHolder.addresses = [new Address()];
    policy.insuredObjects =
      ioType === 'DEVICE'
        ? [
            new InsuredObject({
              ioType: 'DEVICE',
              packageCode: '0',
              device: new Device({ deviceTypeCode: 'DIGITAL-DEVICE' })
            })
          ]
        : [
            new InsuredObject({
              ioType: 'PERSON',
              packageCode: '0',
              person: new Person()
            })
          ];

    this._policy.set(policy);
    this._meta.set({
      pageId,
      productCode,
      stepIndex: 0,
      quoted: false,
      loading: false,
      error: null
    });
    this._maxReachedStep.set(0);
  }

  patchPolicy(partial: Partial<Policy>): void {
    this._policy.update(current => {
      const next = Object.assign(new Policy(), current, partial);
      next.policyHolder = current.policyHolder;
      next.insuredObjects = current.insuredObjects;
      return next;
    });
  }

  setStartDate(startDate: string): void {
    this.patchPolicy({ startDate: toWireDate(startDate) });
    this.resetQuote();
  }

  setPolicyTerm(policyTerm: string): void {
    const months = parsePolicyTermMonths(policyTerm);
    if (months == null) {
      return;
    }
    this.patchPolicy({ policyTerm: toPolicyTerm(months) });
    this.resetQuote();
  }

  /** YYYY-MM-DD for input type=date */
  dateInputValue(iso?: string): string {
    return dateInputPart(iso);
  }

  patchPolicyHolder(updater: (holder: PolicyHolder) => void): void {
    this._policy.update(current => {
      const holder = new PolicyHolder(current.policyHolder);
      updater(holder);
      const next = Object.assign(new Policy(), current);
      next.policyHolder = holder;
      next.insuredObjects = current.insuredObjects;
      return next;
    });
  }

  patchInsuredObject(index: number, updater: (io: InsuredObject) => void): void {
    this._policy.update(current => {
      const objects = [...(current.insuredObjects || [])];
      while (objects.length <= index) {
        objects.push(new InsuredObject({ ioType: 'PERSON', person: new Person() }));
      }
      const io = new InsuredObject(objects[index]);
      if (io.ioType === 'DEVICE') {
        if (!io.device) {
          io.device = new Device();
        }
      } else if (!io.person) {
        io.person = new Person();
      }
      updater(io);
      objects[index] = io;
      const next = Object.assign(new Policy(), current);
      next.insuredObjects = objects;
      // Застрахованный → страхователь (только для PERSON)
      next.policyHolder =
        index === 0 && io.ioType !== 'DEVICE'
          ? this.copyInsuredPersonToHolder(current.policyHolder, io.person)
          : current.policyHolder;
      return next;
    });
  }

  /** Копирует заполненные поля person застрахованного в policyHolder.person */
  private copyInsuredPersonToHolder(holderSrc: PolicyHolder | undefined, insuredPerson?: Person): PolicyHolder {
    const holder = new PolicyHolder(holderSrc);
    if (!insuredPerson) {
      return holder;
    }
    if (!holder.person) {
      holder.person = new Person();
    }
    const fields: (keyof Person)[] = [
      'firstName',
      'lastName',
      'middleName',
      'birthDate',
      'gender',
      'fullName',
      'citizenship'
    ];
    for (const field of fields) {
      const value = insuredPerson[field];
      if (value != null && value !== '') {
        (holder.person as Person)[field] = value as never;
      }
    }
    return holder;
  }

  /** Перед quote/save — синхронизировать insured → policyHolder в draft */
  syncInsuredToPolicyHolder(): void {
    this._policy.update(current => {
      const insuredPerson = current.insuredObjects[0]?.person;
      const next = Object.assign(new Policy(), current);
      next.insuredObjects = current.insuredObjects;
      next.policyHolder = this.copyInsuredPersonToHolder(current.policyHolder, insuredPerson);
      return next;
    });
  }

  /**
   * Результат расчёта не перетирает draft ввода — только premium (и endDate для отображения).
   */
  setFromQuote(result: Policy): void {
    this._policy.update(current => {
      const next = Object.assign(new Policy(), current);
      next.policyHolder = current.policyHolder;
      next.insuredObjects = current.insuredObjects;
      next.premium = result.premium;
      if (result.endDate) {
        next.endDate = result.endDate;
      }
      // не тащим id / covers / processList / status из ответа в draft
      next.id = undefined;
      return next;
    });
    this._meta.update(m => ({ ...m, quoted: true, loading: false, error: null }));
  }

  setLoading(loading: boolean): void {
    this._meta.update(m => ({ ...m, loading }));
  }

  setError(error: string | null): void {
    this._meta.update(m => ({ ...m, error, loading: false }));
  }

  resetQuote(): void {
    this._policy.update(current => {
      const next = Object.assign(new Policy(), current);
      next.premium = undefined;
      next.endDate = undefined;
      next.id = undefined;
      next.policyNumber = undefined;
      next.policyHolder = current.policyHolder;
      next.insuredObjects = current.insuredObjects;
      return next;
    });
    this._meta.update(m => ({ ...m, quoted: false, error: null }));
  }

  goToIssue(): void {
    if (!this.canIssue()) {
      return;
    }
    this.syncInsuredToPolicyHolder();
    this.setStepIndex(1);
  }

  goToPayment(): void {
    this.setStepIndex(2);
  }

  /**
   * Выпуск договора: POST /sales/policies → номер договора → шаг оплаты.
   * При ошибке остаёмся на оформлении и пишем store.error.
   */
  issuePolicy(): void {
    if (this._meta().loading) {
      return;
    }
    this.syncInsuredToPolicyHolder();
    this.setError(null);
    this.setLoading(true);
    this.policyService.savePolicy(this._policy()).subscribe({
      next: result => {
        const policyNumber =
          (result as Policy)?.policyNumber?.trim() ||
          (result as { policyNr?: string })?.policyNr?.trim() ||
          '';
        this._policy.update(current => {
          const next = Object.assign(new Policy(), current);
          next.policyHolder = current.policyHolder;
          next.insuredObjects = current.insuredObjects;
          next.premium = current.premium;
          next.endDate = current.endDate;
          next.policyNumber = policyNumber || current.policyNumber;
          if ((result as Policy)?.id != null) {
            next.id = (result as Policy).id;
          }
          return next;
        });
        this._meta.update(m => ({ ...m, loading: false, error: null }));
        this.setStepIndex(2);
      },
      error: err => {
        this.setError(this.resolveApiError(err, 'Ошибка выпуска договора'));
      },
    });
  }

  private resolveApiError(err: unknown, fallback: string): string {
    const e = err as {
      error?: { message?: string; error?: string; detail?: string } | string;
      message?: string;
    };
    if (typeof e?.error === 'string' && e.error.trim()) {
      return e.error.trim();
    }
    if (e?.error && typeof e.error === 'object') {
      const msg = e.error.message || e.error.error || e.error.detail;
      if (msg && String(msg).trim()) {
        return String(msg).trim();
      }
    }
    if (e?.message && String(e.message).trim()) {
      return String(e.message).trim();
    }
    return fallback;
  }

  goBack(): void {
    const current = this._meta().stepIndex;
    if (current <= 0) {
      return;
    }
    this.setStepIndex(current - 1);
  }

  /** Переход на шаг: назад — всегда; вперёд — только на уже достигнутые (после Оформить / Далее). */
  goToStep(stepIndex: number): void {
    if (stepIndex < 0 || stepIndex > 2) {
      return;
    }
    const current = this._meta().stepIndex;
    if (stepIndex === current) {
      return;
    }
    if (stepIndex < current) {
      this.setStepIndex(stepIndex);
      return;
    }
    // вперёд только если уже были на этом шаге или дальше
    if (stepIndex <= this._maxReachedStep()) {
      this.setStepIndex(stepIndex);
    }
  }

  canNavigateToStep(stepIndex: number): boolean {
    if (stepIndex < 0 || stepIndex > 2) {
      return false;
    }
    const current = this._meta().stepIndex;
    if (stepIndex <= current) {
      return true;
    }
    return stepIndex <= this._maxReachedStep();
  }

  setStepIndex(stepIndex: number): void {
    if (this._meta().stepIndex === stepIndex) {
      return;
    }
    this._meta.update(m => ({ ...m, stepIndex }));
    if (stepIndex > this._maxReachedStep()) {
      this._maxReachedStep.set(stepIndex);
    }
  }
}
