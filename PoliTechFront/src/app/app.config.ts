import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import {provideHttpClient, withInterceptors} from '@angular/common/http';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { BASE_URL } from './shared/tokens';
import { FormlyModule } from '@ngx-formly/core';
import {
  AutoRefreshTokenService,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  includeBearerTokenInterceptor, provideKeycloak, UserActivityService, withAutoRefreshToken
} from 'keycloak-angular';
import {keycloakConfig, keycloakInitOptions, urlCondition} from './keycloak.config';
import {authInterceptor} from './shared/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    /*provideKeycloak({
      config: keycloakConfig,
      initOptions: keycloakInitOptions,
      features: [
        withAutoRefreshToken({
          onInactivityTimeout: 'logout',
          sessionTimeout: 60000
        })
      ],
      providers: [
        AutoRefreshTokenService,
        UserActivityService,
        {
          provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
          useValue: [urlCondition]
        },
      ]
    }), */ // todo: keycloak auth

    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideAnimationsAsync(),
    //provideHttpClient(withInterceptors([includeBearerTokenInterceptor])), // todo: keycloak auth
    provideHttpClient(withInterceptors([authInterceptor])), // todo: rest auth
    FormlyModule.forRoot({
      validationMessages: [
        { name: 'required', message: 'This field is required' },
        { name: 'email', message: 'Please enter a valid email address' }
      ]
    }).providers || []
  ]
};
