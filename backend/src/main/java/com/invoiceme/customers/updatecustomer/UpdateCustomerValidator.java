package com.invoiceme.customers.updatecustomer;

import com.invoiceme.infrastructure.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateCustomerValidator {
    
    private final CustomerRepository customerRepository;
    
    public void validate(UUID customerId, UpdateCustomerRequest request) {
        // Check if customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }
    }
}

