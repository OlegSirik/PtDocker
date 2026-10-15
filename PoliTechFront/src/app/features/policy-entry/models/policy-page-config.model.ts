export type PolicyBlockId =
  | 'quote-ns1'
  | 'quote-gad001'
  | 'policy-holder-person'
  | 'insured02'
  | 'payment';

export type PolicyIoType = 'PERSON' | 'DEVICE';

export interface PolicyPageConfig {
  pageId: string;
  title: string;
  productCode: string;
  /** Тип объекта страхования при init draft (по умолчанию PERSON) */
  ioType?: PolicyIoType;
  quoteBlock: PolicyBlockId;
  issueBlocks: PolicyBlockId[];
  paymentBlock: PolicyBlockId;
}
