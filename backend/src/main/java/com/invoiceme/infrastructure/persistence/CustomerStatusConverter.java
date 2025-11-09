package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.CustomerStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class CustomerStatusConverter implements AttributeConverter<CustomerStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(CustomerStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }
    
    @Override
    public CustomerStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return CustomerStatus.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown CustomerStatus value: " + dbData, e);
        }
    }
}

