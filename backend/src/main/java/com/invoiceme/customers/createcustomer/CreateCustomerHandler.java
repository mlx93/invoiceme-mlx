package com.invoiceme.customers.createcustomer;

import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCustomerHandler {
    
    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public Customer handle(CreateCustomerCommand command) {
        // Check if email already exists
        if (customerRepository.existsByEmail(command.getEmail().getValue())) {
            throw new IllegalArgumentException("Customer with email " + command.getEmail().getValue() + " already exists");
        }
        
        // Create customer using factory method
        Customer customer = Customer.create(
            command.getCompanyName(),
            command.getEmail(),
            command.getCustomerType()
        );
        
        // Set optional fields
        if (command.getContactName() != null) {
            customer.update(
                customer.getCompanyName(),
                command.getContactName(),
                command.getPhone(),
                command.getAddress(),
                customer.getCustomerType()
            );
        } else if (command.getPhone() != null || command.getAddress() != null) {
            customer.update(
                customer.getCompanyName(),
                null,
                command.getPhone(),
                command.getAddress(),
                customer.getCustomerType()
            );
        }
        
        // Save customer
        Customer savedCustomer = customerRepository.save(customer);
        
        // Publish domain events after transaction commit
        eventPublisher.publishEvents(savedCustomer);
        
        return savedCustomer;
    }
}

