package com.invoiceme.customers.deletecustomer;

import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCustomerHandler {
    
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public void handle(DeleteCustomerCommand command) {
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + command.getCustomerId()));
        
        // Check if customer can be deleted (business rule)
        // Check for outstanding invoices
        var customerInvoices = invoiceRepository.findByCustomerId(
            customer.getId(),
            org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        );
        
        boolean hasUnpaidInvoices = customerInvoices.getContent().stream()
            .anyMatch(inv -> (inv.getStatus() == com.invoiceme.domain.common.InvoiceStatus.SENT || 
                             inv.getStatus() == com.invoiceme.domain.common.InvoiceStatus.OVERDUE) &&
                            inv.getBalanceDue().isPositive());
        
        if (hasUnpaidInvoices) {
            throw new IllegalStateException("Cannot delete customer with outstanding invoices");
        }
        
        // Check if customer can be deleted (domain method)
        if (!customer.canBeDeleted()) {
            throw new IllegalStateException("Customer cannot be deleted. Outstanding balance or active templates exist.");
        }
        
        // Mark as inactive (soft delete)
        customer.markAsInactive();
        
        Customer savedCustomer = customerRepository.save(customer);
        
        // Publish domain events after transaction commit
        eventPublisher.publishEvents(savedCustomer);
    }
}

