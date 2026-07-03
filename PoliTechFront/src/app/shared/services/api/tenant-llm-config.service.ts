import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../auth.service';

export interface TenantLlmProviderConfigView {
  baseUrl?: string;
  defaultModel?: string;
  apiKeyMasked?: string;
  apiKeyConfigured?: boolean;
}

export interface TenantLlmConfigView {
  enabled: boolean;
  defaultProvider: string;
  defaultModel: string;
  timeoutMs: number;
  configured: boolean;
  providers: Record<string, TenantLlmProviderConfigView>;
}

export interface TenantLlmProviderConfigUpdate {
  baseUrl?: string;
  apiKey?: string;
  defaultModel?: string;
}

export interface TenantLlmConfigUpdateRequest {
  enabled?: boolean;
  defaultProvider?: string;
  defaultModel?: string;
  timeoutMs?: number;
  providers?: Record<string, TenantLlmProviderConfigUpdate>;
}

export interface TenantLlmApiKeyUpdateRequest {
  providerCode: string;
  apiKey: string;
}

export interface TenantLlmTestResponse {
  success: boolean;
  provider?: string;
  model?: string;
  latencyMs?: number;
}

@Injectable({ providedIn: 'root' })
export class TenantLlmConfigService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  private baseUrl(): string {
    return `${this.auth.baseApiUrl}/admin/tenant/llm-config`;
  }

  getConfig(): Observable<TenantLlmConfigView> {
    return this.http.get<TenantLlmConfigView>(this.baseUrl());
  }

  saveConfig(request: TenantLlmConfigUpdateRequest): Observable<TenantLlmConfigView> {
    return this.http.put<TenantLlmConfigView>(this.baseUrl(), request);
  }

  updateApiKey(request: TenantLlmApiKeyUpdateRequest): Observable<TenantLlmConfigView> {
    return this.http.put<TenantLlmConfigView>(`${this.baseUrl()}/api-key`, request);
  }

  testConnection(): Observable<TenantLlmTestResponse> {
    return this.http.post<TenantLlmTestResponse>(`${this.baseUrl()}/test`, {});
  }
}
