import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }
  
  return true;
};

export const jobSeekerGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const currentUser = authService.getCurrentUser();
  
  if (!authService.isLoggedIn() || !currentUser) {
    router.navigate(['/login']);
    return false;
  }
  
  if (currentUser.role !== 'JOBSEEKER') {
    // Redirect to appropriate dashboard based on role
    if (currentUser.role === 'RECRUITER') {
      router.navigate(['/recruiter-dashboard']);
    } else {
      router.navigate(['/login']);
    }
    return false;
  }
  
  return true;
};
