package com.invoiceme.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Year;
import java.util.regex.Pattern;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class InvoiceNumber {
    
    private static final Pattern INVOICE_NUMBER_PATTERN = Pattern.compile("^INV-(\\d{4})-(\\d{4})$");
    
    @Column(name = "invoice_number", length = 15, nullable = false, unique = true)
    private String value;
    
    private InvoiceNumber(String value) {
        validate(value);
        this.value = value;
    }
    
    public static InvoiceNumber generate(int sequenceNumber) {
        int year = Year.now().getValue();
        return new InvoiceNumber(String.format("INV-%d-%04d", year, sequenceNumber));
    }
    
    public static InvoiceNumber of(String invoiceNumber) {
        return new InvoiceNumber(invoiceNumber);
    }
    
    private static void validate(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Invoice number cannot be null or empty");
        }
        
        if (!INVOICE_NUMBER_PATTERN.matcher(invoiceNumber).matches()) {
            throw new IllegalArgumentException("Invalid invoice number format. Expected: INV-YYYY-####, got: " + invoiceNumber);
        }
    }
    
    public int getYear() {
        var matcher = INVOICE_NUMBER_PATTERN.matcher(value);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalStateException("Invalid invoice number format: " + value);
    }
    
    public int getSequence() {
        var matcher = INVOICE_NUMBER_PATTERN.matcher(value);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(2));
        }
        throw new IllegalStateException("Invalid invoice number format: " + value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}

