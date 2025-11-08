package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.Money;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * JPA converter for Money value object.
 * Database stores only DECIMAL amount (assumes USD).
 * Currency is always USD for MVP.
 */
@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {
    
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("USD");
    
    @Override
    public BigDecimal convertToDatabaseColumn(Money money) {
        if (money == null) {
            return null;
        }
        return money.getAmount();
    }
    
    @Override
    public Money convertToEntityAttribute(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return Money.of(amount, DEFAULT_CURRENCY);
    }
}

