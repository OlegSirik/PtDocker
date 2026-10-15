import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Calculator } from '../calculator.service';
import { AuthService } from '../auth.service';

export type LlmTaskType = 'RULE' | 'COVER' | 'PACKAGE' | 'CALCULATOR' | 'SCHEMA';

export interface LlmAssistRequest {
  taskType: LlmTaskType;
  userMessage: string;
  productId?: number;
  versionNo?: number;
}

export interface LlmRuleDraft {
  code: string;
  name: string;
  condition: string;
  message?: string;
}

export interface LlmCoverDraft {
  code: string;
  name: string;
  risks?: string;
  isMandatory?: boolean;
  vars?: unknown[];
}

export interface LlmPackageDeductibleDraft {
  id: number;
  text: string;
}

export interface LlmPackageCoverDraft {
  code: string;
  isMandatory?: boolean;
  waitingPeriod?: string;
  coverageTerm?: string;
  isDeductibleMandatory?: boolean;
  deductibles?: LlmPackageDeductibleDraft[];
}

export interface LlmPackageDraft {
  code: string;
  name: string;
  covers?: LlmPackageCoverDraft[];
}

export interface LlmSchemaAttributeDraft {
  varCode: string;
  varName: string;
  varPath: string;
  varType: string;
  varDataType?: string;
  varList?: string | null;
  varValue?: string;
  isSystem?: boolean;
  parent_id?: number | null;
  parentVarCode?: string | null;
}

export interface LlmSchemaDraft {
  attributes: LlmSchemaAttributeDraft[];
}

export interface LlmAssistResponse {
  success: boolean;
  taskType: LlmTaskType;
  result?: LlmRuleDraft | LlmSchemaDraft | unknown;
  rawContent?: string;
  warnings?: string[];
  errors?: string[];
}

export interface LlmCalculatorAssistRequest {
  taskType: 'CALCULATOR';
  userMessage: string;
  productId: number;
  versionNo: number;
  packageNo: string;
  currentCalculator: Calculator;
  providerCode?: string;
  model?: string;
}

export interface LlmCalculatorAssistResponse {
  success: boolean;
  calculator?: Calculator;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class LlmService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  private readonly llmHeaders = new HttpHeaders({ 'X-Skip-Global-Error': 'true' });

  assist(
    productId: number,
    versionNo: number,
    userMessage: string,
    taskType: LlmTaskType = 'RULE'
  ): Observable<LlmAssistResponse> {
    const url =
      `${this.auth.baseApiUrl}/admin/products/${productId}/versions/${versionNo}/llm/assist`;
    const body: LlmAssistRequest = {
      taskType,
      userMessage,
      productId,
      versionNo,
    };
    return this.http.post<LlmAssistResponse>(url, body, { headers: this.llmHeaders });
  }

  assistLob(
    lobCode: string,
    userMessage: string,
    taskType: LlmTaskType = 'RULE'
  ): Observable<LlmAssistResponse> {
    const url =
      `${this.auth.baseApiUrl}/admin/lobs/${encodeURIComponent(lobCode)}/llm/assist`;
    const body = {
      taskType,
      userMessage,
      lobCode,
    };
    return this.http.post<LlmAssistResponse>(url, body, { headers: this.llmHeaders });
  }

  assistCalculator(
    productId: number,
    versionNo: number,
    packageNo: string,
    calculator: Calculator
  ): Observable<LlmCalculatorAssistResponse> {
    const url =
      `${this.auth.baseApiUrl}/admin/calculators/products/${productId}/versions/${versionNo}/packages/${packageNo}/llm/assist`;
    const body: LlmCalculatorAssistRequest = {
      taskType: 'CALCULATOR',
      userMessage: (calculator.llmText || '').trim(),
      productId,
      versionNo,
      packageNo,
      currentCalculator: calculator,
    };
    return this.http.post<LlmCalculatorAssistResponse>(url, body, { headers: this.llmHeaders });
  }

  assistSchema(
    contractCode: string,
    userMessage: string,
    parentVarCode?: string
  ): Observable<LlmAssistResponse> {
    const url =
      `${this.auth.baseApiUrl}/admin/schema/${encodeURIComponent(contractCode)}/llm/assist`;
    const body = {
      taskType: 'SCHEMA' as const,
      userMessage,
      contractCode,
      ...(parentVarCode?.trim() ? { parentVarCode: parentVarCode.trim() } : {}),
    };
    return this.http.post<LlmAssistResponse>(url, body, { headers: this.llmHeaders });
  }
}
