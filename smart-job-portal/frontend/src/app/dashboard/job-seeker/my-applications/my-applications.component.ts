import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { JobService, ApplicationDTO } from '../job.service';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-my-applications',
  templateUrl: './my-applications.component.html',
  styleUrls: ['./my-applications.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    HttpClientModule
  ]
})
export class MyApplicationsComponent implements OnInit {
  applications: ApplicationDTO[] = [];
  isLoading = false;
  error = false;
  selectedApplication: ApplicationDTO | null = null;

  constructor(private jobService: JobService) { }

  ngOnInit(): void {
    this.loadMyApplications();
  }

  loadMyApplications(): void {
    this.isLoading = true;
    this.error = false;
    
    this.jobService.getMyApplications().subscribe({
      next: (applications: ApplicationDTO[]) => {
        console.log('Applications loaded:', applications);
        this.applications = applications;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading applications:', error);
        this.error = true;
        this.isLoading = false;
      }
    });
  }

  viewApplicationDetails(application: ApplicationDTO): void {
    this.selectedApplication = application;
  }

  closeApplicationDetails(): void {
    this.selectedApplication = null;
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

  // Helper method to get status badge class
  getStatusBadgeClass(status: string): string {
    switch (status.toUpperCase()) {
      case 'PENDING':
        return 'status-pending';
      case 'REVIEWED':
        return 'status-reviewed';
      case 'ACCEPTED':
        return 'status-accepted';
      case 'REJECTED':
        return 'status-rejected';
      default:
        return 'status-pending';
    }
  }
}
