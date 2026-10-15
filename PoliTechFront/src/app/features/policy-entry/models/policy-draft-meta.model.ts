export interface PolicyDraftMeta {
  pageId: string;
  productCode: string;
  /** 0 — предрасчёт, 1 — оформление, 2 — оплата */
  stepIndex: number;
  quoted: boolean;
  loading: boolean;
  error: string | null;
}
