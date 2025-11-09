package com.invoiceme.customers.reactivatecustomer;

import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactivateCustomerHandler {
    
    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public Customer handle(ReactivateCustomerCommand command) {
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + command.getCustomerId()));
        
        // Reactivate the customer
        customer.markAsActive();
        
        Customer savedCustomer = customerRepository.save(customer);
        
        // Publish domain events after transaction commit
        eventPublisher.publishEvents(savedCustomer);
        
        return savedCustomer;
    }
}

