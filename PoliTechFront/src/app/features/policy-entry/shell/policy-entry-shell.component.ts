import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { PolicyDraftStore } from '../state/policy-draft.store';
import { POLICY_PAGE_REGISTRY } from '../registry/policy-page.registry';
import { PolicyPageConfig, PolicyIoType } from '../models/policy-page-config.model';
import { QuoteNs1Component } from '../blocks/quote-ns1/quote-ns1.component';
import { QuoteGad001Component } from '../blocks/quote-gad001/quote-gad001.component';
import { PolicyHolderPersonComponent } from '../blocks/policy-holder-person/policy-holder-person.component';
import { Insured02Component } from '../blocks/insured02/insured02.component';
import { PaymentBlockComponent } from '../blocks/payment/payment-block.component';
import { PolicyFormService } from '../../../shared/services/api/policy-form.service';

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
  private readonly policyFormService = inject(PolicyFormService);
  readonly store = inject(PolicyDraftStore);

  pageConfig: PolicyPageConfig | null = null;
  pageTitle = '';

  readonly steps = [
    { label: 'Предрасчёт' },
    { label: 'Оформление' },
    { label: 'Оплата' }
  ];

  ngOnInit(): void {
    const pageId = this.route.snapshot.data['pageId'] as string;
    const config = POLICY_PAGE_REGISTRY[pageId] ?? null;
    this.pageConfig = config;
    if (!config) {
      return;
    }

    const qp = this.route.snapshot.queryParamMap;
    const productCode = qp.get('productCode')?.trim() || '';
    const titleFromQuery = qp.get('title')?.trim() || '';
    const ioTypeFromQuery = qp.get('ioType')?.trim() || '';

    if (titleFromQuery || ioTypeFromQuery) {
      this.pageTitle = titleFromQuery || pageId;
      this.store.init(config.pageId, productCode, {
        ioType: this.resolveIoType(ioTypeFromQuery),
      });
      return;
    }

    this.policyFormService.getByPageId(pageId).subscribe({
      next: (form) => {
        this.pageTitle = form.title || pageId;
        this.store.init(config.pageId, productCode, {
          ioType: this.resolveIoType(form.ioType),
        });
      },
      error: () => {
        this.pageTitle = pageId;
        this.store.init(config.pageId, productCode, { ioType: 'PERSON' });
      },
    });
  }

  private resolveIoType(raw?: string | null): PolicyIoType {
    return (raw || '').toUpperCase() === 'DEVICE' ? 'DEVICE' : 'PERSON';
  }

  onStepClick(stepIndex: number): void {
    if (!this.store.canNavigateToStep(stepIndex)) {
      return;
    }
    this.store.goToStep(stepIndex);
  }
}
