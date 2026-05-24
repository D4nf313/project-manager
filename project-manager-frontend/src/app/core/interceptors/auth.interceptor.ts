import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../../auth/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // No interceptar el login ni la selección de workspace
  // (el workspace-selector agrega el header manualmente con el tempToken)
  const isAuthEndpoint =
    req.url.includes('/auth/login') ||
    req.url.includes('/auth/token');

  if (token && !isAuthEndpoint) {
    const cloned = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(cloned);
  }

  return next(req);
};
