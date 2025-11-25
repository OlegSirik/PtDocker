import {KeycloakConfig, KeycloakInitOptions} from 'keycloak-js';
import {createInterceptorCondition, IncludeBearerTokenCondition} from 'keycloak-angular';

export const keycloakConfig: KeycloakConfig = {
  url: 'http://localhost:8000',
  realm: 'keycloak-angular-sandbox',
  clientId: 'keycloak-angular',
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
