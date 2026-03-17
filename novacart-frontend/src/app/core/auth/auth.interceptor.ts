import { HttpBackend, HttpClient, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { TokenStorageService } from './token-storage.service';
import { environment } from '../../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);
  const backend = inject(HttpBackend);
  const rawHttp = new HttpClient(backend);

  const token = tokenStorage.accessToken;
  const authReq = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      const refreshToken = tokenStorage.refreshToken;
      if (err.status !== 401 || req.url.includes('/auth/') || !refreshToken) {
        return throwError(() => err);
      }
      return rawHttp.post<{ accessToken: string; refreshToken: string }>(
        `${environment.apiUrl}/auth/refresh`, { refreshToken }
      ).pipe(
        switchMap(r => {
          tokenStorage.setTokens(r.accessToken, r.refreshToken);
          return next(req.clone({ setHeaders: { Authorization: `Bearer ${r.accessToken}` } }));
        }),
        catchError(e => { tokenStorage.clear(); return throwError(() => e); })
      );
    })
  );
};