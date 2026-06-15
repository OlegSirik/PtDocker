import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

export interface RefDictsSelectResult {
  action: 'ok' | 'cancel';
  dictCode: string;
  selected: string;
}

const SESSION_KEY_PREFIX = 'refdicts:selected:';

@Injectable({ providedIn: 'root' })
export class RefDictsNavigatorService {
  private router = inject(Router);
  private auth = inject(AuthService);

  static readonly RESULT_STATE_KEY = 'refdictsSelectResult';

  openSelect(dictCode: string, selected: string, returnUrl: string): void {
    const tenantCode = this.auth.tenant || '';
    const normalized = (selected || '').trim();
    if (normalized) {
      sessionStorage.setItem(this.sessionKey(dictCode), normalized);
    }
    this.router.navigate(['/', tenantCode, 'admin', 'refdicts', 'select'], {
      queryParams: {
        dictCode,
        selected: normalized,
        returnUrl,
      },
    });
  }

  parseResult(state: unknown): RefDictsSelectResult | null {
    if (!state || typeof state !== 'object') {
      return null;
    }
    const result = state as Partial<RefDictsSelectResult>;
    if (result.action !== 'ok' && result.action !== 'cancel') {
      return null;
    }
    if (!result.dictCode) {
      return null;
    }
    return {
      action: result.action,
      dictCode: result.dictCode,
      selected: result.selected ?? '',
    };
  }

  getSessionSelected(dictCode: string): string {
    return sessionStorage.getItem(this.sessionKey(dictCode)) ?? '';
  }

  setSessionSelected(dictCode: string, selected: string): void {
    sessionStorage.setItem(this.sessionKey(dictCode), selected);
  }

  private sessionKey(dictCode: string): string {
    return `${SESSION_KEY_PREFIX}${dictCode}`;
  }
}
