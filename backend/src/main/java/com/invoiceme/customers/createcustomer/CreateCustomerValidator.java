package com.invoiceme.customers.createcustomer;

import com.invoiceme.domain.common.Email;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateCustomerValidator {
    
    private final CustomerRepository customerRepository;
    
    public void validate(CreateCustomerRequest request) {
        // Email uniqueness check
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Customer with email " + request.getEmail() + " already exists");
        }
    }
}

