import { HttpBackend, HttpClient, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenStorageService } from './token-storage.service';
import { catchError, switchMap, throwError } from 'rxjs';

const AUTH_URL_FRAGMENT = '/api/v1/auth/';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);
  const backend = inject(HttpBackend);
  const rawHttp = new HttpClient(backend);

  const accessToken = tokenStorage.accessToken;
  const authReq = accessToken
    ? req.clone({ setHeaders: { Authorization: `Bearer ${accessToken}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const isUnauthorized = error.status === 401;
      const isAuthEndpoint = req.url.includes(AUTH_URL_FRAGMENT);
      const refreshToken = tokenStorage.refreshToken;

      if (!isUnauthorized || isAuthEndpoint || !refreshToken) {
        return throwError(() => error);
      }

      return rawHttp
        .post<{ accessToken: string; refreshToken: string }>('http://localhost:8811/api/v1/auth/refresh', { refreshToken })
        .pipe(
          switchMap((response) => {
            tokenStorage.setTokens(response.accessToken, response.refreshToken);
            const retryReq = req.clone({ setHeaders: { Authorization: `Bearer ${response.accessToken}` } });
            return next(retryReq);
          }),
          catchError((refreshError) => {
            tokenStorage.clear();
            return throwError(() => refreshError);
          })
        );
    })
  );
};
