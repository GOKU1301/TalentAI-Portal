package com.soprasteria.smartjobportal.controller;

import com.soprasteria.smartjobportal.dto.ApplicationDTO.ApplicationRequest;
import com.soprasteria.smartjobportal.dto.ApplicationDTO.ApplicationResponse;
import com.soprasteria.smartjobportal.dto.ApplicationDTO.StatusUpdateRequest;
import com.soprasteria.smartjobportal.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/apply/{jobId}")
    @PreAuthorize("hasRole('JOBSEEKER')")
    public ResponseEntity<ApplicationResponse> applyForJob(
            @PathVariable Integer jobId,
            @RequestBody ApplicationRequest applicationRequest) {
        ApplicationResponse application = applicationService.applyForJob(jobId, applicationRequest);
        return ResponseEntity.ok(application);
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('JOBSEEKER')")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications() {
        List<ApplicationResponse> applications = applicationService.getMyApplications();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<List<ApplicationResponse>> getJobApplications(@PathVariable Integer jobId) {
        List<ApplicationResponse> applications = applicationService.getJobApplications(jobId);
        return ResponseEntity.ok(applications);
    }
    
    @PutMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Integer applicationId,
            @RequestBody StatusUpdateRequest statusUpdateRequest) {
        ApplicationResponse updatedApplication = applicationService.updateApplicationStatus(applicationId, statusUpdateRequest);
        return ResponseEntity.ok(updatedApplication);
    }
}
