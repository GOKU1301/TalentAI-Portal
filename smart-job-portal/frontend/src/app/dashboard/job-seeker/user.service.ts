import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { ConfigService } from '../../services/config.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private configService: ConfigService
  ) {
    this.apiUrl = this.configService.getApiEndpoint('api/users');
  }

  // Get the HTTP options with authorization header
  private getHttpOptions() {
    const currentUser = this.authService.getCurrentUser();
    const token = currentUser?.token;
    
    console.log('Current user:', currentUser);
    console.log('Using token for API call:', token);
    
    if (!token) {
      console.error('No authentication token found');
    }
    
    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }),
      withCredentials: true // Add this to ensure cookies are sent with the request
    };
  }

  // Get current user's profile
  getUserProfile(): Observable<any> {
    const url = `${this.apiUrl}/profile`;
    const options = this.getHttpOptions();
    console.log('Making API call to:', url);
    console.log('With headers:', options.headers);
    return this.http.get(url, options);
  }

  // Update current user's profile
  updateUserProfile(profileData: any): Observable<any> {
    // Ensure we don't send undefined or null values to the API
    const sanitizedData = {
      fullName: profileData.fullName || '',
      email: profileData.email,
      education: profileData.education || '',
      experience: profileData.experience !== undefined ? profileData.experience : null,
      position: profileData.position || ''
    };
    
    return this.http.put(`${this.apiUrl}/profile`, sanitizedData, this.getHttpOptions());
  }

  // Update job seeker's skills
  updateUserSkills(skills: string): Observable<any> {
    // Ensure we don't send undefined or null values to the API
    const sanitizedSkills = skills || '';
    const payload = { skills: sanitizedSkills };
    const url = `${this.apiUrl}/skills`;
    const options = this.getHttpOptions();
    
    console.log('Updating skills with payload:', payload);
    console.log('API URL:', url);
    console.log('Headers:', options.headers);
    
    return this.http.put(url, payload, options);
  }
}
