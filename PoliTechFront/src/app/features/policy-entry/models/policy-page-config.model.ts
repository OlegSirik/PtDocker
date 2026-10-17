export type PolicyBlockId =
  | 'quote-ns1'
  | 'quote-gad001'
  | 'policy-holder-person'
  | 'insured02'
  | 'payment';

export type PolicyIoType = 'PERSON' | 'DEVICE';

/** Front wiring of Angular blocks by pageId (catalog metadata comes from API). */
export interface PolicyPageConfig {
  pageId: string;
  quoteBlock: PolicyBlockId;
  issueBlocks: PolicyBlockId[];
  paymentBlock: PolicyBlockId;
}
