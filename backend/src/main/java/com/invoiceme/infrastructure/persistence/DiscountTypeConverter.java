package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.DiscountType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class DiscountTypeConverter implements AttributeConverter<DiscountType, String> {
    
    @Override
    public String convertToDatabaseColumn(DiscountType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }
    
    @Override
    public DiscountType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return DiscountType.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown DiscountType value: " + dbData, e);
        }
    }
}

