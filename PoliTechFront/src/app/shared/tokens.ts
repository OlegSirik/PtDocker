import { InjectionToken } from '@angular/core';

export const BASE_URL = new InjectionToken<string>('BASE_URL', {
  providedIn: 'root',
  factory: () => {
    const w = (globalThis as any) as { __env?: { BASE_URL?: string } };
    const fromWindow = w && w.__env && typeof w.__env.BASE_URL === 'string' ? w.__env.BASE_URL : undefined;
    return fromWindow || 'http://localhost:8080';
  }
});


