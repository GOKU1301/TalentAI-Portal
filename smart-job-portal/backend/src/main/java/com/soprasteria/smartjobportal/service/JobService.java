package com.soprasteria.smartjobportal.service;

import com.soprasteria.smartjobportal.dto.JobDTO.JobRequest;
import com.soprasteria.smartjobportal.dto.JobDTO.JobResponse;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    public List<JobResponse> getAllJobs() {
        List<Job> jobs = jobRepository.findAll();
        User currentUser = getCurrentUserOptional().orElse(null);
        
        return jobs.stream()
                .map(job -> {
                    JobResponse response = JobResponse.fromEntity(job);
                    if (currentUser != null) {
                        response.setApplied(applicationRepository.existsByJobAndUser(job, currentUser));
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    public JobResponse getJobById(Integer id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
        
        JobResponse response = JobResponse.fromEntity(job);
        
        User currentUser = getCurrentUserOptional().orElse(null);
        if (currentUser != null) {
            response.setApplied(applicationRepository.existsByJobAndUser(job, currentUser));
        }
        
        return response;
    }

    public JobResponse createJob(JobRequest jobRequest) {
        User currentUser = getCurrentUser();
        
        // Only recruiters can create jobs
        if (currentUser.getRole() != User.Role.RECRUITER && currentUser.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("Only recruiters can create job listings");
        }
        
        Job job = new Job();
        job.setTitle(jobRequest.getTitle());
        job.setDescription(jobRequest.getDescription());
        job.setSkills(jobRequest.getSkills());
        job.setCompany(jobRequest.getCompany() != null ? jobRequest.getCompany() : currentUser.getCompany());
        job.setLocation(jobRequest.getLocation());
        job.setSalary(jobRequest.getSalary());
        
        // Set employment type
        if (jobRequest.getEmploymentType() != null) {
            try {
                job.setEmploymentType(Job.EmploymentType.valueOf(jobRequest.getEmploymentType().toUpperCase().replace("-", "")));
            } catch (IllegalArgumentException e) {
                // Default to FULLTIME if invalid type provided
                job.setEmploymentType(Job.EmploymentType.FULLTIME);
            }
        }
        
        // Set experience level
        if (jobRequest.getExperienceLevel() != null) {
            try {
                job.setExperienceLevel(Job.ExperienceLevel.valueOf(jobRequest.getExperienceLevel().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Default to ENTRY if invalid level provided
                job.setExperienceLevel(Job.ExperienceLevel.ENTRY);
            }
        }
        
        job.setPostedBy(currentUser);
        
        Job savedJob = jobRepository.save(job);
        return JobResponse.fromEntity(savedJob);
    }

    public List<JobResponse> searchJobs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllJobs();
        }
        
        List<Job> jobs = jobRepository.searchJobs(keyword.trim());
        User currentUser = getCurrentUserOptional().orElse(null);
        
        return jobs.stream()
                .map(job -> {
                    JobResponse response = JobResponse.fromEntity(job);
                    if (currentUser != null) {
                        response.setApplied(applicationRepository.existsByJobAndUser(job, currentUser));
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    public List<JobResponse> getMatchingJobs() {
        try {
            System.out.println("Starting getMatchingJobs method");
            
            // Get current user
            User currentUser = null;
            try {
                currentUser = getCurrentUser();
                System.out.println("Current user retrieved: " + currentUser.getUsername());
            } catch (Exception e) {
                System.err.println("Error getting current user: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
            
            // Get user skills
            String userSkills = null;
            try {
                userSkills = currentUser.getSkills();
                System.out.println("User skills: " + (userSkills != null ? userSkills : "null"));
            } catch (Exception e) {
                System.err.println("Error getting user skills: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
            
            if (userSkills == null || userSkills.trim().isEmpty()) {
                System.out.println("No user skills found, returning empty list");
                return Collections.emptyList();
            }
            
            // Get all jobs
            List<Job> allJobs = null;
            try {
                allJobs = jobRepository.findAll();
                System.out.println("Retrieved " + allJobs.size() + " jobs");
            } catch (Exception e) {
                System.err.println("Error retrieving jobs: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
            
            List<JobResponse> matchingJobs = new ArrayList<>();
            
            // Simple matching algorithm based on skill overlap
            for (Job job : allJobs) {
                try {
                    if (job.getSkills() != null && !job.getSkills().isEmpty()) {
                        System.out.println("Processing job ID: " + job.getId() + ", skills: " + job.getSkills());
                        BigDecimal matchPercentage = calculateMatchPercentage(userSkills, job.getSkills());
                        System.out.println("Match percentage: " + matchPercentage);
                        
                        if (matchPercentage.compareTo(BigDecimal.ZERO) > 0) {
                            JobResponse response = JobResponse.fromEntity(job);
                            response.setMatchPercentage(matchPercentage);
                            
                            boolean applied = false;
                            try {
                                applied = applicationRepository.existsByJobAndUser(job, currentUser);
                                System.out.println("User has applied: " + applied);
                            } catch (Exception e) {
                                System.err.println("Error checking if user applied: " + e.getMessage());
                                e.printStackTrace();
                            }
                            
                            response.setApplied(applied);
                            matchingJobs.add(response);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing job ID " + job.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                    // Continue processing other jobs
                }
            }
            
            // Sort by match percentage (descending)
            matchingJobs.sort((j1, j2) -> j2.getMatchPercentage().compareTo(j1.getMatchPercentage()));
            
            System.out.println("Returning " + matchingJobs.size() + " matching jobs");
            return matchingJobs;
        } catch (Exception e) {
            System.err.println("Unhandled exception in getMatchingJobs: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
    
    private Optional<User> getCurrentUserOptional() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                return Optional.empty();
            }
            String username = authentication.getName();
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public List<JobResponse> getJobsPostedByCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                System.err.println("Authentication is null in getJobsPostedByCurrentUser");
                throw new RuntimeException("User not authenticated");
            }
            
            String username = authentication.getName();
            if (username == null || username.isEmpty() || username.equals("anonymousUser")) {
                System.err.println("Invalid username in getJobsPostedByCurrentUser: " + username);
                throw new RuntimeException("Invalid username");
            }
            
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        System.err.println("User not found with username: " + username);
                        return new UsernameNotFoundException("User not found with username: " + username);
                    });
            
            System.out.println("Found user: " + currentUser.getUsername() + " with role: " + currentUser.getRole());
            
            List<Job> jobs = jobRepository.findByPostedBy(currentUser);
            System.out.println("Found " + jobs.size() + " jobs posted by user: " + currentUser.getUsername());
            
            return jobs.stream()
                    .map(job -> {
                        JobResponse response = JobResponse.fromEntity(job);
                        response.setApplied(false); // Not applicable for recruiters viewing their own jobs
                        return response;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getJobsPostedByCurrentUser: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve jobs: " + e.getMessage(), e);
        }
    }
}
