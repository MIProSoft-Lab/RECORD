import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService, RefreshRequest } from '../api';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from 'rxjs';

let isRefreshing = false;
const refreshTokenSubj = new BehaviorSubject<string | null>(null);

const addTokenHeader = (request: HttpRequest<any>, token: string) => {
  return request.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
};

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authApi = inject(AuthService);

  const accessToken = localStorage.getItem('access_token');

  let authReq = req;

  // Skip Authorization header for Cloudinary uploads (they use signature-based auth)
  const isCloudinaryUpload = req.url.includes('cloudinary.com');

  if (accessToken && !isCloudinaryUpload) {
    authReq = addTokenHeader(req, accessToken);
  }

  // El propio endpoint de refresco no debe reintentarse: si responde 401 (refresh token caducado o
  // inválido), su error debe propagarse al catchError de abajo, que limpia los tokens y redirige a
  // login. Si se interceptara aquí, la petición de refresco se quedaría esperando un token que nunca
  // llega y nunca se borrarían las credenciales.
  const isAuthEndpoint = req.url.includes('auth/login') || req.url.includes('auth/refresh');

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isAuthEndpoint) {
        const refreshToken = localStorage.getItem('refresh_token');

        if (!isRefreshing) {
          isRefreshing = true;
          refreshTokenSubj.next(null);

          if (refreshToken) {
            const refreshRequest: RefreshRequest = { token: refreshToken };

            return authApi.refresh(refreshRequest).pipe(
              switchMap((response) => {
                isRefreshing = false;
                localStorage.setItem('access_token', response.token);
                localStorage.setItem('refresh_token', response.refreshToken);

                refreshTokenSubj.next(response.token);

                return next(addTokenHeader(req, response.token));
              }),
              catchError((refreshError) => {
                isRefreshing = false;
                localStorage.removeItem('access_token');
                localStorage.removeItem('refresh_token');
                router.navigate(['login']).then();

                return throwError(() => refreshError);
              }),
            );
          }
        } else {
          return refreshTokenSubj.pipe(
            filter((token) => token !== null),
            take(1),
            switchMap((token) => {
              return next(addTokenHeader(req, token));
            }),
          );
        }
      }

      return throwError(() => error);
    }),
  );
};
