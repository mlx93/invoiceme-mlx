package com.invoiceme.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PostgreSQLEnumConverter implements AttributeConverter<Enum<?>, String> {
    
    @Override
    public String convertToDatabaseColumn(Enum<?> attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }
    
    @Override
    public Enum<?> convertToEntityAttribute(String dbData) {
        // This won't work generically - we need specific converters
        return null;
    }
}

