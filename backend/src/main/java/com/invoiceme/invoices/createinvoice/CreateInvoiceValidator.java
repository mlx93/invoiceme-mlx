package com.invoiceme.invoices.createinvoice;

import com.invoiceme.domain.common.PaymentTerms;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateInvoiceValidator {
    
    private final CustomerRepository customerRepository;
    
    public void validate(CreateInvoiceRequest request) {
        // Validate customer exists
        if (!customerRepository.existsById(request.getCustomerId())) {
            throw new IllegalArgumentException("Customer not found: " + request.getCustomerId());
        }
        
        // Validate at least one line item
        if (request.getLineItems() == null || request.getLineItems().isEmpty()) {
            throw new IllegalArgumentException("At least one line item is required");
        }
        
        // Validate due date for CUSTOM payment terms
        if (request.getPaymentTerms() == PaymentTerms.CUSTOM && request.getDueDate() == null) {
            throw new IllegalArgumentException("Due date is required for CUSTOM payment terms");
        }
        
        // Validate due date >= issue date
        if (request.getDueDate() != null && request.getDueDate().isBefore(request.getIssueDate())) {
            throw new IllegalArgumentException("Due date must be >= issue date");
        }
    }
}

