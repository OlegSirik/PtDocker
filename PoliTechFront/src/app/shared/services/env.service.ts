import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EnvService {
  get BASE_URL(): string {
    const w = (globalThis as any) as { __env?: { BASE_URL?: string } };
    const fromWindow = w && w.__env && typeof w.__env.BASE_URL === 'string' ? w.__env.BASE_URL : undefined;
    return fromWindow || 'http://localhost:8080';
  }

  get TENANT_HEADER(): string | undefined {
    const w = (globalThis as any) as { __env?: { TENANT_HEADER?: string } };
    return w && w.__env && typeof w.__env.TENANT_HEADER === 'string' ? w.__env.TENANT_HEADER : undefined;
  }
}

