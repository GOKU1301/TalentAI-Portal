import { Routes } from '@angular/router';
import { authGuard, jobSeekerGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./auth/register/register.component').then(m => m.RegisterComponent) },
  { 
    path: 'job-seeker-dashboard', 
    loadComponent: () => import('./dashboard/job-seeker/job-seeker-dashboard.component').then(m => m.JobSeekerDashboardComponent),
    canActivate: [jobSeekerGuard]
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];
