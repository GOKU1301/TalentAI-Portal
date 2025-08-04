package com.soprasteria.smartjobportal.service;

import com.soprasteria.smartjobportal.dto.ApplicationDTO.ApplicationRequest;
import com.soprasteria.smartjobportal.dto.ApplicationDTO.ApplicationResponse;
import com.soprasteria.smartjobportal.dto.ApplicationDTO.StatusUpdateRequest;
import com.soprasteria.smartjobportal.model.Application;
import com.soprasteria.smartjobportal.model.Job;
import com.soprasteria.smartjobportal.model.User;
import com.soprasteria.smartjobportal.repository.ApplicationRepository;
import com.soprasteria.smartjobportal.repository.JobRepository;
import com.soprasteria.smartjobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public ApplicationResponse applyForJob(Integer jobId, ApplicationRequest applicationRequest) {
        User currentUser = getCurrentUser();
        
        // Only job seekers can apply for jobs
        if (currentUser.getRole() != User.Role.JOBSEEKER) {
            throw new AccessDeniedException("Only job seekers can apply for jobs");
        }
        
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));
        
        // Check if user has already applied for this job
        if (applicationRepository.existsByJobAndUser(job, currentUser)) {
            throw new RuntimeException("You have already applied for this job");
        }
        
        Application application = new Application();
        application.setJob(job);
        application.setUser(currentUser);
        application.setCoverLetter(applicationRequest.getCoverLetter());
        
        // Calculate match percentage if user has skills
        if (currentUser.getSkills() != null && !currentUser.getSkills().isEmpty() && 
            job.getSkills() != null && !job.getSkills().isEmpty()) {
            BigDecimal matchPercentage = calculateMatchPercentage(currentUser.getSkills(), job.getSkills());
            application.setMatchPercentage(matchPercentage);
        }
        
        Application savedApplication = applicationRepository.save(application);
        return ApplicationResponse.fromEntity(savedApplication);
    }

    public List<ApplicationResponse> getMyApplications() {
        User currentUser = getCurrentUser();
        List<Application> applications = applicationRepository.findByUser(currentUser);
        
        return applications.stream()
                .map(ApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getJobApplications(Integer jobId) {
        User currentUser = getCurrentUser();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));
        
        // Only the job poster or an admin can view applications
        if (!job.getPostedBy().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("You don't have permission to view these applications");
        }
        
        List<Application> applications = applicationRepository.findByJob(job);
        
        return applications.stream()
                .map(ApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateMatchPercentage(String userSkills, String jobSkills) {
        if (userSkills == null || jobSkills == null) {
            return BigDecimal.ZERO;
        }
        
        // Convert skills to lowercase and split by commas
        Set<String> userSkillSet = Arrays.stream(userSkills.toLowerCase().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        
        Set<String> jobSkillSet = Arrays.stream(jobSkills.toLowerCase().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        
        if (userSkillSet.isEmpty() || jobSkillSet.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Count matching skills
        int matchingSkills = 0;
        for (String userSkill : userSkillSet) {
            for (String jobSkill : jobSkillSet) {
                if (jobSkill.contains(userSkill) || userSkill.contains(jobSkill)) {
                    matchingSkills++;
                    break;
                }
            }
        }
        
        // Calculate percentage
        double percentage = (double) matchingSkills / jobSkillSet.size() * 100;
        return BigDecimal.valueOf(percentage).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public ApplicationResponse updateApplicationStatus(Integer applicationId, StatusUpdateRequest statusUpdateRequest) {
        User currentUser = getCurrentUser();
        
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));
        
        Job job = application.getJob();
        
        // Verify that the current user is the recruiter who posted the job
        if (!job.getPostedBy().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("You don't have permission to update this application's status");
        }
        
        // Update the status
        application.setStatus(statusUpdateRequest.getStatus());
        Application updatedApplication = applicationRepository.save(application);
        
        return ApplicationResponse.fromEntity(updatedApplication);
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
