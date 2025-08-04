import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { JobService, JobResponse } from '../../../services/job.service';

@Component({
  selector: 'app-recruiter-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './recruiter-dashboard.component.html',
  styleUrl: './recruiter-dashboard.component.scss'
})
export class RecruiterDashboardComponent implements OnInit {
  jobs: JobResponse[] = [];
  loading = false;
  error = '';

  constructor(private jobService: JobService) {}

  ngOnInit(): void {
    this.loadJobs();
  }

  loadJobs(): void {
    this.loading = true;
    this.error = '';
    
    this.jobService.getMyPostedJobs().subscribe({
      next: (jobs) => {
        this.jobs = jobs;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load jobs. Please try again.';
        this.loading = false;
        console.error('Error loading jobs:', err);
      }
    });
  }
}
