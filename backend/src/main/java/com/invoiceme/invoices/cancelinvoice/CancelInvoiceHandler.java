package com.invoiceme.invoices.cancelinvoice;

import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelInvoiceHandler {
    
    private final InvoiceRepository invoiceRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public void handle(CancelInvoiceCommand command) {
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + command.getInvoiceId()));
        
        // Cancel invoice (publishes InvoiceCancelledEvent)
        invoice.cancel();
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Publish domain events after transaction commit
        eventPublisher.publishEvents(savedInvoice);
    }
}

