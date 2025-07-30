package com.soprasteria.smartjobportal.controller;

import com.soprasteria.smartjobportal.dto.JobDTO.JobRequest;
import com.soprasteria.smartjobportal.dto.JobDTO.JobResponse;
import com.soprasteria.smartjobportal.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        List<JobResponse> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Integer id) {
        JobResponse job = jobService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<JobResponse> createJob(@RequestBody JobRequest jobRequest) {
        JobResponse createdJob = jobService.createJob(jobRequest);
        return ResponseEntity.ok(createdJob);
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobResponse>> searchJobs(@RequestParam(required = false) String keyword) {
        List<JobResponse> jobs = jobService.searchJobs(keyword);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/matching")
    @PreAuthorize("hasRole('JOBSEEKER')")
    public ResponseEntity<List<JobResponse>> getMatchingJobs() {
        List<JobResponse> matchingJobs = jobService.getMatchingJobs();
        return ResponseEntity.ok(matchingJobs);
    }
}
