import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, RegisterRequest } from '../auth.service';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  registerRequest: RegisterRequest = {
    username: '',
    email: '',
    password: '',
    role: 'JOBSEEKER',
    fullName: ''
  };
  confirmPassword = '';
  errorMessage = '';
  isLoading = false;
  registerSuccess = false;
  successMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.registerSuccess = false;
    this.successMessage = '';
    
    // Check if passwords match
    if (this.registerRequest.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      this.isLoading = false;
      return;
    }

    this.authService.register(this.registerRequest).subscribe({
      next: () => {
        this.isLoading = false;
        this.registerSuccess = true;
        this.successMessage = 'Registration successful! You can now login with your credentials.';
        // Not redirecting to login as per user's request
        // this.router.navigate(['/login']);
      },
      error: (error) => {
        this.isLoading = false;
        // Handle different types of error responses
        if (error.error instanceof Error) {
          // A client-side or network error occurred
          this.errorMessage = `Network error: ${error.error.message}`;
        } else if (typeof error.error === 'string') {
          // Backend returned a string error message
          this.errorMessage = error.error || 'Registration failed. Please try again.';
        } else {
          // Backend returned an error object
          this.errorMessage = error.error?.message || 'Registration failed. Please try again.';
        }
        console.error('Registration error:', error);
      }
    });
  }
}
