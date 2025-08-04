import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, LoginRequest } from '../auth.service';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  loginRequest: LoginRequest = {
    username: '',
    password: ''
  };
  errorMessage = '';
  isLoading = false;
  loginSuccess = false;
  successMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.loginSuccess = false;
    this.successMessage = '';
    
    this.authService.login(this.loginRequest).subscribe({
      next: (response) => {
        this.isLoading = false;
        console.log('Login successful', response);
        this.loginSuccess = true;
        this.successMessage = 'Login successful! Welcome back.';
        
        // Implement role-based redirection
        const currentUser = this.authService.getCurrentUser();
        if (currentUser && currentUser.role) {
          if (currentUser.role === 'JOBSEEKER') {
            setTimeout(() => {
              this.router.navigate(['/job-seeker-dashboard']);
            }, 1000); // Short delay to show success message
          } else if (currentUser.role === 'RECRUITER' || currentUser.role === 'ADMIN') {
            setTimeout(() => {
              this.router.navigate(['/dashboard/recruiter']);
            }, 1000); // Short delay to show success message
          }
        }
      },
      error: (error) => {
        this.isLoading = false;
        // Handle different types of error responses
        if (error.error instanceof Error) {
          // A client-side or network error occurred
          this.errorMessage = `Network error: ${error.error.message}`;
        } else if (typeof error.error === 'string') {
          // Backend returned a string error message
          this.errorMessage = error.error || 'Login failed. Please check your credentials.';
        } else {
          // Backend returned an error object
          this.errorMessage = error.error?.message || 'Login failed. Please check your credentials.';
        }
        console.error('Login error:', error);
      }
    });
  }
}
