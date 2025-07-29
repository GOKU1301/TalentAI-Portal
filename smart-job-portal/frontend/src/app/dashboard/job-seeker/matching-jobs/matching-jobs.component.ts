import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { JobService, JobDTO } from '../job.service';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-matching-jobs',
  templateUrl: './matching-jobs.component.html',
  styleUrls: ['./matching-jobs.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule
  ]
})
export class MatchingJobsComponent implements OnInit {
  matchingJobs: JobDTO[] = [];
  isLoading = false;
  error = false;
  selectedJob: JobDTO | null = null;
  coverLetter = '';
  applyingToJobId: number | null = null;
  applicationSuccess = false;
  applicationError = false;
  isApplying = false;
  applicationErrorMessage = '';

  constructor(private jobService: JobService) { }

  ngOnInit(): void {
    this.loadMatchingJobs();
  }

  loadMatchingJobs(): void {
    this.isLoading = true;
    this.error = false;
    
    this.jobService.getMatchingJobs().subscribe({
      next: (jobs: JobDTO[]) => {
        console.log('Matching jobs loaded:', jobs);
        this.matchingJobs = jobs;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading matching jobs:', error);
        this.error = true;
        this.isLoading = false;
      }
    });
  }

  viewJobDetails(job: JobDTO): void {
    this.selectedJob = job;
    this.coverLetter = '';
    this.applicationSuccess = false;
    this.applicationError = false;
  }

  closeJobDetails(): void {
    this.selectedJob = null;
  }

  applyForJob(): void {
    if (!this.selectedJob) return;
    
    this.isApplying = true;
    
    this.jobService.applyForJob(this.selectedJob.id, this.coverLetter).subscribe({
      next: (response) => {
        console.log('Application submitted successfully:', response);
        this.applicationSuccess = true;
        this.applicationError = false;
        this.isApplying = false;
        
        // Reset cover letter
        this.coverLetter = '';
        
        // Update the job status to indicate it's been applied to
        if (this.selectedJob) {
          const jobIndex = this.matchingJobs.findIndex(job => job.id === this.selectedJob!.id);
          if (jobIndex !== -1) {
            this.matchingJobs[jobIndex].applied = true;
          }
        }
        
        // Close modal after a delay
        setTimeout(() => {
          this.closeJobDetails();
          this.applicationSuccess = false;
        }, 2000);
      },
      error: (error) => {
        console.error('Error submitting application:', error);
        this.applicationError = true;
        this.applicationSuccess = false;
        this.isApplying = false;
        
        // Check for specific error types
        if (error.status === 409) {
          this.applicationErrorMessage = 'You have already applied for this job.';
        } else if (error.status === 403) {
          this.applicationErrorMessage = 'You do not have permission to apply for this job.';
        } else {
          this.applicationErrorMessage = 'Failed to submit application. Please try again later.';
        }
      }
    });
  }

  // Helper method to format skills as an array
  formatSkills(skills: string): string[] {
    return skills ? skills.split(',').map(skill => skill.trim()) : [];
  }

  // Helper method to format date
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    });
  }
}
