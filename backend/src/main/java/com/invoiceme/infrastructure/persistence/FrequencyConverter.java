package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.Frequency;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class FrequencyConverter implements AttributeConverter<Frequency, String> {
    
    @Override
    public String convertToDatabaseColumn(Frequency attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }
    
    @Override
    public Frequency convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return Frequency.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown Frequency value: " + dbData, e);
        }
    }
}

