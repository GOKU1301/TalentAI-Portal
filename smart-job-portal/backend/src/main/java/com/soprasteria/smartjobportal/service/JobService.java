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
        User currentUser = getCurrentUser();
        String userSkills = currentUser.getSkills();
        
        if (userSkills == null || userSkills.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Job> allJobs = jobRepository.findAll();
        List<JobResponse> matchingJobs = new ArrayList<>();
        
        // Simple matching algorithm based on skill overlap
        for (Job job : allJobs) {
            if (job.getSkills() != null && !job.getSkills().isEmpty()) {
                BigDecimal matchPercentage = calculateMatchPercentage(userSkills, job.getSkills());
                
                if (matchPercentage.compareTo(BigDecimal.ZERO) > 0) {
                    JobResponse response = JobResponse.fromEntity(job);
                    response.setMatchPercentage(matchPercentage);
                    response.setApplied(applicationRepository.existsByJobAndUser(job, currentUser));
                    matchingJobs.add(response);
                }
            }
        }
        
        // Sort by match percentage (descending)
        matchingJobs.sort((j1, j2) -> j2.getMatchPercentage().compareTo(j1.getMatchPercentage()));
        
        return matchingJobs;
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
}
