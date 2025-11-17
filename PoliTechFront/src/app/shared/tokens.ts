import { InjectionToken } from '@angular/core';

export const BASE_URL = new InjectionToken<string>('BASE_URL', {
  providedIn: 'root',
  factory: () => {
    const w = (globalThis as any) as { __env?: { BASE_URL?: string } };
    const fromWindow = w && w.__env && typeof w.__env.BASE_URL === 'string' ? w.__env.BASE_URL : undefined;
    return 'http://185.173.94.122:8080'
    // TODO разобраться как работает
    // return fromWindow || 'http://localhost:8080';
  }
});


