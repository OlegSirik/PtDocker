import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Calculator } from '../calculator.service';
import { AuthService } from '../auth.service';

export type LlmTaskType = 'RULE' | 'COVER' | 'CALCULATOR';

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

export interface LlmAssistResponse {
  success: boolean;
  taskType: LlmTaskType;
  result?: LlmRuleDraft | unknown;
  rawContent?: string;
  warnings?: string[];
  errors?: string[];
}

export interface LlmCalculatorAssistRequest {
  userMessage: string;
  calculator: Calculator;
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
    return this.http.post<LlmAssistResponse>(url, body);
  }

  assistCalculator(
    productId: number,
    versionNo: number,
    packageNo: string,
    userMessage: string,
    calculator: Calculator
  ): Observable<LlmCalculatorAssistResponse> {
    const url =
      `${this.auth.baseApiUrl}/admin/calculators/products/${productId}/versions/${versionNo}/packages/${packageNo}/llm/assist`;
    const body: LlmCalculatorAssistRequest = {
      userMessage,
      calculator,
    };
    return this.http.post<LlmCalculatorAssistResponse>(url, body);
  }
}
