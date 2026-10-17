import { PolicyPageConfig } from '../models/policy-page-config.model';

/** UI block wiring only — title / productCode / ioType come from API + query. */
export const POLICY_PAGE_REGISTRY: Record<string, PolicyPageConfig> = {
  'ns-policy': {
    pageId: 'ns-policy',
    quoteBlock: 'quote-ns1',
    issueBlocks: ['policy-holder-person', 'insured02'],
    paymentBlock: 'payment',
  },
  policy_gad001: {
    pageId: 'policy_gad001',
    quoteBlock: 'quote-gad001',
    issueBlocks: ['policy-holder-person'],
    paymentBlock: 'payment',
  },
};
