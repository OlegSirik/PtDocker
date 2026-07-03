import { Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {
  TenantLlmConfigService,
  TenantLlmConfigUpdateRequest,
} from '../../../shared/services/api/tenant-llm-config.service';
import { AuthService } from '../../../shared/services/auth.service';
import { resolveLlmErrorMessage } from '../../../shared/utils/llm-error.util';

const PROVIDERS = [
  { code: 'routerai', label: 'RouterAI', defaultBaseUrl: 'https://routerai.ru/api/v1' },
  { code: 'deepseek', label: 'DeepSeek', defaultBaseUrl: 'https://api.deepseek.com' },
] as const;

const DEFAULT_MODEL = 'deepseek/deepseek-v4-flash';

@Component({
  selector: 'app-tenant-llm-config',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './tenant-llm-config.component.html',
  styleUrls: ['./tenant-llm-config.component.scss'],
})
export class TenantLlmConfigComponent implements OnChanges {
  @Input({ required: true }) tenantCode!: string;

  private configService = inject(TenantLlmConfigService);
  private auth = inject(AuthService);
  private snack = inject(MatSnackBar);

  readonly providers = PROVIDERS;

  loading = false;
  saving = false;
  testing = false;
  configured = false;

  enabled = false;
  defaultProvider = 'routerai';
  defaultModel = DEFAULT_MODEL;
  timeoutMs = 120000;

  providerBaseUrl = '';
  providerDefaultModel = '';
  apiKeyInput = '';
  apiKeyMasked: string | null = null;
  apiKeyConfigured = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['tenantCode'] && this.tenantCode) {
      this.auth.tenant = this.tenantCode;
      this.loadConfig();
    }
  }

  loadConfig(): void {
    this.loading = true;
    this.configService.getConfig().subscribe({
      next: (view) => {
        this.loading = false;
        this.applyView(view);
      },
      error: () => {
        this.loading = false;
        this.snack.open('Не удалось загрузить настройки LLM', 'OK', { duration: 3000 });
      },
    });
  }

  private applyView(view: {
    enabled: boolean;
    defaultProvider: string;
    defaultModel: string;
    timeoutMs: number;
    configured: boolean;
    providers: Record<string, { baseUrl?: string; defaultModel?: string; apiKeyMasked?: string; apiKeyConfigured?: boolean }>;
  }): void {
    this.configured = view.configured;
    this.enabled = view.enabled;
    this.defaultProvider = view.defaultProvider || 'routerai';
    this.defaultModel = view.defaultModel || DEFAULT_MODEL;
    this.timeoutMs = view.timeoutMs || 120000;
    this.apiKeyInput = '';

    const providerView = view.providers?.[this.defaultProvider];
    this.providerBaseUrl =
      providerView?.baseUrl || this.defaultBaseUrl(this.defaultProvider);
    this.providerDefaultModel =
      providerView?.defaultModel || this.defaultModel;
    this.apiKeyMasked = providerView?.apiKeyMasked ?? null;
    this.apiKeyConfigured = providerView?.apiKeyConfigured ?? false;
  }

  onProviderChange(): void {
    this.providerBaseUrl = this.defaultBaseUrl(this.defaultProvider);
    this.providerDefaultModel =
      this.defaultProvider === 'deepseek' ? 'deepseek-chat' : this.defaultModel;
    this.apiKeyInput = '';

    if (!this.configured) {
      this.apiKeyMasked = null;
      this.apiKeyConfigured = false;
      return;
    }

    this.configService.getConfig().subscribe({
      next: (view) => {
        const pv = view.providers?.[this.defaultProvider];
        if (!pv) {
          this.apiKeyMasked = null;
          this.apiKeyConfigured = false;
          return;
        }
        this.providerBaseUrl = pv.baseUrl || this.defaultBaseUrl(this.defaultProvider);
        this.providerDefaultModel = pv.defaultModel || this.defaultModel;
        this.apiKeyMasked = pv.apiKeyMasked ?? null;
        this.apiKeyConfigured = pv.apiKeyConfigured ?? false;
      },
    });
  }

  save(): void {
    const request: TenantLlmConfigUpdateRequest = {
      enabled: this.enabled,
      defaultProvider: this.defaultProvider,
      defaultModel: this.defaultModel,
      timeoutMs: this.timeoutMs,
      providers: {
        [this.defaultProvider]: {
          baseUrl: this.providerBaseUrl.trim() || undefined,
          defaultModel: this.providerDefaultModel.trim() || undefined,
          ...(this.apiKeyInput.trim() ? { apiKey: this.apiKeyInput.trim() } : {}),
        },
      },
    };

    this.saving = true;
    this.configService.saveConfig(request).subscribe({
      next: (view) => {
        this.saving = false;
        this.applyView(view);
        this.snack.open('Настройки LLM сохранены', 'OK', { duration: 2500 });
      },
      error: (err) => {
        this.saving = false;
        this.snack.open(resolveLlmErrorMessage(err, 'Ошибка сохранения'), 'OK', {
          duration: 4000,
        });
      },
    });
  }

  testConnection(): void {
    this.testing = true;
    this.configService.testConnection().subscribe({
      next: (result) => {
        this.testing = false;
        const latency = result.latencyMs != null ? ` (${result.latencyMs} мс)` : '';
        this.snack.open(`Подключение успешно${latency}`, 'OK', { duration: 3500 });
      },
      error: (err) => {
        this.testing = false;
        this.snack.open(resolveLlmErrorMessage(err, 'Ошибка проверки подключения'), 'OK', {
          duration: 4500,
        });
      },
    });
  }

  private defaultBaseUrl(providerCode: string): string {
    return PROVIDERS.find((p) => p.code === providerCode)?.defaultBaseUrl ?? '';
  }
}
