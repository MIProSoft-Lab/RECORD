import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

function isTokenExpired(token: string): boolean {
  try {
    const payload = token.split('.')[1];
    const decoded = JSON.parse(atob(payload));
    const expirationDate = decoded.exp * 1000;
    return Date.now() > expirationDate;
  } catch (e) {
    return true;
  }
}

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const accessToken = localStorage.getItem('access_token');
  const refreshToken = localStorage.getItem('refresh_token');

  if (!accessToken || !refreshToken) {
    return router.createUrlTree(['/login']);
  }

  if (isTokenExpired(accessToken)) {
    if (isTokenExpired(refreshToken)) {
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      return router.createUrlTree(['/login']);
    }

    return true;
  }

  return true;
}
