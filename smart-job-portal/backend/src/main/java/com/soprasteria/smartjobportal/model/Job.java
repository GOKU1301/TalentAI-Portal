package com.soprasteria.smartjobportal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jobs")
public class Job {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String skills;
    
    private String company;
    
    private String location;
    
    private BigDecimal salary;
    
    @Convert(converter = com.soprasteria.smartjobportal.converter.EmploymentTypeConverter.class)
    @Column(name = "employment_type")
    private EmploymentType employmentType = EmploymentType.FULLTIME;
    
    @Convert(converter = com.soprasteria.smartjobportal.converter.ExperienceLevelConverter.class)
    @Column(name = "experience_level")
    private ExperienceLevel experienceLevel = ExperienceLevel.ENTRY;
    
    @ManyToOne
    @JoinColumn(name = "posted_by")
    private User postedBy;
    
    @Column(name = "posted_date")
    private LocalDateTime postedDate = LocalDateTime.now();
    
    public enum EmploymentType {
        FULLTIME("Full-time"), 
        PARTTIME("Part-time"), 
        CONTRACT("Contract"), 
        INTERNSHIP("Internship"), 
        TEMPORARY("Temporary");
        
        private final String displayName;
        
        EmploymentType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ExperienceLevel {
        ENTRY("Entry"), 
        MID("Mid"), 
        SENIOR("Senior"), 
        LEAD("Lead");
        
        private final String displayName;
        
        ExperienceLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
