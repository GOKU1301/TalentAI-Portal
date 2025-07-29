import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService, User } from '../../auth/auth.service';
// Using relative path with explicit extension to fix lint error
import { UserService } from './user.service.js';

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
    HttpClientModule
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
    this.currentUser = this.authService.getCurrentUser();
    this.loadUserProfile();
  }
  
  ngAfterViewInit(): void {
    // No longer needed as we're using Angular binding
  }
  
  switchTab(tabId: string): void {
    this.activeTab = tabId;
  }

  loadUserProfile(): void {
    this.isLoading = true;
    
    // Check if user is authenticated
    if (!this.currentUser || !this.currentUser.token) {
      console.error('User not authenticated or missing token');
      alert('Authentication error. Please log in again.');
      this.authService.logout(); // Redirect to login
      this.isLoading = false;
      return;
    }
    
    // IMPORTANT: Using mock data until backend API is fixed
    const useMockData = true;
    
    if (useMockData) {
      // Create mock profile data for demonstration
      setTimeout(() => {
        const mockProfile: UserProfile = {
          id: 1,
          username: this.currentUser?.username || 'jobseeker',
          email: this.currentUser?.email || 'jobseeker@example.com',
          fullName: 'Job Seeker Demo',
          skills: 'JavaScript,Angular,TypeScript,HTML,CSS',
          education: 'Bachelor of Computer Science',
          experience: 2,
          position: 'Frontend Developer'
        };
        
        console.log('Using mock profile data:', mockProfile);
        this.userProfile = mockProfile;
        this.updateProfileForm(mockProfile);
        this.updateSkillsArray(mockProfile.skills || '');
        this.isLoading = false;
      }, 1000); // Simulate network delay
      
      return;
    }
    
    // Real API call
    this.userService.getUserProfile().subscribe({
      next: (profile: UserProfile) => {
        console.log('Profile loaded successfully:', profile);
        this.userProfile = profile;
        this.updateProfileForm(profile);
        this.updateSkillsArray(profile.skills || '');
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading profile:', error);
        
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
    if (this.profileForm.invalid) {
      return;
    }
    
    this.isLoading = true;
    const profileData = this.profileForm.value;
    
    // IMPORTANT: Using mock data until backend API is fixed
    const useMockData = true;
    
    if (useMockData) {
      // Simulate API delay
      setTimeout(() => {
        // Update local userProfile with form data
        this.userProfile = {
          ...this.userProfile,
          ...profileData
        };
        
        this.isEditingProfile = false;
        this.isLoading = false;
        alert('Profile updated successfully!');
        console.log('Profile updated with mock data:', this.userProfile);
      }, 800);
      
      return;
    }
    
    // Real API call
    this.userService.updateUserProfile(profileData).subscribe({
      next: (updatedProfile: UserProfile) => {
        this.userProfile = updatedProfile;
        this.isEditingProfile = false;
        this.isLoading = false;
        alert('Profile updated successfully!');
      },
      error: (error: any) => {
        console.error('Error updating profile:', error);
        alert('Failed to update profile. Please try again.');
        this.isLoading = false;
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
    
    // IMPORTANT: Using mock data until backend API is fixed
    const useMockData = true;
    
    if (useMockData) {
      // Simulate API delay
      setTimeout(() => {
        // Update local userProfile with new skills
        if (this.userProfile) {
          this.userProfile.skills = skillsString;
        }
        
        this.isEditingSkills = false;
        this.isLoading = false;
        alert('Skills updated successfully!');
        console.log('Skills updated with mock data:', skillsString);
      }, 800);
      
      return;
    }
    
    // Real API call
    this.userService.updateUserSkills(skillsString).subscribe({
      next: (response: any) => {
        if (this.userProfile) {
          this.userProfile.skills = skillsString;
        }
        this.isEditingSkills = false;
        this.isLoading = false;
        alert('Skills updated successfully!');
      },
      error: (error: any) => {
        console.error('Error updating skills:', error);
        alert('Failed to update skills. Please try again.');
        this.isLoading = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
