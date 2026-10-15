import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { PolicyDraftStore } from '../state/policy-draft.store';
import { POLICY_PAGE_REGISTRY } from '../registry/policy-page.registry';
import { PolicyPageConfig } from '../models/policy-page-config.model';
import { QuoteNs1Component } from '../blocks/quote-ns1/quote-ns1.component';
import { QuoteGad001Component } from '../blocks/quote-gad001/quote-gad001.component';
import { PolicyHolderPersonComponent } from '../blocks/policy-holder-person/policy-holder-person.component';
import { Insured02Component } from '../blocks/insured02/insured02.component';
import { PaymentBlockComponent } from '../blocks/payment/payment-block.component';

@Component({
  selector: 'app-policy-entry-shell',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    QuoteNs1Component,
    QuoteGad001Component,
    PolicyHolderPersonComponent,
    Insured02Component,
    PaymentBlockComponent
  ],
  providers: [PolicyDraftStore],
  templateUrl: './policy-entry-shell.component.html',
  styleUrl: './policy-entry-shell.component.scss'
})
export class PolicyEntryShellComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  readonly store = inject(PolicyDraftStore);

  pageConfig: PolicyPageConfig | null = null;

  readonly steps = [
    { label: 'Предрасчёт' },
    { label: 'Оформление' },
    { label: 'Оплата' }
  ];

  ngOnInit(): void {
    const pageId = this.route.snapshot.data['pageId'] as string;
    const config = POLICY_PAGE_REGISTRY[pageId] ?? null;
    this.pageConfig = config;
    if (config) {
      this.store.init(config.pageId, config.productCode, { ioType: config.ioType });
    }
  }

  onStepClick(stepIndex: number): void {
    if (!this.store.canNavigateToStep(stepIndex)) {
      return;
    }
    this.store.goToStep(stepIndex);
  }
}
