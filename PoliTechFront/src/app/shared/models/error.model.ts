export interface ErrorModel {
  code: number;
  message: string;
  errors?: ErrorDetail[];
}

export interface ErrorDetail {
  domain?: string;
  reason?: string;
  message?: string;
  field?: string;
}
