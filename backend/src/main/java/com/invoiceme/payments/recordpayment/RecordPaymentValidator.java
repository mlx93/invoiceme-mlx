package com.invoiceme.payments.recordpayment;

import com.invoiceme.domain.common.InvoiceStatus;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RecordPaymentValidator {
    
    private final InvoiceRepository invoiceRepository;
    
    public void validate(UUID invoiceId) {
        var invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            throw new IllegalArgumentException("Invoice not found: " + invoiceId);
        }
        
        var invoice = invoiceOpt.get();
        if (invoice.getStatus() != InvoiceStatus.SENT && invoice.getStatus() != InvoiceStatus.OVERDUE) {
            throw new IllegalStateException(
                "Cannot record payment for invoice with status: " + invoice.getStatus() + 
                ". Invoice must be SENT or OVERDUE."
            );
        }
    }
}

