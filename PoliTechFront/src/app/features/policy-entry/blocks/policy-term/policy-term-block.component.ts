import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { PolicyDraftStore } from '../../state/policy-draft.store';

/** 1..36 месяцев → policyTerm P1M .. P36M */
export const POLICY_TERM_MONTH_OPTIONS: { months: number; value: string; label: string }[] =
  Array.from({ length: 36 }, (_, i) => {
    const months = i + 1;
    return {
      months,
      value: `P${months}M`,
      label: months === 1 ? '1 месяц' : months < 5 ? `${months} месяца` : `${months} месяцев`
    };
  });

@Component({
  selector: 'app-policy-term-block',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatCardModule],
  templateUrl: './policy-term-block.component.html',
  styleUrl: './policy-term-block.component.scss'
})
export class PolicyTermBlockComponent {
  readonly store = inject(PolicyDraftStore);
  readonly monthOptions = POLICY_TERM_MONTH_OPTIONS;

  readonly policy = this.store.policy;

  readonly selectedTerm = computed(() => {
    const term = this.policy().policyTerm || 'P12M';
    return this.monthOptions.some(o => o.value === term) ? term : 'P12M';
  });

  onStartDate(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    if (!value || this.store.dateInputValue(this.policy().startDate) === value) {
      return;
    }
    this.store.setStartDate(value);
  }

  onTermMonths(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    if (!value || this.policy().policyTerm === value) {
      return;
    }
    this.store.setPolicyTerm(value);
  }
}
