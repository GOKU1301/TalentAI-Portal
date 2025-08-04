import { HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const AuthInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const currentUser = authService.getCurrentUser();
  
  if (currentUser && currentUser.token) {
    // Clone the request and add the authorization header
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${currentUser.token}`
      }
    });
    
    console.log('Adding auth token to request:', req.url);
    return next(authReq);
  }
  
  return next(req);
};
