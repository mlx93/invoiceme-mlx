package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.PaymentTerms;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PaymentTermsConverter implements AttributeConverter<PaymentTerms, String> {
    
    @Override
    public String convertToDatabaseColumn(PaymentTerms attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }
    
    @Override
    public PaymentTerms convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return PaymentTerms.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown PaymentTerms value: " + dbData, e);
        }
    }
}

