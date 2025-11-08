package com.invoiceme.customers.updatecustomer;

import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCustomerHandler {
    
    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public Customer handle(UpdateCustomerCommand command) {
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + command.getCustomerId()));
        
        // Update customer using domain method
        customer.update(
            command.getCompanyName(),
            command.getContactName(),
            command.getPhone(),
            command.getAddress(),
            command.getCustomerType()
        );
        
        // Update status if provided
        if (command.getStatus() != null && command.getStatus() != customer.getStatus()) {
            if (command.getStatus() == com.invoiceme.domain.common.CustomerStatus.INACTIVE) {
                customer.markAsInactive();
            } else {
                // For other status changes, we'd need a method on Customer aggregate
                // For now, we'll handle it via reflection or add a method
                throw new UnsupportedOperationException("Status change to " + command.getStatus() + " not yet supported");
            }
        }
        
        Customer savedCustomer = customerRepository.save(customer);
        
        // Publish domain events after transaction commit
        eventPublisher.publishEvents(savedCustomer);
        
        return savedCustomer;
    }
}

