import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { BASE_PATH } from '@core/api';
import { environment } from '../environments/environment';
import { authInterceptor } from '@core/interceptors/auth.interceptor';
import { UserState } from '@core/services/user-state';
import { Observable, of } from 'rxjs';
import { provideTranslateService, TranslateLoader, TranslationObject } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';

export function initializeApp() {
  return () => {
    const userState = inject(UserState);
    const token = localStorage.getItem('access_token');
    if (token) {
      return userState.loadCurrentUser();
    }
    return of(null);
  };
}

export class CustomTranslateLoader implements TranslateLoader {
  constructor(private http: HttpClient) {}

  getTranslation(lang: string): Observable<TranslationObject> {
    return this.http.get<TranslationObject>(`./i18n/${lang}.json`);
  }
}

export function HttpLoaderFactory(http: HttpClient) {
  return new CustomTranslateLoader(http);
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAppInitializer(initializeApp()),
    provideAnimationsAsync(),
    MessageService,
    provideTranslateService({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient],
      }
    }),
    providePrimeNG({
      ripple: true,
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: 'system',
        },
      },
    }),
    { provide: BASE_PATH, useValue: environment.apiUrl },
  ],
};
