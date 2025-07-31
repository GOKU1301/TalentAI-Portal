package com.soprasteria.smartjobportal.converter;

import com.soprasteria.smartjobportal.model.Job.ExperienceLevel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ExperienceLevelConverter implements AttributeConverter<ExperienceLevel, String> {

    @Override
    public String convertToDatabaseColumn(ExperienceLevel attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDisplayName();
    }

    @Override
    public ExperienceLevel convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        for (ExperienceLevel level : ExperienceLevel.values()) {
            if (level.getDisplayName().equalsIgnoreCase(dbData)) {
                return level;
            }
        }
        
        // If no match found, try to match by enum name (fallback)
        try {
            return ExperienceLevel.valueOf(dbData.toUpperCase().replace("-", "").replace(" ", ""));
        } catch (IllegalArgumentException e) {
            // Log the error
            System.err.println("Error converting experience level: " + dbData);
            // Default to ENTRY if no match found
            return ExperienceLevel.ENTRY;
        }
    }
}
