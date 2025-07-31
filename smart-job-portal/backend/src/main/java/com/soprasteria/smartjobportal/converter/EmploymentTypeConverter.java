package com.soprasteria.smartjobportal.converter;

import com.soprasteria.smartjobportal.model.Job.EmploymentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmploymentTypeConverter implements AttributeConverter<EmploymentType, String> {

    @Override
    public String convertToDatabaseColumn(EmploymentType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDisplayName();
    }

    @Override
    public EmploymentType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        for (EmploymentType type : EmploymentType.values()) {
            if (type.getDisplayName().equalsIgnoreCase(dbData)) {
                return type;
            }
        }
        
        // If no match found, try to match by enum name (fallback)
        try {
            return EmploymentType.valueOf(dbData.toUpperCase().replace("-", "").replace(" ", ""));
        } catch (IllegalArgumentException e) {
            // Log the error
            System.err.println("Error converting employment type: " + dbData);
            // Default to FULLTIME if no match found
            return EmploymentType.FULLTIME;
        }
    }
}
