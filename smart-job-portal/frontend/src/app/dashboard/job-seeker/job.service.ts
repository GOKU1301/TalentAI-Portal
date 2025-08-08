import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { ConfigService } from '../../services/config.service';

export interface JobDTO {
  id: number;
  title: string;
  description: string;
  skills: string;
  location: string;
  salary: string;
  employmentType: string;
  experienceLevel: string;
  company: string;
  postedDate: string;
  postedById?: number;
  postedByName?: string;
  matchPercentage?: number;
  applied?: boolean;
}

export interface ApplicationDTO {
  id: number;
  job: JobDTO;
  status: string;
  coverLetter?: string;
  matchPercentage?: number;
  appliedDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class JobService {
  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private configService: ConfigService
  ) {
    this.apiUrl = this.configService.getApiEndpoint('api');
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
      withCredentials: true
    };
  }

  // Get jobs matching the current user's skills
  getMatchingJobs(): Observable<JobDTO[]> {
    const url = `${this.apiUrl}/jobs/matching`;
    const options = this.getHttpOptions();
    console.log('Making API call to:', url);
    console.log('With headers:', options.headers);
    return this.http.get<JobDTO[]>(url, options);
  }

  // Get current user's job applications
  getMyApplications(): Observable<ApplicationDTO[]> {
    const url = `${this.apiUrl}/applications/my-applications`;
    const options = this.getHttpOptions();
    console.log('Making API call to:', url);
    console.log('With headers:', options.headers);
    return this.http.get<ApplicationDTO[]>(url, options);
  }

  // Apply for a job
  applyForJob(jobId: number, coverLetter: string = ''): Observable<any> {
    const url = `${this.apiUrl}/applications/apply/${jobId}`;
    const payload = { coverLetter: coverLetter };
    const options = this.getHttpOptions();
    console.log('Applying for job:', jobId);
    console.log('With cover letter:', coverLetter ? 'Yes' : 'No');
    return this.http.post(url, payload, options);
  }
}
