import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JobResponse } from './job.service';
import { ConfigService } from './config.service';

export enum ApplicationStatus {
  PENDING = 'PENDING',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED'
}

export interface ApplicationRequest {
  coverLetter: string;
}

export interface StatusUpdateRequest {
  status: ApplicationStatus;
}

export interface ApplicationResponse {
  id: number;
  job: JobResponse;
  status: string;
  coverLetter: string;
  matchPercentage: number;
  appliedDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private get apiUrl(): string {
    return this.configService.getApiEndpoint('api/applications');
  }

  constructor(
    private http: HttpClient,
    private configService: ConfigService
  ) { }

  applyForJob(jobId: number, request: ApplicationRequest): Observable<ApplicationResponse> {
    return this.http.post<ApplicationResponse>(`${this.apiUrl}/apply/${jobId}`, request);
  }

  getMyApplications(): Observable<ApplicationResponse[]> {
    return this.http.get<ApplicationResponse[]>(`${this.apiUrl}/my-applications`);
  }

  getJobApplications(jobId: number): Observable<ApplicationResponse[]> {
    return this.http.get<ApplicationResponse[]>(`${this.apiUrl}/job/${jobId}`);
  }

  updateApplicationStatus(applicationId: number, request: StatusUpdateRequest): Observable<ApplicationResponse> {
    return this.http.put<ApplicationResponse>(`${this.apiUrl}/${applicationId}/status`, request);
  }
}
