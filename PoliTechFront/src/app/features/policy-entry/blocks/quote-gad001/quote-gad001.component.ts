import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PolicyService } from '../../../../shared/services/policy.service';
import { Device } from '../../../../shared/models/policy.models';
import { PolicyDraftStore, parsePolicyTermMonths } from '../../state/policy-draft.store';
import { PolicyTermBlockComponent } from '../policy-term/policy-term-block.component';
import { GAD_DEVICE_TYPE_OPTIONS, GAD_PACKAGE_OPTIONS } from './quote-gad001.constants';

@Component({
  selector: 'app-quote-gad001',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    CurrencyPipe,
    PolicyTermBlockComponent
  ],
  templateUrl: './quote-gad001.component.html',
  styleUrl: './quote-gad001.component.scss'
})
export class QuoteGad001Component {
  private readonly policyService = inject(PolicyService);
  readonly store = inject(PolicyDraftStore);

  readonly deviceTypeOptions = GAD_DEVICE_TYPE_OPTIONS;
  readonly packageOptions = GAD_PACKAGE_OPTIONS;

  readonly policy = this.store.policy;
  readonly loading = this.store.loading;
  readonly quoted = this.store.quoted;
  readonly error = this.store.error;
  readonly canIssue = this.store.canIssue;

  readonly insured = computed(() => this.store.policy().insuredObjects[0] ?? null);
  readonly device = computed(() => this.insured()?.device ?? null);

  readonly packageLabel = computed(() => {
    const code = this.insured()?.packageCode ?? '0';
    return this.packageOptions.find(p => p.value === code)?.label ?? code;
  });

  readonly termLabel = computed(() => {
    const months = parsePolicyTermMonths(this.policy().policyTerm);
    if (months == null) {
      return this.policy().policyTerm || '—';
    }
    return `${months} мес. (${this.policy().policyTerm})`;
  });

  private patchDevice(updater: (device: Device) => void): void {
    this.store.patchInsuredObject(0, io => {
      if (!io.device) {
        io.device = new Device();
      }
      updater(io.device);
    });
  }

  onDeviceName(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    if (this.device()?.deviceName === value) {
      return;
    }
    this.patchDevice(d => {
      d.deviceName = value;
    });
  }

  onDeviceType(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    if ((this.device()?.deviceTypeCode ?? '') === value) {
      return;
    }
    this.patchDevice(d => {
      d.deviceTypeCode = value;
    });
    this.store.resetQuote();
  }

  onTradeMark(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    if (this.device()?.tradeMark === value) {
      return;
    }
    this.patchDevice(d => {
      d.tradeMark = value;
    });
  }

  onModel(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    if (this.device()?.model === value) {
      return;
    }
    this.patchDevice(d => {
      d.model = value;
    });
  }

  onSerialNr(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    if (this.device()?.serialNr === value) {
      return;
    }
    this.patchDevice(d => {
      d.serialNr = value;
    });
  }

  onImei(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    if (this.device()?.imei === value) {
      return;
    }
    this.patchDevice(d => {
      d.imei = value;
    });
  }

  onDevicePrice(event: Event): void {
    const raw = (event.target as HTMLInputElement).value.trim();
    const price = raw === '' ? 0 : Number(raw);
    if (Number.isNaN(price) || this.device()?.devicePrice === price) {
      return;
    }
    this.patchDevice(d => {
      d.devicePrice = price;
    });
    this.store.resetQuote();
  }

  onSumInsured(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    if (this.insured()?.sumInsured === value) {
      return;
    }
    this.store.patchInsuredObject(0, io => {
      io.sumInsured = value;
    });
    this.store.resetQuote();
  }

  onPackage(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    if ((this.insured()?.packageCode ?? '0') === value) {
      return;
    }
    this.store.patchInsuredObject(0, io => {
      io.packageCode = value;
    });
    this.store.resetQuote();
  }

  quote(): void {
    this.store.resetQuote();
    this.store.setLoading(true);
    this.policyService.fastCalc(this.store.policy()).subscribe({
      next: result => this.store.setFromQuote(result),
      error: err =>
        this.store.setError(
          err?.error?.message ?? err?.error?.error ?? err?.message ?? 'Ошибка расчёта премии'
        )
    });
  }

  issue(): void {
    this.store.goToIssue();
  }
}
