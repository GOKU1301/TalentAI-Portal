import { Routes } from '@angular/router';
import { authGuard, jobSeekerGuard, recruiterGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./auth/register/register.component').then(m => m.RegisterComponent) },
  { 
    path: 'job-seeker-dashboard', 
    loadComponent: () => import('./dashboard/job-seeker/job-seeker-dashboard.component').then(m => m.JobSeekerDashboardComponent),
    canActivate: [jobSeekerGuard]
  },
  {
    path: 'dashboard/recruiter',
    loadComponent: () => import('./dashboard/recruiter/recruiter-dashboard/recruiter-dashboard.component').then(m => m.RecruiterDashboardComponent),
    canActivate: [recruiterGuard]
  },
  {
    path: 'dashboard/recruiter/create-job',
    loadComponent: () => import('./dashboard/recruiter/create-job/create-job.component').then(m => m.CreateJobComponent),
    canActivate: [recruiterGuard]
  },
  {
    path: 'dashboard/recruiter/manage-applications/:jobId',
    loadComponent: () => import('./dashboard/recruiter/manage-applications/manage-applications.component').then(m => m.ManageApplicationsComponent),
    canActivate: [recruiterGuard]
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];
