package com.invoiceme.invoices.markassent;

import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkAsSentHandler {
    
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public Invoice handle(MarkAsSentCommand command) {
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + command.getInvoiceId()));
        
        // Load customer for event
        Customer customer = customerRepository.findById(invoice.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + invoice.getCustomerId()));
        
        // Check if customer has credit and auto-apply
        if (customer.getCreditBalance().isPositive()) {
            // Apply credit as discount (up to invoice total)
            com.invoiceme.domain.common.Money creditToApply = customer.getCreditBalance();
            if (creditToApply.isGreaterThan(invoice.getTotalAmount())) {
                creditToApply = invoice.getTotalAmount();
            }
            invoice.applyCreditDiscount(creditToApply);
            customer.deductCredit(creditToApply);
            customerRepository.save(customer);
        }
        
        // Mark invoice as sent (publishes InvoiceSentEvent)
        invoice.markAsSent();
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Update event with customer details before publishing
        var events = savedInvoice.getDomainEvents();
        if (!events.isEmpty() && events.get(0) instanceof com.invoiceme.domain.events.InvoiceSentEvent) {
            var event = (com.invoiceme.domain.events.InvoiceSentEvent) events.get(0);
            // Event already has invoice details, customer name/email will be set by listener if needed
        }
        
        // Publish domain events after transaction commit
        eventPublisher.publishEvents(savedInvoice);
        eventPublisher.publishEvents(customer);
        
        return savedInvoice;
    }
}

