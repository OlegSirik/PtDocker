import { PolicyPageConfig } from '../models/policy-page-config.model';

export const POLICY_PAGE_REGISTRY: Record<string, PolicyPageConfig> = {
  'ns-policy': {
    pageId: 'ns-policy',
    title: 'Несчастный случай',
    productCode: 'NS',
    quoteBlock: 'quote-ns1',
    issueBlocks: ['policy-holder-person', 'insured02'],
    paymentBlock: 'payment'
  },
  policy_gad001: {
    pageId: 'policy_gad001',
    title: 'Страхование техники',
    productCode: 'COMPLEX_PROTECTION',
    ioType: 'DEVICE',
    quoteBlock: 'quote-gad001',
    issueBlocks: ['policy-holder-person'],
    paymentBlock: 'payment'
  }
};
