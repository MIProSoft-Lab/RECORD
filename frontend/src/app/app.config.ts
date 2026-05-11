import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { providePrimeNG } from 'primeng/config';
import { definePreset } from '@primeuix/themes';
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
    const darkMode = localStorage.getItem('dark_mode');
    if (darkMode === 'true') {
      document.documentElement.classList.add('dark');
    }
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

const UIBPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#e6f2f9',
      100: '#c2d9ed',
      200: '#9bc0df',
      300: '#72a7d0',
      400: '#558ec5',
      500: '#0065bd',
      600: '#0059a6',
      700: '#004b8d',
      800: '#003d74',
      900: '#002d59',
      950: '#001d3a',
      DEFAULT: '#0065bd',
    },
    colorScheme: {
      light: {
        primary: {
          color: '#0065bd',
          contrastColor: '#ffffff',
          hoverColor: '#0059a6',
          activeColor: '#004b8d',
        },
        focusRing: {
          color: '#0065bd',
        },
      },
      dark: {
        primary: {
          color: '#0065bd',
          contrastColor: '#ffffff',
          hoverColor: '#4d9de0',
          activeColor: '#72a7d0',
        },
        focusRing: {
          color: '#0065bd',
        },
      },
    },
  },
});

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
        preset: UIBPreset,
        options: {
          darkModeSelector: '.dark',
        },
      },
    }),
    { provide: BASE_PATH, useValue: environment.apiUrl },
  ],
};
