import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PolicyService } from '../../../../shared/services/policy.service';
import { PolicyDraftStore, parsePolicyTermMonths } from '../../state/policy-draft.store';
import { PolicyTermBlockComponent } from '../policy-term/policy-term-block.component';
import { NS_PACKAGE_OPTIONS, NS_SPORT_OPTIONS } from './quote-ns1.constants';

@Component({
  selector: 'app-quote-ns1',
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
  templateUrl: './quote-ns1.component.html',
  styleUrl: './quote-ns1.component.scss'
})
export class QuoteNs1Component {
  private readonly policyService = inject(PolicyService);
  readonly store = inject(PolicyDraftStore);

  readonly sportOptions = NS_SPORT_OPTIONS;
  readonly packageOptions = NS_PACKAGE_OPTIONS;

  /** Локальный UI-state — в Policy не пишем */
  selectedSports: string[] = [];

  readonly policy = this.store.policy;
  readonly loading = this.store.loading;
  readonly quoted = this.store.quoted;
  readonly error = this.store.error;
  readonly canIssue = this.store.canIssue;

  readonly insured = computed(() => this.store.policy().insuredObjects[0] ?? null);
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

  onSportsChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.selectedSports = Array.from(select.selectedOptions).map(o => o.value).slice(0, 5);
  }

  onBirthDate(event: Event): void {
    const iso = (event.target as HTMLInputElement).value || '';
    if (this.insured()?.person?.birthDate === iso) {
      return;
    }
    this.store.patchInsuredObject(0, io => {
      io.person!.birthDate = iso;
    });
  }

  onGender(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    if (this.insured()?.person?.gender === value) {
      return;
    }
    this.store.patchInsuredObject(0, io => {
      io.person!.gender = value;
    });
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
    this.store.syncInsuredToPolicyHolder();
    this.store.resetQuote();
    this.store.setLoading(true);
    // JSON собирается заново в PolicyService.buildFreshQuoteBody (без прошлого ответа)
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
