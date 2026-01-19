import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EnvService {
  get BASE_URL(): string {
    const w = (globalThis as any) as { __env?: { BASE_URL?: string } };
    const fromWindow = w && w.__env && typeof w.__env.BASE_URL === 'string' ? w.__env.BASE_URL : undefined;
    return fromWindow || window.location.origin;
  }

  get TENANT_HEADER(): string | undefined {
    const w = (globalThis as any) as { __env?: { TENANT_HEADER?: string } };
    return w && w.__env && typeof w.__env.TENANT_HEADER === 'string' ? w.__env.TENANT_HEADER : undefined;
  }

  get CLIENT_ID(): string | undefined {
    return 'SYS'; 
    // Это ID клиента это приложение
    // ToDo: get from file
  }

  get KEYCLOAK_URL(): string | undefined {
    const w = (globalThis as any) as { __env?: { KEYCLOAK_URL?: string } };
    return w && w.__env && typeof w.__env.KEYCLOAK_URL === 'string' ? w.__env.KEYCLOAK_URL : undefined;
  }

  get KEYCLOAK_REALM(): string | undefined {
    const w = (globalThis as any) as { __env?: { KEYCLOAK_REALM?: string } };
    return w && w.__env && typeof w.__env.KEYCLOAK_REALM === 'string' ? w.__env.KEYCLOAK_REALM : undefined;
  }

  get KEYCLOAK_CLIENT_ID(): string | undefined {
    const w = (globalThis as any) as { __env?: { KEYCLOAK_CLIENT_ID?: string } };
    return w && w.__env && typeof w.__env.KEYCLOAK_CLIENT_ID === 'string'
      ? w.__env.KEYCLOAK_CLIENT_ID
      : undefined;
  }
}

