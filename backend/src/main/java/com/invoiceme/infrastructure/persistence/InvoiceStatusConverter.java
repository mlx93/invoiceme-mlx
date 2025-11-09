package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.InvoiceStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class InvoiceStatusConverter implements AttributeConverter<InvoiceStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(InvoiceStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }
    
    @Override
    public InvoiceStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return InvoiceStatus.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown InvoiceStatus value: " + dbData, e);
        }
    }
}

