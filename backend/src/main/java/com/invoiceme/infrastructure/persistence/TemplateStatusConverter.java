package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.TemplateStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TemplateStatusConverter implements AttributeConverter<TemplateStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(TemplateStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }
    
    @Override
    public TemplateStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return TemplateStatus.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown TemplateStatus value: " + dbData, e);
        }
    }
}

