import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface JobRequest {
  title: string;
  description: string;
  skills: string;
  company: string;
  location: string;
  salary: number;
  employmentType: string;
  experienceLevel: string;
}

export interface JobResponse {
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
  postedById: number;
  postedByName: string;
  matchPercentage?: number;
  applied?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class JobService {
  private apiUrl = `${environment.apiUrl}/api/jobs`;

  constructor(private http: HttpClient) { }

  getAllJobs(): Observable<JobResponse[]> {
    return this.http.get<JobResponse[]>(this.apiUrl);
  }

  getJobById(id: number): Observable<JobResponse> {
    return this.http.get<JobResponse>(`${this.apiUrl}/${id}`);
  }

  createJob(jobRequest: JobRequest): Observable<JobResponse> {
    return this.http.post<JobResponse>(this.apiUrl, jobRequest);
  }

  searchJobs(keyword: string): Observable<JobResponse[]> {
    return this.http.get<JobResponse[]>(`${this.apiUrl}/search?keyword=${keyword}`);
  }

  getMatchingJobs(): Observable<JobResponse[]> {
    return this.http.get<JobResponse[]>(`${this.apiUrl}/matching`);
  }

  getMyPostedJobs(): Observable<JobResponse[]> {
    return this.http.get<JobResponse[]>(`${this.apiUrl}/my-posted-jobs`);
  }
}
