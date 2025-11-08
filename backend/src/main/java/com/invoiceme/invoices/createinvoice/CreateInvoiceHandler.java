package com.invoiceme.invoices.createinvoice;

import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.common.InvoiceNumber;
import com.invoiceme.domain.common.PaymentTerms;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceNumberGenerator;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreateInvoiceHandler {
    
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceNumberGenerator invoiceNumberGenerator;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public Invoice handle(CreateInvoiceCommand command) {
        // Validate customer exists
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + command.getCustomerId()));
        
        // Generate invoice number if not provided
        InvoiceNumber invoiceNumber = command.getInvoiceNumber();
        if (invoiceNumber == null) {
            invoiceNumber = invoiceNumberGenerator.generateNext();
        }
        
        // Calculate due date if not provided
        LocalDate dueDate = command.getDueDate();
        if (dueDate == null) {
            dueDate = calculateDueDate(command.getIssueDate(), command.getPaymentTerms());
        }
        
        // Create invoice using factory method
        Invoice invoice = Invoice.create(
            command.getCustomerId(),
            invoiceNumber,
            command.getIssueDate(),
            dueDate,
            command.getPaymentTerms()
        );
        
        // Add line items
        int sortOrder = 0;
        for (LineItem lineItem : command.getLineItems()) {
            // Set sort order if not set
            if (lineItem.getSortOrder() == null) {
                // We need to create a new LineItem with sortOrder set
                // Since LineItem is immutable after creation, we'll handle this in the mapper
            }
            invoice.addLineItem(lineItem);
        }
        
        // Set notes if provided
        if (command.getNotes() != null) {
            invoice.updateNotes(command.getNotes());
        }
        
        // Save invoice
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Publish domain events after transaction commit
        eventPublisher.publishEvents(savedInvoice);
        
        return savedInvoice;
    }
    
    private LocalDate calculateDueDate(LocalDate issueDate, PaymentTerms paymentTerms) {
        return switch (paymentTerms) {
            case NET_30 -> issueDate.plusDays(30);
            case DUE_ON_RECEIPT -> issueDate;
            case CUSTOM -> throw new IllegalArgumentException("Due date is required for CUSTOM payment terms");
        };
    }
}

