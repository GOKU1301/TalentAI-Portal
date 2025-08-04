import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { JobService, JobRequest } from '../../../services/job.service';

@Component({
  selector: 'app-create-job',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './create-job.component.html',
  styleUrl: './create-job.component.scss'
})
export class CreateJobComponent {
  jobForm: FormGroup;
  loading = false;
  error = '';
  success = false;
  
  employmentTypes = [
    { value: 'FULL_TIME', label: 'Full Time' },
    { value: 'PART_TIME', label: 'Part Time' },
    { value: 'CONTRACT', label: 'Contract' },
    { value: 'INTERNSHIP', label: 'Internship' },
    { value: 'FREELANCE', label: 'Freelance' }
  ];
  
  experienceLevels = [
    { value: 'ENTRY', label: 'Entry Level' },
    { value: 'JUNIOR', label: 'Junior' },
    { value: 'MID', label: 'Mid Level' },
    { value: 'SENIOR', label: 'Senior' },
    { value: 'LEAD', label: 'Lead' },
    { value: 'MANAGER', label: 'Manager' }
  ];

  constructor(
    private fb: FormBuilder,
    private jobService: JobService,
    private router: Router
  ) {
    this.jobForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5)]],
      company: ['', [Validators.required]],
      description: ['', [Validators.required, Validators.minLength(20)]],
      location: ['', [Validators.required]],
      skills: ['', [Validators.required]],
      salary: [null, [Validators.required, Validators.min(0)]],
      employmentType: ['FULL_TIME', [Validators.required]],
      experienceLevel: ['MID', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.jobForm.invalid) {
      this.markFormGroupTouched(this.jobForm);
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = false;

    const jobRequest: JobRequest = {
      ...this.jobForm.value,
      salary: Number(this.jobForm.value.salary)
    };

    this.jobService.createJob(jobRequest).subscribe({
      next: (response) => {
        this.loading = false;
        this.success = true;
        setTimeout(() => {
          this.router.navigate(['/dashboard/recruiter']);
        }, 2000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to create job. Please try again.';
        console.error('Error creating job:', err);
      }
    });
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if ((control as any).controls) {
        this.markFormGroupTouched(control as FormGroup);
      }
    });
  }
}
