package com.soprasteria.smartjobportal.dto;

import com.soprasteria.smartjobportal.model.Application;
import com.soprasteria.smartjobportal.model.Application.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApplicationDTO {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApplicationRequest {
        private String coverLetter;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatusUpdateRequest {
        private ApplicationStatus status;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApplicationResponse {
        private Integer id;
        private JobDTO.JobResponse job;
        private String status;
        private String coverLetter;
        private BigDecimal matchPercentage;
        private String appliedDate;

        public static ApplicationResponse fromEntity(Application application) {
            return ApplicationResponse.builder()
                    .id(application.getId())
                    .job(JobDTO.JobResponse.fromEntity(application.getJob()))
                    .status(application.getStatus().name())
                    .coverLetter(application.getCoverLetter())
                    .matchPercentage(application.getMatchPercentage())
                    .appliedDate(formatDate(application.getAppliedDate()))
                    .build();
        }

        private static String formatDate(LocalDateTime dateTime) {
            return dateTime != null ? dateTime.format(DATE_FORMATTER) : null;
        }
    }
}
