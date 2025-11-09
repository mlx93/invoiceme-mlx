package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.CustomerType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class CustomerTypeConverter implements AttributeConverter<CustomerType, String> {
    
    @Override
    public String convertToDatabaseColumn(CustomerType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }
    
    @Override
    public CustomerType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return CustomerType.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown CustomerType value: " + dbData, e);
        }
    }
}

