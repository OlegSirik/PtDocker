export interface ApiErrorBody {
  code?: number;
  message?: string;
  errors?: Array<{
    domain?: string;
    reason?: string;
    message?: string;
    field?: string;
  }>;
}

export function isLlmNotConfigured(err: unknown): boolean {
  return hasLlmReason(err, 'NOT_CONFIGURED');
}

export function isLlmDisabled(err: unknown): boolean {
  return hasLlmReason(err, 'DISABLED');
}

export function resolveLlmErrorMessage(
  err: unknown,
  fallback = 'Ошибка вызова LLM'
): string {
  const body = getErrorBody(err);
  if (isLlmNotConfigured(err)) {
    return (
      body?.message ||
      'LLM недоступен. Настройте интеграцию в параметрах тенанта (вкладка LLM).'
    );
  }
  if (isLlmDisabled(err)) {
    return body?.message || 'LLM отключён для этого тенанта.';
  }
  return body?.message || (err as Error)?.message || fallback;
}

function hasLlmReason(err: unknown, reason: string): boolean {
  const body = getErrorBody(err);
  return body?.errors?.some((e) => e.domain === 'LLM' && e.reason === reason) ?? false;
}

function getErrorBody(err: unknown): ApiErrorBody | undefined {
  return (err as { error?: ApiErrorBody })?.error;
}
