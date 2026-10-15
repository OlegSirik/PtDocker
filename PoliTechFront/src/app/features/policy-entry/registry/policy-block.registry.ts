import { Type } from '@angular/core';
import { PolicyBlockId } from '../models/policy-page-config.model';
import { QuoteNs1Component } from '../blocks/quote-ns1/quote-ns1.component';
import { QuoteGad001Component } from '../blocks/quote-gad001/quote-gad001.component';
import { PolicyHolderPersonComponent } from '../blocks/policy-holder-person/policy-holder-person.component';
import { Insured02Component } from '../blocks/insured02/insured02.component';
import { PaymentBlockComponent } from '../blocks/payment/payment-block.component';

export const POLICY_BLOCK_REGISTRY: Record<PolicyBlockId, Type<unknown>> = {
  'quote-ns1': QuoteNs1Component,
  'quote-gad001': QuoteGad001Component,
  'policy-holder-person': PolicyHolderPersonComponent,
  insured02: Insured02Component,
  payment: PaymentBlockComponent
};
