import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApplicationService, ApplicationResponse, ApplicationStatus, StatusUpdateRequest } from '../../../services/application.service';
import { JobService, JobResponse } from '../../../services/job.service';

@Component({
  selector: 'app-manage-applications',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './manage-applications.component.html',
  styleUrl: './manage-applications.component.scss'
})
export class ManageApplicationsComponent implements OnInit {
  jobId: number | null = null;
  job: JobResponse | null = null;
  applications: ApplicationResponse[] = [];
  loading = false;
  error = '';
  updateSuccess = '';
  applicationStatus = ApplicationStatus; // For template access

  constructor(
    private route: ActivatedRoute,
    private applicationService: ApplicationService,
    private jobService: JobService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('jobId');
      if (id) {
        this.jobId = +id;
        this.loadJobDetails();
        this.loadApplications();
      } else {
        this.error = 'Job ID not found in route';
      }
    });
  }

  loadJobDetails(): void {
    if (!this.jobId) return;
    
    this.jobService.getJobById(this.jobId).subscribe({
      next: (job) => {
        this.job = job;
      },
      error: (err) => {
        this.error = 'Failed to load job details';
        console.error('Error loading job:', err);
      }
    });
  }

  loadApplications(): void {
    if (!this.jobId) return;
    
    this.loading = true;
    this.error = '';
    
    this.applicationService.getJobApplications(this.jobId).subscribe({
      next: (applications) => {
        this.applications = applications;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load applications. Please try again.';
        this.loading = false;
        console.error('Error loading applications:', err);
      }
    });
  }

  updateStatus(application: ApplicationResponse, status: ApplicationStatus): void {
    this.updateSuccess = '';
    this.error = '';
    
    const request: StatusUpdateRequest = {
      status: status
    };
    
    this.applicationService.updateApplicationStatus(application.id, request).subscribe({
      next: (updatedApplication) => {
        // Update the application in the list
        const index = this.applications.findIndex(app => app.id === updatedApplication.id);
        if (index !== -1) {
          this.applications[index] = updatedApplication;
        }
        
        this.updateSuccess = `Application status updated to ${updatedApplication.status}`;
        
        // Clear success message after 3 seconds
        setTimeout(() => {
          this.updateSuccess = '';
        }, 3000);
      },
      error: (err) => {
        this.error = 'Failed to update application status';
        console.error('Error updating application status:', err);
      }
    });
  }
}
