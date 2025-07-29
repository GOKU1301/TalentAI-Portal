import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService, User } from '../../auth/auth.service';
// Using relative path with explicit extension to fix lint error
import { UserService } from './user.service.js';
import { MatchingJobsComponent } from './matching-jobs/matching-jobs.component';
import { MyApplicationsComponent } from './my-applications/my-applications.component';

export interface UserProfile {
  id?: number;
  username: string;
  email: string;
  fullName?: string;
  skills?: string;
  education?: string;
  experience?: number;
  position?: string;
}

@Component({
  selector: 'app-job-seeker-dashboard',
  templateUrl: './job-seeker-dashboard.component.html',
  styleUrls: ['./job-seeker-dashboard.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    HttpClientModule,
    MatchingJobsComponent,
    MyApplicationsComponent
  ]
})
export class JobSeekerDashboardComponent implements OnInit, AfterViewInit {
  currentUser: User | null = null;
  userProfile: UserProfile | null = null;
  profileForm: FormGroup;
  skillsForm: FormGroup;
  skillsArray: string[] = [];
  newSkill: string = '';
  isLoading = false;
  isEditingProfile = false;
  isEditingSkills = false;
  activeTab: string = 'profile-tab';
  profileLoadError = false;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private fb: FormBuilder
  ) {
    this.profileForm = this.fb.group({
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      education: [''],
      experience: [null],
      position: ['']
    });

    this.skillsForm = this.fb.group({
      skills: ['']
    });
  }

  ngOnInit(): void {
    // Get current user from auth service
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        // Initialize an empty profile to prevent showing error message during loading
        this.userProfile = {
          id: undefined,
          username: user.username || '',
          email: user.email || '',
          fullName: '',
          skills: '',
          education: '',
          experience: undefined,
          position: ''
        };
        this.loadUserProfile();
      }
    });
  }
  
  ngAfterViewInit(): void {
    // No longer needed as we're using Angular binding
  }
  
  switchTab(tabId: string): void {
    this.activeTab = tabId;
  }

  loadUserProfile(): void {
    this.isLoading = true;
    this.profileLoadError = false; // Reset error flag on each load attempt
    
    // Check if user is authenticated
    if (!this.currentUser || !this.currentUser.token) {
      console.error('User not authenticated or missing token');
      alert('Authentication error. Please log in again.');
      this.authService.logout(); // Redirect to login
      this.isLoading = false;
      return;
    }
    
    // Use real API data now that the backend is fixed
    
    // Real API call
    console.log('About to call getUserProfile()');
    console.log('Current user:', this.currentUser);
    this.userService.getUserProfile().subscribe({
      next: (profile: UserProfile) => {
        console.log('Profile loaded successfully:', profile);
        this.userProfile = profile;
        this.updateProfileForm(profile);
        this.updateSkillsArray(profile.skills || '');
        this.isLoading = false;
        
        // Check if profile is mostly empty and show a helpful message
        if (!profile.fullName && !profile.skills && !profile.education && !profile.position) {
          console.log('Profile is empty, showing welcome message');
          setTimeout(() => {
            alert('Welcome! Please complete your profile information to get started.');
          }, 500);
        }
      },
      error: (error: any) => {
        console.error('Error loading profile:', error);
        this.profileLoadError = true; // Set error flag for actual API errors
        
        // Check for specific error types
        if (error.status === 403) {
          alert('Access denied. Your session may have expired. Please log in again.');
          this.authService.logout(); // Redirect to login
        } else if (error.status === 404) {
          // For new users, create an empty profile template
          console.log('Profile not found. Creating empty profile template for new user.');
          this.userProfile = {
            id: undefined,
            username: this.currentUser?.username || '',
            email: this.currentUser?.email || '',
            fullName: '',
            skills: '',
            education: '',
            experience: undefined,
            position: ''
          };
          this.updateProfileForm(this.userProfile);
          this.updateSkillsArray('');
          this.isLoading = false;
          this.profileLoadError = false; // Reset error flag as we're handling this case
          
          // Inform user they need to complete their profile
          alert('Welcome! Please complete your profile information.');
        } else {
          alert('Failed to load profile. Please try again.');
          this.isLoading = false;
        }
      }
    });
  }

  updateProfileForm(profile: UserProfile): void {
    this.profileForm.patchValue({
      fullName: profile.fullName || '',
      email: profile.email,
      education: profile.education || '',
      experience: profile.experience || null,
      position: profile.position || ''
    });
  }

  updateSkillsArray(skillsString: string | undefined | null): void {
    if (skillsString && skillsString.trim() !== '') {
      // Filter out empty skills after splitting and trimming
      this.skillsArray = skillsString.split(',')
        .map(skill => skill.trim())
        .filter(skill => skill !== '');
    } else {
      this.skillsArray = [];
    }
  }

  toggleEditProfile(): void {
    this.isEditingProfile = !this.isEditingProfile;
    if (!this.isEditingProfile && this.userProfile) {
      this.updateProfileForm(this.userProfile);
    }
  }

  toggleEditSkills(): void {
    this.isEditingSkills = !this.isEditingSkills;
  }

  updateProfile(): void {
    if (!this.userProfile) {
      console.error('Cannot update profile: userProfile is null or undefined');
      return;
    }
    
    // Get values from the form instead of using the userProfile object directly
    const formValues = this.profileForm.value;
    
    // Create a new profile object with form values while preserving id and username
    const updatedProfile: UserProfile = {
      id: this.userProfile.id,
      username: this.userProfile.username,
      email: formValues.email,
      fullName: formValues.fullName,
      education: formValues.education || '',
      experience: formValues.experience !== null ? formValues.experience : undefined,
      position: formValues.position || '',
      skills: this.userProfile.skills // Preserve existing skills
    };
    
    console.log('Attempting to update profile with data:', updatedProfile);
    
    this.userService.updateUserProfile(updatedProfile).subscribe({
      next: (updatedProfile) => {
        console.log('Profile updated successfully:', updatedProfile);
        this.userProfile = updatedProfile;
        this.isEditingProfile = false; // Exit edit mode after successful update
        alert('Profile updated successfully!');
      },
      error: (error) => {
        console.error('Error updating profile:', error);
        console.error('Error details:', error.status, error.statusText, error.error);
        alert('Failed to update profile. Please try again.');
      }
    });
  }

  addSkill(): void {
    if (this.newSkill && this.newSkill.trim() !== '') {
      if (!this.skillsArray.includes(this.newSkill.trim())) {
        this.skillsArray.push(this.newSkill.trim());
      }
      this.newSkill = '';
    }
  }

  removeSkill(skill: string): void {
    this.skillsArray = this.skillsArray.filter(s => s !== skill);
  }

  updateSkills(): void {
    this.isLoading = true;
    const skillsString = this.skillsArray.join(',');
    
    console.log('Attempting to update skills with:', skillsString);
    console.log('Current user role:', this.currentUser?.role);
    
    // Real API call
    this.userService.updateUserSkills(skillsString).subscribe({
      next: (response: any) => {
        console.log('Skills updated successfully, response:', response);
        if (this.userProfile) {
          this.userProfile.skills = skillsString;
        }
        this.isEditingSkills = false;
        this.isLoading = false;
        alert('Skills updated successfully!');
      },
      error: (error: any) => {
        console.error('Error updating skills:', error);
        console.error('Error details:', error.status, error.statusText);
        if (error.error) {
          console.error('Error body:', error.error);
        }
        alert('Failed to update skills. Please try again.');
        this.isLoading = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
