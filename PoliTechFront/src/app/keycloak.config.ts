import { KeycloakConfig, KeycloakInitOptions } from 'keycloak-js';
import { createInterceptorCondition, IncludeBearerTokenCondition } from 'keycloak-angular';

const env = (globalThis as any) as {
  __env?: { KEYCLOAK_URL?: string; KEYCLOAK_REALM?: string; KEYCLOAK_CLIENT_ID?: string };
};

export const keycloakConfig: KeycloakConfig = {
  url: env.__env?.KEYCLOAK_URL || window.location.origin,
  realm: env.__env?.KEYCLOAK_REALM || '',
  clientId: env.__env?.KEYCLOAK_CLIENT_ID || '',
};

export const keycloakInitOptions: KeycloakInitOptions = {
  onLoad: 'check-sso',
  silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
  checkLoginIframe: false
};

export const urlCondition = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: /^.*$/i,
  bearerPrefix: 'Bearer'
});
