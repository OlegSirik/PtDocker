import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
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
}

export interface LlmAssistResponse {
  success: boolean;
  taskType: LlmTaskType;
  result?: LlmRuleDraft | unknown;
  rawContent?: string;
  warnings?: string[];
  errors?: string[];
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
}
