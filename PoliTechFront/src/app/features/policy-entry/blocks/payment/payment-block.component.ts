import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { PolicyDraftStore } from '../../state/policy-draft.store';

@Component({
  selector: 'app-payment-block',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatCardModule, MatButtonModule],
  templateUrl: './payment-block.component.html',
  styleUrl: './payment-block.component.scss'
})
export class PaymentBlockComponent {
  readonly store = inject(PolicyDraftStore);

  /** Заглушка — без API (todo §0.10) */
  onPaid(): void {}
}
