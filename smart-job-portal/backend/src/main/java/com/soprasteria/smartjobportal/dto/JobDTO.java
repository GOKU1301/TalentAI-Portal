package com.soprasteria.smartjobportal.dto;

import com.soprasteria.smartjobportal.model.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JobDTO {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobRequest {
        private String title;
        private String description;
        private String skills;
        private String company;
        private String location;
        private BigDecimal salary;
        private String employmentType;
        private String experienceLevel;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobResponse {
        private Integer id;
        private String title;
        private String description;
        private String skills;
        private String location;
        private String salary;
        private String employmentType;
        private String experienceLevel;
        private String company;
        private String postedDate;
        private Integer postedById;
        private String postedByName;
        private BigDecimal matchPercentage;
        private Boolean applied;

        public static JobResponse fromEntity(Job job) {
            return JobResponse.builder()
                    .id(job.getId())
                    .title(job.getTitle())
                    .description(job.getDescription())
                    .skills(job.getSkills())
                    .location(job.getLocation())
                    .salary(job.getSalary() != null ? job.getSalary().toString() : null)
                    .employmentType(job.getEmploymentType().getDisplayName())
                    .experienceLevel(job.getExperienceLevel().getDisplayName())
                    .company(job.getCompany())
                    .postedDate(formatDate(job.getPostedDate()))
                    .postedById(job.getPostedBy() != null ? job.getPostedBy().getId() : null)
                    .postedByName(job.getPostedBy() != null ? job.getPostedBy().getFullName() : null)
                    .build();
        }

        private static String formatDate(LocalDateTime dateTime) {
            return dateTime != null ? dateTime.format(DATE_FORMATTER) : null;
        }
    }
}
